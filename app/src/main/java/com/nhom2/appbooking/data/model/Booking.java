package com.nhom2.appbooking.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Đơn đặt phòng (GET /bookings/my, POST /bookings/{hotelId}). */
public class Booking {
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_CANCELLED = "CANCELLED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String PAYMENT_PAID = "PAID";
    public static final String PAYMENT_REFUNDED = "REFUNDED";

    public long id;
    public String checkInDate;
    public String checkOutDate;
    public Double totalPrice;
    public String status;
    public String paymentStatus;
    public String createdAt;
    public Hotel hotel;
    public List<BookingRoom> bookingRooms = new ArrayList<>();

    public boolean isPaid() {
        return PAYMENT_PAID.equals(paymentStatus);
    }

    /** Còn thanh toán được: đơn đang chờ hoặc đã xác nhận mà chưa trả tiền (giống điều kiện của backend). */
    public boolean canPay() {
        return !isPaid() && !PAYMENT_REFUNDED.equals(paymentStatus)
                && (STATUS_PENDING.equals(status) || STATUS_CONFIRMED.equals(status));
    }

    /** Mã đơn hiển thị giống trên web, vd BK000047. */
    public String code() {
        return String.format(Locale.ROOT, "BK%06d", id);
    }
}
