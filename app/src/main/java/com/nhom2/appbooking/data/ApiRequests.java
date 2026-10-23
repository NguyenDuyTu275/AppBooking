package com.nhom2.appbooking.data;

import com.nhom2.appbooking.data.model.Hotel;

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

    /** Quên mật khẩu bước 1: backend gửi OTP tới email của tài khoản và trả về email đã che. */
    public static class ForgotPassword {
        final String username;

        public ForgotPassword(String username) {
            this.username = username;
        }
    }

    public static class ResetPassword {
        final String username;
        final String otp;
        final String newPassword;

        public ResetPassword(String username, String otp, String newPassword) {
            this.username = username;
            this.otp = otp;
            this.newPassword = newPassword;
        }
    }

    /** PUT /users/me chỉ đổi mật khẩu; email và ảnh đại diện có API riêng. */
    public static class UpdateProfile {
        final String newPassword;

        public UpdateProfile(String newPassword) {
            this.newPassword = newPassword;
        }
    }

    /** Đổi email 2 bước: gửi OTP tới email mới (otp null), rồi xác nhận bằng OTP. */
    public static class ChangeEmail {
        final String email;
        final String otp;

        public ChangeEmail(String email, String otp) {
            this.email = email;
            this.otp = otp;
        }
    }

    /** Đánh giá mới; backend tự gắn người viết là tài khoản đang đăng nhập. */
    public static class CreateReview {
        final HotelRef hotel;
        final int rating;
        final String comment;

        public CreateReview(long hotelId, int rating, String comment) {
            this.hotel = new HotelRef(hotelId);
            this.rating = rating;
            this.comment = comment;
        }
    }

    static class HotelRef {
        final long id;

        HotelRef(long id) {
            this.id = id;
        }
    }

    public static class GoogleLogin {
        final String credential;

        public GoogleLogin(String credential) {
            this.credential = credential;
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

    // ---- Quản trị ----

    /** Phản hồi của admin khi duyệt / từ chối đơn hoặc yêu cầu (có thể để trống khi duyệt). */
    public static class ProcessRequest {
        final String response;

        public ProcessRequest(String response) {
            this.response = response;
        }
    }

    public static class ReviewReply {
        final String reply;

        public ReviewReply(String reply) {
            this.reply = reply;
        }
    }

    /**
     * Tài khoản admin tạo / sửa. PUT /users/{id} lưu đè cả bản ghi, nên khi sửa phải gửi lại avatarUrl;
     * password để null thì backend giữ mật khẩu cũ.
     */
    public static class UserUpsert {
        final Long id;
        final String username;
        final String email;
        final String password;
        final String role;
        final String avatarUrl;

        public UserUpsert(Long id, String username, String email, String password, String role, String avatarUrl) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.password = password;
            this.role = role;
            this.avatarUrl = avatarUrl;
        }
    }

    /**
     * Khách sạn admin tạo / sửa. PUT /hotels/{id} lưu đè cả bản ghi, nên khi sửa phải gửi lại cả ảnh, điểm,
     * trạng thái và ngày tạo; không gửi danh sách phòng (phòng do API /rooms quản lý).
     */
    public static class HotelUpsert {
        final Long id;
        final String name;
        final String city;
        final String address;
        final String description;
        final String imageUrl;
        final Double ratingAvg;
        final String status;
        final String createdAt;

        public HotelUpsert(Hotel existing,
                           String name, String city, String address, String description) {
            this.id = existing == null ? null : existing.id;
            this.imageUrl = existing == null ? null : existing.imageUrl;
            this.ratingAvg = existing == null ? null : existing.ratingAvg;
            this.status = existing == null ? null : existing.status;
            this.createdAt = existing == null ? null : existing.createdAt;
            this.name = name;
            this.city = city;
            this.address = address;
            this.description = description;
        }
    }

    public static class RoomUpsert {
        final String name;
        final String type;
        final double pricePerNight;
        final int capacity;
        final int quantity;
        final String description;

        public RoomUpsert(String name, String type, double pricePerNight, int capacity, int quantity,
                          String description) {
            this.name = name;
            this.type = type;
            this.pricePerNight = pricePerNight;
            this.capacity = capacity;
            this.quantity = quantity;
            this.description = description;
        }
    }
}
