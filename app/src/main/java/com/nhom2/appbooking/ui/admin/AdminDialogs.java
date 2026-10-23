package com.nhom2.appbooking.ui.admin;

import android.content.Context;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.databinding.DialogAdminTextBinding;
import com.nhom2.appbooking.ui.UiUtils;

/** Hộp thoại của trang quản trị, thay cho Modal / Modal.confirm trên web. */
final class AdminDialogs {

    interface Submit {
        void onSubmit(AlertDialog dialog);
    }

    interface TextSubmit {
        void onSubmit(AlertDialog dialog, String text);
    }

    private AdminDialogs() {
    }

    /**
     * Nút xác nhận không tự đóng hộp thoại: nơi gọi kiểm tra dữ liệu, gọi API rồi tự đóng khi thành công,
     * nên nhập sai hoặc backend báo lỗi thì dữ liệu đã nhập vẫn còn (như Modal + Form trên web).
     */
    static AlertDialog showForm(Context context, CharSequence title, View content, CharSequence positive,
                                boolean danger, Submit submit) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(content)
                .setNegativeButton(R.string.admin_cancel, null)
                .setPositiveButton(positive, null)
                .create();
        dialog.setOnShowListener(shown -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (danger) {
                button.setTextColor(ContextCompat.getColor(context, R.color.error));
            }
            button.setOnClickListener(v -> submit.onSubmit(dialog));
        });
        dialog.show();
        return dialog;
    }

    /** Ô bắt buộc nhập: để trống thì báo lỗi ngay dưới ô (như rules required trên web). */
    static boolean requireText(TextInputLayout layout, String value, @StringRes int fieldName) {
        boolean ok = value != null && !value.isEmpty();
        Context context = layout.getContext();
        layout.setError(ok ? null : context.getString(R.string.admin_field_required, context.getString(fieldName)));
        return ok;
    }

    /** Khóa nút xác nhận trong lúc chờ backend để không gửi hai lần. */
    static void setBusy(AlertDialog dialog, boolean busy) {
        Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (button != null) {
            button.setEnabled(!busy);
        }
    }

    static void confirmDelete(Context context, @StringRes int title, @StringRes int message, Runnable onConfirm) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.admin_cancel, null)
                .setPositiveButton(R.string.admin_delete, (d, which) -> onConfirm.run())
                .create();
        dialog.setOnShowListener(shown -> dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(context, R.color.error)));
        dialog.show();
    }

    /** Hộp nhập một ô chữ: lý do từ chối, phản hồi yêu cầu, trả lời đánh giá. */
    static final class TextDialog {
        private final Context context;
        private CharSequence title;
        private CharSequence quoted;
        private CharSequence prompt;
        private CharSequence placeholder;
        private String initial;
        private int maxLength;
        @StringRes
        private int requiredMessage;
        private CharSequence positive;
        private boolean danger;

        TextDialog(Context context) {
            this.context = context;
        }

        TextDialog title(CharSequence value) {
            title = value;
            return this;
        }

        /** Nội dung đang được trả lời, hiện trong khung xám phía trên ô nhập. */
        TextDialog quoted(CharSequence value) {
            quoted = value;
            return this;
        }

        TextDialog prompt(CharSequence value) {
            prompt = value;
            return this;
        }

        TextDialog placeholder(CharSequence value) {
            placeholder = value;
            return this;
        }

        TextDialog initial(String value) {
            initial = value;
            return this;
        }

        TextDialog maxLength(int value) {
            maxLength = value;
            return this;
        }

        /** Bắt buộc nhập: để trống thì hiện thông báo này và không gửi. */
        TextDialog required(@StringRes int message) {
            requiredMessage = message;
            return this;
        }

        TextDialog positive(CharSequence value, boolean isDanger) {
            positive = value;
            danger = isDanger;
            return this;
        }

        void show(TextSubmit submit) {
            DialogAdminTextBinding form = DialogAdminTextBinding.inflate(LayoutInflater.from(context));
            form.textContext.setVisibility(quoted == null ? View.GONE : View.VISIBLE);
            form.textContext.setText(quoted);
            form.textPrompt.setVisibility(prompt == null ? View.GONE : View.VISIBLE);
            form.textPrompt.setText(prompt);
            form.editInput.setHint(placeholder);
            if (maxLength > 0) {
                form.layoutInput.setCounterEnabled(true);
                form.layoutInput.setCounterMaxLength(maxLength);
                form.editInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
            }
            if (initial != null) {
                form.editInput.setText(initial);
            }
            showForm(context, title, form.getRoot(), positive, danger, dialog -> {
                String text = UiUtils.text(form.editInput);
                if (requiredMessage != 0 && text.isEmpty()) {
                    form.layoutInput.setError(context.getString(requiredMessage));
                    return;
                }
                form.layoutInput.setError(null);
                submit.onSubmit(dialog, text);
            });
        }
    }
}
