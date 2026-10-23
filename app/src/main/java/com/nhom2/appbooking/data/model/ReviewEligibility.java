package com.nhom2.appbooking.data.model;

/** Khách có được đánh giá khách sạn không (GET /hotels/{id}/review-eligibility); reason giải thích khi không được. */
public class ReviewEligibility {
    public boolean canReview;
    public String reason;
}
