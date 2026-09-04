package com.example.groupassignment2app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.Item;

import java.util.List;


public class HomeFragment extends Fragment {

    
    private static final String[] CATEGORIES = {
            "All", "Electronics", "Study Materials", "Books",
            "Sports Equipment", "Presentation Materials", "Formal Wear", "Other"
    };

    private static final int[] CATEGORY_ICONS = {
            R.drawable.ic_category_all,
            R.drawable.electronics_icon,
            R.drawable.calculator_icon,
            R.drawable.book_icon,
            R.drawable.sports_icon,
            R.drawable.stationaries_icon,
            R.drawable.formalwear_icon,
            R.drawable.more_icon
    };

    private RecyclerView recyclerItems;
    private LinearLayout categoryRow;
    private TextView sectionTitle, emptyText;
    private SwipeRefreshLayout swipeRefresh;
    private ItemAdapter adapter;

    private final Repo repo = Repo.get();
    private String activeCategory = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        
        InsetUtil.padTop(view.findViewById(R.id.headerBar));

        recyclerItems = view.findViewById(R.id.recyclerItems);
        categoryRow = view.findViewById(R.id.categoryRow);
        sectionTitle = view.findViewById(R.id.txtSectionTitle);
        emptyText = view.findViewById(R.id.txtEmptyHome);
        swipeRefresh = view.findViewById(R.id.swipeRefreshHome);

        
        swipeRefresh.setColorSchemeResources(R.color.lendly_blue);
        swipeRefresh.setOnRefreshListener(this::loadItems);

        
        GridSpan.apply(recyclerItems);
        adapter = new ItemAdapter(this::openDetail, ItemAdapter.GRID);
        recyclerItems.setAdapter(adapter);

        
        view.findViewById(R.id.searchBarContainer).setOnClickListener(v ->
                open(new SearchFragment()));

        view.findViewById(R.id.btnFavorites).setOnClickListener(v ->
                open(ItemListFragment.newInstance(ItemListFragment.MODE_FAVORITES)));

        view.findViewById(R.id.btnHistory).setOnClickListener(v ->
                open(ItemListFragment.newInstance(ItemListFragment.MODE_HISTORY)));

        view.findViewById(R.id.btnOrders).setOnClickListener(v ->
                open(ItemListFragment.newInstance(ItemListFragment.MODE_ORDERS)));

        buildCategoryRow(inflater);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadItems();
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (recyclerItems != null) GridSpan.apply(recyclerItems);
    }

    private void buildCategoryRow(LayoutInflater inflater) {
        categoryRow.removeAllViews();

        for (int i = 0; i < CATEGORIES.length; i++) {
            final String category = CATEGORIES[i];
            View chip = inflater.inflate(R.layout.item_category_circle, categoryRow, false);

            ((TextView) chip.findViewById(R.id.txtCategoryName)).setText(category);
            ((ImageView) chip.findViewById(R.id.imgCategoryIcon)).setImageResource(CATEGORY_ICONS[i]);

            chip.setOnClickListener(v -> {
                activeCategory = "All".equals(category) ? null : category;
                sectionTitle.setText("All".equals(category) ? "Being Let Go" : category);
                loadItems();
            });

            categoryRow.addView(chip);
        }
    }

    private void loadItems() {
        if (!isAdded()) return;

        repo.loadMarketplace(activeCategory, new Repo.Result<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                if (!isAdded()) return;
                swipeRefresh.setRefreshing(false);
                adapter.setItems(items);
                emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                emptyText.setText(activeCategory == null
                        ? "Nothing listed yet.\nBe the first: Profile \u2192 List an item."
                        : "No items in " + activeCategory + " right now.");
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return;
                
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText("Could not load items.\n" + e.getMessage());
            }
        });
    }

    private void openDetail(Item item) {
        open(ItemDetailFragment.newInstance(item.getItemId()));
    }

    private void open(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showFragment(fragment, true);
        }
    }
}