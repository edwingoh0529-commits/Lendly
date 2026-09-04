package com.example.groupassignment2app.model;

import com.google.firebase.Timestamp;

public class ChatSummary {

    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String lastMessage;
    private Timestamp lastMessageAt;

    public ChatSummary(String chatId, String otherUserId, String otherUserName,
                       String lastMessage, Timestamp lastMessageAt) {
        this.chatId = chatId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
    }

    public String getChatId() { return chatId; }
    public String getOtherUserId() { return otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String n) { this.otherUserName = n; }
    public String getLastMessage() { return lastMessage; }
    public Timestamp getLastMessageAt() { return lastMessageAt; }
}