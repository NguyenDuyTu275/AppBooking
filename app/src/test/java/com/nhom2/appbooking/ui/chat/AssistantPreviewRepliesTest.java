package com.nhom2.appbooking.ui.chat;

import static org.junit.Assert.assertEquals;

import com.nhom2.appbooking.R;

import org.junit.Test;

public class AssistantPreviewRepliesTest {

    @Test
    public void vietnameseSuggestionsGetMatchingReplies() {
        assertEquals(R.string.assistant_reply_hotel, AssistantPreviewReplies.forQuestion("Tìm khách sạn phù hợp"));
        assertEquals(R.string.assistant_reply_booking, AssistantPreviewReplies.forQuestion("Cách đặt phòng như thế nào?"));
        assertEquals(R.string.assistant_reply_payment, AssistantPreviewReplies.forQuestion("Hướng dẫn thanh toán"));
    }

    @Test
    public void englishSuggestionsGetMatchingReplies() {
        assertEquals(R.string.assistant_reply_hotel, AssistantPreviewReplies.forQuestion("Find a suitable hotel"));
        assertEquals(R.string.assistant_reply_booking, AssistantPreviewReplies.forQuestion("How do I book a room?"));
        assertEquals(R.string.assistant_reply_payment, AssistantPreviewReplies.forQuestion("How to pay"));
    }

    @Test
    public void greetingAndUnknownQuestions() {
        assertEquals(R.string.assistant_reply_greeting, AssistantPreviewReplies.forQuestion("Xin chào"));
        assertEquals(R.string.assistant_reply_default, AssistantPreviewReplies.forQuestion("???"));
    }
}
