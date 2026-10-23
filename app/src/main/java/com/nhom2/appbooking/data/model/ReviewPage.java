package com.nhom2.appbooking.data.model;

import java.util.ArrayList;
import java.util.List;

/** Một trang đánh giá (GET /hotels/{hotelId}/reviews, GET /admin/reviews). */
public class ReviewPage {
    public List<Review> content = new ArrayList<>();
    public int page;
    public long totalElements;
    public int totalPages;
    public Double averageRating;

    /** Còn trang sau để tải tiếp. */
    public boolean hasMore() {
        return page + 1 < totalPages;
    }
}
