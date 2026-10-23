package com.nhom2.appbooking.data.model;

public class Review {
    public long id;
    public Integer rating;
    public String comment;
    public String createdAt;
    public String username;
    public String hotelReply;
    /** Chỉ có trong danh sách đánh giá của admin (GET /admin/reviews). */
    public Long hotelId;
    public String hotelName;

    public boolean hasReply() {
        return hotelReply != null && !hotelReply.trim().isEmpty();
    }
}
