package com.nhom2.appbooking.ui.hotel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.data.model.Review;
import com.nhom2.appbooking.data.model.ReviewPage;
import com.nhom2.appbooking.databinding.ActivityHotelDetailBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.booking.BookingActivity;
import com.nhom2.appbooking.ui.home.HotelAdapter;
import com.nhom2.appbooking.util.Formatters;

import java.util.Collections;
import java.util.List;

/** Chi tiết khách sạn: ảnh, mô tả, phòng để đặt và đánh giá của khách. */
public class HotelDetailActivity extends AppCompatActivity {

    private static final String EXTRA_HOTEL_ID = "hotel_id";
    private static final int REVIEW_PAGE_SIZE = 10;

    private ActivityHotelDetailBinding binding;
    private final ImageAdapter imageAdapter = new ImageAdapter();
    private final ReviewAdapter reviewAdapter = new ReviewAdapter();
    private RoomAdapter roomAdapter;
    private long hotelId;
    private Hotel hotel;

    public static void start(Context context, long hotelId) {
        context.startActivity(new Intent(context, HotelDetailActivity.class).putExtra(EXTRA_HOTEL_ID, hotelId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHotelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        hotelId = getIntent().getLongExtra(EXTRA_HOTEL_ID, -1L);

        binding.recyclerImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerImages.setAdapter(imageAdapter);

        roomAdapter = new RoomAdapter(room -> {
            if (hotel != null) {
                BookingActivity.start(this, hotel, room);
            }
        });
        binding.recyclerRooms.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerRooms.setAdapter(roomAdapter);

        binding.recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReviews.setAdapter(reviewAdapter);

        loadHotel();
        loadReviews();
    }

    private void loadHotel() {
        ApiClient.get(this).getHotel(hotelId).enqueue(new ApiCallback<Hotel>(this) {
            @Override
            protected void onSuccess(Hotel body) {
                if (body == null) {
                    onError(getString(R.string.error_generic));
                    return;
                }
                hotel = body;
                bind(body);
            }

            @Override
            protected void onError(String message) {
                binding.progress.setVisibility(View.GONE);
                super.onError(message);
            }
        });
    }

    private void bind(Hotel h) {
        binding.toolbar.setTitle(h.name);
        binding.textName.setText(h.name);
        binding.textAddress.setText(HotelAdapter.location(h));

        String rating = Formatters.rating(h.ratingAvg);
        binding.textRating.setVisibility(rating == null ? View.GONE : View.VISIBLE);
        binding.textRating.setText(rating);
        long reviews = h.reviewCount == null ? 0 : h.reviewCount;
        binding.textReviewCount.setText(getString(R.string.reviews_count, (int) reviews));
        binding.textDescription.setText(h.description);

        imageAdapter.submit(h.allImages());
        roomAdapter.submit(h.roomsOrEmpty());

        binding.progress.setVisibility(View.GONE);
        binding.scroll.setVisibility(View.VISIBLE);
    }

    private void loadReviews() {
        ApiClient.get(this).getReviews(hotelId, 0, REVIEW_PAGE_SIZE).enqueue(new ApiCallback<ReviewPage>(this) {
            @Override
            protected void onSuccess(ReviewPage page) {
                List<Review> reviews = page == null || page.content == null ? Collections.emptyList() : page.content;
                reviewAdapter.submit(reviews);
                binding.textNoReviews.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            protected void onError(String message) {
                // Không tải được đánh giá thì vẫn xem và đặt phòng được
                binding.textNoReviews.setVisibility(View.VISIBLE);
            }
        });
    }
}
