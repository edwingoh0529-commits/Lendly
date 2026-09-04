package com.example.groupassignment2app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Item {

    @Exclude
    private String itemId;

    private String itemName;
    private String description;
    private String category;
    private String condition;

    private Double lendingFee;
    private Double salePrice;

    private String itemType;
    private String status;

    private String imageUrl;
    private String imageBase64;

    private String ownerId;
    private String ownerName;

    private String pickupLocationName;
    private Double pickupMapX;
    private Double pickupMapY;

    private Timestamp createdAt;

    public Item() {
    }

    public Item(String itemName, String description, String category, String condition,
                Double lendingFee, Double salePrice, String itemType,
                String imageUrl, String ownerId, String status) {
        this.itemName = itemName;
        this.description = description;
        this.category = category;
        this.condition = condition;
        this.lendingFee = lendingFee;
        this.salePrice = salePrice;
        this.itemType = itemType;
        this.imageUrl = imageUrl;
        this.ownerId = ownerId;
        this.status = status;
    }

    @Exclude
    public String getItemId() { return itemId; }
    @Exclude
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public Double getLendingFee() { return lendingFee == null ? 0.0 : lendingFee; }
    public void setLendingFee(Double lendingFee) { this.lendingFee = lendingFee; }

    public Double getSalePrice() { return salePrice == null ? 0.0 : salePrice; }
    public void setSalePrice(Double salePrice) { this.salePrice = salePrice; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getPickupLocationName() { return pickupLocationName; }
    public void setPickupLocationName(String p) { this.pickupLocationName = p; }

    public Double getPickupMapX() { return pickupMapX; }
    public void setPickupMapX(Double pickupMapX) { this.pickupMapX = pickupMapX; }

    public Double getPickupMapY() { return pickupMapY; }
    public void setPickupMapY(Double pickupMapY) { this.pickupMapY = pickupMapY; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Exclude
    public String getPriceLabel() {
        if ("SELL".equals(itemType)) {
            return String.format(java.util.Locale.getDefault(), "RM %.2f", getSalePrice());
        }
        if ("BOTH".equals(itemType)) {
            return String.format(java.util.Locale.getDefault(),
                    "RM %.2f/day  ·  Buy RM %.2f", getLendingFee(), getSalePrice());
        }
        return String.format(java.util.Locale.getDefault(), "RM %.2f/day", getLendingFee());
    }

    @Exclude
    public boolean canBorrow() { return "LEND".equals(itemType) || "BOTH".equals(itemType); }

    @Exclude
    public boolean canBuy() { return "SELL".equals(itemType) || "BOTH".equals(itemType); }
}