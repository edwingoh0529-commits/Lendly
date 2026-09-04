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
import com.example.groupassignment2app.model.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemListFragment extends Fragment {

    public static final String MODE_FAVORITES = "favorites";
    public static final String MODE_HISTORY = "history";
    public static final String MODE_ORDERS = "orders";

    private static final String ARG_MODE = "mode";

    private String mode;
    private ItemAdapter adapter;
    private TextView emptyText;

    private final Repo repo = Repo.get();

    public static ItemListFragment newInstance(String mode) {
        ItemListFragment f = new ItemListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) mode = getArguments().getString(ARG_MODE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        
        InsetUtil.padTop(view.findViewById(R.id.headerBar));

        TextView title = view.findViewById(R.id.txtListTitle);
        emptyText = view.findViewById(R.id.txtEmptyList);
        RecyclerView recycler = view.findViewById(R.id.recyclerList);

        title.setText(titleFor(mode));

        adapter = new ItemAdapter(item -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity())
                        .showFragment(ItemDetailFragment.newInstance(item.getItemId()), true);
            }
        }, ItemAdapter.ROW);
        recycler.setAdapter(adapter);

        view.findViewById(R.id.btnListBack).setOnClickListener(v -> goBack());

        load();
        return view;
    }

    private String titleFor(String mode) {
        if (MODE_HISTORY.equals(mode)) return "My borrows & purchases";
        if (MODE_ORDERS.equals(mode)) return "Requests on my items";
        return "Favourites";
    }

    private void load() {
        if (MODE_FAVORITES.equals(mode)) {
            repo.loadFavorites(this::show);
            return;
        }

        Repo.Result<List<BorrowRequest>> handler = requests -> {
            List<String> ids = new ArrayList<>();
            for (BorrowRequest r : requests) {
                if (r.getItemId() != null && !ids.contains(r.getItemId())) ids.add(r.getItemId());
            }
            loadItemsById(ids);
        };

        if (MODE_HISTORY.equals(mode)) repo.loadMyRequests(handler);
        else repo.loadIncomingRequests(handler);
    }

    private void loadItemsById(List<String> ids) {
        if (ids.isEmpty()) { show(new ArrayList<>()); return; }

        List<Item> items = new ArrayList<>();
        final int[] left = { ids.size() };
        for (String id : ids) {
            repo.loadItem(id, new Repo.Result<Item>() {
                @Override public void onSuccess(Item item) { items.add(item); done(); }
                @Override public void onError(Exception e) { done(); }
                private void done() { if (--left[0] == 0) show(items); }
            });
        }
    }

    private void show(List<Item> items) {
        if (!isAdded()) return;
        adapter.setItems(items);
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        emptyText.setText(emptyMessage());
    }

    private String emptyMessage() {
        if (MODE_HISTORY.equals(mode)) return "You have not borrowed or bought anything yet.";
        if (MODE_ORDERS.equals(mode)) return "Nobody has requested your items yet.";
        return "No favourites yet.\nTap the star on any item to save it here.";
    }

    private void goBack() {
        if (isAdded()) requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
}