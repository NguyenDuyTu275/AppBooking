package com.nhom2.appbooking.data;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.nhom2.appbooking.util.AppLanguage;

import java.util.Locale;

/**
 * Ngôn ngữ của app (VI / EN như nút trên header của web). AppCompat tự lưu lựa chọn và áp dụng lại khi mở app
 * (autoStoreLocales trong AndroidManifest), nên ở đây chỉ đọc và đổi.
 */
public final class LanguageSettings {

    private LanguageSettings() {
    }

    public static AppLanguage current() {
        LocaleListCompat chosen = AppCompatDelegate.getApplicationLocales();
        Locale locale = chosen.isEmpty() ? Locale.getDefault() : chosen.get(0);
        return AppLanguage.from(locale);
    }

    /** Đổi ngôn ngữ; các màn hình đang mở tự dựng lại theo ngôn ngữ mới. */
    public static void apply(AppLanguage language) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag));
    }
}
