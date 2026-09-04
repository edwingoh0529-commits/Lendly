package com.example.groupassignment2app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupassignment2app.data.ImageUtil;
import com.example.groupassignment2app.model.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int GRID = 0;
    public static final int ROW = 1;

    public interface OnItemClick {
        void onItemClick(Item item);
    }

    private List<Item> items = new ArrayList<>();
    private final OnItemClick listener;
    private final int style;

    public ItemAdapter(OnItemClick listener, int style) {
        this.listener = listener;
        this.style = style;
    }

    public void setItems(List<Item> newItems) {
        this.items = newItems == null ? new ArrayList<>() : newItems;
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return items.isEmpty(); }

    @Override
    public int getItemViewType(int position) { return style; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == GRID) {
            return new GridHolder(inflater.inflate(R.layout.item_card, parent, false));
        }
        return new RowHolder(inflater.inflate(R.layout.item_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Item item = items.get(position);
        if (holder instanceof GridHolder) ((GridHolder) holder).bind(item, listener);
        else ((RowHolder) holder).bind(item, listener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class GridHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name, price, badge;

        GridHolder(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.ivItemImage);
            name = v.findViewById(R.id.tvItemName);
            price = v.findViewById(R.id.tvItemPrice);
            badge = v.findViewById(R.id.tvItemBadge);
        }

        void bind(Item item, OnItemClick listener) {
            name.setText(item.getItemName());
            price.setText(item.getPriceLabel());
            badge.setText(labelFor(item));
            ImageUtil.loadInto(image, item);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    static class RowHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView name, price, meta;

        RowHolder(@NonNull View v) {
            super(v);
            image = v.findViewById(R.id.ivRowImage);
            name = v.findViewById(R.id.tvRowName);
            price = v.findViewById(R.id.tvRowPrice);
            meta = v.findViewById(R.id.tvRowMeta);
        }

        void bind(Item item, OnItemClick listener) {
            name.setText(item.getItemName());
            price.setText(item.getPriceLabel());
            meta.setText(item.getCategory() + "  ·  " + item.getCondition());
            ImageUtil.loadInto(image, item);
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    private static String labelFor(Item item) {
        if ("BORROWED".equals(item.getStatus())) return "On loan";
        if ("SOLD".equals(item.getStatus())) return "Sold";
        if ("SELL".equals(item.getItemType())) return "For sale";
        if ("BOTH".equals(item.getItemType())) return "Lend or buy";
        return "For lending";
    }
}