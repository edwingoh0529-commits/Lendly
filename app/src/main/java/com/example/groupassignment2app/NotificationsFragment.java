package com.example.groupassignment2app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.BorrowRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NotificationsFragment extends Fragment {

    private static final int STALE_AFTER_DAYS = 14;
    private static final int MAX_NOTICES = 50;

    private NotificationAdapter adapter;
    private TextView emptyText;
    private final Repo repo = Repo.get();

    public static class Notice {
        public final String title;
        public final String subtitle;
        public final String badge;
        public final long timeMillis;
        public final boolean unread;

        Notice(String title, String subtitle, String badge, long timeMillis, boolean unread) {
            this.title = title;
            this.subtitle = subtitle;
            this.badge = badge;
            this.timeMillis = timeMillis;
            this.unread = unread;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        InsetUtil.padTop(view.findViewById(R.id.headerBar));


        emptyText = view.findViewById(R.id.txtEmptyNotifications);
        RecyclerView recycler = view.findViewById(R.id.recyclerNotifications);

        adapter = new NotificationAdapter();
        recycler.setAdapter(adapter);

        view.findViewById(R.id.btnGoToLends).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity main = (MainActivity) getActivity();
                main.showFragment(new LendsFragment(), false);
                main.selectTab(R.id.nav_lends);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        List<Notice> notices = new ArrayList<>();

        repo.loadIncomingRequests(incoming -> {
            for (BorrowRequest r : incoming) {
                long when = r.getCreatedAt() == null ? 0 : r.getCreatedAt().toDate().getTime();

                if (BorrowRequest.PENDING.equals(r.getStatus())) {
                    notices.add(new Notice(
                            name(r.getBorrowerName()) + " wants to "
                                    + ("BUY".equals(r.getType()) ? "buy" : "borrow")
                                    + " your item",
                            r.getItemName(), "ACTION NEEDED", when, true));
                } else if (BorrowRequest.OVERDUE.equals(r.getStatus())) {
                    notices.add(new Notice(
                            r.getItemName() + " is overdue",
                            "Due back " + r.getReturnDate()
                                    + " from " + name(r.getBorrowerName()),
                            "OVERDUE", when, true));
                } else if (BorrowRequest.RETURNED.equals(r.getStatus()) && !r.getLenderRated()) {
                    notices.add(new Notice(
                            "Rate " + name(r.getBorrowerName()),
                            r.getItemName() + " was returned", "RATE", when, true));
                }
            }

            repo.loadMyRequests(mine -> {
                if (!isAdded()) return;

                for (BorrowRequest r : mine) {
                    long when = r.getCreatedAt() == null ? 0 : r.getCreatedAt().toDate().getTime();

                    if (BorrowRequest.ACCEPTED.equals(r.getStatus())) {
                        notices.add(new Notice(
                                name(r.getLenderName()) + " accepted your request",
                                r.getItemName() + "  ·  return by " + r.getReturnDate(),
                                "ACCEPTED", when, true));
                    } else if (BorrowRequest.REJECTED.equals(r.getStatus())) {
                        notices.add(new Notice(
                                name(r.getLenderName()) + " declined your request",
                                r.getItemName(), "DECLINED", when, false));
                    } else if (BorrowRequest.OVERDUE.equals(r.getStatus())) {
                        notices.add(new Notice(
                                "Please return " + r.getItemName(),
                                "It was due on " + r.getReturnDate(), "OVERDUE", when, true));
                    } else if (BorrowRequest.PURCHASED.equals(r.getStatus())) {
                        notices.add(new Notice(
                                "Purchase confirmed",
                                r.getItemName() + " from " + name(r.getLenderName()),
                                "BOUGHT", when, false));
                    } else if (BorrowRequest.RETURNED.equals(r.getStatus()) && !r.getBorrowerRated()) {
                        notices.add(new Notice(
                                "Rate " + name(r.getLenderName()),
                                "You returned " + r.getItemName(), "RATE", when, true));
                    }
                }

                Collections.sort(notices, (a, b) -> Long.compare(b.timeMillis, a.timeMillis));

                List<Notice> visible = trim(notices);
                adapter.setNotices(visible);
                emptyText.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private List<Notice> trim(List<Notice> notices) {
        long cutoff = System.currentTimeMillis()
                - (long) STALE_AFTER_DAYS * 24 * 60 * 60 * 1000;

        List<Notice> kept = new ArrayList<>();
        for (Notice n : notices) {
            boolean needsAction = n.unread;
            boolean recent = n.timeMillis == 0 || n.timeMillis >= cutoff;
            if (needsAction || recent) kept.add(n);
            if (kept.size() >= MAX_NOTICES) break;
        }
        return kept;
    }

    private String name(String s) { return s == null || s.isEmpty() ? "A student" : s; }
}