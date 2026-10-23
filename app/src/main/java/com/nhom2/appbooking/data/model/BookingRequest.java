package com.nhom2.appbooking.data.model;

/** Yêu cầu hủy / đổi ngày của khách (GET /admin/booking-requests). */
public class BookingRequest {
    public static final String TYPE_CANCEL = "CANCEL";
    public static final String TYPE_CHANGE_DATE = "CHANGE_DATE";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    public long id;
    public String type;
    public String newCheckIn;
    public String newCheckOut;
    public String reason;
    public String status;
    public String adminResponse;
    public String createdAt;
    public Booking booking;

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isChangeDate() {
        return TYPE_CHANGE_DATE.equals(type);
    }
}
