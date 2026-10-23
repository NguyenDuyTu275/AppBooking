package com.nhom2.appbooking.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Link Google Maps của một khách sạn, cùng chuỗi tìm kiếm và tham số với trang chi tiết của web
 * (iframe "output=embed" và nút "Hiển thị bản đồ"). Không cần API key.
 */
public final class MapLinks {

    private static final String EMBED_URL = "https://maps.google.com/maps?q=%s&t=&z=14&ie=UTF8&iwloc=&output=embed";
    private static final String SEARCH_URL = "https://www.google.com/maps/search/?api=1&query=%s";

    private MapLinks() {
    }

    /** "Tên Địa chỉ Thành phố" như web, bỏ phần bị thiếu. */
    public static String query(String name, String address, String city) {
        StringBuilder query = new StringBuilder();
        for (String part : new String[]{name, address, city}) {
            if (part != null && !part.trim().isEmpty()) {
                if (query.length() > 0) {
                    query.append(' ');
                }
                query.append(part.trim());
            }
        }
        return query.toString();
    }

    /** Bản đồ nhúng để hiện trong WebView. */
    public static String embedUrl(String query) {
        return String.format(EMBED_URL, encode(query));
    }

    /** Mở trong app Google Maps (hoặc trình duyệt nếu máy không có app). */
    public static String searchUrl(String query) {
        return String.format(SEARCH_URL, encode(query));
    }

    /** Giống encodeURIComponent của web: khoảng trắng thành %20 chứ không phải dấu +. */
    private static String encode(String text) {
        try {
            // Bản nhận Charset chỉ có từ Android 13; minSdk là 26
            return URLEncoder.encode(text, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 is always supported", e);
        }
    }
}
