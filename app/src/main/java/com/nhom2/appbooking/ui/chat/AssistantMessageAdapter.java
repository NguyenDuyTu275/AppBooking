package com.nhom2.appbooking.ui.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.databinding.ItemAssistantMessageBinding;

import java.util.ArrayList;
import java.util.List;

final class AssistantMessageAdapter extends RecyclerView.Adapter<AssistantMessageAdapter.Holder> {

    private final List<AssistantMessage> messages = new ArrayList<>();

    void add(String text, boolean fromUser) {
        add(text, fromUser, null, null, false);
    }

    void add(String text, boolean fromUser, android.net.Uri attachmentUri,
             String attachmentName, boolean imageAttachment) {
        messages.add(new AssistantMessage(text, fromUser, attachmentUri,
                attachmentName, imageAttachment));
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAssistantMessageBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AssistantMessage message = messages.get(position);
        boolean fromUser = message.fromUser;
        holder.binding.rowMessage.setGravity(fromUser ? Gravity.END : Gravity.START);
        holder.binding.textAvatar.setVisibility(fromUser ? View.GONE : View.VISIBLE);
        holder.binding.messageContent.setBackgroundResource(
                fromUser ? R.drawable.bg_ai_bubble_user : R.drawable.bg_ai_bubble);
        int textColor = ContextCompat.getColor(
                holder.binding.getRoot().getContext(),
                fromUser ? R.color.white : R.color.text_primary);
        holder.binding.textMessage.setTextColor(textColor);
        holder.binding.textMessage.setText(message.text);
        holder.binding.textMessage.setVisibility(message.text.isEmpty() ? View.GONE : View.VISIBLE);
        holder.binding.textAttachment.setVisibility(
                message.attachmentName == null ? View.GONE : View.VISIBLE);
        if (message.attachmentName != null) {
            holder.binding.textAttachment.setText(holder.binding.getRoot().getContext()
                    .getString(R.string.chat_attachment_name, message.attachmentName));
            holder.binding.textAttachment.setTextColor(textColor);
        }
        holder.binding.imageAttachment.setVisibility(
                message.imageAttachment && message.attachmentUri != null ? View.VISIBLE : View.GONE);
        if (message.imageAttachment && message.attachmentUri != null) {
            Glide.with(holder.binding.imageAttachment)
                    .load(message.attachmentUri)
                    .centerCrop()
                    .into(holder.binding.imageAttachment);
        } else {
            Glide.with(holder.binding.imageAttachment).clear(holder.binding.imageAttachment);
        }

        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) holder.binding.messageContent.getLayoutParams();
        params.setMargins(fromUser ? 0 : dp(holder, 8), 0, 0, 0);
        holder.binding.messageContent.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private int dp(Holder holder, int value) {
        return (int) (value * holder.binding.getRoot().getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemAssistantMessageBinding binding;

        Holder(ItemAssistantMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
