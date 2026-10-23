package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDateTime;

public class TimeAgoTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 22, 12, 0);

    @Test
    public void picksTheLargestUnitLikeTheWebsite() {
        assertEquals(TimeAgo.Unit.JUST_NOW, TimeAgo.between(NOW.minusSeconds(30), NOW).unit);

        TimeAgo minutes = TimeAgo.between(NOW.minusMinutes(5), NOW);
        assertEquals(TimeAgo.Unit.MINUTES, minutes.unit);
        assertEquals(5, minutes.count);

        TimeAgo hours = TimeAgo.between(NOW.minusHours(3).minusMinutes(20), NOW);
        assertEquals(TimeAgo.Unit.HOURS, hours.unit);
        assertEquals(3, hours.count);

        assertEquals(TimeAgo.Unit.ONE_DAY, TimeAgo.between(NOW.minusHours(30), NOW).unit);

        TimeAgo days = TimeAgo.between(NOW.minusDays(4), NOW);
        assertEquals(TimeAgo.Unit.DAYS, days.unit);
        assertEquals(4, days.count);
    }

    @Test
    public void futureTimesCountAsJustNow() {
        // Đồng hồ máy chủ và điện thoại có thể lệch nhau vài phút
        assertEquals(TimeAgo.Unit.JUST_NOW, TimeAgo.between(NOW.plusMinutes(3), NOW).unit);
    }

    @Test
    public void parsesBackendTimestamps() {
        assertEquals(LocalDateTime.of(2026, 10, 22, 11, 33, 29, 123_000_000),
                TimeAgo.parse("2026-10-22T11:33:29.123"));
        assertNull(TimeAgo.parse("hôm qua"));
        assertNull(TimeAgo.parse(null));
    }
}
