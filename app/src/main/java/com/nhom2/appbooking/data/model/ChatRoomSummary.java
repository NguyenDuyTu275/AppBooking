package com.nhom2.appbooking.data.model;

/** Một cuộc trò chuyện hỗ trợ trong danh sách của admin (GET /admin/support-chat/rooms). */
public class ChatRoomSummary {
    public long roomId;
    public String username;
    public String lastMessageAt;
    public String lastMessagePreview;
    public long unreadCount;
}
