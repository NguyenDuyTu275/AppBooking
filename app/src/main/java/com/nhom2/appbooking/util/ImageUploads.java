package com.nhom2.appbooking.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Ảnh admin được phép tải lên: cùng loại file với web và cùng giới hạn dung lượng với backend. */
public final class ImageUploads {

    /** Bằng spring.servlet.multipart.max-file-size (5MB) của backend. */
    public static final long MAX_BYTES = 5L * 1024 * 1024;

    // Web nhận .jpg, .jpeg, .png, .webp, .gif
    private static final Set<String> ALLOWED_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/webp", "image/gif"));

    private ImageUploads() {
    }

    public static boolean isAllowedType(String mimeType) {
        return mimeType != null && ALLOWED_TYPES.contains(mimeType.toLowerCase(Locale.ROOT));
    }

    public static boolean isAllowedSize(long bytes) {
        return bytes > 0 && bytes <= MAX_BYTES;
    }
}
