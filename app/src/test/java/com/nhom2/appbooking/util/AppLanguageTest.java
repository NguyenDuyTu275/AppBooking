package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Locale;

public class AppLanguageTest {

    @Test
    public void onlyEnglishSwitchesAwayFromTheVietnameseDefault() {
        assertEquals(AppLanguage.EN, AppLanguage.from(Locale.forLanguageTag("en-US")));
        assertEquals(AppLanguage.EN, AppLanguage.from(Locale.ENGLISH));
        assertEquals(AppLanguage.VI, AppLanguage.from(Locale.forLanguageTag("vi-VN")));
        assertEquals(AppLanguage.VI, AppLanguage.from(Locale.FRENCH));
        assertEquals(AppLanguage.VI, AppLanguage.from(null));
    }

    @Test
    public void otherTogglesBetweenTheTwoLanguages() {
        assertEquals(AppLanguage.EN, AppLanguage.VI.other());
        assertEquals(AppLanguage.VI, AppLanguage.EN.other());
    }
}
