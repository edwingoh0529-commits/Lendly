package com.example.groupassignment2app;

import android.content.Context;
import android.content.res.Resources;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class GridSpan {

    
    private static final int TARGET_CARD_WIDTH_DP = 180;

    private static final int MIN_COLUMNS = 2;
    private static final int MAX_COLUMNS = 5;

    
    public static int forCurrentWidth(Context context) {
        Resources res = context.getResources();
        float widthDp = res.getConfiguration().screenWidthDp;

        int columns = Math.round(widthDp / TARGET_CARD_WIDTH_DP);
        return Math.max(MIN_COLUMNS, Math.min(MAX_COLUMNS, columns));
    }

    
    public static GridLayoutManager layoutManager(Context context) {
        return new GridLayoutManager(context, forCurrentWidth(context));
    }

    
    public static void apply(RecyclerView recycler) {
        int columns = forCurrentWidth(recycler.getContext());

        if (recycler.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager existing = (GridLayoutManager) recycler.getLayoutManager();
            if (existing.getSpanCount() != columns) existing.setSpanCount(columns);
            return;
        }
        recycler.setLayoutManager(new GridLayoutManager(recycler.getContext(), columns));
    }
}