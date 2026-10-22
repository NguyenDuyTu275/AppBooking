package com.nhom2.appbooking.ui.bookings;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.Booking;

/** Nhãn và màu cho trạng thái đơn / thanh toán. */
final class BookingStatusUi {

    private BookingStatusUi() {
    }

    static int statusLabel(String status) {
        if (Booking.STATUS_CONFIRMED.equals(status)) {
            return R.string.status_confirmed;
        } else if (Booking.STATUS_CANCELLED.equals(status)) {
            return R.string.status_cancelled;
        } else if (Booking.STATUS_REJECTED.equals(status)) {
            return R.string.status_rejected;
        }
        return R.string.status_pending;
    }

    static int statusBackground(String status) {
        if (Booking.STATUS_CONFIRMED.equals(status)) {
            return R.drawable.bg_chip_success;
        } else if (Booking.STATUS_CANCELLED.equals(status) || Booking.STATUS_REJECTED.equals(status)) {
            return R.drawable.bg_chip_error;
        }
        return R.drawable.bg_chip_warning;
    }

    static int statusColor(String status) {
        if (Booking.STATUS_CONFIRMED.equals(status)) {
            return R.color.success;
        } else if (Booking.STATUS_CANCELLED.equals(status) || Booking.STATUS_REJECTED.equals(status)) {
            return R.color.error;
        }
        return R.color.warning;
    }

    static int paymentLabel(Booking booking) {
        if (booking.isPaid()) {
            return R.string.pay_paid;
        } else if (Booking.PAYMENT_REFUNDED.equals(booking.paymentStatus)) {
            return R.string.pay_refunded;
        }
        return R.string.pay_unpaid;
    }

    static int paymentBackground(Booking booking) {
        return booking.isPaid() ? R.drawable.bg_chip_success : R.drawable.bg_chip_grey;
    }

    static int paymentColor(Booking booking) {
        return booking.isPaid() ? R.color.success : R.color.text_secondary;
    }
}
