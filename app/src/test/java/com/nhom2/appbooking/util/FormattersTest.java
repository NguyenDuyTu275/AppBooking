package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.time.LocalDate;

public class FormattersTest {

    @Test
    public void moneyUsesVietnameseGroupingAndDongSign() {
        assertEquals("1.200.000đ", Formatters.money(1_200_000.0));
        assertEquals("4.800.000đ", Formatters.money(4_799_999.6));
        assertEquals("-", Formatters.money(null));
    }

    @Test
    public void dateShowsDayMonthYear() {
        assertEquals("09/06/2026", Formatters.date("2026-06-09"));
        assertEquals("22/10/2026", Formatters.date("2026-10-22T11:33:29.123"));
        assertEquals("-", Formatters.date((String) null));
    }

    @Test
    public void nightsBetweenCheckInAndCheckOut() {
        assertEquals(3, Formatters.nights(LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 2)));
    }

    @Test
    public void timeTakesHourAndMinute() {
        assertEquals("11:33", Formatters.time("2026-10-22T11:33:29.123"));
        assertEquals("", Formatters.time("2026-10-22"));
    }

    @Test
    public void ratingHasOneDecimalAndHidesMissingScores() {
        assertEquals("4.8", Formatters.rating(4.75));
        assertNull(Formatters.rating(null));
        assertNull(Formatters.rating(0.0));
    }

    @Test
    public void starsAreClampedToFive() {
        assertEquals("★★★★☆", Formatters.stars(4));
        assertEquals("★★★★★", Formatters.stars(9));
        assertEquals("☆☆☆☆☆", Formatters.stars(null));
    }
}
