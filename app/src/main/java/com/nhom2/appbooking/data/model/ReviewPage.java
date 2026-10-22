package com.nhom2.appbooking.data.model;

import java.util.ArrayList;
import java.util.List;

/** Một trang đánh giá (GET /hotels/{hotelId}/reviews). */
public class ReviewPage {
    public List<Review> content = new ArrayList<>();
    public long totalElements;
    public Double averageRating;
}
