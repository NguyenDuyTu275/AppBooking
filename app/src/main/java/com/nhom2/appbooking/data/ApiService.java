package com.nhom2.appbooking.data;

import com.nhom2.appbooking.data.model.AdminOverview;
import com.nhom2.appbooking.data.model.AppNotification;
import com.nhom2.appbooking.data.model.ReviewEligibility;
import com.nhom2.appbooking.data.model.BankAppList;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.data.model.BookingRequest;
import com.nhom2.appbooking.data.model.ChatRoomSummary;
import com.nhom2.appbooking.data.model.DailyRevenue;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.data.model.PaymentQr;
import com.nhom2.appbooking.data.model.ReceiptScanResult;
import com.nhom2.appbooking.data.model.RevenueReceipt;
import com.nhom2.appbooking.data.model.Review;
import com.nhom2.appbooking.data.model.ReviewPage;
import com.nhom2.appbooking.data.model.Room;
import com.nhom2.appbooking.data.model.SupportChatHistory;
import com.nhom2.appbooking.data.model.SupportMessage;
import com.nhom2.appbooking.data.model.UserProfile;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/** Các API backend (Spring Boot) mà app dùng. */
public interface ApiService {

    // ---- Tài khoản: các API này trả về chuỗi (JWT hoặc thông báo) ----

    /** Trả về JWT. */
    @POST("auth/login")
    Call<String> login(@Body ApiRequests.Login body);

    /** Gửi mã OTP tới email; tài khoản chỉ được tạo sau khi xác nhận OTP. */
    @POST("auth/register")
    Call<String> register(@Body ApiRequests.Register body);

    @POST("auth/verify-otp")
    Call<String> verifyOtp(@Body ApiRequests.VerifyOtp body);

    /** Đổi Google ID token lấy JWT; email chưa có tài khoản thì backend tự tạo. */
    @POST("auth/google")
    Call<String> googleLogin(@Body ApiRequests.GoogleLogin body);

    /** Trả về email của tài khoản đã được che (vd ng***@gmail.com). */
    @POST("auth/forgot-password")
    Call<String> forgotPassword(@Body ApiRequests.ForgotPassword body);

    @POST("auth/reset-password")
    Call<String> resetPassword(@Body ApiRequests.ResetPassword body);

    @GET("users/me")
    Call<UserProfile> getMe();

    @PUT("users/me")
    Call<UserProfile> updateMe(@Body ApiRequests.UpdateProfile body);

    @Multipart
    @POST("users/me/avatar")
    Call<UserProfile> uploadAvatar(@Part MultipartBody.Part file);

    /** Gửi OTP tới email mới; trả về email đã che. */
    @POST("users/me/email/otp")
    Call<String> requestEmailChange(@Body ApiRequests.ChangeEmail body);

    @PUT("users/me/email")
    Call<UserProfile> confirmEmailChange(@Body ApiRequests.ChangeEmail body);

    // ---- Yêu thích: mỗi API trả về id các khách sạn đã lưu, mới nhất trước ----

    @GET("wishlist")
    Call<List<Long>> getWishlist();

    @PUT("wishlist/{hotelId}")
    Call<List<Long>> addToWishlist(@Path("hotelId") long hotelId);

    @DELETE("wishlist/{hotelId}")
    Call<List<Long>> removeFromWishlist(@Path("hotelId") long hotelId);

    @DELETE("wishlist")
    Call<Void> clearWishlist();

    // ---- Thông báo ----

    @GET("notifications/my")
    Call<List<AppNotification>> getMyNotifications();

    @PUT("notifications/{id}/read")
    Call<AppNotification> markNotificationRead(@Path("id") long id);

    @DELETE("notifications/{id}")
    Call<Void> deleteNotification(@Path("id") long id);

    // ---- Khách sạn (không cần đăng nhập) ----

    @GET("hotels")
    Call<List<Hotel>> getHotels();

    @GET("hotels/{id}")
    Call<Hotel> getHotel(@Path("id") long id);

    @GET("hotels/{hotelId}/reviews")
    Call<ReviewPage> getReviews(@Path("hotelId") long hotelId, @Query("page") int page, @Query("size") int size);

    @GET("hotels/{hotelId}/review-eligibility")
    Call<ReviewEligibility> getReviewEligibility(@Path("hotelId") long hotelId);

    @POST("api/reviews")
    Call<Review> createReview(@Body ApiRequests.CreateReview body);

    // ---- Đặt phòng và thanh toán ----

    @POST("bookings/{hotelId}")
    Call<Booking> createBooking(@Path("hotelId") long hotelId, @Body ApiRequests.CreateBooking body);

    @GET("bookings/my")
    Call<List<Booking>> getMyBookings();

    @PUT("bookings/cancel/{bookingId}")
    Call<Booking> cancelBooking(@Path("bookingId") long bookingId);

    @GET("bookings/{bookingId}/payment-qr")
    Call<PaymentQr> getPaymentQr(@Path("bookingId") long bookingId);

    /** Hóa đơn PDF của đơn đã thanh toán. */
    @Streaming
    @GET("bookings/{bookingId}/invoice")
    Call<ResponseBody> downloadInvoice(@Path("bookingId") long bookingId);

    // ---- Chat hỗ trợ ----

    @GET("api/support-chat/me")
    Call<SupportChatHistory> getSupportChat();

    @POST("api/support-chat/me/messages")
    Call<SupportMessage> sendSupportMessage(@Body ApiRequests.SendMessage body);

    @POST("api/support-chat/me/read")
    Call<Void> markSupportChatRead();

    // ---- Quản trị (chỉ ADMIN), giống adminApi / userApi / hotelApi / roomApi / imageApi của web ----

    @GET("admin/statistics/overview")
    Call<AdminOverview> getAdminOverview();

    @Streaming
    @GET("admin/reports/bookings.xlsx")
    Call<ResponseBody> exportBookingsExcel();

    /** status null = mọi trạng thái (Retrofit bỏ tham số null). */
    @GET("admin/bookings")
    Call<List<Booking>> getAdminBookings(@Query("status") String status);

    @PUT("admin/bookings/{bookingId}/approve")
    Call<Booking> approveBooking(@Path("bookingId") long bookingId);

    @PUT("admin/bookings/{bookingId}/reject")
    Call<Booking> rejectBooking(@Path("bookingId") long bookingId, @Body ApiRequests.ProcessRequest body);

    @PUT("admin/bookings/{bookingId}/paid")
    Call<Booking> markBookingPaid(@Path("bookingId") long bookingId);

    // Quét hóa đơn bằng máy ảnh (backend gọi Gemini) và báo cáo doanh thu theo ngày

    @Multipart
    @POST("admin/receipts/scan")
    Call<ReceiptScanResult> scanReceipt(@Part MultipartBody.Part file);

    /** Ghi hóa đơn tiền mặt chưa có trên hệ thống sau khi admin kiểm tra thông tin. */
    @POST("admin/receipts")
    Call<RevenueReceipt> createCashReceipt(@Body ApiRequests.CashReceipt body);

    /** date dạng yyyy-MM-dd; null = hôm nay theo giờ Việt Nam. */
    @GET("admin/receipts/daily")
    Call<DailyRevenue> getDailyRevenue(@Query("date") String date);

    /** Các khoản đã ghi trong khoảng ngày (yyyy-MM-dd), để cộng tiền mặt vào biểu đồ doanh thu. */
    @GET("admin/receipts")
    Call<List<RevenueReceipt>> getRevenueReceipts(@Query("from") String from, @Query("to") String to);

    @GET("admin/booking-requests")
    Call<List<BookingRequest>> getBookingRequests(@Query("status") String status);

    @PUT("admin/booking-requests/{requestId}/approve")
    Call<BookingRequest> approveBookingRequest(@Path("requestId") long requestId,
                                               @Body ApiRequests.ProcessRequest body);

    @PUT("admin/booking-requests/{requestId}/reject")
    Call<BookingRequest> rejectBookingRequest(@Path("requestId") long requestId,
                                              @Body ApiRequests.ProcessRequest body);

    @GET("users")
    Call<List<UserProfile>> getUsers();

    @POST("users")
    Call<UserProfile> createUser(@Body ApiRequests.UserUpsert body);

    @PUT("users/{id}")
    Call<UserProfile> updateUser(@Path("id") long id, @Body ApiRequests.UserUpsert body);

    @DELETE("users/{id}")
    Call<String> deleteUser(@Path("id") long id);

    @POST("hotels")
    Call<Hotel> createHotel(@Body ApiRequests.HotelUpsert body);

    @PUT("hotels/{id}")
    Call<Hotel> updateHotel(@Path("id") long id, @Body ApiRequests.HotelUpsert body);

    @DELETE("hotels/{id}")
    Call<String> deleteHotel(@Path("id") long id);

    @GET("rooms/hotel/{hotelId}")
    Call<List<Room>> getRooms(@Path("hotelId") long hotelId);

    @POST("rooms/{hotelId}")
    Call<Room> createRoom(@Path("hotelId") long hotelId, @Body ApiRequests.RoomUpsert body);

    @PUT("rooms/{id}")
    Call<Room> updateRoom(@Path("id") long id, @Body ApiRequests.RoomUpsert body);

    @DELETE("rooms/{id}")
    Call<String> deleteRoom(@Path("id") long id);

    @Multipart
    @POST("api/images/hotel/{hotelId}")
    Call<ResponseBody> uploadHotelImage(@Path("hotelId") long hotelId, @Part MultipartBody.Part file);

    @Multipart
    @POST("api/images/room/{roomId}")
    Call<ResponseBody> uploadRoomImage(@Path("roomId") long roomId, @Part MultipartBody.Part file);

    @GET("admin/reviews")
    Call<ReviewPage> getAdminReviews(@Query("page") int page, @Query("size") int size,
                                     @Query("unanswered") boolean unanswered);

    @PUT("admin/reviews/{reviewId}/reply")
    Call<Review> replyReview(@Path("reviewId") long reviewId, @Body ApiRequests.ReviewReply body);

    @DELETE("admin/reviews/{reviewId}/reply")
    Call<Review> removeReviewReply(@Path("reviewId") long reviewId);

    @GET("admin/support-chat/rooms")
    Call<List<ChatRoomSummary>> getChatRooms();

    @GET("admin/support-chat/rooms/{roomId}/messages")
    Call<List<SupportMessage>> getRoomMessages(@Path("roomId") long roomId);

    @POST("admin/support-chat/rooms/{roomId}/messages")
    Call<SupportMessage> replyInRoom(@Path("roomId") long roomId, @Body ApiRequests.SendMessage body);

    @POST("admin/support-chat/rooms/{roomId}/read")
    Call<Void> markRoomRead(@Path("roomId") long roomId);

    // ---- Bên ngoài ----

    /** Danh sách app ngân hàng hỗ trợ deeplink của VietQR (máy chủ khác, không gửi token). */
    @GET
    Call<BankAppList> getBankApps(@Url String url);
}
