package com.nhom2.appbooking.ui;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nhom2.appbooking.BuildConfig;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.util.ImageUrls;

/** Tải ảnh qua mạng bằng Glide. */
public final class ImageLoader {

    private ImageLoader() {
    }

    /** Ảnh khách sạn / phòng: cắt vừa khung. Đường dẫn tương đối được ghép với địa chỉ backend. */
    public static void load(ImageView view, String url) {
        Glide.with(view)
                .load(ImageUrls.resolve(url, BuildConfig.API_BASE_URL))
                .placeholder(R.drawable.bg_placeholder)
                .error(R.drawable.bg_placeholder)
                .centerCrop()
                .into(view);
    }

    /** Ảnh đại diện: cắt tròn như avatar trên web. */
    public static void loadAvatar(ImageView view, String url) {
        Glide.with(view)
                .load(ImageUrls.resolve(url, BuildConfig.API_BASE_URL))
                .circleCrop()
                .into(view);
    }

    /** Ảnh cần giữ nguyên tỉ lệ (mã QR, logo ngân hàng). */
    public static void loadFit(ImageView view, String url) {
        Glide.with(view)
                .load(url)
                .fitCenter()
                .into(view);
    }
}
