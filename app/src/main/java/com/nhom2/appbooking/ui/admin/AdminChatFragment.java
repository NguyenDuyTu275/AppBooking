package com.nhom2.appbooking.ui.admin;

import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.model.ChatRoomSummary;
import com.nhom2.appbooking.databinding.ItemChatRoomBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.BindingAdapter;
import com.nhom2.appbooking.util.Formatters;

import java.util.List;

/**
 * Mục "Hỗ trợ khách hàng": các cuộc trò chuyện với khách, mới nhất lên đầu (cột trái của AdminSupportChat trên web).
 * Web nhận tin qua WebSocket; app tự tải lại vài giây một lần như màn chat của khách.
 */
public class AdminChatFragment extends AdminListFragment {

    private static final long POLL_INTERVAL_MS = 5000;
    private static final long MAX_BADGE = 99;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            fetch(false);
            handler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    private BindingAdapter<ChatRoomSummary, ItemChatRoomBinding> adapter;

    @Override
    protected int titleRes() {
        return R.string.admin_menu_chat;
    }

    @Override
    protected void setUp() {
        adapter = new BindingAdapter<>(ItemChatRoomBinding::inflate, this::bind);
        binding.recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(poll, POLL_INTERVAL_MS);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(poll);
    }

    @Override
    protected void load() {
        fetch(true);
    }

    private void fetch(boolean showSpinner) {
        if (showSpinner) {
            setLoading(true);
        }
        ApiClient.get(requireContext()).getChatRooms().enqueue(new ApiCallback<List<ChatRoomSummary>>(this) {
            @Override
            protected void onSuccess(List<ChatRoomSummary> rooms) {
                setLoading(false);
                adapter.submit(rooms);
                showEmpty(adapter.isEmpty(), R.string.admin_chat_empty);
            }

            @Override
            protected void onError(String message) {
                setLoading(false);
                if (showSpinner) {
                    super.onError(getString(R.string.admin_chat_rooms_load_failed));
                }
                // Lần tự cập nhật sau sẽ thử lại, không làm phiền bằng thông báo lỗi
            }
        });
    }

    private void bind(ItemChatRoomBinding b, ChatRoomSummary room) {
        boolean unread = room.unreadCount > 0;
        b.textUsername.setText(room.username);
        b.textUsername.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
        b.textPreview.setText(room.lastMessagePreview);
        b.textTime.setText(Formatters.shortDateTime(room.lastMessageAt));
        b.textUnread.setVisibility(unread ? View.VISIBLE : View.GONE);
        b.textUnread.setText(room.unreadCount > MAX_BADGE ? MAX_BADGE + "+" : String.valueOf(room.unreadCount));
        b.getRoot().setOnClickListener(v -> AdminChatRoomActivity.start(requireContext(), room.roomId, room.username));
    }
}
