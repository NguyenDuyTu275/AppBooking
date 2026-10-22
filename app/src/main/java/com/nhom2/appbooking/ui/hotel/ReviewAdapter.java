package com.nhom2.appbooking.ui.hotel;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.Review;
import com.nhom2.appbooking.databinding.ItemReviewBinding;
import com.nhom2.appbooking.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.Holder> {

    private final List<Review> reviews = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<Review> items) {
        reviews.clear();
        reviews.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Review review = reviews.get(position);
        ItemReviewBinding b = holder.binding;

        b.textUsername.setText(review.username);
        b.textStars.setText(Formatters.stars(review.rating));
        b.textComment.setText(review.comment);
        b.textDate.setText(Formatters.date(review.createdAt));

        boolean hasReply = review.hotelReply != null && !review.hotelReply.trim().isEmpty();
        b.textReply.setVisibility(hasReply ? View.VISIBLE : View.GONE);
        if (hasReply) {
            b.textReply.setText(b.getRoot().getContext().getString(R.string.hotel_reply, review.hotelReply));
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemReviewBinding binding;

        Holder(ItemReviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
