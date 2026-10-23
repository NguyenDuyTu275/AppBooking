package com.nhom2.appbooking.ui;

import android.app.Activity;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nhom2.appbooking.R;

/** Shared floating shortcut to the AI assistant on signed-in screens. */
public final class AssistantFab {

    private AssistantFab() {
    }

    public static void attach(@NonNull Activity activity, @NonNull ViewGroup host) {
        FloatingActionButton button = new FloatingActionButton(activity);
        button.setImageResource(R.drawable.ic_assistant);
        button.setImageTintList(android.content.res.ColorStateList.valueOf(
                activity.getColor(R.color.white)));
        button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                activity.getColor(R.color.assistant_blue)));
        button.setContentDescription(activity.getString(R.string.open_ai_assistant));
        button.setSize(FloatingActionButton.SIZE_NORMAL);
        button.setCompatElevation(dp(activity, 8));
        button.setOnClickListener(view -> MainActivity.openAssistant(activity));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                dp(activity, 56), dp(activity, 56), Gravity.BOTTOM | Gravity.END);
        params.setMargins(dp(activity, 16), dp(activity, 16), dp(activity, 16), dp(activity, 16));
        host.addView(button, params);
    }

    private static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }
}
