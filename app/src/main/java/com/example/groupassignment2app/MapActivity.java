package com.example.groupassignment2app;

import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupassignment2app.model.PickupPoint;

import java.util.ArrayList;
import java.util.List;


public class MapActivity extends AppCompatActivity {

    public static final String EXTRA_VIEW_ONLY = "viewOnly";
    public static final String EXTRA_MAP_X = "pickupMapX";
    public static final String EXTRA_MAP_Y = "pickupMapY";
    public static final String EXTRA_LOCATION_NAME = "pickupLocationName";

    private FrameLayout zoomLayer, pinLayer;
    private ImageView imgMap;
    private TextView btnBack, txtSelected, txtCoordinates, txtInstruction;
    private Button btnConfirm, btnPickFromList, btnZoomIn, btnZoomOut;

    private final List<PickupPoint> points = PickupPoint.all();
    private final List<View> pinViews = new ArrayList<>();

    private PickupPoint selected;
    private boolean viewOnly = false;

    
    private float zoom = 1.0f;
    private static final float MIN_ZOOM = 1.0f, MAX_ZOOM = 4.0f, ZOOM_STEP = 0.5f;

    
    private float startRawX, startRawY, startTransX, startTransY;
    private boolean dragging = false;
    private static final float DRAG_THRESHOLD = 12f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        InsetUtil.padTop(findViewById(R.id.headerBar));

        zoomLayer = findViewById(R.id.zoomLayer);
        pinLayer = findViewById(R.id.pinLayer);
        imgMap = findViewById(R.id.imgCampusMap);
        btnBack = findViewById(R.id.btnMapBack);
        txtSelected = findViewById(R.id.txtSelectedLocation);
        txtCoordinates = findViewById(R.id.txtCoordinates);
        txtInstruction = findViewById(R.id.txtMapInstruction);
        btnConfirm = findViewById(R.id.btnConfirmLocation);
        btnPickFromList = findViewById(R.id.btnPickFromList);
        btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomOut = findViewById(R.id.btnZoomOut);

        viewOnly = getIntent().getBooleanExtra(EXTRA_VIEW_ONLY, false);

        buildPins();
        setupPanAndZoom();

        if (viewOnly) {
            setupViewOnly();
        } else {
            setupPicking();
        }

        btnBack.setOnClickListener(v -> finish());
        btnZoomIn.setOnClickListener(v -> zoomIn());
        btnZoomOut.setOnClickListener(v -> zoomOut());
    }


    private void setupPicking() {
        txtInstruction.setText("Tap one of the pins to choose a pickup point");
        btnConfirm.setEnabled(false);
        btnConfirm.setOnClickListener(v -> confirm());
        btnPickFromList.setOnClickListener(v -> showList());
    }

    private void setupViewOnly() {
        String name = getIntent().getStringExtra(EXTRA_LOCATION_NAME);
        selected = PickupPoint.byName(name);

        
        if (selected == null && getIntent().hasExtra(EXTRA_MAP_X)) {
            selected = nearest(
                    getIntent().getFloatExtra(EXTRA_MAP_X, 0.5f),
                    getIntent().getFloatExtra(EXTRA_MAP_Y, 0.5f));
        }

        txtInstruction.setText("Pickup point chosen by the owner \u2014 view only");
        txtSelected.setText(selected != null ? selected.name
                : (name == null || name.isEmpty() ? "Pickup point" : name));
        txtCoordinates.setText("Use +/- to zoom, drag to move around");

        btnConfirm.setVisibility(View.GONE);
        btnPickFromList.setVisibility(View.GONE);

        paintPins();
    }

    
    private PickupPoint nearest(float x, float y) {
        PickupPoint best = null;
        double bestDist = Double.MAX_VALUE;
        for (PickupPoint p : points) {
            double d = Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }


    private void buildPins() {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (PickupPoint point : points) {
            View pin = inflater.inflate(R.layout.item_map_pin, pinLayer, false);
            ((TextView) pin.findViewById(R.id.txtPinLabel)).setText(point.shortName());

            if (!viewOnly) {
                pin.setOnClickListener(v -> {
                    selected = point;
                    txtSelected.setText(point.name);
                    txtCoordinates.setText("Tap Confirm to use this pickup point");
                    btnConfirm.setEnabled(true);
                    paintPins();
                });
            } else {
                
                pin.setClickable(false);
            }

            pinLayer.addView(pin);
            pinViews.add(pin);
        }

        
        pinLayer.post(this::positionPins);
    }

    private void positionPins() {
        RectF img = imageRect();
        if (img.width() <= 0) { pinLayer.post(this::positionPins); return; }

        for (int i = 0; i < pinViews.size(); i++) {
            View pin = pinViews.get(i);
            PickupPoint p = points.get(i);

            float x = img.left + p.x * img.width();
            float y = img.top + p.y * img.height();

            
            pin.setX(x - pin.getWidth() / 2f);
            pin.setY(y - pin.getHeight());

            
            pin.setPivotX(pin.getWidth() / 2f);
            pin.setPivotY(pin.getHeight());
            pin.setScaleX(1f / zoom);
            pin.setScaleY(1f / zoom);
        }
        paintPins();
    }

    
    private void paintPins() {
        for (int i = 0; i < pinViews.size(); i++) {
            View pin = pinViews.get(i);
            boolean isSelected = selected != null && selected.name.equals(points.get(i).name);

            TextView label = pin.findViewById(R.id.txtPinLabel);
            View dot = pin.findViewById(R.id.viewPinDot);

            label.setBackgroundResource(isSelected
                    ? R.drawable.bg_pin_label_selected : R.drawable.bg_pin_label);
            label.setTextColor(getColor(isSelected ? R.color.white : R.color.lendly_blue));
            dot.setBackgroundResource(isSelected
                    ? R.drawable.bg_pin_dot_selected : R.drawable.bg_pin_dot);

            
            if (viewOnly) pin.setAlpha(isSelected ? 1f : 0.35f);
        }
    }

    
    private void showList() {
        String[] names = new String[points.size()];
        for (int i = 0; i < points.size(); i++) names[i] = points.get(i).name;

        int checked = -1;
        if (selected != null) {
            for (int i = 0; i < points.size(); i++) {
                if (points.get(i).name.equals(selected.name)) checked = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Choose a pickup point")
                .setSingleChoiceItems(names, checked, (dialog, which) -> {
                    selected = points.get(which);
                    txtSelected.setText(selected.name);
                    txtCoordinates.setText("Tap Confirm to use this pickup point");
                    btnConfirm.setEnabled(true);
                    paintPins();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private RectF imageRect() {
        if (imgMap.getDrawable() == null) {
            return new RectF(0, 0, zoomLayer.getWidth(), zoomLayer.getHeight());
        }
        RectF rect = new RectF(0, 0,
                imgMap.getDrawable().getIntrinsicWidth(),
                imgMap.getDrawable().getIntrinsicHeight());
        imgMap.getImageMatrix().mapRect(rect);
        rect.offset(imgMap.getLeft(), imgMap.getTop());
        return rect;
    }


    private void setupPanAndZoom() {
        zoomLayer.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {

                case MotionEvent.ACTION_DOWN:
                    startRawX = event.getRawX();
                    startRawY = event.getRawY();
                    startTransX = zoomLayer.getTranslationX();
                    startTransY = zoomLayer.getTranslationY();
                    dragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - startRawX;
                    float dy = event.getRawY() - startRawY;

                    if (Math.abs(dx) > DRAG_THRESHOLD || Math.abs(dy) > DRAG_THRESHOLD) {
                        dragging = true;
                    }
                    
                    if (zoom > 1.0f && dragging) {
                        zoomLayer.setTranslationX(clampPan(startTransX + dx, true));
                        zoomLayer.setTranslationY(clampPan(startTransY + dy, false));
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!dragging && !viewOnly) {
                        Toast.makeText(this,
                                "Tap one of the labelled pins", Toast.LENGTH_SHORT).show();
                    }
                    view.performClick();
                    return true;
            }
            return false;
        });
    }

    
    private float clampPan(float value, boolean horizontal) {
        float viewSize = horizontal ? zoomLayer.getWidth() : zoomLayer.getHeight();
        float limit = viewSize * (zoom - 1f) / 2f;
        return Math.max(-limit, Math.min(limit, value));
    }

    private void zoomIn() {
        if (zoom >= MAX_ZOOM) { toast("Maximum zoom"); return; }
        zoom = Math.min(MAX_ZOOM, zoom + ZOOM_STEP);
        applyZoom();
    }

    private void zoomOut() {
        if (zoom <= MIN_ZOOM) { toast("Minimum zoom"); return; }
        zoom = Math.max(MIN_ZOOM, zoom - ZOOM_STEP);
        if (zoom == MIN_ZOOM) {
            zoomLayer.setTranslationX(0f);
            zoomLayer.setTranslationY(0f);
        } else {
            zoomLayer.setTranslationX(clampPan(zoomLayer.getTranslationX(), true));
            zoomLayer.setTranslationY(clampPan(zoomLayer.getTranslationY(), false));
        }
        applyZoom();
    }

    private void applyZoom() {
        zoomLayer.animate()
                .scaleX(zoom).scaleY(zoom)
                .setDuration(180)
                .withEndAction(this::positionPins)
                .start();
        btnZoomIn.setEnabled(zoom < MAX_ZOOM);
        btnZoomOut.setEnabled(zoom > MIN_ZOOM);
    }


    private void confirm() {
        if (selected == null) {
            toast("Choose a pickup point first");
            return;
        }
        Intent result = new Intent();
        result.putExtra(EXTRA_MAP_X, selected.x);
        result.putExtra(EXTRA_MAP_Y, selected.y);
        result.putExtra(EXTRA_LOCATION_NAME, selected.name);
        setResult(RESULT_OK, result);
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}