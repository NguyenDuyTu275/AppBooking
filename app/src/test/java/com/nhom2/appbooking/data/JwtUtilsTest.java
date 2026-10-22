package com.nhom2.appbooking.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JwtUtilsTest {

    private static String token(String payloadJson) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8))
                + "." + encoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))
                + ".signature";
    }

    @Test
    public void readsUsernameRoleAndExpiry() {
        JwtUtils.Payload payload = JwtUtils.decode(token("{\"sub\":\"duytu23\",\"role\":\"ADMIN\",\"exp\":1792645997}"));

        assertEquals("duytu23", payload.username);
        assertEquals("ADMIN", payload.role);
        assertEquals(1792645997L, payload.expiresAt);
    }

    @Test
    public void missingExpiryIsZero() {
        assertEquals(0L, JwtUtils.decode(token("{\"sub\":\"khach\",\"role\":\"USER\"}")).expiresAt);
    }

    @Test
    public void rejectsTextThatIsNotAJwt() {
        assertThrows(IllegalArgumentException.class, () -> JwtUtils.decode("Sai mat khau"));
    }
}
