package com.nhom2.appbooking.data.model;

import java.util.ArrayList;
import java.util.List;

/** Lịch sử chat hỗ trợ của khách (GET /api/support-chat/me). */
public class SupportChatHistory {
    public Long roomId;
    public List<SupportMessage> messages = new ArrayList<>();
}
