package com.example.groupassignment2app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.model.ChatSummary;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.Holder> {

    public interface OnChatClick { void onChatClick(ChatSummary chat); }

    private List<ChatSummary> chats = new ArrayList<>();
    private final OnChatClick listener;

    public ChatListAdapter(OnChatClick listener) { this.listener = listener; }

    public void setChats(List<ChatSummary> chats) {
        this.chats = chats == null ? new ArrayList<>() : chats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChatSummary chat = chats.get(position);

        String name = chat.getOtherUserName() == null ? "Lendly User" : chat.getOtherUserName();
        holder.name.setText(name);
        holder.avatar.setText(name.substring(0, 1).toUpperCase(Locale.getDefault()));

        String last = chat.getLastMessage();
        holder.preview.setText(last == null || last.isEmpty() ? "Say hello" : last);
        holder.time.setText(formatTime(chat));

        holder.itemView.setOnClickListener(v -> listener.onChatClick(chat));
    }

    
    private String formatTime(ChatSummary chat) {
        if (chat.getLastMessageAt() == null) return "";
        Date date = chat.getLastMessageAt().toDate();

        Calendar then = Calendar.getInstance();
        then.setTime(date);
        Calendar now = Calendar.getInstance();

        boolean sameDay = then.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                && then.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR);

        SimpleDateFormat f = new SimpleDateFormat(sameDay ? "h:mm a" : "d MMM", Locale.getDefault());
        return f.format(date);
    }

    @Override
    public int getItemCount() { return chats.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView avatar, name, preview, time;

        Holder(@NonNull View v) {
            super(v);
            avatar = v.findViewById(R.id.txtChatRowAvatar);
            name = v.findViewById(R.id.txtChatRowName);
            preview = v.findViewById(R.id.txtChatRowPreview);
            time = v.findViewById(R.id.txtChatRowTime);
        }
    }
}