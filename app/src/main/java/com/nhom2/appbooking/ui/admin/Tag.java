package com.nhom2.appbooking.ui.admin;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.nhom2.appbooking.R;

/** Nhãn màu như thẻ &lt;Tag color="..."&gt; của Ant Design trên trang quản trị của web. */
public enum Tag {
    ORANGE(R.color.tag_orange_bg, R.color.tag_orange_text, R.color.tag_orange_border),
    GOLD(R.color.tag_gold_bg, R.color.tag_gold_text, R.color.tag_gold_border),
    GREEN(R.color.tag_green_bg, R.color.tag_green_text, R.color.tag_green_border),
    RED(R.color.tag_red_bg, R.color.tag_red_text, R.color.tag_red_border),
    VOLCANO(R.color.tag_volcano_bg, R.color.tag_volcano_text, R.color.tag_volcano_border),
    BLUE(R.color.tag_blue_bg, R.color.tag_blue_text, R.color.tag_blue_border),
    CYAN(R.color.tag_cyan_bg, R.color.tag_cyan_text, R.color.tag_cyan_border),
    MAGENTA(R.color.tag_magenta_bg, R.color.tag_magenta_text, R.color.tag_magenta_border),
    GREY(R.color.tag_grey_bg, R.color.tag_grey_text, R.color.tag_grey_border);

    private static final float CORNER_DP = 4f;
    private static final float STROKE_DP = 1f;

    @ColorRes
    private final int background;
    @ColorRes
    private final int text;
    @ColorRes
    private final int border;

    Tag(@ColorRes int background, @ColorRes int text, @ColorRes int border) {
        this.background = background;
        this.text = text;
        this.border = border;
    }

    public void applyTo(TextView view, CharSequence label) {
        Context context = view.getContext();
        float density = context.getResources().getDisplayMetrics().density;
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(ContextCompat.getColor(context, background));
        shape.setStroke(Math.round(STROKE_DP * density), ContextCompat.getColor(context, border));
        shape.setCornerRadius(CORNER_DP * density);
        view.setBackground(shape);
        view.setTextColor(ContextCompat.getColor(context, text));
        view.setText(label);
    }
}
