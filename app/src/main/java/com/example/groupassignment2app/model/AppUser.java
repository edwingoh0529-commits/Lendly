package com.example.groupassignment2app.model;

import com.google.firebase.firestore.Exclude;

public class AppUser {

    @Exclude
    private String uid;

    private String name;
    private String email;
    private String studentId;

    private Double ratingTotal;
    private Long ratingCount;

    public AppUser() { }

    public AppUser(String name, String email, String studentId) {
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.ratingTotal = 0.0;
        this.ratingCount = 0L;
    }

    @Exclude
    public String getUid() { return uid; }
    @Exclude
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Double getRatingTotal() { return ratingTotal == null ? 0.0 : ratingTotal; }
    public void setRatingTotal(Double ratingTotal) { this.ratingTotal = ratingTotal; }

    public Long getRatingCount() { return ratingCount == null ? 0L : ratingCount; }
    public void setRatingCount(Long ratingCount) { this.ratingCount = ratingCount; }

    @Exclude
    public float getAverageRating() {
        if (getRatingCount() == 0) return 0f;
        return (float) (getRatingTotal() / getRatingCount());
    }
}