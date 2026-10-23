package com.nhom2.appbooking.util;

import java.util.regex.Pattern;

/** Kiểm tra dữ liệu nhập trong các form quản trị (cùng điều kiện với form trên web). */
public final class FormRules {

    // android.util.Patterns không có trong unit test, nên dùng biểu thức riêng: có @ và có dấu chấm ở tên miền
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    /**
     * Cùng STRONG_PASSWORD_PATTERN của web và UserService.validatePasswordStrength của backend: 8-72 ký tự,
     * không khoảng trắng, có chữ hoa, chữ thường, số và ký tự khác.
     */
    private static final Pattern STRONG_PASSWORD =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,72}$");

    private FormRules() {
    }

    public static boolean isEmail(String text) {
        return text != null && EMAIL.matcher(text.trim()).matches();
    }

    public static boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD.matcher(password).matches();
    }

    /** Số nguyên không nhỏ hơn {@code min}; null nếu để trống, không phải số hoặc quá nhỏ. */
    public static Integer intAtLeast(String text, int min) {
        if (text == null) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value >= min ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Số (có thể có phần lẻ) không nhỏ hơn {@code min}; null nếu không hợp lệ. */
    public static Double numberAtLeast(String text, double min) {
        if (text == null) {
            return null;
        }
        try {
            double value = Double.parseDouble(text.trim());
            return !Double.isNaN(value) && !Double.isInfinite(value) && value >= min ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
