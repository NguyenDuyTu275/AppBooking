package com.nhom2.appbooking.ui.hotel;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.databinding.ItemImageBinding;
import com.nhom2.appbooking.ui.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Dải ảnh vuốt ngang ở đầu trang khách sạn. */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Holder> {

    private final List<String> urls = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<String> images) {
        urls.clear();
        urls.addAll(images);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ImageLoader.load(holder.binding.image, urls.get(position));
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemImageBinding binding;

        Holder(ItemImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
