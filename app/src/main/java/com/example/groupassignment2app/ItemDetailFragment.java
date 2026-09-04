package com.example.groupassignment2app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.groupassignment2app.data.ImageUtil;
import com.example.groupassignment2app.data.Repo;
import com.example.groupassignment2app.model.BorrowRequest;
import com.example.groupassignment2app.model.Item;


public class ItemDetailFragment extends Fragment {

    private static final String ARG_ITEM_ID = "itemId";

    private static final String[] PAYMENT_METHODS = {
            "Cash", "DuitNow", "Touch \u2018n Go eWallet", "Bank Transfer"
    };

    private String itemId;
    private Item item;
    private boolean isFavorite = false;

    private ImageView image;
    private TextView name, category, description, price, condition, owner, statusLabel, pickup;
    private ImageButton btnFavorite, btnEdit, btnDelete;
    private Button btnBorrow, btnBuy, btnChat, btnViewMap;

    private final Repo repo = Repo.get();

    public static ItemDetailFragment newInstance(String itemId) {
        ItemDetailFragment f = new ItemDetailFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_item_detail, container, false);

        image = view.findViewById(R.id.ivDetailImage);
        name = view.findViewById(R.id.txtDetailName);
        category = view.findViewById(R.id.txtDetailCategory);
        description = view.findViewById(R.id.txtDetailDescription);
        price = view.findViewById(R.id.txtDetailPrice);
        condition = view.findViewById(R.id.txtDetailCondition);
        owner = view.findViewById(R.id.txtDetailOwner);
        statusLabel = view.findViewById(R.id.txtDetailStatus);
        pickup = view.findViewById(R.id.txtDetailPickup);

        btnFavorite = view.findViewById(R.id.btnDetailFavorite);
        btnEdit = view.findViewById(R.id.btnDetailEdit);
        btnDelete = view.findViewById(R.id.btnDetailDelete);

        btnBorrow = view.findViewById(R.id.btnDetailBorrow);
        btnBuy = view.findViewById(R.id.btnDetailBuy);
        btnChat = view.findViewById(R.id.btnDetailChat);
        btnViewMap = view.findViewById(R.id.btnDetailViewMap);

        view.findViewById(R.id.btnDetailBack).setOnClickListener(v -> goBack());
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        loadItem();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadItem();
    }

    private void loadItem() {
        if (itemId == null) { toast("Invalid item"); goBack(); return; }

        repo.loadItem(itemId, new Repo.Result<Item>() {
            @Override public void onSuccess(Item loaded) {
                if (!isAdded()) return;
                item = loaded;
                render();
                checkFavorite();
            }
            @Override public void onError(Exception e) {
                if (!isAdded()) return;
                toast("This item is no longer available");
                goBack();
            }
        });
    }

    private void render() {
        name.setText(item.getItemName());
        category.setText(item.getCategory());
        description.setText(item.getDescription());
        price.setText(item.getPriceLabel());
        condition.setText(item.getCondition());
        owner.setText("Listed by " + (item.getOwnerName() == null ? "a student" : item.getOwnerName()));
        ImageUtil.loadInto(image, item);

        String pickupName = item.getPickupLocationName();
        boolean hasPickup = pickupName != null && !pickupName.isEmpty();
        pickup.setText(hasPickup ? pickupName : "No pickup point set");
        btnViewMap.setVisibility(item.getPickupMapX() != null ? View.VISIBLE : View.GONE);
        btnViewMap.setOnClickListener(v -> openMapViewer());

        boolean mine = repo.uid() != null && repo.uid().equals(item.getOwnerId());
        boolean available = "AVAILABLE".equals(item.getStatus());

        if (available) {
            statusLabel.setVisibility(View.GONE);
        } else {
            statusLabel.setVisibility(View.VISIBLE);
            statusLabel.setText("BORROWED".equals(item.getStatus())
                    ? "Currently on loan" : "This item has been sold");
        }

        if (mine) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnFavorite.setVisibility(View.GONE);
            btnBorrow.setVisibility(View.GONE);
            btnBuy.setVisibility(View.GONE);
            btnChat.setVisibility(View.GONE);

            btnEdit.setOnClickListener(v -> open(EditItemFragment.newInstance(item.getItemId())));
            btnDelete.setOnClickListener(v -> confirmDelete());
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnFavorite.setVisibility(View.VISIBLE);
            btnChat.setVisibility(View.VISIBLE);

            btnBorrow.setVisibility(item.canBorrow() && available ? View.VISIBLE : View.GONE);
            btnBuy.setVisibility(item.canBuy() && available ? View.VISIBLE : View.GONE);

            btnBorrow.setOnClickListener(v -> openRequestForm("BORROW"));
            btnBuy.setOnClickListener(v -> confirmBuy());
            btnChat.setOnClickListener(v -> openChat());
        }
    }

    private void openRequestForm(String type) {
        Intent intent = new Intent(requireContext(), BorrowRequestActivity.class);
        intent.putExtra(BorrowRequestActivity.EXTRA_ITEM_ID, item.getItemId());
        intent.putExtra(BorrowRequestActivity.EXTRA_ITEM_NAME, item.getItemName());
        intent.putExtra(BorrowRequestActivity.EXTRA_OWNER_ID, item.getOwnerId());
        intent.putExtra(BorrowRequestActivity.EXTRA_OWNER_NAME, item.getOwnerName());
        intent.putExtra(BorrowRequestActivity.EXTRA_FEE, item.getLendingFee());
        intent.putExtra(BorrowRequestActivity.EXTRA_TYPE, type);

        intent.putExtra(BorrowRequestActivity.EXTRA_PICKUP_NAME, item.getPickupLocationName());
        if (item.getPickupMapX() != null) {
            intent.putExtra(BorrowRequestActivity.EXTRA_PICKUP_X, item.getPickupMapX().floatValue());
            intent.putExtra(BorrowRequestActivity.EXTRA_PICKUP_Y, item.getPickupMapY().floatValue());
        }
        startActivity(intent);
    }

    private void confirmBuy() {
        final int[] chosen = { 0 };

        new AlertDialog.Builder(requireContext())
                .setTitle(String.format(java.util.Locale.getDefault(),
                        "Buy for RM %.2f \u2014 how will you pay?", item.getSalePrice()))
                .setSingleChoiceItems(PAYMENT_METHODS, 0, (d, which) -> chosen[0] = which)
                .setPositiveButton("Send request",
                        (d, w) -> sendBuyRequest(PAYMENT_METHODS[chosen[0]]))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendBuyRequest(String paymentMethod) {
        repo.checkAvailable(item.getItemId(), new Repo.Result<Boolean>() {
            @Override public void onSuccess(Boolean available) {
                if (!available) {
                    toast("Sorry, this item is no longer available");
                    loadItem();
                    return;
                }
                repo.hasOpenRequestFor(item.getItemId(), already -> {
                    if (!isAdded()) return;
                    if (already) {
                        toast("You already have an open request for this item");
                        return;
                    }
                    writeBuyRequest(paymentMethod);
                });
            }
            @Override public void onError(Exception e) { writeBuyRequest(paymentMethod); }
        });
    }

    private void writeBuyRequest(String paymentMethod) {
        BorrowRequest request = new BorrowRequest();
        request.setItemId(item.getItemId());
        request.setItemName(item.getItemName());
        request.setBorrowerId(repo.uid());
        request.setBorrowerName(repo.currentName());
        request.setLenderId(item.getOwnerId());
        request.setLenderName(item.getOwnerName());
        request.setStatus(BorrowRequest.PENDING);
        request.setType("BUY");
        request.setMessage("Would like to buy this item.");
        request.setPaymentMethod(paymentMethod);

        repo.createRequest(request)
                .addOnSuccessListener(v -> {
                    toast("Purchase request sent");
                    goBack();
                })
                .addOnFailureListener(e -> toast("Could not send: " + e.getMessage()));
    }

    private void openChat() {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_OTHER_ID, item.getOwnerId());
        intent.putExtra(ChatActivity.EXTRA_OTHER_NAME, item.getOwnerName());
        intent.putExtra(ChatActivity.EXTRA_ITEM_NAME, item.getItemName());
        intent.putExtra(ChatActivity.EXTRA_PREFILL,
                "Hi, is \"" + item.getItemName() + "\" still available?");
        startActivity(intent);
    }

    private void openMapViewer() {
        Intent intent = new Intent(requireContext(), MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_VIEW_ONLY, true);
        intent.putExtra(MapActivity.EXTRA_MAP_X, item.getPickupMapX().floatValue());
        intent.putExtra(MapActivity.EXTRA_MAP_Y, item.getPickupMapY().floatValue());
        intent.putExtra(MapActivity.EXTRA_LOCATION_NAME, item.getPickupLocationName());
        startActivity(intent);
    }

    private void checkFavorite() {
        repo.isFavorite(itemId, fav -> {
            if (!isAdded()) return;
            isFavorite = fav;
            paintFavorite();
        });
    }

    private void toggleFavorite() {
        boolean target = !isFavorite;
        isFavorite = target;
        paintFavorite();

        (target ? repo.addFavorite(itemId) : repo.removeFavorite(itemId))
                .addOnSuccessListener(v ->
                        toast(target ? "Saved to Favourites" : "Removed from Favourites"))
                .addOnFailureListener(e -> {
                    isFavorite = !target;
                    paintFavorite();
                    toast("Could not update favourites");
                });
    }

    private void paintFavorite() {
        btnFavorite.setImageResource(isFavorite
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete listing")
                .setMessage("Remove \"" + item.getItemName() + "\" from Lendly?")
                .setPositiveButton("Delete", (d, w) -> repo.deleteItem(itemId)
                        .addOnSuccessListener(v -> { toast("Listing deleted"); goBack(); })
                        .addOnFailureListener(e -> toast("Delete failed: " + e.getMessage())))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void open(Fragment fragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showFragment(fragment, true);
        }
    }

    private void goBack() {
        if (isAdded()) requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    private void toast(String msg) {
        if (isAdded()) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}