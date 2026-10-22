package com.nhom2.appbooking.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.databinding.FragmentHomeBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.ui.hotel.HotelDetailActivity;
import com.nhom2.appbooking.util.TextSearch;

import java.util.ArrayList;
import java.util.List;

/** Tab "Khách sạn": danh sách khách sạn, tìm theo tên / thành phố. */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HotelAdapter adapter;
    private final List<Hotel> allHotels = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        adapter = new HotelAdapter(hotel -> HotelDetailActivity.start(requireContext(), hotel.id));
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recycler.setAdapter(adapter);

        binding.swipe.setColorSchemeResources(R.color.primary);
        binding.swipe.setOnRefreshListener(this::load);
        binding.editSearch.addTextChangedListener(UiUtils.afterTextChanged(text -> applyFilter()));

        load();
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
                applyFilter();
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
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
