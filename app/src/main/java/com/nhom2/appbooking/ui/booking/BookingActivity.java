package com.nhom2.appbooking.ui.booking;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.data.model.Room;
import com.nhom2.appbooking.databinding.ActivityBookingBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.payment.PaymentActivity;
import com.nhom2.appbooking.util.Formatters;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;

/** Chọn ngày nhận / trả phòng rồi tạo đơn đặt phòng. */
public class BookingActivity extends AppCompatActivity {

    private static final String EXTRA_HOTEL_ID = "hotel_id";
    private static final String EXTRA_HOTEL_NAME = "hotel_name";
    private static final String EXTRA_ROOM_ID = "room_id";
    private static final String EXTRA_ROOM_NAME = "room_name";
    private static final String EXTRA_PRICE = "price";

    private ActivityBookingBinding binding;
    private long hotelId;
    private long roomId;
    private double pricePerNight;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public static void start(Context context, Hotel hotel, Room room) {
        Intent intent = new Intent(context, BookingActivity.class)
                .putExtra(EXTRA_HOTEL_ID, hotel.id)
                .putExtra(EXTRA_HOTEL_NAME, hotel.name)
                .putExtra(EXTRA_ROOM_ID, room.id)
                .putExtra(EXTRA_ROOM_NAME, room.name)
                .putExtra(EXTRA_PRICE, room.pricePerNight == null ? 0d : room.pricePerNight);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        hotelId = intent.getLongExtra(EXTRA_HOTEL_ID, -1L);
        roomId = intent.getLongExtra(EXTRA_ROOM_ID, -1L);
        pricePerNight = intent.getDoubleExtra(EXTRA_PRICE, 0d);

        binding.textHotel.setText(intent.getStringExtra(EXTRA_HOTEL_NAME));
        binding.textRoom.setText(intent.getStringExtra(EXTRA_ROOM_NAME));
        binding.textPrice.setText(getString(R.string.per_night, Formatters.money(pricePerNight)));

        binding.buttonDates.setOnClickListener(v -> pickDates());
        binding.buttonConfirm.setOnClickListener(v -> confirm());
    }

    private void pickDates() {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
                .build();
        MaterialDatePicker<Pair<Long, Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.choose_dates)
                .setCalendarConstraints(constraints)
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            if (selection.first == null || selection.second == null) {
                return;
            }
            LocalDate from = toDate(selection.first);
            LocalDate to = toDate(selection.second);
            if (Formatters.nights(from, to) < 1) {
                Toast.makeText(this, R.string.pick_dates_first, Toast.LENGTH_SHORT).show();
                return;
            }
            checkIn = from;
            checkOut = to;
            updateSummary();
        });
        picker.show(getSupportFragmentManager(), "dates");
    }

    /** Bộ chọn ngày trả về nửa đêm UTC của ngày được chọn. */
    private static LocalDate toDate(long utcMillis) {
        return Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private void updateSummary() {
        long nights = Formatters.nights(checkIn, checkOut);
        binding.textCheckIn.setText(Formatters.date(checkIn));
        binding.textCheckOut.setText(Formatters.date(checkOut));
        binding.textNights.setText(getString(R.string.nights_value, (int) nights));
        binding.textTotal.setText(Formatters.money(nights * pricePerNight));
    }

    private void confirm() {
        if (checkIn == null || checkOut == null) {
            Toast.makeText(this, R.string.pick_dates_first, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        ApiRequests.CreateBooking body = new ApiRequests.CreateBooking(
                checkIn.toString(),
                checkOut.toString(),
                Collections.singletonList(new ApiRequests.RoomItem(roomId, 1)));
        ApiClient.get(this).createBooking(hotelId, body).enqueue(new ApiCallback<Booking>(this) {
            @Override
            protected void onSuccess(Booking booking) {
                if (booking == null) {
                    onError(getString(R.string.error_generic));
                    return;
                }
                Toast.makeText(BookingActivity.this, R.string.booking_created, Toast.LENGTH_SHORT).show();
                PaymentActivity.start(BookingActivity.this, booking.id);
                finish();
            }

            @Override
            protected void onError(String message) {
                setLoading(false);
                super.onError(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.buttonConfirm.setEnabled(!loading);
        binding.buttonDates.setEnabled(!loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
