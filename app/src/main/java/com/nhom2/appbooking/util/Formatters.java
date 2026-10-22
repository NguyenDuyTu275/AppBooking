package com.nhom2.appbooking.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Định dạng tiền, ngày, điểm đánh giá để hiển thị. */
public final class Formatters {

    private static final Locale VIETNAM = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formatters() {
    }

    /** 1200000.0 -> "1.200.000đ" */
    public static String money(Double amount) {
        if (amount == null) {
            return "-";
        }
        return NumberFormat.getIntegerInstance(VIETNAM).format(Math.round(amount)) + "đ";
    }

    /** "2026-06-09" (hoặc "2026-06-09T10:00:00") -> "09/06/2026" */
    public static String date(String isoDate) {
        if (isoDate == null || isoDate.length() < 10) {
            return "-";
        }
        try {
            return LocalDate.parse(isoDate.substring(0, 10)).format(DATE);
        } catch (DateTimeParseException e) {
            return isoDate;
        }
    }

    public static String date(LocalDate date) {
        return date.format(DATE);
    }

    /** Số đêm giữa ngày nhận và ngày trả phòng. */
    public static long nights(LocalDate checkIn, LocalDate checkOut) {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    /** "2026-10-22T11:33:29.123" -> "11:33" */
    public static String time(String isoDateTime) {
        if (isoDateTime == null) {
            return "";
        }
        int t = isoDateTime.indexOf('T');
        return t >= 0 && isoDateTime.length() >= t + 6 ? isoDateTime.substring(t + 1, t + 6) : "";
    }

    /** 4.75 -> "4.8"; null -> null */
    public static String rating(Double rating) {
        return rating == null || rating <= 0 ? null : String.format(Locale.ROOT, "%.1f", rating);
    }

    /** 4 -> "★★★★☆" */
    public static String stars(Integer rating) {
        int value = rating == null ? 0 : Math.max(0, Math.min(5, rating));
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            builder.append(i <= value ? '★' : '☆');
        }
        return builder.toString();
    }
}
