package com.nhom2.appbooking.util;

import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.data.model.RevenueReceipt;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Doanh thu đã thu của một tháng, cho biểu đồ ở trang Tổng quan của admin. */
public final class MonthlyRevenue {

    private static final int YEAR_MONTH_LENGTH = 7;

    public final YearMonth month;
    public final double revenue;

    MonthlyRevenue(YearMonth month, double revenue) {
        this.month = month;
        this.revenue = revenue;
    }

    /**
     * {@code count} tháng gần nhất tính đến {@code current} (tháng cũ trước), cùng cách tính với biểu đồ trên web:
     * chỉ đơn đã thanh toán, xếp vào tháng của ngày nhận phòng (thiếu thì ngày tạo đơn).
     */
    public static List<MonthlyRevenue> lastMonths(List<Booking> bookings, YearMonth current, int count) {
        return lastMonths(bookings, null, current, count);
    }

    /**
     * Như trên, cộng thêm hóa đơn tiền mặt admin quét tại khách sạn vào tháng của ngày ghi doanh thu.
     * Hóa đơn của đơn trên hệ thống không cộng lại vì đơn đó đã được tính khi thanh toán.
     */
    public static List<MonthlyRevenue> lastMonths(List<Booking> bookings, List<RevenueReceipt> receipts,
                                                  YearMonth current, int count) {
        Map<YearMonth, Double> totals = new LinkedHashMap<>();
        for (int i = count - 1; i >= 0; i--) {
            totals.put(current.minusMonths(i), 0.0);
        }
        if (bookings != null) {
            for (Booking booking : bookings) {
                YearMonth month = monthOf(booking);
                if (booking.isPaid() && booking.totalPrice != null && month != null && totals.containsKey(month)) {
                    totals.put(month, totals.get(month) + booking.totalPrice);
                }
            }
        }
        if (receipts != null) {
            for (RevenueReceipt receipt : receipts) {
                YearMonth month = monthOf(receipt.revenueDate);
                if (receipt.isCash() && receipt.amount != null && month != null && totals.containsKey(month)) {
                    totals.put(month, totals.get(month) + receipt.amount);
                }
            }
        }
        List<MonthlyRevenue> result = new ArrayList<>();
        for (Map.Entry<YearMonth, Double> entry : totals.entrySet()) {
            result.add(new MonthlyRevenue(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private static YearMonth monthOf(Booking booking) {
        return monthOf(booking.checkInDate != null && !booking.checkInDate.isEmpty()
                ? booking.checkInDate : booking.createdAt);
    }

    private static YearMonth monthOf(String date) {
        if (date == null || date.length() < YEAR_MONTH_LENGTH) {
            return null;
        }
        try {
            return YearMonth.parse(date.substring(0, YEAR_MONTH_LENGTH));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
