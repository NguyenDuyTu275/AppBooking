package com.nhom2.appbooking.util;

/** Số tiền admin nhập hoặc sửa trong form ("1.200.000đ", "1,200,000", "1200000"). */
public final class MoneyInput {

    // Tối đa 15 chữ số: vẫn chính xác khi đổi sang double
    private static final int MAX_DIGITS = 15;

    private MoneyInput() {
    }

    /** Bỏ mọi ký tự không phải chữ số; trả về null khi không có số hoặc số bằng 0. */
    public static Double parse(String text) {
        if (text == null) {
            return null;
        }
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > MAX_DIGITS) {
            return null;
        }
        double value = Double.parseDouble(digits);
        return value > 0 ? value : null;
    }

    /** 1200000.0 -> "1200000": số trơn để admin sửa trong ô nhập. */
    public static String plain(Double amount) {
        return amount == null ? "" : String.valueOf(Math.round(amount));
    }
}
