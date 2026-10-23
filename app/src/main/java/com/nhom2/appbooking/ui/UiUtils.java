package com.nhom2.appbooking.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

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

    /** Logo hai màu: phần đầu theo màu chữ của chỗ đặt logo, phần sau màu vàng (như logo trên header của web). */
    public static CharSequence brand(Context context) {
        String lead = context.getString(R.string.brand_lead);
        String accent = context.getString(R.string.brand_accent);
        SpannableString logo = new SpannableString(lead + accent);
        logo.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.accent)),
                lead.length(), logo.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return logo;
    }

    public static void copy(Context context, String label, String value) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && value != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText(label, value));
            Toast.makeText(context, context.getString(R.string.copied, label), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cho các thẻ trong danh sách trượt lên lần lượt. Gọi ngay sau khi vừa tải dữ liệu,
     * không gọi khi lọc theo từng chữ gõ vào để danh sách không nhấp nháy.
     */
    public static void animateListIn(RecyclerView list) {
        if (list.getLayoutAnimation() == null) {
            list.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(list.getContext(), R.anim.list_layout_enter));
        }
        list.scheduleLayoutAnimation();
    }

    /** Hiện dần một khối nội dung vừa tải xong thay vì bật ra đột ngột. */
    public static void fadeIn(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(view.getResources().getInteger(R.integer.content_fade_ms))
                .start();
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
