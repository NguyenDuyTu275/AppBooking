package com.nhom2.appbooking.data;

import com.nhom2.appbooking.data.model.BankAppList;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.data.model.PaymentQr;
import com.nhom2.appbooking.data.model.ReviewPage;
import com.nhom2.appbooking.data.model.SupportChatHistory;
import com.nhom2.appbooking.data.model.SupportMessage;
import com.nhom2.appbooking.data.model.UserProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
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

    @GET("users/me")
    Call<UserProfile> getMe();

    // ---- Khách sạn (không cần đăng nhập) ----

    @GET("hotels")
    Call<List<Hotel>> getHotels();

    @GET("hotels/{id}")
    Call<Hotel> getHotel(@Path("id") long id);

    @GET("hotels/{hotelId}/reviews")
    Call<ReviewPage> getReviews(@Path("hotelId") long hotelId, @Query("page") int page, @Query("size") int size);

    // ---- Đặt phòng và thanh toán ----

    @POST("bookings/{hotelId}")
    Call<Booking> createBooking(@Path("hotelId") long hotelId, @Body ApiRequests.CreateBooking body);

    @GET("bookings/my")
    Call<List<Booking>> getMyBookings();

    @PUT("bookings/cancel/{bookingId}")
    Call<Booking> cancelBooking(@Path("bookingId") long bookingId);

    @GET("bookings/{bookingId}/payment-qr")
    Call<PaymentQr> getPaymentQr(@Path("bookingId") long bookingId);

    // ---- Chat hỗ trợ ----

    @GET("api/support-chat/me")
    Call<SupportChatHistory> getSupportChat();

    @POST("api/support-chat/me/messages")
    Call<SupportMessage> sendSupportMessage(@Body ApiRequests.SendMessage body);

    @POST("api/support-chat/me/read")
    Call<Void> markSupportChatRead();

    // ---- Bên ngoài ----

    /** Danh sách app ngân hàng hỗ trợ deeplink của VietQR (máy chủ khác, không gửi token). */
    @GET
    Call<BankAppList> getBankApps(@Url String url);
}
