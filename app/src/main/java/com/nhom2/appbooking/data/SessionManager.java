package com.nhom2.appbooking.data;

import android.content.Context;
import android.content.SharedPreferences;

/** Lưu phiên đăng nhập (JWT, tên, quyền) trong SharedPreferences. */
public class SessionManager {

    private static final String PREFS = "appbooking_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EXPIRES_AT = "expires_at";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Lưu JWT vừa đăng nhập; ném IllegalArgumentException nếu token không hợp lệ. */
    public void save(String token) {
        JwtUtils.Payload payload = JwtUtils.decode(token);
        prefs.edit()
                .putString(KEY_TOKEN, token.trim())
                .putString(KEY_USERNAME, payload.username)
                .putString(KEY_ROLE, payload.role)
                .putLong(KEY_EXPIRES_AT, payload.expiresAt)
                .apply();
    }

    public boolean isLoggedIn() {
        String token = prefs.getString(KEY_TOKEN, null);
        long expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L);
        return token != null && (expiresAt == 0L || expiresAt * 1000L > System.currentTimeMillis());
    }

    /** JWT còn hạn, hoặc null. */
    public String getToken() {
        return isLoggedIn() ? prefs.getString(KEY_TOKEN, null) : null;
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public boolean isAdmin() {
        return "ADMIN".equals(prefs.getString(KEY_ROLE, null));
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
