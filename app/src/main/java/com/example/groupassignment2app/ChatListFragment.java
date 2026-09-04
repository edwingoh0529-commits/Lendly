package com.example.groupassignment2app;

import android.content.Intent;
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
import com.example.groupassignment2app.model.ChatSummary;

import java.util.List;


public class ChatListFragment extends Fragment {

    private ChatListAdapter adapter;
    private TextView emptyText;
    private final Repo repo = Repo.get();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        
        InsetUtil.padTop(view.findViewById(R.id.headerBar));

        emptyText = view.findViewById(R.id.txtEmptyChats);
        RecyclerView recycler = view.findViewById(R.id.recyclerChats);

        adapter = new ChatListAdapter(chat -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_OTHER_ID, chat.getOtherUserId());
            intent.putExtra(ChatActivity.EXTRA_OTHER_NAME, chat.getOtherUserName());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        repo.loadChats(new Repo.Result<List<ChatSummary>>() {
            @Override public void onSuccess(List<ChatSummary> chats) {
                if (!isAdded()) return;
                adapter.setChats(chats);
                emptyText.setVisibility(chats.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(Exception e) {
                if (!isAdded()) return;
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText("Could not load chats.\n" + e.getMessage());
            }
        });
    }
}