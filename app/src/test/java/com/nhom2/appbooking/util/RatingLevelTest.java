package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Cùng bộ ca với frontend/src/utils/hotelDisplay.test.ts để app và web gắn cùng một nhãn. */
public class RatingLevelTest {

    @Test
    public void scoresMapToTheSameLabelsAsTheWebsite() {
        assertEquals(RatingLevel.EXCEPTIONAL, RatingLevel.of(9.5));
        assertEquals(RatingLevel.EXCEPTIONAL, RatingLevel.of(9.0));
        assertEquals(RatingLevel.VERY_GOOD, RatingLevel.of(8.9));
        assertEquals(RatingLevel.VERY_GOOD, RatingLevel.of(8.0));
        assertEquals(RatingLevel.GOOD, RatingLevel.of(7.0));
        assertEquals(RatingLevel.OKAY, RatingLevel.of(6.7));
        assertEquals(RatingLevel.OKAY, RatingLevel.of(5.0));
        assertEquals(RatingLevel.AVERAGE, RatingLevel.of(4.0));
        assertEquals(RatingLevel.AVERAGE, RatingLevel.of(0.5));
    }

    @Test
    public void missingScoreMeansNotRatedYet() {
        assertEquals(RatingLevel.NONE, RatingLevel.of(null));
        assertEquals(RatingLevel.NONE, RatingLevel.of(0.0));
    }
}
