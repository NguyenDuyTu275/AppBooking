package com.nhom2.appbooking.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import com.nhom2.appbooking.R;

/** Hàm nhỏ dùng chung cho giao diện. */
public final class UiUtils {

    private UiUtils() {
    }

    /** Nội dung ô nhập, đã bỏ khoảng trắng hai đầu. */
    public static String text(TextView view) {
        CharSequence value = view.getText();
        return value == null ? "" : value.toString().trim();
    }

    public static void copy(Context context, String label, String value) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && value != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, value));
            Toast.makeText(context, context.getString(R.string.copied, label), Toast.LENGTH_SHORT).show();
        }
    }

    /** Gọi lại mỗi khi chữ trong ô nhập thay đổi. */
    public interface OnTextChanged {
        void onTextChanged(String text);
    }

    public static TextWatcher afterTextChanged(OnTextChanged listener) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                listener.onTextChanged(s == null ? "" : s.toString());
            }
        };
    }
}
