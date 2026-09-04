package com.example.groupassignment2app.model;

import com.google.firebase.Timestamp;

public class Message {

    private String senderId;
    private String receiverId;
    private String text;

    private String imageBase64;

    private Timestamp timestamp;

    public Message() { }

    public Message(String senderId, String receiverId, String text) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    @com.google.firebase.firestore.Exclude
    public boolean hasImage() { return imageBase64 != null && !imageBase64.isEmpty(); }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}