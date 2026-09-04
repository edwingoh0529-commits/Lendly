package com.example.groupassignment2app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.groupassignment2app.data.ImageUtil;
import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.Item;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class EditItemFragment extends Fragment {

    private static final String ARG_ITEM_ID = "itemId";

    private static final String[] CATEGORIES = {
            "Electronics", "Study Materials", "Books", "Sports Equipment",
            "Presentation Materials", "Formal Wear", "Other"
    };
    private static final String[] CONDITIONS = { "New", "Like New", "Good", "Fair", "Poor" };

    private String itemId;
    private Item existing;

    private ImageView photo;
    private TextInputEditText edtName, edtDescription, edtFee, edtSalePrice;
    private AutoCompleteTextView actvCategory, actvCondition;
    private RadioGroup typeGroup;
    private RadioButton rbLend, rbSell, rbBoth;
    private TextInputLayout tilFee, tilSalePrice;
    private TextView title, pickupLabel;
    private Button btnSave, btnPickLocation;
    private ProgressBar progress;

    private String imageBase64;             
    private String pickupName;
    private Float pickupX, pickupY;

    private final Repo repo = Repo.get();

    public static EditItemFragment newInstance(String itemId) {
        EditItemFragment f = new EditItemFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_ID, itemId);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) itemId = getArguments().getString(ARG_ITEM_ID);
    }

    
    private final ActivityResultLauncher<Intent> imagePicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                Uri uri = result.getData().getData();
                if (uri == null) return;

                
                imageBase64 = ImageUtil.uriToBase64(requireContext(), uri);
                if (imageBase64 == null) {
                    toast("Could not read that image");
                    return;
                }
                photo.setImageBitmap(ImageUtil.base64ToBitmap(imageBase64));
                photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            });

    
    private final ActivityResultLauncher<Intent> mapPicker =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                Intent data = result.getData();
                pickupX = data.getFloatExtra(MapActivity.EXTRA_MAP_X, 0f);
                pickupY = data.getFloatExtra(MapActivity.EXTRA_MAP_Y, 0f);
                pickupName = data.getStringExtra(MapActivity.EXTRA_LOCATION_NAME);
                pickupLabel.setText("Pickup: " + pickupName);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_item, container, false);
        InsetUtil.padTop(view.findViewById(R.id.headerBar));


        title = view.findViewById(R.id.txtEditTitle);
        photo = view.findViewById(R.id.ivEditPhoto);
        edtName = view.findViewById(R.id.edtItemName);
        edtDescription = view.findViewById(R.id.edtDescription);
        actvCategory = view.findViewById(R.id.actvCategory);
        actvCondition = view.findViewById(R.id.actvCondition);
        typeGroup = view.findViewById(R.id.rgItemType);
        rbLend = view.findViewById(R.id.rbLend);
        rbSell = view.findViewById(R.id.rbSell);
        rbBoth = view.findViewById(R.id.rbBoth);
        edtFee = view.findViewById(R.id.edtLendingFee);
        edtSalePrice = view.findViewById(R.id.edtSalePrice);
        tilFee = view.findViewById(R.id.tilLendingFee);
        tilSalePrice = view.findViewById(R.id.tilSalePrice);
        pickupLabel = view.findViewById(R.id.txtPickupLabel);
        btnPickLocation = view.findViewById(R.id.btnPickLocation);
        btnSave = view.findViewById(R.id.btnSaveItem);
        progress = view.findViewById(R.id.progressSave);

        view.findViewById(R.id.btnEditBack).setOnClickListener(v -> goBack());

        actvCategory.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, CATEGORIES));
        actvCondition.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, CONDITIONS));

        typeGroup.setOnCheckedChangeListener((g, checkedId) -> updatePriceFields(checkedId));
        updatePriceFields(R.id.rbLend);

        photo.setOnClickListener(v -> imagePicker.launch(
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)));

        btnPickLocation.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), MapActivity.class);
            mapPicker.launch(intent);
        });

        btnSave.setOnClickListener(v -> save());

        if (itemId != null) {
            title.setText("Edit item");
            loadExisting();
        }

        return view;
    }

    
    private void updatePriceFields(int checkedId) {
        boolean showFee = checkedId == R.id.rbLend || checkedId == R.id.rbBoth;
        boolean showPrice = checkedId == R.id.rbSell || checkedId == R.id.rbBoth;
        tilFee.setVisibility(showFee ? View.VISIBLE : View.GONE);
        tilSalePrice.setVisibility(showPrice ? View.VISIBLE : View.GONE);
    }

    private void loadExisting() {
        repo.loadItem(itemId, new Repo.Result<Item>() {
            @Override public void onSuccess(Item item) {
                if (!isAdded()) return;
                existing = item;

                edtName.setText(item.getItemName());
                edtDescription.setText(item.getDescription());
                actvCategory.setText(item.getCategory(), false);
                actvCondition.setText(item.getCondition(), false);

                if ("SELL".equals(item.getItemType())) rbSell.setChecked(true);
                else if ("BOTH".equals(item.getItemType())) rbBoth.setChecked(true);
                else rbLend.setChecked(true);

                edtFee.setText(String.valueOf(item.getLendingFee()));
                edtSalePrice.setText(String.valueOf(item.getSalePrice()));

                if (item.getPickupLocationName() != null) {
                    pickupName = item.getPickupLocationName();
                    pickupLabel.setText("Pickup: " + pickupName);
                }
                if (item.getPickupMapX() != null) {
                    pickupX = item.getPickupMapX().floatValue();
                    pickupY = item.getPickupMapY().floatValue();
                }
                ImageUtil.loadInto(photo, item);
            }
            @Override public void onError(Exception e) {
                if (isAdded()) { toast("Could not load item"); goBack(); }
            }
        });
    }

    private void save() {
        String name = text(edtName);
        String desc = text(edtDescription);
        String cat = actvCategory.getText().toString().trim();
        String cond = actvCondition.getText().toString().trim();

        if (name.isEmpty()) { edtName.setError("Required"); edtName.requestFocus(); return; }
        if (desc.isEmpty()) { edtDescription.setError("Required"); edtDescription.requestFocus(); return; }
        if (cat.isEmpty()) { actvCategory.setError("Pick a category"); return; }
        if (cond.isEmpty()) { actvCondition.setError("Pick a condition"); return; }

        String type = rbSell.isChecked() ? "SELL" : rbBoth.isChecked() ? "BOTH" : "LEND";
        double fee = parse(text(edtFee));
        double salePrice = parse(text(edtSalePrice));

        if (("LEND".equals(type) || "BOTH".equals(type)) && fee <= 0) {
            edtFee.setError("Enter a daily fee (0.50 or more)");
            edtFee.requestFocus();
            return;
        }
        if (("SELL".equals(type) || "BOTH".equals(type)) && salePrice <= 0) {
            edtSalePrice.setError("Enter a sale price");
            edtSalePrice.requestFocus();
            return;
        }

        Item item = existing != null ? existing : new Item();
        item.setItemId(itemId);
        item.setItemName(name);
        item.setDescription(desc);
        item.setCategory(cat);
        item.setCondition(cond);
        item.setItemType(type);
        item.setLendingFee(fee);
        item.setSalePrice(salePrice);
        if (item.getStatus() == null) item.setStatus("AVAILABLE");
        if (imageBase64 != null) item.setImageBase64(imageBase64);
        if (pickupName != null) item.setPickupLocationName(pickupName);
        if (pickupX != null) {
            item.setPickupMapX(pickupX.doubleValue());
            item.setPickupMapY(pickupY.doubleValue());
        }

        setBusy(true);
        repo.saveItem(item)
                .addOnSuccessListener(v -> {
                    setBusy(false);
                    toast(existing == null ? "Item listed!" : "Changes saved");
                    goBack();
                })
                .addOnFailureListener(e -> {
                    setBusy(false);
                    toast("Save failed: " + e.getMessage());
                });
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!busy);
        btnSave.setText(busy ? "Saving...." : "Save item");
    }

    private String text(TextInputEditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private double parse(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private void goBack() {
        if (isAdded()) requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    private void toast(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}