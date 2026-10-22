package com.nhom2.appbooking.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Đọc phần payload của JWT do backend cấp (việc kiểm tra chữ ký là của backend). */
public final class JwtUtils {

    private JwtUtils() {
    }

    public static final class Payload {
        public final String username;
        public final String role;
        /** Hết hạn lúc nào, tính bằng giây kể từ 1970; 0 nếu token không có exp. */
        public final long expiresAt;

        Payload(String username, String role, long expiresAt) {
            this.username = username;
            this.role = role;
            this.expiresAt = expiresAt;
        }
    }

    public static Payload decode(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token rỗng");
        }
        String[] parts = token.trim().split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Token không đúng định dạng JWT");
        }
        String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        JsonObject claims = JsonParser.parseString(json).getAsJsonObject();
        JsonElement exp = claims.get("exp");
        return new Payload(
                text(claims, "sub"),
                text(claims, "role"),
                exp == null || exp.isJsonNull() ? 0L : exp.getAsLong());
    }

    private static String text(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value == null || value.isJsonNull() ? null : value.getAsString();
    }
}
