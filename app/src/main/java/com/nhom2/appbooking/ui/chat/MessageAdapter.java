package com.nhom2.appbooking.ui.chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.SupportMessage;
import com.nhom2.appbooking.databinding.ItemMessageBinding;
import com.nhom2.appbooking.util.Formatters;

import java.util.ArrayList;
import java.util.List;

/** Bong bóng chat: tin của khách bên phải (xanh), tin của nhân viên bên trái (trắng). */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    private final List<SupportMessage> messages = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<SupportMessage> items) {
        messages.clear();
        messages.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SupportMessage message = messages.get(position);
        ItemMessageBinding b = holder.binding;
        Context context = b.getRoot().getContext();
        boolean mine = !message.fromAdmin;

        b.container.setGravity(mine ? Gravity.END : Gravity.START);
        b.bubble.setBackgroundResource(mine ? R.drawable.bg_bubble_me : R.drawable.bg_bubble_admin);
        int textColor = ContextCompat.getColor(context, mine ? R.color.white : R.color.text_primary);
        b.textMessage.setTextColor(textColor);
        b.textTime.setTextColor(ContextCompat.getColor(context, mine ? R.color.placeholder : R.color.text_secondary));
        b.textMessage.setText(message.content);
        b.textTime.setText(Formatters.time(message.createdAt));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemMessageBinding binding;

        Holder(ItemMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
