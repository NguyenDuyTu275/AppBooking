package com.nhom2.appbooking.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.databinding.ActivityMainBinding;
import com.nhom2.appbooking.ui.account.AccountFragment;
import com.nhom2.appbooking.ui.auth.LoginActivity;
import com.nhom2.appbooking.ui.bookings.MyBookingsFragment;
import com.nhom2.appbooking.ui.chat.ChatFragment;
import com.nhom2.appbooking.ui.home.HomeFragment;

/** Màn hình chính với 4 tab: Khách sạn, Đơn của tôi, Hỗ trợ, Tài khoản. */
public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_OPEN_BOOKINGS = "open_bookings";
    private static final String EXTRA_OPEN_ASSISTANT = "open_assistant";
    private static final String STATE_TAB = "tab";

    private ActivityMainBinding binding;
    private int currentTab;

    /** Mở trang chính ở tab "Đơn của tôi" (vd sau khi thanh toán). */
    public static void openBookings(Context context) {
        Intent intent = new Intent(context, MainActivity.class)
                .putExtra(EXTRA_OPEN_BOOKINGS, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void openAssistant(Context context) {
        Intent intent = new Intent(context, MainActivity.class)
                .putExtra(EXTRA_OPEN_ASSISTANT, true)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!new SessionManager(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AssistantFab.attach(this, binding.container);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });

        if (savedInstanceState == null) {
            selectTab(tabFrom(getIntent()));
            if (getIntent().getBooleanExtra(EXTRA_OPEN_ASSISTANT, false)) {
                showAssistant();
            }
        } else {
            // Fragment đã được khôi phục, chỉ cần đặt lại tiêu đề
            currentTab = savedInstanceState.getInt(STATE_TAB, R.id.nav_home);
            binding.toolbar.setTitle(titleOf(currentTab));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        selectTab(tabFrom(intent));
        if (intent.getBooleanExtra(EXTRA_OPEN_ASSISTANT, false)) {
            showAssistant();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, currentTab);
    }

    private int tabFrom(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_OPEN_ASSISTANT, false)) {
            return R.id.nav_chat;
        }
        return intent.getBooleanExtra(EXTRA_OPEN_BOOKINGS, false) ? R.id.nav_bookings : R.id.nav_home;
    }

    private void showAssistant() {
        binding.bottomNav.setSelectedItemId(R.id.nav_chat);
        showTab(R.id.nav_chat);
        getSupportFragmentManager().executePendingTransactions();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof ChatFragment) {
            ((ChatFragment) fragment).showAssistantMode();
        }
    }

    private void selectTab(int tabId) {
        binding.bottomNav.setSelectedItemId(tabId);
        // Tab đã được chọn sẵn thì listener không chạy, nên gọi trực tiếp
        showTab(tabId);
    }

    private void showTab(int tabId) {
        if (tabId == currentTab) {
            return;
        }
        currentTab = tabId;
        Fragment fragment;
        if (tabId == R.id.nav_bookings) {
            fragment = new MyBookingsFragment();
        } else if (tabId == R.id.nav_chat) {
            fragment = new ChatFragment();
        } else if (tabId == R.id.nav_account) {
            fragment = new AccountFragment();
        } else {
            fragment = new HomeFragment();
        }
        binding.toolbar.setTitle(titleOf(tabId));
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private int titleOf(int tabId) {
        if (tabId == R.id.nav_bookings) {
            return R.string.tab_bookings;
        } else if (tabId == R.id.nav_chat) {
            return R.string.tab_chat;
        } else if (tabId == R.id.nav_account) {
            return R.string.tab_account;
        }
        return R.string.app_name;
    }
}
