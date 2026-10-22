package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TextSearchTest {

    @Test
    public void normalizeDropsVietnameseMarksAndCase() {
        assertEquals("ha noi", TextSearch.normalize("Hà Nội"));
        assertEquals("da nang", TextSearch.normalize("Đà Nẵng"));
    }

    @Test
    public void matchesAnyFieldWithoutMarks() {
        assertTrue(TextSearch.matches("ha noi", "Lucien Hotel", "Hà Nội"));
        assertTrue(TextSearch.matches("LUCIEN", "Lucien Hotel", null));
        assertFalse(TextSearch.matches("da nang", "Lucien Hotel", "Hà Nội"));
    }

    @Test
    public void emptyQueryMatchesEverything() {
        assertTrue(TextSearch.matches("  ", "Lucien Hotel"));
    }
}
