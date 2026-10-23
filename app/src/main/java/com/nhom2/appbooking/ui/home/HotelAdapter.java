package com.nhom2.appbooking.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.databinding.ItemHotelBinding;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.util.Formatters;
import com.nhom2.appbooking.util.RatingLevel;

import java.util.ArrayList;
import java.util.List;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.Holder> {

    public interface OnHotelClick {
        void onClick(Hotel hotel);
    }

    /** Nút tim trên ảnh, như thẻ khách sạn trên web; màn hình nào không cần thì truyền null. */
    public interface Wishlist {
        boolean isWished(long hotelId);

        void toggle(Hotel hotel);
    }

    private final List<Hotel> items = new ArrayList<>();
    private final OnHotelClick listener;
    private final Wishlist wishlist;

    public HotelAdapter(OnHotelClick listener, Wishlist wishlist) {
        this.listener = listener;
        this.wishlist = wishlist;
    }

    /** Vẽ lại các nút tim sau khi danh sách yêu thích đổi. */
    @SuppressLint("NotifyDataSetChanged")
    public void refreshWishes() {
        notifyDataSetChanged();
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
        String location = location(hotel);
        b.textLocation.setText(location.isEmpty() ? context.getString(R.string.location_pending) : location);

        String rating = Formatters.rating(hotel.ratingAvg);
        b.textRating.setText(rating == null ? context.getString(R.string.rating_na) : rating);
        b.textRatingWord.setText(ratingWord(hotel.ratingAvg));
        long reviews = hotel.reviewCount == null ? 0 : hotel.reviewCount;
        b.textReviews.setText(context.getString(R.string.reviews_count, (int) reviews));

        Double minPrice = hotel.minPrice();
        b.textPrice.setText(minPrice == null ? context.getString(R.string.contact_for_price) : Formatters.money(minPrice));

        ImageLoader.load(b.imageHotel, hotel.coverImage());
        b.getRoot().setOnClickListener(v -> listener.onClick(hotel));

        b.buttonWish.setVisibility(wishlist == null ? View.GONE : View.VISIBLE);
        if (wishlist != null) {
            showWish(b.buttonWish, wishlist.isWished(hotel.id));
            b.buttonWish.setOnClickListener(v -> wishlist.toggle(hotel));
        }
    }

    /** Tim đỏ khi đã lưu, tim viền xám khi chưa (màu như trên web). */
    public static void showWish(ImageView button, boolean wished) {
        Context context = button.getContext();
        button.setImageResource(wished ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        button.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(context, wished ? R.color.wish_active : R.color.wish_inactive)));
        button.setContentDescription(context.getString(wished ? R.string.wishlist_remove : R.string.wishlist_add));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Nhãn của điểm đánh giá ("Tốt", "Ổn"...), giống thẻ khách sạn trên web. */
    @StringRes
    public static int ratingWord(Double score) {
        switch (RatingLevel.of(score)) {
            case EXCEPTIONAL:
                return R.string.rating_exceptional;
            case VERY_GOOD:
                return R.string.rating_very_good;
            case GOOD:
                return R.string.rating_good;
            case OKAY:
                return R.string.rating_okay;
            case AVERAGE:
                return R.string.rating_average;
            default:
                return R.string.rating_none;
        }
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
