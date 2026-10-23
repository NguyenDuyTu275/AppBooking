package com.nhom2.appbooking.ui.bookings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.databinding.ItemBookingBinding;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.Holder> {

    public interface Listener {
        void onPay(Booking booking);

        void onCancel(Booking booking);

        void onInvoice(Booking booking);
    }

    private final List<Booking> bookings = new ArrayList<>();
    private final Listener listener;

    public BookingAdapter(Listener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<Booking> items) {
        bookings.clear();
        bookings.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemBookingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Booking booking = bookings.get(position);
        ItemBookingBinding b = holder.binding;
        Context context = b.getRoot().getContext();

        b.textHotel.setText(booking.hotel == null ? "" : booking.hotel.name);
        b.textDates.setText(context.getString(R.string.date_range,
                Formatters.date(booking.checkInDate), Formatters.date(booking.checkOutDate)));
        b.textCode.setText(context.getString(R.string.booking_code, booking.code()));
        b.textTotal.setText(Formatters.money(booking.totalPrice));
        ImageLoader.load(b.imageHotel, booking.hotel == null ? null : booking.hotel.coverImage());

        b.textStatus.setText(BookingStatusUi.statusLabel(booking.status));
        b.textStatus.setBackgroundResource(BookingStatusUi.statusBackground(booking.status));
        b.textStatus.setTextColor(ContextCompat.getColor(context, BookingStatusUi.statusColor(booking.status)));
        b.textPayment.setText(BookingStatusUi.paymentLabel(booking));
        b.textPayment.setBackgroundResource(BookingStatusUi.paymentBackground(booking));
        b.textPayment.setTextColor(ContextCompat.getColor(context, BookingStatusUi.paymentColor(booking)));

        // Đơn chưa trả tiền: thanh toán hoặc hủy (backend quyết định có cho hủy hay không)
        boolean payable = booking.canPay();
        // Đơn đã thanh toán: tải hóa đơn PDF như nút "Hóa đơn" trên web
        boolean paid = booking.isPaid();
        b.layoutActions.setVisibility(payable || paid ? View.VISIBLE : View.GONE);
        b.buttonPay.setVisibility(payable ? View.VISIBLE : View.GONE);
        b.buttonCancel.setVisibility(payable ? View.VISIBLE : View.GONE);
        b.buttonInvoice.setVisibility(paid ? View.VISIBLE : View.GONE);
        b.buttonPay.setOnClickListener(v -> listener.onPay(booking));
        b.buttonCancel.setOnClickListener(v -> listener.onCancel(booking));
        b.buttonInvoice.setOnClickListener(v -> listener.onInvoice(booking));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemBookingBinding binding;

        Holder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
