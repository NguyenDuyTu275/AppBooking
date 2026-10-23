package com.nhom2.appbooking.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.databinding.FragmentHomeBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.MainActivity;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.ui.hotel.HotelDetailActivity;
import com.nhom2.appbooking.ui.wishlist.WishlistState;
import com.nhom2.appbooking.util.TextSearch;

import java.util.ArrayList;
import java.util.List;

/** Tab "Khách sạn": khách sạn nổi bật (điểm cao trước, như trang chủ web), tìm theo tên / thành phố. */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HotelAdapter adapter;
    private final List<Hotel> allHotels = new ArrayList<>();
    private final WishlistState wishlist = WishlistState.forFragment(this);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new HotelAdapter(hotel -> HotelDetailActivity.start(requireContext(), hotel.id),
                new HotelAdapter.Wishlist() {
                    @Override
                    public boolean isWished(long hotelId) {
                        return wishlist.contains(hotelId);
                    }

                    @Override
                    public void toggle(Hotel hotel) {
                        wishlist.toggle(hotel.id, HomeFragment.this::onWishlistChanged);
                    }
                });
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        binding.swipe.setColorSchemeResources(R.color.primary);
        binding.swipe.setOnRefreshListener(this::load);
        binding.editSearch.addTextChangedListener(UiUtils.afterTextChanged(text -> applyFilter()));
        // Danh sách đã lọc theo từng chữ gõ vào; nút "Tìm" chỉ cất bàn phím để xem kết quả
        binding.buttonSearch.setOnClickListener(v -> hideKeyboard());
        binding.editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                return true;
            }
            return false;
        });

        load();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tim có thể đã đổi ở trang chi tiết hoặc trang Yêu thích
        wishlist.load(this::onWishlistChanged);
    }

    private void onWishlistChanged() {
        adapter.refreshWishes();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshBadges();
        }
    }

    private void load() {
        binding.swipe.setRefreshing(true);
        ApiClient.get(requireContext()).getHotels().enqueue(new ApiCallback<List<Hotel>>(this) {
            @Override
            protected void onSuccess(List<Hotel> hotels) {
                binding.swipe.setRefreshing(false);
                allHotels.clear();
                if (hotels != null) {
                    allHotels.addAll(hotels);
                }
                allHotels.sort(Hotel.BY_RATING_DESC);
                applyFilter();
                UiUtils.animateListIn(binding.recycler);
            }

            @Override
            protected void onError(String message) {
                binding.swipe.setRefreshing(false);
                applyFilter();
                super.onError(message);
            }
        });
    }

    private void applyFilter() {
        String query = UiUtils.text(binding.editSearch);
        List<Hotel> shown = new ArrayList<>();
        for (Hotel hotel : allHotels) {
            if (TextSearch.matches(query, hotel.name, hotel.city, hotel.address)) {
                shown.add(hotel);
            }
        }
        adapter.submit(shown);
        binding.textEmpty.setVisibility(shown.isEmpty() && !binding.swipe.isRefreshing() ? View.VISIBLE : View.GONE);
        showSectionHeader(query.isEmpty(), shown.size());
    }

    private void showSectionHeader(boolean featured, int count) {
        if (featured) {
            binding.textSectionTitle.setText(R.string.featured_title);
            binding.textSectionSubtitle.setText(R.string.featured_subtitle);
        } else {
            binding.textSectionTitle.setText(R.string.search_results_title);
            binding.textSectionSubtitle.setText(getString(R.string.search_results_count, count));
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(binding.editSearch.getWindowToken(), 0);
        }
        binding.editSearch.clearFocus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
