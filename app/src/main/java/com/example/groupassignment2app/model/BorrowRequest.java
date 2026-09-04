package com.example.groupassignment2app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class BorrowRequest {

    public static final String PENDING   = "PENDING";
    public static final String ACCEPTED  = "ACCEPTED";
    public static final String REJECTED  = "REJECTED";
    public static final String RETURNED  = "RETURNED";
    public static final String OVERDUE   = "OVERDUE";
    public static final String PURCHASED = "PURCHASED";

    @Exclude
    private String id;

    private String itemId;
    private String itemName;

    private String borrowerId;
    private String borrowerName;
    private String lenderId;
    private String lenderName;

    private String status;
    private String type;

    private String requestDate;
    private String returnDate;
    private String message;

    private String meetupLocationName;
    private Double meetupMapX;
    private Double meetupMapY;

    private String paymentMethod;
    private Boolean paymentReceived;

    private Boolean borrowerRated;
    private Boolean lenderRated;

    private Timestamp createdAt;

    public BorrowRequest() { }

    @Exclude
    public String getId() { return id; }
    @Exclude
    public void setId(String id) { this.id = id; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getBorrowerId() { return borrowerId; }
    public void setBorrowerId(String borrowerId) { this.borrowerId = borrowerId; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public String getLenderId() { return lenderId; }
    public void setLenderId(String lenderId) { this.lenderId = lenderId; }

    public String getLenderName() { return lenderName; }
    public void setLenderName(String lenderName) { this.lenderName = lenderName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type == null ? "BORROW" : type; }
    public void setType(String type) { this.type = type; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getMeetupLocationName() { return meetupLocationName; }
    public void setMeetupLocationName(String n) { this.meetupLocationName = n; }

    public Double getMeetupMapX() { return meetupMapX; }
    public void setMeetupMapX(Double x) { this.meetupMapX = x; }

    public Double getMeetupMapY() { return meetupMapY; }
    public void setMeetupMapY(Double y) { this.meetupMapY = y; }

    @Exclude
    public boolean hasMeetupPoint() { return meetupMapX != null && meetupMapY != null; }

    public String getPaymentMethod() {
        return paymentMethod == null || paymentMethod.isEmpty() ? "Cash" : paymentMethod;
    }
    public void setPaymentMethod(String m) { this.paymentMethod = m; }

    public Boolean getPaymentReceived() { return paymentReceived != null && paymentReceived; }
    public void setPaymentReceived(Boolean b) { this.paymentReceived = b; }

    public Boolean getBorrowerRated() { return borrowerRated != null && borrowerRated; }
    public void setBorrowerRated(Boolean b) { this.borrowerRated = b; }

    public Boolean getLenderRated() { return lenderRated != null && lenderRated; }
    public void setLenderRated(Boolean b) { this.lenderRated = b; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Exclude
    public boolean isFinished() {
        return RETURNED.equals(status) || REJECTED.equals(status) || PURCHASED.equals(status);
    }
}