package com.nhom2.appbooking.ui.chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.model.SupportChatHistory;
import com.nhom2.appbooking.data.model.SupportMessage;
import com.nhom2.appbooking.databinding.FragmentChatBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.UiUtils;

import java.util.Collections;
import java.util.List;

/** Tab "Hỗ trợ": chat với nhân viên khách sạn, tự cập nhật vài giây một lần khi đang mở. */
public class ChatFragment extends Fragment {

    private static final long POLL_INTERVAL_MS = 4000;

    private FragmentChatBinding binding;
    private final MessageAdapter adapter = new MessageAdapter();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastMessageId = -1;

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            load();
            handler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.recyclerMessages.setLayoutManager(layoutManager);
        binding.recyclerMessages.setAdapter(adapter);
        binding.buttonSend.setOnClickListener(v -> send());
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(poll);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(poll);
    }

    private void load() {
        ApiClient.get(requireContext()).getSupportChat().enqueue(new ApiCallback<SupportChatHistory>(this) {
            @Override
            protected void onSuccess(SupportChatHistory history) {
                List<SupportMessage> messages = history == null || history.messages == null
                        ? Collections.emptyList() : history.messages;
                binding.textEmpty.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
                long newest = messages.isEmpty() ? -1 : messages.get(messages.size() - 1).id;
                if (newest != lastMessageId) {
                    lastMessageId = newest;
                    adapter.submit(messages);
                    binding.recyclerMessages.scrollToPosition(Math.max(0, messages.size() - 1));
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
        ApiClient.get(requireContext()).markSupportChatRead().enqueue(new ApiCallback<Void>(this) {
            @Override
            protected void onSuccess(Void body) {
            }

            @Override
            protected void onError(String message) {
            }
        });
    }

    private void send() {
        String text = UiUtils.text(binding.editMessage);
        if (text.isEmpty()) {
            return;
        }
        binding.buttonSend.setEnabled(false);
        ApiClient.get(requireContext())
                .sendSupportMessage(new ApiRequests.SendMessage(text))
                .enqueue(new ApiCallback<SupportMessage>(this) {
                    @Override
                    protected void onSuccess(SupportMessage body) {
                        binding.buttonSend.setEnabled(true);
                        binding.editMessage.setText("");
                        load();
                    }

                    @Override
                    protected void onError(String message) {
                        binding.buttonSend.setEnabled(true);
                        super.onError(message);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(poll);
        binding = null;
    }
}
