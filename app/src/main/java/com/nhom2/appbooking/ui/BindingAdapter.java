package com.nhom2.appbooking.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.util.ArrayList;
import java.util.List;

/** Adapter danh sách dùng View Binding: chỉ cần truyền cách inflate layout và cách gắn dữ liệu cho một dòng. */
public class BindingAdapter<T, B extends ViewBinding> extends RecyclerView.Adapter<BindingAdapter.Holder<B>> {

    public interface Inflater<B> {
        B inflate(LayoutInflater inflater, ViewGroup parent, boolean attachToParent);
    }

    public interface Binder<T, B> {
        void bind(B binding, T item);
    }

    private final List<T> items = new ArrayList<>();
    private final Inflater<B> inflater;
    private final Binder<T, B> binder;

    public BindingAdapter(Inflater<B> inflater, Binder<T, B> binder) {
        this.inflater = inflater;
        this.binder = binder;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<T> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    /** Thêm vào cuối (tải trang tiếp theo). */
    public void append(List<T> more) {
        if (more == null || more.isEmpty()) {
            return;
        }
        int start = items.size();
        items.addAll(more);
        notifyItemRangeInserted(start, more.size());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @NonNull
    @Override
    public Holder<B> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder<>(inflater.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder<B> holder, int position) {
        binder.bind(holder.binding, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class Holder<B extends ViewBinding> extends RecyclerView.ViewHolder {
        final B binding;

        Holder(B binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
