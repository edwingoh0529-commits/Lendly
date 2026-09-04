package com.example.groupassignment2app.data;

import androidx.annotation.NonNull;

import com.example.groupassignment2app.model.AppUser;
import com.example.groupassignment2app.model.BorrowRequest;
import com.example.groupassignment2app.model.ChatSummary;
import com.example.groupassignment2app.model.Item;
import com.example.groupassignment2app.model.Review;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


public class Repo {

    
    public static final String USERS    = "users";
    public static final String ITEMS    = "items";
    public static final String CHATS    = "chats";
    public static final String MESSAGES = "messages";
    public static final String REQUESTS = "requests";
    public static final String REVIEWS  = "reviews";

    private static Repo instance;

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    
    private final Map<String, String> nameCache = new HashMap<>();

    private Repo() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized Repo get() {
        if (instance == null) instance = new Repo();
        return instance;
    }

    

    public interface Result<T> {
        void onSuccess(T data);
        default void onError(Exception e) {  }
    }

    

    public FirebaseAuth auth() { return auth; }

    public boolean isLoggedIn() { return auth.getCurrentUser() != null; }

    public String uid() {
        FirebaseUser u = auth.getCurrentUser();
        return u == null ? null : u.getUid();
    }

    public String currentEmail() {
        FirebaseUser u = auth.getCurrentUser();
        return u == null ? "" : u.getEmail();
    }

    
    public String currentName() {
        String myUid = uid();
        if (myUid != null && nameCache.containsKey(myUid)) return nameCache.get(myUid);
        FirebaseUser u = auth.getCurrentUser();
        if (u == null) return "Lendly User";
        if (u.getDisplayName() != null && !u.getDisplayName().trim().isEmpty()) {
            return u.getDisplayName();
        }
        String email = u.getEmail();
        if (email != null && email.contains("@")) return email.substring(0, email.indexOf('@'));
        return "Lendly User";
    }

    public void signOut() {
        nameCache.clear();
        auth.signOut();
    }

    
    public Task<Void> saveUserProfile(String name, String email, String studentId) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("email", email);
        data.put("studentId", studentId);
        nameCache.put(uid(), name);
        return db.collection(USERS).document(uid()).set(data, SetOptions.merge());
    }

    public void loadUser(String userId, @NonNull Result<AppUser> cb) {
        db.collection(USERS).document(userId).get()
                .addOnSuccessListener(doc -> {
                    AppUser u = doc.toObject(AppUser.class);
                    if (u == null) u = new AppUser("Lendly User", "", "");
                    u.setUid(userId);
                    if (u.getName() != null) nameCache.put(userId, u.getName());
                    cb.onSuccess(u);
                })
                .addOnFailureListener(cb::onError);
    }

    
    public void loadNames(Set<String> userIds, @NonNull Result<Map<String, String>> cb) {
        Map<String, String> found = new HashMap<>();
        List<String> missing = new ArrayList<>();
        for (String id : userIds) {
            if (nameCache.containsKey(id)) found.put(id, nameCache.get(id));
            else missing.add(id);
        }
        if (missing.isEmpty()) { cb.onSuccess(found); return; }

        final int[] remaining = { missing.size() };
        for (String id : missing) {
            db.collection(USERS).document(id).get()
                    .addOnCompleteListener(task -> {
                        String name = "Lendly User";
                        if (task.isSuccessful() && task.getResult() != null
                                && task.getResult().getString("name") != null) {
                            name = task.getResult().getString("name");
                        }
                        nameCache.put(id, name);
                        found.put(id, name);
                        remaining[0]--;
                        if (remaining[0] == 0) cb.onSuccess(found);
                    });
        }
    }

   
    private Item toItem(DocumentSnapshot doc) {
        Item item = doc.toObject(Item.class);
        if (item != null) item.setItemId(doc.getId());
        return item;
    }

    private List<Item> toItems(QuerySnapshot snap) {
        List<Item> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap) {
            Item item = toItem(doc);
            if (item != null) list.add(item);
        }
        
        Collections.sort(list, (a, b) -> {
            Timestamp ta = a.getCreatedAt(), tb = b.getCreatedAt();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        return list;
    }

    
    public void loadMarketplace(String category, @NonNull Result<List<Item>> cb) {
        db.collection(ITEMS).whereEqualTo("status", "AVAILABLE").get()
                .addOnSuccessListener(snap -> {
                    List<Item> all = toItems(snap);
                    List<Item> out = new ArrayList<>();
                    String me = uid();
                    for (Item i : all) {
                        if (me != null && me.equals(i.getOwnerId())) continue;
                        if (category != null && !category.equals(i.getCategory())) continue;
                        out.add(i);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onError);
    }

    
    public void loadMyItems(@NonNull Result<List<Item>> cb) {
        if (uid() == null) { cb.onSuccess(new ArrayList<>()); return; }
        db.collection(ITEMS).whereEqualTo("ownerId", uid()).get()
                .addOnSuccessListener(snap -> cb.onSuccess(toItems(snap)))
                .addOnFailureListener(cb::onError);
    }

    public void loadItem(String itemId, @NonNull Result<Item> cb) {
        db.collection(ITEMS).document(itemId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { cb.onError(new Exception("Item not found")); return; }
                    cb.onSuccess(toItem(doc));
                })
                .addOnFailureListener(cb::onError);
    }

    
    public void searchItems(String keyword, @NonNull Result<List<Item>> cb) {
        loadMarketplace(null, new Result<List<Item>>() {
            @Override public void onSuccess(List<Item> all) {
                if (keyword == null || keyword.trim().isEmpty()) { cb.onSuccess(all); return; }
                String q = keyword.toLowerCase(Locale.getDefault()).trim();

                List<Item> exact = new ArrayList<>();
                List<Item> similar = new ArrayList<>();
                Set<String> cats = new HashSet<>();

                for (Item i : all) {
                    String name = i.getItemName() == null ? "" : i.getItemName().toLowerCase(Locale.getDefault());
                    String desc = i.getDescription() == null ? "" : i.getDescription().toLowerCase(Locale.getDefault());
                    if (name.contains(q) || desc.contains(q)) {
                        exact.add(i);
                        if (i.getCategory() != null) cats.add(i.getCategory());
                    }
                }
                for (Item i : all) {
                    if (exact.contains(i)) continue;
                    if (i.getCategory() != null && cats.contains(i.getCategory())) similar.add(i);
                }
                List<Item> result = new ArrayList<>(exact);
                result.addAll(similar);
                cb.onSuccess(result);
            }
            @Override public void onError(Exception e) { cb.onError(e); }
        });
    }

    public Task<Void> saveItem(Item item) {
        item.setOwnerId(uid());
        item.setOwnerName(currentName());
        if (item.getStatus() == null) item.setStatus("AVAILABLE");

        if (item.getItemId() == null || item.getItemId().isEmpty()) {
            DocumentReference ref = db.collection(ITEMS).document();
            item.setItemId(ref.getId());
            item.setCreatedAt(Timestamp.now());
            return ref.set(item);
        }
        return db.collection(ITEMS).document(item.getItemId()).set(item, SetOptions.merge());
    }

    public Task<Void> deleteItem(String itemId) {
        return db.collection(ITEMS).document(itemId).delete();
    }

    public Task<Void> setItemStatus(String itemId, String status) {
        return db.collection(ITEMS).document(itemId).update("status", status);
    }

    
    public Task<Void> addFavorite(String itemId) {
        Map<String, Object> data = new HashMap<>();
        data.put("itemId", itemId);
        data.put("savedAt", Timestamp.now());
        return db.collection(USERS).document(uid())
                .collection("favorites").document(itemId).set(data);
    }

    public Task<Void> removeFavorite(String itemId) {
        return db.collection(USERS).document(uid())
                .collection("favorites").document(itemId).delete();
    }

    public void isFavorite(String itemId, @NonNull Result<Boolean> cb) {
        if (uid() == null) { cb.onSuccess(false); return; }
        db.collection(USERS).document(uid()).collection("favorites").document(itemId).get()
                .addOnSuccessListener(doc -> cb.onSuccess(doc.exists()))
                .addOnFailureListener(e -> cb.onSuccess(false));
    }

    public void loadFavorites(@NonNull Result<List<Item>> cb) {
        if (uid() == null) { cb.onSuccess(new ArrayList<>()); return; }
        db.collection(USERS).document(uid()).collection("favorites").get()
                .addOnSuccessListener(snap -> {
                    List<String> ids = new ArrayList<>();
                    for (QueryDocumentSnapshot d : snap) ids.add(d.getId());
                    if (ids.isEmpty()) { cb.onSuccess(new ArrayList<>()); return; }

                    List<Item> items = new ArrayList<>();
                    final int[] left = { ids.size() };
                    for (String id : ids) {
                        db.collection(ITEMS).document(id).get()
                                .addOnCompleteListener(t -> {
                                    if (t.isSuccessful() && t.getResult() != null && t.getResult().exists()) {
                                        Item i = toItem(t.getResult());
                                        if (i != null) items.add(i);
                                    }
                                    left[0]--;
                                    if (left[0] == 0) cb.onSuccess(items);
                                });
                    }
                })
                .addOnFailureListener(cb::onError);
    }

    

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private SimpleDateFormat dateFormat() {
        SimpleDateFormat f = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        f.setTimeZone(TimeZone.getTimeZone("Asia/Kuala_Lumpur"));
        return f;
    }

    
    private void markOverdue(List<BorrowRequest> requests) {
        SimpleDateFormat f = dateFormat();
        Date today;
        try { today = f.parse(f.format(new Date())); } catch (Exception e) { return; }
        if (today == null) return;

        for (BorrowRequest r : requests) {
            if (!BorrowRequest.ACCEPTED.equals(r.getStatus())) continue;
            if (r.getReturnDate() == null) continue;
            try {
                Date due = f.parse(r.getReturnDate());
                if (due != null && due.before(today)) {
                    r.setStatus(BorrowRequest.OVERDUE);
                    db.collection(REQUESTS).document(r.getId())
                            .update("status", BorrowRequest.OVERDUE);
                }
            } catch (Exception ignored) { }
        }
    }

    private List<BorrowRequest> toRequests(QuerySnapshot snap) {
        List<BorrowRequest> list = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snap) {
            BorrowRequest r = doc.toObject(BorrowRequest.class);
            r.setId(doc.getId());
            list.add(r);
        }
        Collections.sort(list, (a, b) -> {
            Timestamp ta = a.getCreatedAt(), tb = b.getCreatedAt();
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });
        markOverdue(list);
        return list;
    }
    
    public static final int ARCHIVE_AFTER_DAYS = 14;

   
    public static boolean isArchived(BorrowRequest r) {
        if (!r.isFinished()) return false;
        if (r.getCreatedAt() == null) return false;

        long ageMillis = System.currentTimeMillis() - r.getCreatedAt().toDate().getTime();
        long days = ageMillis / (1000L * 60 * 60 * 24);
        return days >= ARCHIVE_AFTER_DAYS;
    }
    
    public void hasOpenRequestFor(String itemId, @NonNull Result<Boolean> cb) {
        if (uid() == null || itemId == null) { cb.onSuccess(false); return; }

        db.collection(REQUESTS).whereEqualTo("borrowerId", uid()).get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        if (!itemId.equals(doc.getString("itemId"))) continue;
                        String status = doc.getString("status");
                        
                        if (BorrowRequest.PENDING.equals(status)
                                || BorrowRequest.ACCEPTED.equals(status)
                                || BorrowRequest.OVERDUE.equals(status)) {
                            cb.onSuccess(true);
                            return;
                        }
                    }
                    cb.onSuccess(false);
                })
                .addOnFailureListener(e -> cb.onSuccess(false));
    }

    public Task<Void> createRequest(BorrowRequest request) {
        DocumentReference ref = db.collection(REQUESTS).document();
        request.setId(ref.getId());
        request.setCreatedAt(Timestamp.now());
        return ref.set(request);
    }

    
    public void loadIncomingRequests(@NonNull Result<List<BorrowRequest>> cb) {
        if (uid() == null) { cb.onSuccess(new ArrayList<>()); return; }
        db.collection(REQUESTS).whereEqualTo("lenderId", uid()).get()
                .addOnSuccessListener(snap -> cb.onSuccess(toRequests(snap)))
                .addOnFailureListener(cb::onError);
    }

    
    public void loadMyRequests(@NonNull Result<List<BorrowRequest>> cb) {
        if (uid() == null) { cb.onSuccess(new ArrayList<>()); return; }
        db.collection(REQUESTS).whereEqualTo("borrowerId", uid()).get()
                .addOnSuccessListener(snap -> cb.onSuccess(toRequests(snap)))
                .addOnFailureListener(cb::onError);
    }

    
    public Task<Void> updateRequestStatus(BorrowRequest request, String newStatus) {
        final String itemId = request.getItemId();

        String itemStatus = null;
        if (BorrowRequest.ACCEPTED.equals(newStatus)) itemStatus = "BORROWED";
        else if (BorrowRequest.RETURNED.equals(newStatus)) itemStatus = "AVAILABLE";
        else if (BorrowRequest.PURCHASED.equals(newStatus)) itemStatus = "SOLD";

        final String finalItemStatus = itemStatus;
        final boolean closesOthers = BorrowRequest.ACCEPTED.equals(newStatus)
                || BorrowRequest.PURCHASED.equals(newStatus);

        
        if (!closesOthers || itemId == null || uid() == null) {
            return writeBatch(request, newStatus, itemId, finalItemStatus, new ArrayList<>());
        }

        return db.collection(REQUESTS).whereEqualTo("lenderId", uid()).get()
                .continueWithTask(task -> {
                    List<String> toReject = new ArrayList<>();
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            if (doc.getId().equals(request.getId())) continue;
                            if (!itemId.equals(doc.getString("itemId"))) continue;
                            if (!BorrowRequest.PENDING.equals(doc.getString("status"))) continue;
                            toReject.add(doc.getId());
                        }
                    }
                    return writeBatch(request, newStatus, itemId, finalItemStatus, toReject);
                });
    }

    
    private Task<Void> writeBatch(BorrowRequest request, String newStatus,
                                  String itemId, String itemStatus,
                                  List<String> alsoReject) {
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        batch.update(db.collection(REQUESTS).document(request.getId()),
                "status", newStatus);

        if (itemId != null && itemStatus != null) {
            batch.update(db.collection(ITEMS).document(itemId), "status", itemStatus);
        }

        for (String otherId : alsoReject) {
            batch.update(db.collection(REQUESTS).document(otherId),
                    "status", BorrowRequest.REJECTED);
        }
        return batch.commit();
    }

    
    public void checkAvailable(String itemId, @NonNull Result<Boolean> cb) {
        db.collection(ITEMS).document(itemId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) { cb.onSuccess(false); return; }
                    cb.onSuccess("AVAILABLE".equals(doc.getString("status")));
                })
                .addOnFailureListener(cb::onError);
    }

    
    public Task<Void> markPaymentReceived(String requestId) {
        return db.collection(REQUESTS).document(requestId)
                .update("paymentReceived", true);
    }

    public Task<Void> markRated(String requestId, boolean iAmBorrower) {
        return db.collection(REQUESTS).document(requestId)
                .update(iAmBorrower ? "borrowerRated" : "lenderRated", true);
    }

    
    public Task<Void> submitReview(Review review) {
        review.setReviewerId(uid());
        review.setReviewerName(currentName());
        review.setCreatedAt(Timestamp.now());

        DocumentReference reviewRef = db.collection(REVIEWS).document();
        DocumentReference userRef = db.collection(USERS).document(review.getRevieweeId());

        com.google.firebase.firestore.WriteBatch batch = db.batch();
        batch.set(reviewRef, review);
        Map<String, Object> bump = new HashMap<>();
        bump.put("ratingTotal", FieldValue.increment(review.getRating()));
        bump.put("ratingCount", FieldValue.increment(1));
        batch.set(userRef, bump, SetOptions.merge());
        return batch.commit();
    }

    public void loadReviewsFor(String userId, @NonNull Result<List<Review>> cb) {
        db.collection(REVIEWS).whereEqualTo("revieweeId", userId).get()
                .addOnSuccessListener(snap -> {
                    List<Review> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        Review r = doc.toObject(Review.class);
                        r.setId(doc.getId());
                        list.add(r);
                    }
                    Collections.sort(list, (a, b) -> {
                        Timestamp ta = a.getCreatedAt(), tb = b.getCreatedAt();
                        if (ta == null || tb == null) return 0;
                        return tb.compareTo(ta);
                    });
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onError);
    }

    
    public static String chatIdFor(String uidA, String uidB) {
        return uidA.compareTo(uidB) < 0 ? uidA + "_" + uidB : uidB + "_" + uidA;
    }

    public static List<String> participantsFor(String uidA, String uidB) {
        return uidA.compareTo(uidB) < 0 ? Arrays.asList(uidA, uidB) : Arrays.asList(uidB, uidA);
    }

    public Task<Void> ensureChat(String otherUserId) {
        String chatId = chatIdFor(uid(), otherUserId);
        Map<String, Object> data = new HashMap<>();
        data.put("participantIds", participantsFor(uid(), otherUserId));
        return db.collection(CHATS).document(chatId).set(data, SetOptions.merge());
    }

    
    public void loadChats(@NonNull Result<List<ChatSummary>> cb) {
        if (uid() == null) { cb.onSuccess(new ArrayList<>()); return; }
        db.collection(CHATS).whereArrayContains("participantIds", uid()).get()
                .addOnSuccessListener(snap -> {
                    List<ChatSummary> chats = new ArrayList<>();
                    Set<String> otherIds = new HashSet<>();

                    for (QueryDocumentSnapshot doc : snap) {
                        List<String> parts = (List<String>) doc.get("participantIds");
                        if (parts == null) continue;
                        String other = null;
                        for (String p : parts) if (!p.equals(uid())) other = p;
                        if (other == null) continue;
                        otherIds.add(other);
                        chats.add(new ChatSummary(
                                doc.getId(), other, null,
                                doc.getString("lastMessage"),
                                doc.getTimestamp("lastMessageAt")));
                    }

                    Collections.sort(chats, new Comparator<ChatSummary>() {
                        @Override public int compare(ChatSummary a, ChatSummary b) {
                            Timestamp ta = a.getLastMessageAt(), tb = b.getLastMessageAt();
                            if (ta == null && tb == null) return 0;
                            if (ta == null) return 1;
                            if (tb == null) return -1;
                            return tb.compareTo(ta);
                        }
                    });

                    if (otherIds.isEmpty()) { cb.onSuccess(chats); return; }
                    loadNames(otherIds, names -> {
                        for (ChatSummary c : chats) {
                            String n = names.get(c.getOtherUserId());
                            c.setOtherUserName(n == null ? "Lendly User" : n);
                        }
                        cb.onSuccess(chats);
                    });
                })
                .addOnFailureListener(cb::onError);
    }

    public FirebaseFirestore db() { return db; }
}