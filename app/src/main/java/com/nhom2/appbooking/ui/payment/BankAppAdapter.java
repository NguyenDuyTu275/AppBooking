package com.nhom2.appbooking.ui.payment;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.BankApp;
import com.nhom2.appbooking.databinding.ItemBankAppBinding;
import com.nhom2.appbooking.ui.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Lưới app ngân hàng trong popup chọn ngân hàng. */
public class BankAppAdapter extends RecyclerView.Adapter<BankAppAdapter.Holder> {

    public interface OnSelect {
        void onSelect(BankApp app);
    }

    private final List<BankApp> apps = new ArrayList<>();
    private final OnSelect listener;

    public BankAppAdapter(OnSelect listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<BankApp> items) {
        apps.clear();
        apps.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemBankAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        BankApp app = apps.get(position);
        ItemBankAppBinding b = holder.binding;

        b.textName.setText(app.appName);
        ImageLoader.loadFit(b.imageLogo, app.appLogo);
        if (app.isAutofill()) {
            b.textTag.setText(R.string.autofill);
            b.textTag.setBackgroundResource(R.drawable.bg_tag_green);
            b.textTag.setTextColor(ContextCompat.getColor(b.getRoot().getContext(), R.color.success));
        } else {
            b.textTag.setText(R.string.open_only);
            b.textTag.setBackgroundResource(R.drawable.bg_tag_grey);
            b.textTag.setTextColor(ContextCompat.getColor(b.getRoot().getContext(), R.color.text_secondary));
        }
        b.getRoot().setOnClickListener(v -> listener.onSelect(app));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemBankAppBinding binding;

        Holder(ItemBankAppBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
