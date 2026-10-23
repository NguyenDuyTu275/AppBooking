package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;

import com.nhom2.appbooking.data.model.Booking;

import org.junit.Test;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MonthlyRevenueTest {

    private static Booking booking(String checkIn, String createdAt, String paymentStatus, Double total) {
        Booking booking = new Booking();
        booking.checkInDate = checkIn;
        booking.createdAt = createdAt;
        booking.paymentStatus = paymentStatus;
        booking.totalPrice = total;
        return booking;
    }

    @Test
    public void coversTheLastMonthsOldestFirstEvenWithoutBookings() {
        List<MonthlyRevenue> months = MonthlyRevenue.lastMonths(Collections.emptyList(), YearMonth.of(2026, 2), 6);

        assertEquals(6, months.size());
        assertEquals(YearMonth.of(2025, 9), months.get(0).month);
        assertEquals(YearMonth.of(2026, 2), months.get(5).month);
        assertEquals(0.0, months.get(5).revenue, 0.001);
    }

    @Test
    public void onlyPaidBookingsCountInTheMonthOfCheckIn() {
        List<Booking> bookings = Arrays.asList(
                booking("2026-10-23", "2026-09-30T10:00:00", "PAID", 2_400_000.0),
                booking("2026-10-05", null, "PAID", 1_000_000.0),
                booking("2026-10-07", null, "UNPAID", 9_000_000.0),
                booking("2026-10-08", null, "REFUNDED", 5_000_000.0),
                booking("2026-09-01", null, "PAID", 500_000.0));

        List<MonthlyRevenue> months = MonthlyRevenue.lastMonths(bookings, YearMonth.of(2026, 10), 6);

        assertEquals(3_400_000.0, months.get(5).revenue, 0.001);
        assertEquals(500_000.0, months.get(4).revenue, 0.001);
    }

    @Test
    public void fallsBackToCreationDateAndIgnoresMonthsOutsideTheChart() {
        List<Booking> bookings = Arrays.asList(
                booking(null, "2026-08-15T09:30:00.123", "PAID", 700_000.0),
                booking("2025-01-10", null, "PAID", 1_000_000.0),
                booking("not a date", null, "PAID", 1_000_000.0));

        List<MonthlyRevenue> months = MonthlyRevenue.lastMonths(bookings, YearMonth.of(2026, 10), 6);

        assertEquals(700_000.0, months.get(3).revenue, 0.001);
        double total = 0;
        for (MonthlyRevenue month : months) {
            total += month.revenue;
        }
        assertEquals(700_000.0, total, 0.001);
    }
}
