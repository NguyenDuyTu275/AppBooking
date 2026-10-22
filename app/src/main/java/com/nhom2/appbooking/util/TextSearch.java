package com.nhom2.appbooking.util;

import java.text.Normalizer;
import java.util.Locale;

/** So khớp chữ không phân biệt dấu và hoa thường: "ha noi" khớp "Hà Nội". */
public final class TextSearch {

    private TextSearch() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String withoutMarks = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return withoutMarks.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }

    /** true nếu một trong các trường chứa từ khoá (từ khoá rỗng thì luôn khớp). */
    public static boolean matches(String query, String... fields) {
        String q = normalize(query);
        if (q.isEmpty()) {
            return true;
        }
        for (String field : fields) {
            if (normalize(field).contains(q)) {
                return true;
            }
        }
        return false;
    }
}
