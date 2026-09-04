package com.example.groupassignment2app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.data.ImageUtil;
import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.Message;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    public static final String EXTRA_OTHER_ID = "otherUserId";
    public static final String EXTRA_OTHER_NAME = "otherUserName";
    public static final String EXTRA_ITEM_NAME = "itemName";
    
    public static final String EXTRA_PREFILL = "prefill";

    private TextView btnBack, txtName, txtAvatar, txtSubtitle;
    private ImageButton btnSend, btnAttach;
    private EditText edtMessage;
    private RecyclerView recycler;

    private final Repo repo = Repo.get();
    private final List<Message> messages = new ArrayList<>();
    private MessageAdapter adapter;
    private ListenerRegistration listener;

    private String myId, otherId, otherName, chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        
        InsetUtil.padTop(findViewById(R.id.headerBar));

        btnBack = findViewById(R.id.btnChatBack);
        txtName = findViewById(R.id.txtOtherUserName);
        txtAvatar = findViewById(R.id.txtChatAvatar);
        txtSubtitle = findViewById(R.id.txtChatSubtitle);
        btnSend = findViewById(R.id.btnSend);
        btnAttach = findViewById(R.id.btnAttachPhoto);
        edtMessage = findViewById(R.id.edtMessage);
        recycler = findViewById(R.id.recyclerMessages);

        if (!repo.isLoggedIn()) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        myId = repo.uid();

        otherId = getIntent().getStringExtra(EXTRA_OTHER_ID);
        otherName = getIntent().getStringExtra(EXTRA_OTHER_NAME);
        if (otherName == null || otherName.trim().isEmpty()) otherName = "Lendly User";

        if (otherId == null || otherId.trim().isEmpty()) {
            Toast.makeText(this, "Could not identify the other student", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (myId.equals(otherId)) {
            Toast.makeText(this, "You cannot chat with yourself", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtName.setText(otherName);
        txtAvatar.setText(otherName.substring(0, 1).toUpperCase(Locale.getDefault()));

        String aboutItem = getIntent().getStringExtra(EXTRA_ITEM_NAME);
        txtSubtitle.setText(aboutItem == null || aboutItem.isEmpty()
                ? "Lendly chat" : "About: " + aboutItem);

        
        String prefill = getIntent().getStringExtra(EXTRA_PREFILL);
        if (prefill != null && !prefill.isEmpty()) {
            edtMessage.setText(prefill);
            edtMessage.setSelection(edtMessage.getText().length());
        }

        chatId = Repo.chatIdFor(myId, otherId);

        adapter = new MessageAdapter(messages, myId);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setStackFromEnd(true);
        recycler.setLayoutManager(layout);
        recycler.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> photoPicker.launch(
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)));

        
        repo.ensureChat(otherId)
                .addOnSuccessListener(v -> listenForMessages())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Could not open chat", e);
                    Toast.makeText(this, "Could not open chat: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    
    private final ActivityResultLauncher<Intent> photoPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                Uri uri = result.getData().getData();
                if (uri == null) return;

                btnAttach.setEnabled(false);
                Toast.makeText(this, "Preparing photo\u2026", Toast.LENGTH_SHORT).show();

                String base64 = ImageUtil.uriToChatBase64(this, uri);
                btnAttach.setEnabled(true);

                if (base64 == null) {
                    Toast.makeText(this, "Could not read that image", Toast.LENGTH_SHORT).show();
                    return;
                }
                send(edtMessage.getText().toString().trim(), base64);
            });

    private void sendMessage() {
        String text = edtMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (text.length() > 1000) {
            edtMessage.setError("Message is too long");
            return;
        }
        send(text, null);
    }

    
    private void send(String text, String imageBase64) {
        boolean hasPhoto = imageBase64 != null && !imageBase64.isEmpty();
        if (TextUtils.isEmpty(text) && !hasPhoto) return;

        DocumentReference chatRef = repo.db().collection(Repo.CHATS).document(chatId);
        DocumentReference messageRef = chatRef.collection(Repo.MESSAGES).document();

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("participantIds", Repo.participantsFor(myId, otherId));
        
        chatData.put("lastMessage", hasPhoto
                ? (TextUtils.isEmpty(text) ? "\uD83D\uDCF7 Photo" : "\uD83D\uDCF7 " + text)
                : text);
        chatData.put("lastMessageAt", FieldValue.serverTimestamp());

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", myId);
        messageData.put("receiverId", otherId);
        messageData.put("text", text);
        if (hasPhoto) messageData.put("imageBase64", imageBase64);
        messageData.put("timestamp", FieldValue.serverTimestamp());

        
        WriteBatch batch = repo.db().batch();
        batch.set(chatRef, chatData, SetOptions.merge());
        batch.set(messageRef, messageData);

        edtMessage.setText("");

        batch.commit().addOnFailureListener(e -> {
            Log.e(TAG, "Send failed", e);
            Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_LONG).show();
            edtMessage.setText(text);
        });
    }

    private void listenForMessages() {
        if (listener != null) listener.remove();

        listener = repo.db()
                .collection(Repo.CHATS).document(chatId)
                .collection(Repo.MESSAGES)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        return;
                    }
                    if (snapshot == null) return;

                    messages.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Message m = doc.toObject(Message.class);
                        if (m != null) messages.add(m);
                    }
                    adapter.notifyDataSetChanged();

                    if (!messages.isEmpty()) {
                        recycler.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}