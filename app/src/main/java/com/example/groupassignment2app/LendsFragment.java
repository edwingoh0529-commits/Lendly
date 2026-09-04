package com.example.groupassignment2app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.BorrowRequest;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


public class LendsFragment extends Fragment {

    private TabLayout tabs;
    private RecyclerView recycler;
    private TextView emptyText, toggleArchive;

    private boolean showArchived = false;

    private BorrowRequestAdapter ownerAdapter, borrowerAdapter;
    private final Repo repo = Repo.get();

    private int currentTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_lends, container, false);

        InsetUtil.padTop(view.findViewById(R.id.headerBar));

        tabs = view.findViewById(R.id.tabsLends);
        recycler = view.findViewById(R.id.recyclerRequests);
        emptyText = view.findViewById(R.id.txtEmptyLends);
        toggleArchive = view.findViewById(R.id.txtToggleArchive);

        toggleArchive.setOnClickListener(v -> {
            showArchived = !showArchived;
            load();
        });

        ownerAdapter = new BorrowRequestAdapter(BorrowRequestAdapter.AS_OWNER, ownerActions);
        borrowerAdapter = new BorrowRequestAdapter(BorrowRequestAdapter.AS_BORROWER, borrowerActions);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                showArchived = false;
                load();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { load(); }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        if (!isAdded()) return;

        if (currentTab == 0) {
            recycler.setAdapter(ownerAdapter);
            repo.loadIncomingRequests(new Repo.Result<List<BorrowRequest>>() {
                @Override public void onSuccess(List<BorrowRequest> data) {
                    if (!isAdded()) return;
                    List<BorrowRequest> visible = applyArchiveFilter(data);
                    ownerAdapter.setRequests(visible);
                    showEmpty(visible.isEmpty(),
                            "No one has asked to borrow or buy your items yet.\n\n"
                                    + "List something from the Profile tab.");
                }
                @Override public void onError(Exception e) { showError(e); }
            });
        } else {
            recycler.setAdapter(borrowerAdapter);
            repo.loadMyRequests(new Repo.Result<List<BorrowRequest>>() {
                @Override public void onSuccess(List<BorrowRequest> data) {
                    if (!isAdded()) return;
                    List<BorrowRequest> visible = applyArchiveFilter(data);
                    borrowerAdapter.setRequests(visible);
                    showEmpty(visible.isEmpty(),
                            "You have not asked to borrow or buy anything yet.\n\n"
                                    + "Browse Home to find items.");
                }
                @Override public void onError(Exception e) { showError(e); }
            });
        }
    }


    private List<BorrowRequest> applyArchiveFilter(List<BorrowRequest> all) {
        List<BorrowRequest> current = new ArrayList<>();
        int archivedCount = 0;

        for (BorrowRequest r : all) {
            if (Repo.isArchived(r)) archivedCount++;
            else current.add(r);
        }

        if (archivedCount == 0) {
            toggleArchive.setVisibility(View.GONE);
        } else {
            toggleArchive.setVisibility(View.VISIBLE);
            toggleArchive.setText(showArchived
                    ? "Hide older requests"
                    : "Show " + archivedCount + " older request"
                      + (archivedCount == 1 ? "" : "s"));
        }

        return showArchived ? all : current;
    }

    private void showEmpty(boolean empty, String message) {
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        emptyText.setText(message);
    }

    private void showError(Exception e) {
        if (!isAdded()) return;
        showEmpty(true, "Could not load requests.\n" + e.getMessage());
    }


    private final BorrowRequestAdapter.Listener ownerActions = new BorrowRequestAdapter.Listener() {

        @Override public void onAccept(BorrowRequest r) {
            confirmAccept(r);
        }

        @Override public void onReject(BorrowRequest r) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reject request?")
                    .setMessage("Turn down " + safe(r.getBorrowerName())
                            + "'s request for " + r.getItemName() + "?")
                    .setPositiveButton("Reject", (d, w) ->
                            update(r, BorrowRequest.REJECTED, "Request rejected"))
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        @Override public void onMarkReturned(BorrowRequest r) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Item returned?")
                    .setMessage("Confirm that " + safe(r.getBorrowerName())
                            + " gave back " + r.getItemName()
                            + ".\n\nThe item goes back on the marketplace and you can both "
                            + "leave a rating.")
                    .setPositiveButton("Yes, returned", (d, w) ->
                            update(r, BorrowRequest.RETURNED, "Marked as returned"))
                    .setNegativeButton("Not yet", null)
                    .show();
        }

        @Override public void onRate(BorrowRequest r) {
            openRating(r, r.getBorrowerId(), safe(r.getBorrowerName()), false);
        }

        @Override public void onMessage(BorrowRequest r) {
            openChat(r.getBorrowerId(), safe(r.getBorrowerName()), r.getItemName());
        }

        @Override public void onViewMeetup(BorrowRequest r) { showMeetup(r); }

        @Override public void onMarkPaid(BorrowRequest r) { confirmPaid(r); }
    };


    private final BorrowRequestAdapter.Listener borrowerActions = new BorrowRequestAdapter.Listener() {
        @Override public void onAccept(BorrowRequest r) { }
        @Override public void onReject(BorrowRequest r) { }
        @Override public void onMarkReturned(BorrowRequest r) { }

        @Override public void onRate(BorrowRequest r) {
            openRating(r, r.getLenderId(), safe(r.getLenderName()), true);
        }

        @Override public void onMessage(BorrowRequest r) {
            openChat(r.getLenderId(), safe(r.getLenderName()), r.getItemName());
        }

        @Override public void onViewMeetup(BorrowRequest r) { showMeetup(r); }

        @Override public void onMarkPaid(BorrowRequest r) { }
    };


    private void confirmAccept(BorrowRequest r) {
        boolean buying = "BUY".equals(r.getType());
        String newStatus = buying ? BorrowRequest.PURCHASED : BorrowRequest.ACCEPTED;

        StringBuilder message = new StringBuilder();
        message.append(buying ? "Confirm the sale of " : "Accept ")
                .append(buying ? r.getItemName() : safe(r.getBorrowerName())
                                                   + "'s request for " + r.getItemName())
                .append("?");

        if (r.hasMeetupPoint() && r.getMeetupLocationName() != null) {
            message.append("\n\nYou will both meet at ")
                    .append(r.getMeetupLocationName())
                    .append(".");
        }
        if (!buying) {
            message.append("\n\nReturn by ").append(r.getReturnDate()).append(".");
        }
        message.append("\n\nPayment: ").append(r.getPaymentMethod()).append(".");

        new AlertDialog.Builder(requireContext())
                .setTitle(buying ? "Confirm sale?" : "Accept request?")
                .setMessage(message.toString())
                .setPositiveButton(buying ? "Confirm sale" : "Accept", (d, w) ->
                        update(r, newStatus, buying ? "Sale confirmed" : "Request accepted"))
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void confirmPaid(BorrowRequest r) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Payment received?")
                .setMessage("Confirm that " + safe(r.getBorrowerName())
                        + " paid you by " + r.getPaymentMethod() + " for "
                        + r.getItemName() + ".\n\nThis is a record only \u2014 Lendly "
                        + "does not move any money.")
                .setPositiveButton("Yes, paid", (d, w) ->
                        repo.markPaymentReceived(r.getId())
                                .addOnSuccessListener(v -> { toast("Marked as paid"); load(); })
                                .addOnFailureListener(e ->
                                        toast("Could not update: " + e.getMessage())))
                .setNegativeButton("Not yet", null)
                .show();
    }

    private void update(BorrowRequest r, String status, String message) {
        repo.updateRequestStatus(r, status)
                .addOnSuccessListener(v -> {
                    toast(message);
                    load();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshPendingBadge();
                    }
                })
                .addOnFailureListener(e -> toast("Could not update: " + e.getMessage()));
    }

    private void openRating(BorrowRequest r, String targetId, String targetName, boolean iAmBorrower) {
        Intent intent = new Intent(requireContext(), RatingReviewActivity.class);
        intent.putExtra(RatingReviewActivity.EXTRA_REQUEST_ID, r.getId());
        intent.putExtra(RatingReviewActivity.EXTRA_ITEM_NAME, r.getItemName());
        intent.putExtra(RatingReviewActivity.EXTRA_TARGET_ID, targetId);
        intent.putExtra(RatingReviewActivity.EXTRA_TARGET_NAME, targetName);
        intent.putExtra(RatingReviewActivity.EXTRA_I_AM_BORROWER, iAmBorrower);
        startActivity(intent);
    }


    private void showMeetup(BorrowRequest r) {
        if (!r.hasMeetupPoint()) return;
        Intent intent = new Intent(requireContext(), MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_VIEW_ONLY, true);
        intent.putExtra(MapActivity.EXTRA_MAP_X, r.getMeetupMapX().floatValue());
        intent.putExtra(MapActivity.EXTRA_MAP_Y, r.getMeetupMapY().floatValue());
        intent.putExtra(MapActivity.EXTRA_LOCATION_NAME, r.getMeetupLocationName());
        startActivity(intent);
    }

    private void openChat(String otherId, String otherName, String itemName) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_OTHER_ID, otherId);
        intent.putExtra(ChatActivity.EXTRA_OTHER_NAME, otherName);
        intent.putExtra(ChatActivity.EXTRA_ITEM_NAME, itemName);
        startActivity(intent);
    }

    private String safe(String s) { return s == null || s.isEmpty() ? "the student" : s; }

    private void toast(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}