package com.nhom2.appbooking.data.model;

import java.util.List;

/** Báo cáo doanh thu một ngày từ các hóa đơn đã quét (GET /admin/receipts/daily). */
public class DailyRevenue {
    public String date;
    public List<RevenueReceipt> receipts;
    public double cashTotal;
    public double invoiceTotal;
    public double total;
}
