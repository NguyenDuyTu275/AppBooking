package com.nhom2.appbooking.util;

/** Ghép đường dẫn ảnh tương đối của backend (vd /uploads/...) thành URL đầy đủ. */
public final class ImageUrls {

    private ImageUrls() {
    }

    public static String resolve(String url, String baseUrl) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String value = url.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            return value;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + (value.startsWith("/") ? value : "/" + value);
    }
}
