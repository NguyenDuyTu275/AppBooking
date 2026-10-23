package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FormRulesTest {

    @Test
    public void emailNeedsAtSignAndDomainWithDot() {
        assertTrue(FormRules.isEmail("guest@gmail.com"));
        assertTrue(FormRules.isEmail("  guest@mail.example.vn "));
        assertFalse(FormRules.isEmail("guest@gmail"));
        assertFalse(FormRules.isEmail("guest gmail.com"));
        assertFalse(FormRules.isEmail(""));
        assertFalse(FormRules.isEmail(null));
    }

    @Test
    public void strongPasswordFollowsTheBackendPolicy() {
        assertTrue(FormRules.isStrongPassword("Strong1!"));
        assertFalse(FormRules.isStrongPassword("Short1!"));
        assertFalse(FormRules.isStrongPassword("nouppercase1!"));
        assertFalse(FormRules.isStrongPassword("NOLOWERCASE1!"));
        assertFalse(FormRules.isStrongPassword("NoDigits!!"));
        assertFalse(FormRules.isStrongPassword("NoSymbol123"));
        assertFalse(FormRules.isStrongPassword("Has Space1!"));
        assertFalse(FormRules.isStrongPassword(null));
    }

    @Test
    public void integersBelowTheMinimumOrNotNumbersAreRejected() {
        assertEquals(Integer.valueOf(2), FormRules.intAtLeast(" 2 ", 1));
        assertEquals(Integer.valueOf(0), FormRules.intAtLeast("0", 0));
        assertNull(FormRules.intAtLeast("0", 1));
        assertNull(FormRules.intAtLeast("1.5", 1));
        assertNull(FormRules.intAtLeast("", 0));
        assertNull(FormRules.intAtLeast(null, 0));
    }

    @Test
    public void pricesMustBeAtLeastTheMinimum() {
        assertEquals(1_200_000.0, FormRules.numberAtLeast("1200000", 1), 0.001);
        assertNull(FormRules.numberAtLeast("0", 1));
        assertNull(FormRules.numberAtLeast("abc", 1));
        assertNull(FormRules.numberAtLeast("NaN", 1));
    }
}
