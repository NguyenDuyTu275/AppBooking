package com.nhom2.appbooking.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.databinding.ItemHotelBinding;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.Holder> {

    public interface OnHotelClick {
        void onClick(Hotel hotel);
    }

    private final List<Hotel> items = new ArrayList<>();
    private final OnHotelClick listener;

    public HotelAdapter(OnHotelClick listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<Hotel> hotels) {
        items.clear();
        items.addAll(hotels);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemHotelBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Hotel hotel = items.get(position);
        ItemHotelBinding b = holder.binding;
        Context context = b.getRoot().getContext();

        b.textName.setText(hotel.name);
        b.textLocation.setText(location(hotel));

        String rating = Formatters.rating(hotel.ratingAvg);
        b.textRating.setVisibility(rating == null ? View.GONE : View.VISIBLE);
        b.textRating.setText(rating);
        long reviews = hotel.reviewCount == null ? 0 : hotel.reviewCount;
        b.textReviews.setText(rating == null && reviews == 0
                ? context.getString(R.string.no_rating)
                : context.getString(R.string.reviews_count, (int) reviews));

        Double minPrice = hotel.minPrice();
        b.textPrice.setText(minPrice == null ? "" : context.getString(R.string.price_from, Formatters.money(minPrice)));

        ImageLoader.load(b.imageHotel, hotel.coverImage());
        b.getRoot().setOnClickListener(v -> listener.onClick(hotel));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** "Địa chỉ, Thành phố" bỏ qua phần bị thiếu. */
    public static String location(Hotel hotel) {
        if (hotel.address == null || hotel.address.isEmpty()) {
            return hotel.city == null ? "" : hotel.city;
        }
        return hotel.city == null || hotel.city.isEmpty() ? hotel.address : hotel.address + ", " + hotel.city;
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemHotelBinding binding;

        Holder(ItemHotelBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
