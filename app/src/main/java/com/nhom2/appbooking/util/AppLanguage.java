package com.nhom2.appbooking.util;

import java.util.Locale;

/** Hai ngôn ngữ của app, giống nút VI | EN trên header của web; tiếng Việt là mặc định. */
public enum AppLanguage {
    VI("vi"),
    EN("en");

    public final String tag;

    AppLanguage(String tag) {
        this.tag = tag;
    }

    /** Ngôn ngữ đang dùng; mọi ngôn ngữ khác tiếng Anh đều hiện tiếng Việt (bộ chuỗi mặc định). */
    public static AppLanguage from(Locale locale) {
        return locale != null && EN.tag.equals(locale.getLanguage()) ? EN : VI;
    }

    /** Ngôn ngữ còn lại, cho nút chuyển nhanh trong menu. */
    public AppLanguage other() {
        return this == VI ? EN : VI;
    }
}
