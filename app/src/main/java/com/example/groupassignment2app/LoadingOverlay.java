package com.example.groupassignment2app;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

public class LoadingOverlay {

    private final View root;
    private final TextView message;

    public LoadingOverlay(Activity activity) {
        root = activity.findViewById(R.id.loadingOverlay);
        message = root == null ? null : root.findViewById(R.id.txtLoadingMessage);
    }

    public void show(String text) {
        if (root == null) return;
        if (message != null) message.setText(text);
        root.setVisibility(View.VISIBLE);
    }

    public void hide() {
        if (root != null) root.setVisibility(View.GONE);
    }

    public boolean isShowing() {
        return root != null && root.getVisibility() == View.VISIBLE;
    }
}