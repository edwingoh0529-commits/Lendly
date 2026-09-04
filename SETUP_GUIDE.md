# Lendly — Setup Guide (Important)

Group 5 · UCCD3223 Mobile Applications Development · Group Assignment 2

This guide takes you from the zip file to a running app. Follow it in order.
Steps 1–4 are the ones you cannot skip.

---

## 1. Open the project

1. Unzip `Lendly-merged.zip` somewhere with a short path.
   Good: `C:\AndroidProjects\Lendly`
   Bad: `C:\Users\...\OneDrive\Desktop\Assignment\Final Version (2)\...`
   Long paths and OneDrive sync both cause weird Gradle errors on Windows.
2. Android Studio → **File → Open** → pick the `Lendly` folder itself
   (the one containing `settings.gradle.kts`). Do **not** open the `app`
   folder.
3. Wait for the bottom status bar to finish "Gradle sync". First time this
   downloads a few hundred MB, so it can take 5–10 minutes.

If sync fails, jump to the Troubleshooting section at the bottom.

---

## 2. Turn on Email/Password login in Firebase

Your Firebase project is `lendly-323fa`. The `google-services.json` in
`app/` is already correct, so you do not need to re-download it.

1. Go to <https://console.firebase.google.com> → open **lendly-323fa**
2. Left menu → **Build → Authentication** → **Get started**
3. **Sign-in method** tab → click **Email/Password**
4. Turn on the first toggle (leave "Email link" off) → **Save**

Without this, the sign-up screen fails with
`CONFIGURATION_NOT_FOUND`.

---

## 3. Create the Firestore database

1. Left menu → **Build → Firestore Database** → **Create database**
2. Choose **Start in test mode** for now
3. Location: pick **asia-southeast1 (Singapore)** — closest to Malaysia
4. Click **Enable**

---

## 4. Paste the security rules  ← the step people forget

Test mode expires after 30 days, and after that the app looks completely
broken: every list is empty and every save fails, with no error message.

1. Firestore Database → **Rules** tab
2. Delete everything in the box
3. Open `firebase/firestore.rules` from this project, copy all of it, paste in
4. Click **Publish**

These rules say, in plain English: you must be logged in to see anything,
and you can only change data that belongs to you. There are two carefully
limited exceptions, both commented in the file — one lets a review bump
someone else's star average, the other lets an accepted borrow flip an
item's status.

**You do not need Firebase Storage.** Photos are stored as compressed text
inside Firestore, so the whole project stays on the free Spark plan.

---

## 5. Run it

1. Plug in a phone with USB debugging on, or start an emulator
   (**Device Manager → Create Device**, pick Pixel 6, API 34+)
2. Press the green ▶ Run button

---

## 6. Demo walkthrough — create TWO accounts

The marketplace deliberately hides your own listings, because you cannot
borrow from yourself. So with one account the Home tab looks empty and it
seems broken. **Make two accounts.**

**Account A** (the lender):
1. Sign up as `a@test.com` / `123456`
2. Profile tab → **Add samples** → adds 7 example listings
3. Or Profile → **List an item** to add your own with a photo and a pickup
   point on the campus map

**Account B** (the borrower):
1. Profile → **Log out** → Sign up as `b@test.com` / `123456`
2. Home tab → account A's items are now visible
3. Tap an item → **Borrow** → pick dates → **Send request**
4. Or tap **Message the owner** to start a chat

**Back to account A:**
1. Log out, log in as `a@test.com`
2. **Activity** tab shows the request, red badge on the bell
3. **Lends** tab → *Lending out* → **Accept**
4. When it comes back → **Mark returned**
5. Both sides can now **Rate** each other → the stars appear on the profile

Tip for the demo video: run account A on the emulator and account B on a
real phone at the same time. Chat updates live on both, which looks good.

---

## What each file does

```
app/src/main/java/com/example/groupassignment2app/
│
├── data/
│   ├── Repo.java          ← EVERY Firestore read and write lives here
│   ├── ImageUtil.java     ← shrinks photos, converts to/from Base64
│   └── SampleData.java    ← the 7 demo listings
│
├── model/                 ← plain data classes that mirror Firestore docs
│   ├── Item.java  BorrowRequest.java  Review.java
│   ├── AppUser.java  Message.java  ChatSummary.java
│
├── LoginActivity / SignUpActivity      ← real email+password auth
├── MainActivity                        ← the 5-tab shell
│
├── HomeFragment          ← marketplace, categories, search bar
├── SearchFragment        ← live search
├── ItemDetailFragment    ← borrow / buy / chat / map / edit / delete
├── EditItemFragment      ← the "list an item" form
├── ItemListFragment      ← Favourites, History, Orders (one reusable screen)
├── ProfileFragment       ← reputation, my listings, logout
├── ChatListFragment      ← all my conversations
├── ChatActivity          ← live 1-to-1 chat
├── LendsFragment         ← accept / reject / mark returned / rate
├── NotificationsFragment ← the Activity feed
├── BorrowRequestActivity ← date picker + cost preview
├── RatingReviewActivity  ← stars + comment
└── MapActivity           ← campus pickup point (pick mode + view mode)
```

### Firestore collections

| Collection | Holds | Key fields |
|---|---|---|
| `users` | student profiles | `name`, `ratingTotal`, `ratingCount` |
| `users/{uid}/favorites` | saved items | doc id = itemId |
| `items` | listings | `ownerId`, `status`, `itemType`, `imageBase64` |
| `requests` | borrow + buy requests | `borrowerId`, `lenderId`, `status` |
| `reviews` | star ratings | `reviewerId`, `revieweeId`, `rating` |
| `chats` | conversations | `participantIds` (array of 2 uids) |
| `chats/{id}/messages` | messages | `senderId`, `text`, `timestamp` |

### Request status flow

```
PENDING ──accept──> ACCEPTED ──owner marks returned──> RETURNED ──> both rate
   │                    │
   │                    └──past return date──> OVERDUE
   └──reject──> REJECTED

BUY requests: PENDING ──confirm──> PURCHASED
```

---

## Troubleshooting

**Gradle sync fails / "plugin not found"**
File → **Invalidate Caches → Invalidate and Restart**. If it still fails,
check you have internet and that no VPN is blocking `dl.google.com`.

**"Failed to resolve: com.github.bumptech.glide"**
Glide comes from Maven Central, which is already in `settings.gradle.kts`.
This is almost always a network issue — retry the sync.

**App installs but the Home tab is always empty**
Expected with one account. See step 6 — make a second account.

**Everything is empty and nothing saves, on both accounts**
You skipped step 4, or test mode expired. Paste the rules and Publish.

**`CONFIGURATION_NOT_FOUND` when signing up**
Step 2 — Email/Password is not enabled.

**`PERMISSION_DENIED` in Logcat**
The rules are published but you are signed out. Log out and back in.

**`FAILED_PRECONDITION: The query requires an index`**
Should not happen — every query in `Repo.java` deliberately uses a single
`where` filter and sorts in Java to avoid composite indexes. If you add a
new query with two filters plus `orderBy`, Firestore will print a URL in
Logcat; clicking it creates the index for you.

**Photo upload does nothing**
On Android 13+ the emulator sometimes has no images. Open the emulator's
Photos app first, or drag a JPG onto the emulator window.

**How do I see the data?**
Firebase Console → Firestore Database → **Data** tab. Every write shows up
there within a second, which is the fastest way to check something worked.

---

## Report notes

Things worth mentioning in your Assignment 2 write-up, since they were
design decisions rather than accidents:

- **One data layer.** All Firestore access goes through `Repo.java` so the
  collection and field names are declared once. Before merging, the three
  modules each talked to storage in their own way and two used fake
  in-memory lists.
- **Base64 photos instead of Cloud Storage.** Firebase now requires the paid
  Blaze plan for Cloud Storage. Images are downscaled to 800px and JPEG
  compressed, then stored as text in the item document, keeping the project
  free while still supporting real photos.
- **Trust system.** `submitReview()` writes the review and increments the
  reviewee's `ratingTotal` / `ratingCount` in a single batch, so the profile
  average never needs to read every review. This is the rating mechanism
  described in section 3.3.2 of the proposal.
- **Normalised map coordinates.** The pickup point is stored as two numbers
  between 0 and 1 rather than pixels, so the pin lands in the same spot on
  any screen size.
- **Index-free queries.** Single-filter queries with client-side sorting,
  chosen so the app never hits Firestore's composite index requirement.
