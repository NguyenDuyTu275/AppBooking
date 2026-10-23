package com.nhom2.appbooking.ui.chat;

import java.util.Locale;

final class AssistantPreviewReplies {

    private AssistantPreviewReplies() {
    }

    static String forQuestion(String question) {
        String text = question.toLowerCase(Locale.ROOT);
        if (containsAny(text, "thanh toán", "thanh toan", "trả tiền", "tra tien", "qr")) {
            return "Bạn có thể thanh toán bằng mã QR hoặc app ngân hàng ở màn hình đơn đặt phòng. "
                    + "Sau khi chuyển khoản, hệ thống thường tự xác nhận trong 1–3 phút.";
        }
        if (containsAny(text, "đặt phòng", "dat phong", "đặt khách sạn", "dat khach san")) {
            return "Bạn hãy chọn khách sạn, xem phòng còn trống, chọn ngày nhận và trả phòng rồi xác nhận đặt phòng. "
                    + "Tổng tiền sẽ được hiển thị trước khi bạn xác nhận.";
        }
        if (containsAny(text, "khách sạn", "khach san", "tìm", "tim", "phòng", "phong")) {
            return "Mở tab Khách sạn để tìm theo tên hoặc thành phố. Chọn một khách sạn để xem ảnh, đánh giá "
                    + "và các phòng còn trống nhé.";
        }
        if (containsAny(text, "xin chào", "xin chao", "chào", "hello", "hi")) {
            return "Chào bạn! Mình có thể hướng dẫn tìm khách sạn, đặt phòng hoặc thanh toán. "
                    + "Bạn muốn tìm hiểu mục nào?";
        }
        return "Mình đã nhận câu hỏi. Trong bản xem trước này, mình có thể hỗ trợ về tìm khách sạn, "
                + "cách đặt phòng và thanh toán. Bạn thử hỏi một trong các chủ đề đó nhé.";
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
