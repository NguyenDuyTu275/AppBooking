package com.nhom2.appbooking.data;

import java.util.List;

/** Dữ liệu gửi lên backend. */
public final class ApiRequests {

    private ApiRequests() {
    }

    public static class Login {
        final String username;
        final String password;

        public Login(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class Register {
        final String username;
        final String password;
        final String email;

        public Register(String username, String password, String email) {
            this.username = username;
            this.password = password;
            this.email = email;
        }
    }

    public static class VerifyOtp {
        final String email;
        final String otp;

        public VerifyOtp(String email, String otp) {
            this.email = email;
            this.otp = otp;
        }
    }

    public static class CreateBooking {
        final String checkInDate;
        final String checkOutDate;
        final List<RoomItem> rooms;

        /** Ngày dạng yyyy-MM-dd. */
        public CreateBooking(String checkInDate, String checkOutDate, List<RoomItem> rooms) {
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.rooms = rooms;
        }
    }

    public static class RoomItem {
        final long roomId;
        final int quantity;

        public RoomItem(long roomId, int quantity) {
            this.roomId = roomId;
            this.quantity = quantity;
        }
    }

    public static class SendMessage {
        final String content;

        public SendMessage(String content) {
            this.content = content;
        }
    }
}
