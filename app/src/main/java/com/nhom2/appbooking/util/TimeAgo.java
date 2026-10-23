package com.nhom2.appbooking.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/** "5 phút trước", "Một ngày trước"... cho danh sách thông báo, cùng cách tính với hàm timeAgo trên header của web. */
public final class TimeAgo {

    public enum Unit {
        JUST_NOW,
        MINUTES,
        HOURS,
        ONE_DAY,
        DAYS
    }

    public final Unit unit;
    /** Số phút / giờ / ngày tương ứng với {@link #unit}; 0 với JUST_NOW. */
    public final long count;

    TimeAgo(Unit unit, long count) {
        this.unit = unit;
        this.count = count;
    }

    public static TimeAgo between(LocalDateTime then, LocalDateTime now) {
        Duration elapsed = Duration.between(then, now);
        long days = elapsed.toDays();
        long hours = elapsed.toHours();
        long minutes = elapsed.toMinutes();
        if (days == 1) {
            return new TimeAgo(Unit.ONE_DAY, 1);
        }
        if (days > 1) {
            return new TimeAgo(Unit.DAYS, days);
        }
        if (hours > 0) {
            return new TimeAgo(Unit.HOURS, hours);
        }
        if (minutes > 0) {
            return new TimeAgo(Unit.MINUTES, minutes);
        }
        return new TimeAgo(Unit.JUST_NOW, 0);
    }

    /** Thời điểm backend trả về (giờ địa phương, vd "2026-10-22T11:33:29.123"); null nếu không đọc được. */
    public static LocalDateTime parse(String isoDateTime) {
        if (isoDateTime == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(isoDateTime);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
