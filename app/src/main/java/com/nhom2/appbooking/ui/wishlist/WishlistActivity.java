package com.nhom2.appbooking.ui.wishlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.databinding.ActivityWishlistBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.ui.home.HotelAdapter;
import com.nhom2.appbooking.ui.hotel.HotelDetailActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** "Danh sách yêu thích": khách sạn đã lưu, mới lưu trước; bỏ tim hoặc xóa tất cả (như /wishlist trên web). */
public class WishlistActivity extends AppCompatActivity {

    private ActivityWishlistBinding binding;
    private final WishlistState wishlist = WishlistState.forActivity(this);
    private final Map<Long, Hotel> hotelsById = new HashMap<>();
    private HotelAdapter adapter;

    public static void start(Context context) {
        context.startActivity(new Intent(context, WishlistActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWishlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new HotelAdapter(hotel -> HotelDetailActivity.start(this, hotel.id), new HotelAdapter.Wishlist() {
            @Override
            public boolean isWished(long hotelId) {
                return wishlist.contains(hotelId);
            }

            @Override
            public void toggle(Hotel hotel) {
                wishlist.toggle(hotel.id, WishlistActivity.this::show);
            }
        });
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
        binding.swipe.setColorSchemeResources(R.color.primary);
        binding.swipe.setOnRefreshListener(this::load);
        binding.buttonClear.setOnClickListener(v -> confirmClear());
        binding.buttonExplore.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    /** Backend chỉ trả về id đã lưu, nên ghép với danh sách khách sạn như web. */
    private void load() {
        binding.swipe.setRefreshing(true);
        ApiClient.get(this).getHotels().enqueue(new ApiCallback<List<Hotel>>(this) {
            @Override
            protected void onSuccess(List<Hotel> hotels) {
                // Lỗi khi tải danh sách id đã có ApiCallback báo, không để vòng xoay quay mãi
                binding.swipe.setRefreshing(false);
                hotelsById.clear();
                if (hotels != null) {
                    for (Hotel hotel : hotels) {
                        hotelsById.put(hotel.id, hotel);
                    }
                }
                wishlist.load(() -> {
                    show();
                    UiUtils.animateListIn(binding.recycler);
                });
            }

            @Override
            protected void onError(String message) {
                binding.swipe.setRefreshing(false);
                super.onError(message);
            }
        });
    }

    private void show() {
        binding.swipe.setRefreshing(false);
        List<Hotel> saved = new ArrayList<>();
        for (long id : wishlist.ids()) {
            Hotel hotel = hotelsById.get(id);
            if (hotel != null) {
                saved.add(hotel);
            }
        }
        adapter.submit(saved);
        boolean empty = saved.isEmpty();
        binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.layoutHeader.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.textCount.setText(getString(R.string.wishlist_saved_count, saved.size()));
    }

    private void confirmClear() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.wishlist_clear_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.wishlist_clear_all, (dialog, which) -> clear())
                .show();
    }

    private void clear() {
        ApiClient.get(this).clearWishlist().enqueue(new ApiCallback<Void>(this) {
            @Override
            protected void onSuccess(Void body) {
                wishlist.load(WishlistActivity.this::show);
            }
        });
    }
}
