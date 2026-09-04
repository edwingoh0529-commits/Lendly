package com.example.groupassignment2app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.Item;

import java.util.List;


public class SearchFragment extends Fragment {

    private EditText input;
    private TextView emptyText;
    private ItemAdapter adapter;
    private final Repo repo = Repo.get();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        
        InsetUtil.padTop(view.findViewById(R.id.headerBar));


        input = view.findViewById(R.id.edtSearchInput);
        emptyText = view.findViewById(R.id.txtEmptySearch);
        RecyclerView recycler = view.findViewById(R.id.recyclerSearch);
        GridSpan.apply(recycler);

        adapter = new ItemAdapter(item -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity())
                        .showFragment(ItemDetailFragment.newInstance(item.getItemId()), true);
            }
        }, ItemAdapter.GRID);
        recycler.setAdapter(adapter);

        view.findViewById(R.id.btnSearchBack).setOnClickListener(v -> goBack());

        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                search(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        input.requestFocus();
        search("");

        return view;
    }

    private void search(String keyword) {
        if (!isAdded()) return;
        repo.searchItems(keyword, new Repo.Result<List<Item>>() {
            @Override public void onSuccess(List<Item> items) {
                if (!isAdded()) return;
                adapter.setItems(items);
                emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }
            @Override public void onError(Exception e) {
                if (!isAdded()) return;
                emptyText.setVisibility(View.VISIBLE);
            }
        });
    }

    private void goBack() {
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
}