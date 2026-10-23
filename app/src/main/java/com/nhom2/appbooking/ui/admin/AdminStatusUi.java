package com.nhom2.appbooking.ui.admin;

import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.data.model.BookingRequest;
import com.nhom2.appbooking.util.Formatters;

/** Nhãn và màu trạng thái giống các hàm getStatusTag / getPaymentStatusTag ở trang quản trị của web. */
final class AdminStatusUi {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";
    private static final int GOOD_SCORE = 8;
    private static final int OKAY_SCORE = 5;

    private AdminStatusUi() {
    }

    static void bookingStatus(TextView view, String status) {
        if (Booking.STATUS_PENDING.equals(status)) {
            Tag.ORANGE.applyTo(view, view.getContext().getString(R.string.admin_status_pending));
        } else if (Booking.STATUS_CONFIRMED.equals(status)) {
            Tag.GREEN.applyTo(view, view.getContext().getString(R.string.admin_status_confirmed));
        } else if (Booking.STATUS_REJECTED.equals(status)) {
            Tag.RED.applyTo(view, view.getContext().getString(R.string.admin_status_rejected));
        } else if (Booking.STATUS_CANCELLED.equals(status)) {
            Tag.GREY.applyTo(view, view.getContext().getString(R.string.admin_status_cancelled));
        } else {
            Tag.GREY.applyTo(view, status);
        }
    }

    static void paymentStatus(TextView view, String status) {
        if (Booking.PAYMENT_UNPAID.equals(status)) {
            Tag.VOLCANO.applyTo(view, view.getContext().getString(R.string.admin_payment_unpaid));
        } else if (Booking.PAYMENT_PAID.equals(status)) {
            Tag.CYAN.applyTo(view, view.getContext().getString(R.string.admin_payment_paid));
        } else if (Booking.PAYMENT_REFUNDED.equals(status)) {
            Tag.GREY.applyTo(view, view.getContext().getString(R.string.admin_payment_refunded));
        } else {
            Tag.GREY.applyTo(view, status);
        }
    }

    static void requestType(TextView view, String type) {
        if (BookingRequest.TYPE_CANCEL.equals(type)) {
            Tag.MAGENTA.applyTo(view, view.getContext().getString(R.string.admin_requests_type_cancel));
        } else if (BookingRequest.TYPE_CHANGE_DATE.equals(type)) {
            Tag.CYAN.applyTo(view, view.getContext().getString(R.string.admin_requests_type_change_date));
        } else {
            Tag.GREY.applyTo(view, type);
        }
    }

    static void requestStatus(TextView view, String status) {
        if (BookingRequest.STATUS_PENDING.equals(status)) {
            Tag.GOLD.applyTo(view, view.getContext().getString(R.string.admin_requests_status_pending));
        } else if (BookingRequest.STATUS_APPROVED.equals(status)) {
            Tag.GREEN.applyTo(view, view.getContext().getString(R.string.admin_status_confirmed));
        } else if (BookingRequest.STATUS_REJECTED.equals(status)) {
            Tag.RED.applyTo(view, view.getContext().getString(R.string.admin_status_rejected));
        } else {
            Tag.GREY.applyTo(view, status);
        }
    }

    static void role(TextView view, String role) {
        if (ROLE_ADMIN.equals(role)) {
            Tag.RED.applyTo(view, ROLE_ADMIN);
        } else {
            Tag.BLUE.applyTo(view, role == null ? ROLE_USER : role);
        }
    }

    /** Điểm đánh giá: từ 8 xanh lá, từ 5 vàng, dưới 5 đỏ (như cột "Điểm" trên web). */
    static void reviewScore(TextView view, Integer rating) {
        int score = rating == null ? 0 : rating;
        Tag tag = score >= GOOD_SCORE ? Tag.GREEN : score >= OKAY_SCORE ? Tag.GOLD : Tag.RED;
        tag.applyTo(view, view.getContext().getString(R.string.admin_reviews_score, score));
    }

    /** Điểm trung bình của khách sạn: nhãn vàng, chưa có điểm thì gạch ngang. */
    static void hotelRating(TextView view, Double rating) {
        String value = Formatters.rating(rating);
        if (value == null) {
            view.setBackground(null);
            view.setTextColor(ContextCompat.getColor(view.getContext(), R.color.text_muted));
            view.setText(R.string.admin_hotels_no_rating);
        } else {
            Tag.GOLD.applyTo(view, value);
        }
    }
}
