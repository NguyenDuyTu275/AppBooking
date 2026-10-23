package com.nhom2.appbooking.data.model;

/** Thông báo của người dùng (GET /notifications/my); tiêu đề và nội dung đã theo ngôn ngữ của request. */
public class AppNotification {
    public long id;
    public String title;
    public String message;
    public Boolean read;
    public String createdAt;

    public boolean isRead() {
        return Boolean.TRUE.equals(read);
    }
}
