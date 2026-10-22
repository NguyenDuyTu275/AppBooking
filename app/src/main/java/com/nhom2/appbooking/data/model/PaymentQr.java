package com.nhom2.appbooking.data.model;

/** Thông tin thanh toán chuyển khoản (GET /bookings/{bookingId}/payment-qr). */
public class PaymentQr {
    public Long bookingId;
    public String qrCodeUrl;
    public String bankName;
    public String accountNumber;
    public String accountName;
    public Double amount;
    /** "DH <mã đơn>": Sepay dựa vào nội dung này để tự xác nhận đơn. */
    public String transferContent;
    /** Deeplink VietQR chưa có app/url; null nếu backend chưa hỗ trợ. */
    public String bankAppDeeplink;
}
