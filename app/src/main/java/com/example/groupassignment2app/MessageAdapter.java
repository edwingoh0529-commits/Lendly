package com.example.groupassignment2app;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.data.ImageUtil;
import com.example.groupassignment2app.model.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    private final List<Message> messages;
    private final String myId;

    public MessageAdapter(List<Message> messages, String myId) {
        this.messages = messages;
        this.myId = myId;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Message message = messages.get(position);
        boolean mine = myId.equals(message.getSenderId());

        holder.sent.setVisibility(mine ? View.VISIBLE : View.GONE);
        holder.received.setVisibility(mine ? View.GONE : View.VISIBLE);

        ImageView image = mine ? holder.sentImage : holder.receivedImage;
        TextView body = mine ? holder.sentText : holder.receivedText;
        TextView stamp = mine ? holder.sentTime : holder.receivedTime;

        if (message.hasImage()) {
            Bitmap bmp = ImageUtil.base64ToBitmap(message.getImageBase64());
            if (bmp != null) {
                image.setVisibility(View.VISIBLE);
                image.setImageBitmap(bmp);
            } else {
                image.setVisibility(View.GONE);
            }
        } else {
            image.setVisibility(View.GONE);
        }

        String text = message.getText();
        if (text == null || text.trim().isEmpty()) {
            body.setVisibility(View.GONE);
        } else {
            body.setVisibility(View.VISIBLE);
            body.setText(text);
        }

        stamp.setText(time(message));
    }

    private String time(Message message) {
        if (message.getTimestamp() == null) return "";
        return new SimpleDateFormat("h:mm a", Locale.getDefault())
                .format(message.getTimestamp().toDate());
    }

    @Override
    public int getItemCount() { return messages.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final LinearLayout sent, received;
        final TextView sentText, receivedText, sentTime, receivedTime;
        final ImageView sentImage, receivedImage;

        Holder(@NonNull View v) {
            super(v);
            sent = v.findViewById(R.id.layout_sent);
            received = v.findViewById(R.id.layout_received);
            sentText = v.findViewById(R.id.txt_sent_message);
            receivedText = v.findViewById(R.id.txt_received_message);
            sentTime = v.findViewById(R.id.txt_sent_time);
            receivedTime = v.findViewById(R.id.txt_received_time);
            sentImage = v.findViewById(R.id.img_sent);
            receivedImage = v.findViewById(R.id.img_received);
        }
    }
}