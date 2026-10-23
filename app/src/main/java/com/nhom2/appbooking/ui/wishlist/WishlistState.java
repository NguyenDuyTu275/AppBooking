package com.nhom2.appbooking.ui.wishlist;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.ui.ApiCallback;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;

/**
 * Danh sách khách sạn đã lưu của một màn hình. Mỗi API yêu thích của backend trả về toàn bộ danh sách mới nhất,
 * nên mỗi màn hình giữ bản riêng thay vì dùng chung một biến tĩnh.
 */
public final class WishlistState {

    public interface Changed {
        void onChanged();
    }

    private final Set<Long> ids = new LinkedHashSet<>();
    private final Fragment fragment;
    private final Activity activity;

    private WishlistState(Fragment fragment, Activity activity) {
        this.fragment = fragment;
        this.activity = activity;
    }

    public static WishlistState forFragment(Fragment fragment) {
        return new WishlistState(fragment, null);
    }

    public static WishlistState forActivity(Activity activity) {
        return new WishlistState(null, activity);
    }

    public boolean contains(long hotelId) {
        return ids.contains(hotelId);
    }

    public int size() {
        return ids.size();
    }

    /** Id đã lưu, mới nhất trước (thứ tự backend trả về). */
    public List<Long> ids() {
        return new ArrayList<>(ids);
    }

    public void load(Changed changed) {
        enqueue(ApiClient.get(context()).getWishlist(), changed);
    }

    public void toggle(long hotelId, Changed changed) {
        enqueue(contains(hotelId)
                ? ApiClient.get(context()).removeFromWishlist(hotelId)
                : ApiClient.get(context()).addToWishlist(hotelId), changed);
    }

    private void enqueue(Call<List<Long>> call, Changed changed) {
        call.enqueue(fragment != null ? new IdsCallback(fragment, changed) : new IdsCallback(activity, changed));
    }

    private Context context() {
        return fragment != null ? fragment.requireContext() : activity;
    }

    private final class IdsCallback extends ApiCallback<List<Long>> {
        private final Changed changed;

        IdsCallback(Fragment owner, Changed changed) {
            super(owner);
            this.changed = changed;
        }

        IdsCallback(Activity owner, Changed changed) {
            super(owner);
            this.changed = changed;
        }

        @Override
        protected void onSuccess(List<Long> newIds) {
            ids.clear();
            if (newIds != null) {
                ids.addAll(newIds);
            }
            changed.onChanged();
        }
    }
}
