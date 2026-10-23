package com.nhom2.appbooking.util;

/**
 * Mức điểm đánh giá trung bình, cùng ngưỡng với website (frontend/src/utils/hotelDisplay.ts: ratingLabelKey)
 * để một khách sạn hiện cùng một nhãn ("Tốt", "Ổn"...) trên app và trên web.
 */
public enum RatingLevel {
    EXCEPTIONAL,
    VERY_GOOD,
    GOOD,
    OKAY,
    AVERAGE,
    NONE;

    private static final double EXCEPTIONAL_MIN = 9;
    private static final double VERY_GOOD_MIN = 8;
    private static final double GOOD_MIN = 7;
    private static final double OKAY_MIN = 5;

    public static RatingLevel of(Double score) {
        if (score == null || score <= 0) {
            return NONE;
        }
        if (score >= EXCEPTIONAL_MIN) {
            return EXCEPTIONAL;
        }
        if (score >= VERY_GOOD_MIN) {
            return VERY_GOOD;
        }
        if (score >= GOOD_MIN) {
            return GOOD;
        }
        if (score >= OKAY_MIN) {
            return OKAY;
        }
        return AVERAGE;
    }
}
