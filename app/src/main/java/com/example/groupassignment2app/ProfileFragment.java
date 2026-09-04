package com.example.groupassignment2app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.AppUser;
import com.example.groupassignment2app.model.Item;

import java.util.List;
import java.util.Locale;


public class ProfileFragment extends Fragment {

    private TextView name, email, ratingText, listingCount, emptyText;
    private RatingBar ratingBar;
    private ItemAdapter adapter;

    private final Repo repo = Repo.get();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        InsetUtil.padTop(view.findViewById(R.id.headerBar));


        name = view.findViewById(R.id.txtProfileName);
        email = view.findViewById(R.id.txtProfileEmail);
        ratingText = view.findViewById(R.id.txtProfileRating);
        ratingBar = view.findViewById(R.id.ratingProfile);
        listingCount = view.findViewById(R.id.txtListingCount);
        emptyText = view.findViewById(R.id.txtEmptyProfile);

        RecyclerView recycler = view.findViewById(R.id.recyclerMyItems);
        GridSpan.apply(recycler);
        adapter = new ItemAdapter(item -> open(ItemDetailFragment.newInstance(item.getItemId())),
                ItemAdapter.GRID);
        recycler.setAdapter(adapter);

        view.findViewById(R.id.btnListItem).setOnClickListener(v ->
                open(EditItemFragment.newInstance(null)));

        view.findViewById(R.id.btnMyReviews).setOnClickListener(v -> showReviews());

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> confirmLogout());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfile();
        loadMyItems();
    }

    private void loadProfile() {
        email.setText(repo.currentEmail());
        name.setText(repo.currentName());

        if (repo.uid() == null) return;
        repo.loadUser(repo.uid(), new Repo.Result<AppUser>() {
            @Override public void onSuccess(AppUser user) {
                if (!isAdded()) return;
                name.setText(user.getName() == null ? repo.currentName() : user.getName());

                float avg = user.getAverageRating();
                ratingBar.setRating(avg);
                if (user.getRatingCount() == 0) {
                    ratingText.setText("No reviews yet");
                } else {
                    ratingText.setText(String.format(Locale.getDefault(),
                            "%.1f out of 5  ·  %d review%s",
                            avg, user.getRatingCount(), user.getRatingCount() == 1 ? "" : "s"));
                }
            }
        });
    }

    private void loadMyItems() {
        repo.loadMyItems(new Repo.Result<List<Item>>() {
            @Override public void onSuccess(List<Item> items) {
                if (!isAdded()) return;
                adapter.setItems(items);
                listingCount.setText(items.size() + (items.size() == 1 ? " listing" : " listings"));
                emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showReviews() {
        if (repo.uid() == null) return;
        repo.loadReviewsFor(repo.uid(), reviews -> {
            if (!isAdded()) return;
            if (reviews.isEmpty()) {
                toast("You have not received any reviews yet");
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (com.example.groupassignment2app.model.Review r : reviews) {
                sb.append("\u2605 ").append(String.format(Locale.getDefault(), "%.0f", r.getRating()))
                        .append("  from ").append(r.getReviewerName())
                        .append("\n").append(r.getComment() == null || r.getComment().isEmpty()
                                ? "(no comment)" : r.getComment())
                        .append("\n\n");
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Reviews about me")
                    .setMessage(sb.toString().trim())
                    .setPositiveButton("Close", null)
                    .show();
        });
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Log out?")
                .setPositiveButton("Log out", (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).logout();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void open(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showFragment(fragment, true);
        }
    }

    private void toast(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}