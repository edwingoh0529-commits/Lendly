package com.example.groupassignment2app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {

    private List<NotificationsFragment.Notice> notices = new ArrayList<>();

    public void setNotices(List<NotificationsFragment.Notice> notices) {
        this.notices = notices == null ? new ArrayList<>() : notices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        NotificationsFragment.Notice n = notices.get(position);
        h.title.setText(n.title);
        h.subtitle.setText(n.subtitle);
        h.badge.setText(n.badge);
        h.dot.setVisibility(n.unread ? View.VISIBLE : View.INVISIBLE);
        h.time.setText(n.timeMillis == 0 ? ""
                : new SimpleDateFormat("d MMM", Locale.getDefault()).format(new Date(n.timeMillis)));
    }

    @Override
    public int getItemCount() { return notices.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title, subtitle, badge, time;
        final View dot;

        Holder(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.txtNotifTitle);
            subtitle = v.findViewById(R.id.txtNotifSubtitle);
            badge = v.findViewById(R.id.txtNotifBadge);
            time = v.findViewById(R.id.txtNotifTime);
            dot = v.findViewById(R.id.viewUnreadDot);
        }
    }
}