package com.nhom2.appbooking.ui.admin;

import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.databinding.ItemAdminBookingBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.util.Formatters;

import java.util.List;

import retrofit2.Call;

/** Mục "Quản lý Đặt phòng": lọc theo trạng thái, duyệt / từ chối, đánh dấu đã thu tiền (như AdminBookings trên web). */
public class AdminBookingsFragment extends AdminListFragment {

    private static final String NOT_AVAILABLE = "N/A";
    private static final int[] FILTER_LABELS = {
            R.string.admin_filter_all,
            R.string.admin_status_pending,
            R.string.admin_status_confirmed,
            R.string.admin_status_rejected,
            R.string.admin_status_cancelled,
    };
    private static final String[] FILTER_VALUES = {
            null,
            Booking.STATUS_PENDING,
            Booking.STATUS_CONFIRMED,
            Booking.STATUS_REJECTED,
            Booking.STATUS_CANCELLED,
    };

    private BindingAdapter<Booking, ItemAdminBookingBinding> adapter;
    private String statusFilter;

    @Override
    protected int titleRes() {
        return R.string.admin_menu_bookings;
    }

    @Override
    protected void setUp() {
        adapter = new BindingAdapter<>(ItemAdminBookingBinding::inflate, this::bind);
        binding.recycler.setAdapter(adapter);
        showFilters(FILTER_LABELS, FILTER_VALUES, status -> {
            statusFilter = status;
            load();
        });
    }

    @Override
    protected void load() {
        setLoading(true);
        ApiClient.get(requireContext()).getAdminBookings(statusFilter).enqueue(new ApiCallback<List<Booking>>(this) {
            @Override
            protected void onSuccess(List<Booking> bookings) {
                setLoading(false);
                adapter.submit(bookings);
                showEmpty(adapter.isEmpty(), R.string.admin_bookings_empty);
            }

            @Override
            protected void onError(String message) {
                setLoading(false);
                super.onError(getString(R.string.admin_bookings_load_failed));
            }
        });
    }

    private void bind(ItemAdminBookingBinding b, Booking booking) {
        b.textId.setText(getString(R.string.admin_bookings_number, (int) booking.id));
        b.textGuest.setText(booking.user == null || booking.user.username == null ? NOT_AVAILABLE : booking.user.username);
        b.textHotel.setText(booking.hotel == null || booking.hotel.name == null ? NOT_AVAILABLE : booking.hotel.name);
        b.textDates.setText(getString(R.string.date_range,
                Formatters.date(booking.checkInDate), Formatters.date(booking.checkOutDate)));
        b.textTotal.setText(Formatters.money(booking.totalPrice));
        AdminStatusUi.bookingStatus(b.textStatus, booking.status);
        AdminStatusUi.paymentStatus(b.textPayment, booking.paymentStatus);

        boolean decide = booking.canAdminDecide();
        boolean markPaid = booking.canAdminMarkPaid();
        b.buttonApprove.setVisibility(decide ? View.VISIBLE : View.GONE);
        b.buttonReject.setVisibility(decide ? View.VISIBLE : View.GONE);
        b.buttonPaid.setVisibility(markPaid ? View.VISIBLE : View.GONE);
        b.layoutActions.setVisibility(decide || markPaid ? View.VISIBLE : View.GONE);

        b.buttonApprove.setOnClickListener(v -> run(
                ApiClient.get(requireContext()).approveBooking(booking.id), R.string.admin_bookings_approved));
        b.buttonPaid.setOnClickListener(v -> run(
                ApiClient.get(requireContext()).markBookingPaid(booking.id), R.string.admin_bookings_marked_paid));
        b.buttonReject.setOnClickListener(v -> new AdminDialogs.TextDialog(requireContext())
                .title(getString(R.string.admin_bookings_reject_title))
                .prompt(getString(R.string.admin_reason_prompt))
                .placeholder(getString(R.string.admin_bookings_reject_placeholder))
                .required(R.string.admin_reason_required)
                .positive(getString(R.string.admin_confirm_reject), true)
                .show((dialog, reason) -> reject(dialog, booking, reason)));
    }

    private void reject(AlertDialog dialog, Booking booking, String reason) {
        AdminDialogs.setBusy(dialog, true);
        ApiClient.get(requireContext())
                .rejectBooking(booking.id, new ApiRequests.ProcessRequest(reason))
                .enqueue(new ApiCallback<Booking>(this) {
                    @Override
                    protected void onSuccess(Booking body) {
                        dialog.dismiss();
                        toast(R.string.admin_bookings_rejected);
                        load();
                    }

                    @Override
                    protected void onError(String message) {
                        AdminDialogs.setBusy(dialog, false);
                        super.onError(message);
                    }
                });
    }

    private void run(Call<Booking> call, @StringRes int success) {
        setLoading(true);
        call.enqueue(new ApiCallback<Booking>(this) {
            @Override
            protected void onSuccess(Booking body) {
                toast(success);
                load();
            }

            @Override
            protected void onError(String message) {
                setLoading(false);
                super.onError(message);
            }
        });
    }
}
