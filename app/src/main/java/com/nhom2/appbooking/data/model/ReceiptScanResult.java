package com.nhom2.appbooking.data.model;

import com.google.gson.annotations.SerializedName;

/** Kết quả quét một hóa đơn (POST /admin/receipts/scan). */
public class ReceiptScanResult {

    /** Đơn chưa thanh toán: backend đã đánh dấu đã thanh toán và ghi vào doanh thu hôm nay. */
    public static final String BOOKING_MARKED_PAID = "BOOKING_MARKED_PAID";
    /** Đơn đã thanh toán trên hệ thống: đã thêm vào báo cáo doanh thu hôm nay. */
    public static final String ADDED_TO_DAILY_REPORT = "ADDED_TO_DAILY_REPORT";
    /** Hóa đơn của đơn này đã có trong báo cáo, không ghi thêm. */
    public static final String ALREADY_RECORDED = "ALREADY_RECORDED";
    /** Hóa đơn chưa có trên hệ thống (tiền mặt): admin kiểm tra rồi xác nhận để ghi. */
    public static final String NEW_CASH_RECEIPT = "NEW_CASH_RECEIPT";

    public String outcome;
    public Scanned scanned;
    /** Khoản đã ghi; null khi {@link #NEW_CASH_RECEIPT}. */
    public RevenueReceipt receipt;
    public Long bookingId;
    /** Ngày đề xuất ghi doanh thu cho hóa đơn tiền mặt, yyyy-MM-dd. */
    public String suggestedDate;

    /** Các trường Gemini đọc được từ ảnh; trường không in trên hóa đơn là null. */
    public static class Scanned {
        @SerializedName("isReceipt")
        public boolean receipt;
        public String bookingCode;
        public String receiptNumber;
        public Double totalAmount;
        public String paymentDate;
        public String customerName;
        public String hotelName;
        public String paymentMethod;
        public String description;
    }
}
