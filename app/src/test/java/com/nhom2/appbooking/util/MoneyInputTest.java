package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MoneyInputTest {

    @Test
    public void readsAmountsWrittenTheVietnameseOrEnglishWay() {
        assertEquals(1_200_000.0, MoneyInput.parse("1.200.000đ"), 0.001);
        assertEquals(1_200_000.0, MoneyInput.parse("1,200,000 VND"), 0.001);
        assertEquals(850_000.0, MoneyInput.parse(" 850000 "), 0.001);
    }

    @Test
    public void emptyZeroOrHugeAmountsAreRejected() {
        assertNull(MoneyInput.parse(null));
        assertNull(MoneyInput.parse(""));
        assertNull(MoneyInput.parse("đ"));
        assertNull(MoneyInput.parse("0"));
        assertNull(MoneyInput.parse("1234567890123456"));
    }

    @Test
    public void plainDropsDecimalsAndSeparatorsForEditing() {
        assertEquals("1200000", MoneyInput.plain(1_200_000.0));
        assertEquals("", MoneyInput.plain(null));
    }
}
