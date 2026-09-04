package com.example.groupassignment2app;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class InsetUtil {


    public static void padTop(View view) {
        final int originalTop = view.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());

            v.setPadding(v.getPaddingLeft(), originalTop + bars.top,
                    v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }


    public static void padBottom(View view) {
        final int originalBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
                    v.getPaddingRight(), originalBottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }


    public static void padTopAndBottom(View view) {
        final int originalTop = view.getPaddingTop();
        final int originalBottom = view.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                            | WindowInsetsCompat.Type.displayCutout());

            v.setPadding(v.getPaddingLeft(), originalTop + bars.top,
                    v.getPaddingRight(), originalBottom + bars.bottom);
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(view);
    }
}