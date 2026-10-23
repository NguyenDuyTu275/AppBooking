package com.nhom2.appbooking.ui.chat;

import androidx.annotation.StringRes;

import com.nhom2.appbooking.R;

import java.util.Locale;

/** Câu trả lời mẫu của trợ lý (bản xem trước), nhận câu hỏi tiếng Việt lẫn tiếng Anh và trả lời theo ngôn ngữ của app. */
final class AssistantPreviewReplies {

    private AssistantPreviewReplies() {
    }

    @StringRes
    static int forQuestion(String question) {
        String text = question.toLowerCase(Locale.ROOT);
        if (containsAny(text, "thanh toán", "thanh toan", "trả tiền", "tra tien", "qr", "pay", "transfer")) {
            return R.string.assistant_reply_payment;
        }
        if (containsAny(text, "đặt phòng", "dat phong", "đặt khách sạn", "dat khach san", "book", "reserv")) {
            return R.string.assistant_reply_booking;
        }
        if (containsAny(text, "khách sạn", "khach san", "tìm", "tim", "phòng", "phong",
                "hotel", "find", "search", "room")) {
            return R.string.assistant_reply_hotel;
        }
        if (containsAny(text, "xin chào", "xin chao", "chào", "hello", "hi")) {
            return R.string.assistant_reply_greeting;
        }
        return R.string.assistant_reply_default;
    }

    private static boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
