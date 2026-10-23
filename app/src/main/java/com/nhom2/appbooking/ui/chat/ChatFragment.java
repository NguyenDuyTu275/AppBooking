package com.nhom2.appbooking.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.SupportChatSocket;
import com.nhom2.appbooking.data.model.SupportChatHistory;
import com.nhom2.appbooking.data.model.SupportMessage;
import com.nhom2.appbooking.databinding.FragmentChatBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.UiUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Tab "Hỗ trợ": chat với nhân viên khách sạn. Tin mới tới ngay qua WebSocket khi khung hỗ trợ đang mở;
 * vẫn tải lại định kỳ phòng khi kết nối có vấn đề.
 */
public class ChatFragment extends Fragment {

    private static final long POLL_INTERVAL_MS = 15000;

    private FragmentChatBinding binding;
    private final MessageAdapter adapter = new MessageAdapter();
    private final AssistantMessageAdapter assistantAdapter = new AssistantMessageAdapter();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastMessageId = -1;
    private SupportChatSocket socket;
    private final SupportChatSocket.Listener socketListener = new SupportChatSocket.Listener() {
        @Override
        public void onMessage(SupportMessage message) {
            if (isSupportMode() && showMessage(message) && message.fromAdmin) {
                markRead();
            }
        }

        @Override
        public void onReconnected() {
            load();
        }
    };
    private ActivityResultLauncher<Intent> attachmentPicker;
    private boolean attachmentForAssistant;
    private Uri assistantAttachmentUri;
    private String assistantAttachmentName;
    private boolean assistantAttachmentIsImage;

    private final Runnable poll = new Runnable() {
        @Override
        public void run() {
            if (isSupportMode()) {
                load();
                handler.postDelayed(this, POLL_INTERVAL_MS);
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attachmentPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() != Activity.RESULT_OK
                            || result.getData() == null
                            || result.getData().getData() == null) {
                        return;
                    }
                    handleAttachment(result.getData().getData(), attachmentForAssistant);
                });
    }

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
        LinearLayoutManager assistantLayout = new LinearLayoutManager(requireContext());
        assistantLayout.setStackFromEnd(true);
        binding.recyclerAssistantMessages.setLayoutManager(assistantLayout);
        binding.recyclerAssistantMessages.setAdapter(assistantAdapter);
        binding.buttonSend.setOnClickListener(v -> send());
        binding.buttonAssistantSend.setOnClickListener(v -> sendAssistantMessage());
        binding.buttonAssistantEmoji.setOnClickListener(v -> showEmojiPicker(true));
        binding.buttonSupportEmoji.setOnClickListener(v -> showEmojiPicker(false));
        binding.buttonAssistantAttach.setOnClickListener(v -> showAttachmentPicker(true));
        binding.buttonSupportAttach.setOnClickListener(v -> showAttachmentPicker(false));
        binding.buttonSuggestionHotel.setOnClickListener(v -> sendAssistantMessage(
                getString(R.string.assistant_suggestion_hotel)));
        binding.buttonSuggestionBooking.setOnClickListener(v -> sendAssistantMessage(
                getString(R.string.assistant_suggestion_booking)));
        binding.buttonSuggestionPayment.setOnClickListener(v -> sendAssistantMessage(
                getString(R.string.assistant_suggestion_payment)));
        binding.chatModeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                setSupportMode(checkedId == R.id.button_mode_support);
            }
        });
        binding.editAssistantMessage.setOnEditorActionListener((textView, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE) {
                sendAssistantMessage();
                return true;
            }
            return false;
        });
        setSupportMode(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSupportMode()) {
            handler.post(poll);
            openSocket();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(poll);
        closeSocket();
    }

    private void openSocket() {
        if (socket == null) {
            socket = SupportChatSocket.connect(requireContext(), socketListener);
        }
    }

    private void closeSocket() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
    }

    /** Hiện tin vừa gửi hoặc vừa được đẩy tới; false nếu tin đã có. */
    private boolean showMessage(SupportMessage message) {
        if (binding == null || !adapter.add(message)) {
            return false;
        }
        lastMessageId = Math.max(lastMessageId, message.id);
        binding.textEmpty.setVisibility(View.GONE);
        binding.recyclerMessages.scrollToPosition(adapter.getItemCount() - 1);
        return true;
    }

    private void load() {
        if (binding == null || !isSupportMode()) {
            return;
        }
        ApiClient.get(requireContext()).getSupportChat().enqueue(new ApiCallback<SupportChatHistory>(this) {
            @Override
            protected void onSuccess(SupportChatHistory history) {
                if (binding == null || !isSupportMode()) {
                    return;
                }
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
        if (binding == null || !isSupportMode()) {
            return;
        }
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
                        showMessage(body);
                    }

                    @Override
                    protected void onError(String message) {
                        binding.buttonSend.setEnabled(true);
                        super.onError(message);
                    }
                });
    }

    private void sendAssistantMessage() {
        if (binding != null) {
            sendAssistantMessage(UiUtils.text(binding.editAssistantMessage));
        }
    }

    private void sendAssistantMessage(String text) {
        if (binding == null || (text.trim().isEmpty() && assistantAttachmentUri == null)) {
            return;
        }
        binding.assistantWelcome.setVisibility(View.GONE);
        binding.recyclerAssistantMessages.setVisibility(View.VISIBLE);
        binding.editAssistantMessage.setText("");
        String question = text.trim();
        assistantAdapter.add(question, true, assistantAttachmentUri,
                assistantAttachmentName, assistantAttachmentIsImage);
        assistantAttachmentUri = null;
        assistantAttachmentName = null;
        assistantAttachmentIsImage = false;
        binding.textAssistantAttachment.setVisibility(View.GONE);
        assistantAdapter.add(getString(AssistantPreviewReplies.forQuestion(question)), false);
        scrollAssistantToBottom();
    }

    private void showAttachmentPicker(boolean forAssistant) {
        if (binding == null) {
            return;
        }
        attachmentForAssistant = forAssistant;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.chat_choose_attachment)
                .setItems(new CharSequence[]{
                        getString(R.string.chat_choose_image),
                        getString(R.string.chat_choose_file)
                }, (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(which == 0 ? "image/*" : "*/*");
                    attachmentPicker.launch(intent);
                })
                .show();
    }

    private void handleAttachment(Uri uri, boolean forAssistant) {
        if (binding == null) {
            return;
        }
        String name = getAttachmentName(uri);
        String mimeType = requireContext().getContentResolver().getType(uri);
        boolean isImage = mimeType != null && mimeType.startsWith("image/");
        if (forAssistant) {
            assistantAttachmentUri = uri;
            assistantAttachmentName = name;
            assistantAttachmentIsImage = isImage;
            binding.textAssistantAttachment.setText(
                    getString(R.string.chat_attachment_name, name));
            binding.textAssistantAttachment.setVisibility(View.VISIBLE);
            return;
        }

        EditText input = binding.editMessage;
        String prefix = getString(R.string.chat_attachment_name, name);
        if (input.length() > 0) {
            prefix += "\n";
        }
        input.getText().insert(0, prefix);
        Toast.makeText(requireContext(), R.string.support_attachment_text_only,
                Toast.LENGTH_LONG).show();
    }

    private String getAttachmentName(Uri uri) {
        try (Cursor cursor = requireContext().getContentResolver().query(
                uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    String name = cursor.getString(nameIndex);
                    if (name != null && !name.trim().isEmpty()) {
                        return name;
                    }
                }
            }
        }
        String fallback = uri.getLastPathSegment();
        return fallback == null || fallback.trim().isEmpty() ? "tệp đính kèm" : fallback;
    }

    private void showEmojiPicker(boolean forAssistant) {
        if (binding == null) {
            return;
        }
        String[] emojis = {"😀", "😊", "😂", "😍", "🥰", "👍", "🙏", "🎉", "❤️", "😢"};
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.chat_emojis)
                .setItems(emojis, (dialog, which) -> {
                    EditText input = forAssistant
                            ? binding.editAssistantMessage : binding.editMessage;
                    int cursor = Math.max(input.getSelectionStart(), 0);
                    input.getText().insert(cursor, emojis[which]);
                    input.requestFocus();
                })
                .show();
    }

    private void scrollAssistantToBottom() {
        if (binding != null) {
            RecyclerView messages = binding.recyclerAssistantMessages;
            messages.post(() -> messages.scrollToPosition(assistantAdapter.getItemCount() - 1));
        }
    }

    private boolean isSupportMode() {
        return binding != null && binding.supportPanel.getVisibility() == View.VISIBLE;
    }

    private void setSupportMode(boolean supportMode) {
        if (binding == null) {
            return;
        }
        binding.assistantPanel.setVisibility(supportMode ? View.GONE : View.VISIBLE);
        binding.assistantComposer.setVisibility(supportMode ? View.GONE : View.VISIBLE);
        binding.supportPanel.setVisibility(supportMode ? View.VISIBLE : View.GONE);
        handler.removeCallbacks(poll);
        if (supportMode && isResumed()) {
            handler.post(poll);
            openSocket();
        } else {
            closeSocket();
        }
    }

    public void showAssistantMode() {
        if (binding != null) {
            binding.chatModeGroup.check(R.id.button_mode_assistant);
            setSupportMode(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(poll);
        closeSocket();
        binding = null;
    }
}
