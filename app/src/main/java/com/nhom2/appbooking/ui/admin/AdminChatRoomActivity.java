package com.nhom2.appbooking.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.SupportMessage;
import com.nhom2.appbooking.databinding.ActivityAdminChatRoomBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.ui.chat.MessageAdapter;

import java.util.List;

/** Admin trả lời một khách trong chat hỗ trợ (cột phải của AdminSupportChat trên web), tự cập nhật vài giây một lần. */
public class AdminChatRoomActivity extends AppCompatActivity {

    private static final String EXTRA_ROOM_ID = "room_id";
    private static final String EXTRA_USERNAME = "username";
    private static final long POLL_INTERVAL_MS = 4000;
    /** Bằng SupportChatService.MAX_MESSAGE_LENGTH của backend. */
    private static final int MAX_MESSAGE_LENGTH = 2000;

    private final MessageAdapter adapter = new MessageAdapter(true);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            load();
            handler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    private ActivityAdminChatRoomBinding binding;
    private long roomId;
    private long lastMessageId = -1;

    public static void start(Context context, long roomId, String username) {
        context.startActivity(new Intent(context, AdminChatRoomActivity.class)
                .putExtra(EXTRA_ROOM_ID, roomId)
                .putExtra(EXTRA_USERNAME, username));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminChatRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        roomId = getIntent().getLongExtra(EXTRA_ROOM_ID, -1L);
        binding.toolbar.setTitle(getIntent().getStringExtra(EXTRA_USERNAME));
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.chat.recyclerMessages.setLayoutManager(layoutManager);
        binding.chat.recyclerMessages.setAdapter(adapter);
        binding.chat.textEmpty.setVisibility(View.GONE);
        binding.chat.editMessage.setHint(R.string.admin_chat_placeholder);
        binding.chat.editMessage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_MESSAGE_LENGTH)});
        binding.chat.buttonSend.setOnClickListener(v -> send());
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(poll);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(poll);
    }

    private void load() {
        ApiClient.get(this).getRoomMessages(roomId).enqueue(new ApiCallback<List<SupportMessage>>(this) {
            @Override
            protected void onSuccess(List<SupportMessage> messages) {
                if (messages == null || messages.isEmpty()) {
                    return;
                }
                long newest = messages.get(messages.size() - 1).id;
                if (newest != lastMessageId) {
                    lastMessageId = newest;
                    adapter.submit(messages);
                    binding.chat.recyclerMessages.scrollToPosition(messages.size() - 1);
                    markRead();
                }
            }

            @Override
            protected void onError(String message) {
                // Lần cập nhật sau sẽ thử lại
            }
        });
    }

    private void markRead() {
        ApiClient.get(this).markRoomRead(roomId).enqueue(new ApiCallback<Void>(this) {
            @Override
            protected void onSuccess(Void body) {
            }

            @Override
            protected void onError(String message) {
            }
        });
    }

    private void send() {
        String text = UiUtils.text(binding.chat.editMessage);
        if (text.isEmpty()) {
            return;
        }
        binding.chat.buttonSend.setEnabled(false);
        ApiClient.get(this).replyInRoom(roomId, new ApiRequests.SendMessage(text))
                .enqueue(new ApiCallback<SupportMessage>(this) {
                    @Override
                    protected void onSuccess(SupportMessage body) {
                        binding.chat.buttonSend.setEnabled(true);
                        binding.chat.editMessage.setText("");
                        load();
                    }

                    @Override
                    protected void onError(String message) {
                        binding.chat.buttonSend.setEnabled(true);
                        super.onError(message);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(poll);
    }
}
