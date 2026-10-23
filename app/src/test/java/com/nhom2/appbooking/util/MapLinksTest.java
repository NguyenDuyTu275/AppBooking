package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MapLinksTest {

    @Test
    public void queryJoinsNameAddressAndCitySkippingMissingParts() {
        assertEquals("Lotte 54 Liễu Giai Hà Nội", MapLinks.query("Lotte", " 54 Liễu Giai ", "Hà Nội"));
        assertEquals("Lotte Hà Nội", MapLinks.query("Lotte", null, "Hà Nội"));
        assertEquals("Lotte", MapLinks.query("Lotte", "  ", null));
    }

    @Test
    public void embedUrlMatchesTheWebIframe() {
        assertEquals("https://maps.google.com/maps?q=Lotte%20H%C3%A0%20N%E1%BB%99i&t=&z=14&ie=UTF8&iwloc=&output=embed",
                MapLinks.embedUrl("Lotte Hà Nội"));
    }

    @Test
    public void searchUrlEncodesLikeEncodeUriComponent() {
        assertEquals("https://www.google.com/maps/search/?api=1&query=A%20%26%20B",
                MapLinks.searchUrl("A & B"));
    }
}
