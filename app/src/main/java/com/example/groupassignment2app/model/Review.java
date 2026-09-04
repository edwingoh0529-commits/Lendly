package com.example.groupassignment2app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Review {

    @Exclude
    private String id;

    private String requestId;
    private String itemName;
    private String reviewerId;
    private String reviewerName;
    private String revieweeId;
    private Double rating;
    private String comment;
    private Timestamp createdAt;

    public Review() { }

    public Review(String requestId, String itemName, String reviewerId, String reviewerName,
                  String revieweeId, double rating, String comment) {
        this.requestId = requestId;
        this.itemName = itemName;
        this.reviewerId = reviewerId;
        this.reviewerName = reviewerName;
        this.revieweeId = revieweeId;
        this.rating = rating;
        this.comment = comment;
    }

    @Exclude
    public String getId() { return id; }
    @Exclude
    public void setId(String id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getRevieweeId() { return revieweeId; }
    public void setRevieweeId(String revieweeId) { this.revieweeId = revieweeId; }

    public Double getRating() { return rating == null ? 0.0 : rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}