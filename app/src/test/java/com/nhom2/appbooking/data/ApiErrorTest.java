package com.nhom2.appbooking.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ApiErrorTest {

    @Test
    public void readsTranslatedMessageAndBusinessCode() {
        ApiError error = ApiError.parse("{\"message\":\"Sai tên đăng nhập hoặc mật khẩu.\",\"code\":\"error.login.invalidCredentials\"}");

        assertEquals("Sai tên đăng nhập hoặc mật khẩu.", error.message);
        assertEquals("error.login.invalidCredentials", error.code);
    }

    @Test
    public void springSecurityDefaultErrorHasNoBusinessCode() {
        ApiError error = ApiError.parse("{\"timestamp\":\"2026-10-22T01:40:02\",\"status\":403,\"error\":\"Forbidden\",\"path\":\"/bookings/my\"}");

        assertNull(error.code);
        assertNull(error.message);
    }

    @Test
    public void nonJsonBodyGivesEmptyError() {
        ApiError error = ApiError.parse("<html>Bad gateway</html>");

        assertNull(error.message);
        assertNull(error.code);
    }
}
