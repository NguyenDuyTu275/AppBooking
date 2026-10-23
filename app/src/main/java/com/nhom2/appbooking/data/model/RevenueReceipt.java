package com.nhom2.appbooking.data.model;

/** Một khoản trong báo cáo doanh thu theo ngày, ghi bằng cách quét hóa đơn (GET /admin/receipts/daily). */
public class RevenueReceipt {

    /** Khách trả tiền mặt tại khách sạn, hóa đơn chưa có trên hệ thống. */
    public static final String SOURCE_CASH = "CASH";
    /** Hóa đơn của một đơn đặt phòng trên hệ thống. */
    public static final String SOURCE_SYSTEM_INVOICE = "SYSTEM_INVOICE";

    public long id;
    public String source;
    public Long bookingId;
    public Double amount;
    /** Ngày tính vào báo cáo, dạng yyyy-MM-dd. */
    public String revenueDate;
    public String receiptNumber;
    public String customerName;
    public String hotelName;
    public String description;
    public String createdBy;
    public String createdAt;

    public boolean isCash() {
        return SOURCE_CASH.equals(source);
    }
}
