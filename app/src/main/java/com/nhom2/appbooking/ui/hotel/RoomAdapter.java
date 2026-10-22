package com.nhom2.appbooking.ui.hotel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.model.Room;
import com.nhom2.appbooking.databinding.ItemRoomBinding;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.util.Formatters;

import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.Holder> {

    public interface OnBook {
        void onBook(Room room);
    }

    private final List<Room> rooms = new ArrayList<>();
    private final OnBook listener;

    public RoomAdapter(OnBook listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submit(List<Room> items) {
        rooms.clear();
        rooms.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemRoomBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Room room = rooms.get(position);
        ItemRoomBinding b = holder.binding;
        Context context = b.getRoot().getContext();

        b.textRoomName.setText(room.name);
        String type = room.type == null ? "" : room.type;
        int capacity = room.capacity == null ? 0 : room.capacity;
        b.textRoomInfo.setText(context.getString(R.string.room_info, type, capacity));
        b.textRoomPrice.setText(context.getString(R.string.per_night, Formatters.money(room.pricePerNight)));
        ImageLoader.load(b.imageRoom, room.firstImage());
        b.buttonBook.setEnabled(room.pricePerNight != null);
        b.buttonBook.setOnClickListener(v -> listener.onBook(room));
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemRoomBinding binding;

        Holder(ItemRoomBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
