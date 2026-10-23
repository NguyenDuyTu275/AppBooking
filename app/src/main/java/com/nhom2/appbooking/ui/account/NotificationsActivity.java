package com.nhom2.appbooking.ui.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.AppNotification;
import com.nhom2.appbooking.databinding.ActivityNotificationsBinding;
import com.nhom2.appbooking.databinding.ItemNotificationBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.util.Formatters;
import com.nhom2.appbooking.util.TimeAgo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** "Thông báo của tôi": xem, đánh dấu đã đọc, xóa (như popup chuông trên header của web). */
public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private BindingAdapter<AppNotification, ItemNotificationBinding> adapter;
    private final List<AppNotification> notifications = new ArrayList<>();

    public static void start(Context context) {
        context.startActivity(new Intent(context, NotificationsActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationOnClickListener(v -> finish());
        binding.toolbar.inflateMenu(R.menu.notifications_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_mark_all) {
                markAllRead();
                return true;
            }
            return false;
        });

        adapter = new BindingAdapter<>(ItemNotificationBinding::inflate, this::bind);
        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
        binding.recycler.setAdapter(adapter);
        binding.swipe.setColorSchemeResources(R.color.primary);
        binding.swipe.setOnRefreshListener(this::load);
        load();
    }

    private void load() {
        binding.swipe.setRefreshing(true);
        ApiClient.get(this).getMyNotifications().enqueue(new ApiCallback<List<AppNotification>>(this) {
            @Override
            protected void onSuccess(List<AppNotification> body) {
                binding.swipe.setRefreshing(false);
                notifications.clear();
                if (body != null) {
                    notifications.addAll(body);
                }
                show();
            }

            @Override
            protected void onError(String message) {
                binding.swipe.setRefreshing(false);
                super.onError(message);
            }
        });
    }

    private void show() {
        adapter.submit(notifications);
        binding.textEmpty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        binding.toolbar.setTitle(getString(R.string.notifications_title_count, notifications.size()));
    }

    private void bind(ItemNotificationBinding b, AppNotification notification) {
        b.textTitle.setText(notification.title);
        b.textTime.setText(timeAgo(notification.createdAt));
        b.card.setCardBackgroundColor(ContextCompat.getColor(this,
                notification.isRead() ? R.color.card : R.color.notification_unread_bg));
        b.card.setOnClickListener(v -> open(notification));
        b.buttonDelete.setOnClickListener(v -> delete(notification));
    }

    private String timeAgo(String createdAt) {
        LocalDateTime then = TimeAgo.parse(createdAt);
        if (then == null) {
            return "";
        }
        TimeAgo ago = TimeAgo.between(then, LocalDateTime.now());
        switch (ago.unit) {
            case MINUTES:
                return getString(R.string.time_minutes, (int) ago.count);
            case HOURS:
                return getString(R.string.time_hours, (int) ago.count);
            case ONE_DAY:
                return getString(R.string.time_one_day);
            case DAYS:
                return getString(R.string.time_days, (int) ago.count);
            default:
                return getString(R.string.time_just_now);
        }
    }

    /** Mở chi tiết và đánh dấu đã đọc, như bấm vào thông báo trên web. */
    private void open(AppNotification notification) {
        markRead(notification);
        String received = Formatters.date(notification.createdAt) + " " + Formatters.time(notification.createdAt);
        new MaterialAlertDialogBuilder(this)
                .setTitle(notification.title)
                .setMessage(notification.message + "\n\n" + getString(R.string.notifications_received_at, received.trim()))
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void markRead(AppNotification notification) {
        if (notification.isRead()) {
            return;
        }
        ApiClient.get(this).markNotificationRead(notification.id).enqueue(new ApiCallback<AppNotification>(this) {
            @Override
            protected void onSuccess(AppNotification body) {
                notification.read = true;
                show();
            }
        });
    }

    private void markAllRead() {
        for (AppNotification notification : new ArrayList<>(notifications)) {
            markRead(notification);
        }
    }

    private void delete(AppNotification notification) {
        ApiClient.get(this).deleteNotification(notification.id).enqueue(new ApiCallback<Void>(this) {
            @Override
            protected void onSuccess(Void body) {
                notifications.remove(notification);
                show();
            }
        });
    }
}
