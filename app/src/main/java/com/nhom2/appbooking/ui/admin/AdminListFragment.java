package com.nhom2.appbooking.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.databinding.FragmentAdminListBinding;
import com.nhom2.appbooking.databinding.ItemFilterChipBinding;

import java.util.function.Consumer;

/** Khung chung cho các mục quản trị dạng danh sách: tiêu đề, nút thêm / bộ lọc, kéo để tải lại, thông báo trống. */
public abstract class AdminListFragment extends Fragment {

    protected FragmentAdminListBinding binding;

    @StringRes
    protected abstract int titleRes();

    /** Gắn adapter, nút và bộ lọc; gọi một lần khi tạo giao diện. */
    protected abstract void setUp();

    protected abstract void load();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.textTitle.setText(titleRes());
        binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.swipe.setColorSchemeResources(R.color.primary);
        binding.swipe.setOnRefreshListener(this::load);
        setUp();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại mỗi lần quay về mục này, vd sau khi sửa phòng ở màn khác
        load();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected void setLoading(boolean loading) {
        if (binding != null) {
            binding.swipe.setRefreshing(loading);
        }
    }

    protected void showEmpty(boolean empty, @StringRes int message) {
        if (binding != null) {
            binding.textEmpty.setText(message);
            binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        }
    }

    protected void showAddButton(@StringRes int label, View.OnClickListener listener) {
        binding.buttonAdd.setText(label);
        binding.buttonAdd.setOnClickListener(listener);
        binding.buttonAdd.setVisibility(View.VISIBLE);
    }

    /**
     * Bộ lọc trạng thái như ô Select trên web; lựa chọn đầu tiên là "Tất cả" (giá trị null).
     *
     * @param values giá trị gửi lên backend, cùng thứ tự với {@code labels}; values[0] là null
     */
    protected void showFilters(int[] labels, String[] values, Consumer<String> onSelected) {
        binding.chipFilters.removeAllViews();
        for (int i = 0; i < labels.length; i++) {
            Chip chip = ItemFilterChipBinding.inflate(getLayoutInflater(), binding.chipFilters, false).getRoot();
            chip.setId(View.generateViewId());
            chip.setText(labels[i]);
            binding.chipFilters.addView(chip);
            // Chọn sẵn "Tất cả" trước khi gắn listener: lần tải đầu do onResume lo, không tải hai lần
            chip.setChecked(i == 0);
            String value = values[i];
            chip.setOnCheckedChangeListener((button, checked) -> {
                if (checked) {
                    onSelected.accept(value);
                }
            });
        }
        binding.scrollFilters.setVisibility(View.VISIBLE);
    }

    protected void showSwitch(@StringRes int label, boolean checked, Consumer<Boolean> onChanged) {
        binding.switchOption.setText(label);
        binding.switchOption.setChecked(checked);
        binding.switchOption.setOnCheckedChangeListener((button, isChecked) -> onChanged.accept(isChecked));
        binding.switchOption.setVisibility(View.VISIBLE);
    }

    protected void toast(@StringRes int message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
