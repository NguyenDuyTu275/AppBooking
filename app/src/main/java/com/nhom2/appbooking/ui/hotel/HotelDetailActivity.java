package com.nhom2.appbooking.ui.hotel;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.data.model.Review;
import com.nhom2.appbooking.data.model.ReviewEligibility;
import com.nhom2.appbooking.data.model.ReviewPage;
import com.nhom2.appbooking.databinding.ActivityHotelDetailBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.ui.booking.BookingActivity;
import com.nhom2.appbooking.ui.home.HotelAdapter;
import com.nhom2.appbooking.ui.wishlist.WishlistState;
import com.nhom2.appbooking.util.Formatters;
import com.nhom2.appbooking.util.MapLinks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Chi tiết khách sạn: ảnh, mô tả, phòng để đặt, lưu yêu thích, xem và viết đánh giá. */
public class HotelDetailActivity extends AppCompatActivity {

    private static final String EXTRA_HOTEL_ID = "hotel_id";
    private static final int REVIEW_PAGE_SIZE = 10;
    /** Thang điểm 1-10 như backend (ReviewService.MIN_RATING / MAX_RATING) và 10 ngôi sao trên web. */
    private static final int MAX_SCORE = 10;
    private static final float STAR_TEXT_SP = 26f;
    private static final String STAR = "★";

    private ActivityHotelDetailBinding binding;
    private final ImageAdapter imageAdapter = new ImageAdapter();
    private final ReviewAdapter reviewAdapter = new ReviewAdapter();
    private final WishlistState wishlist = WishlistState.forActivity(this);
    private final List<TextView> stars = new ArrayList<>();
    private RoomAdapter roomAdapter;
    private long hotelId;
    private Hotel hotel;
    private int selectedScore;

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

        binding.toolbar.inflateMenu(R.menu.hotel_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_wish) {
                wishlist.toggle(hotelId, this::showWish);
                return true;
            }
            return false;
        });
        showWish();
        setUpMap();
        buildStars();
        binding.reviewForm.buttonSubmit.setOnClickListener(v -> submitReview());

        loadHotel();
        loadReviews();
        loadEligibility();
        wishlist.load(this::showWish);
    }

    /**
     * Ô bản đồ như trang chi tiết của web: nhúng trang Google Maps "output=embed" (không cần API key) và chỉ để xem;
     * bấm vào ô hoặc vào địa chỉ thì mở Google Maps.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void setUpMap() {
        WebSettings settings = binding.webMap.getSettings();
        // Trang bản đồ nhúng của Google cần JavaScript để vẽ bản đồ
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        binding.webMap.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Chỉ hiện đúng trang nhúng: không cho chuyển sang trang khác trong ô nhỏ này
                return true;
            }
        });
        binding.mapOverlay.setOnClickListener(v -> openMap());
        binding.buttonShowMap.setOnClickListener(v -> openMap());
        binding.textAddress.setOnClickListener(v -> openMap());
    }

    private String mapQuery() {
        return hotel == null ? "" : MapLinks.query(hotel.name, hotel.address, hotel.city);
    }

    private void openMap() {
        String query = mapQuery();
        if (query.isEmpty()) {
            return;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MapLinks.searchUrl(query))));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show();
        }
    }

    private void showWish() {
        boolean wished = wishlist.contains(hotelId);
        MenuItem item = binding.toolbar.getMenu().findItem(R.id.action_wish);
        item.setIcon(wished ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        item.setTitle(wished ? R.string.wishlist_remove : R.string.wishlist_add);
        // Tim đỏ khi đã lưu như trên web; chưa lưu thì trắng cho nổi trên thanh xanh
        item.setIconTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, wished ? R.color.wish_active : R.color.white)));
    }

    /** 10 ngôi sao bấm được, như StarRating trên web. */
    private void buildStars() {
        binding.reviewForm.layoutStars.removeAllViews();
        stars.clear();
        for (int score = 1; score <= MAX_SCORE; score++) {
            TextView star = new TextView(this);
            star.setText(STAR);
            star.setTextSize(STAR_TEXT_SP);
            star.setGravity(Gravity.CENTER);
            star.setContentDescription(getString(R.string.review_star, score));
            int value = score;
            star.setOnClickListener(v -> selectScore(value));
            binding.reviewForm.layoutStars.addView(star,
                    new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            stars.add(star);
        }
        selectScore(0);
    }

    private void selectScore(int score) {
        selectedScore = score;
        for (int i = 0; i < stars.size(); i++) {
            stars.get(i).setTextColor(ContextCompat.getColor(this, i < score ? R.color.accent : R.color.star_empty));
        }
        binding.reviewForm.textSelected.setVisibility(score > 0 ? View.VISIBLE : View.GONE);
        binding.reviewForm.textSelected.setText(score > 0
                ? getString(R.string.review_selected_score, score, getString(HotelAdapter.ratingWord((double) score)))
                : null);
    }

    /** Chỉ khách đã đặt và thanh toán mới được đánh giá; backend trả lý do khi chưa được. */
    private void loadEligibility() {
        ApiClient.get(this).getReviewEligibility(hotelId).enqueue(new ApiCallback<ReviewEligibility>(this) {
            @Override
            protected void onSuccess(ReviewEligibility eligibility) {
                boolean canReview = eligibility != null && eligibility.canReview;
                boolean hasReason = eligibility != null && eligibility.reason != null && !eligibility.reason.isEmpty();
                binding.reviewForm.layoutForm.setVisibility(canReview ? View.VISIBLE : View.GONE);
                binding.reviewForm.textBlocked.setVisibility(!canReview && hasReason ? View.VISIBLE : View.GONE);
                binding.reviewForm.textBlocked.setText(hasReason ? eligibility.reason : null);
            }

            @Override
            protected void onError(String message) {
                // Không biết có được đánh giá hay không thì ẩn form; xem và đặt phòng vẫn bình thường
                binding.reviewForm.layoutForm.setVisibility(View.GONE);
            }
        });
    }

    private void submitReview() {
        String comment = UiUtils.text(binding.reviewForm.editComment);
        if (selectedScore == 0) {
            Toast.makeText(this, R.string.review_pick_rating, Toast.LENGTH_SHORT).show();
            return;
        }
        if (comment.isEmpty()) {
            Toast.makeText(this, R.string.review_enter_comment, Toast.LENGTH_SHORT).show();
            return;
        }
        binding.reviewForm.buttonSubmit.setEnabled(false);
        ApiClient.get(this).createReview(new ApiRequests.CreateReview(hotelId, selectedScore, comment))
                .enqueue(new ApiCallback<Review>(this) {
                    @Override
                    protected void onSuccess(Review body) {
                        binding.reviewForm.buttonSubmit.setEnabled(true);
                        Toast.makeText(HotelDetailActivity.this, R.string.review_submitted, Toast.LENGTH_LONG).show();
                        binding.reviewForm.editComment.setText("");
                        selectScore(0);
                        // Điểm trung bình của khách sạn vừa đổi: tải lại cả khách sạn và danh sách đánh giá
                        loadHotel();
                        loadReviews();
                        loadEligibility();
                    }

                    @Override
                    protected void onError(String message) {
                        binding.reviewForm.buttonSubmit.setEnabled(true);
                        super.onError(message);
                    }
                });
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
        binding.textRating.setText(rating == null ? getString(R.string.rating_na) : rating);
        binding.textRatingWord.setText(HotelAdapter.ratingWord(h.ratingAvg));
        long reviews = h.reviewCount == null ? 0 : h.reviewCount;
        binding.textReviewCount.setText(getString(R.string.reviews_count, (int) reviews));
        binding.textDescription.setText(h.description);

        String query = mapQuery();
        binding.cardMap.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
        String mapUrl = MapLinks.embedUrl(query);
        // Tải lại khách sạn (vd sau khi gửi đánh giá) thì không tải lại bản đồ nếu địa chỉ không đổi
        if (!query.isEmpty() && !mapUrl.equals(binding.webMap.getUrl())) {
            binding.webMap.loadUrl(mapUrl);
        }

        imageAdapter.submit(h.allImages());
        roomAdapter.submit(h.roomsOrEmpty());

        binding.progress.setVisibility(View.GONE);
        UiUtils.fadeIn(binding.scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.webMap.onResume();
    }

    @Override
    protected void onPause() {
        binding.webMap.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        binding.webMap.destroy();
        super.onDestroy();
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
