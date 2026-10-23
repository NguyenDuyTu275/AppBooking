package com.nhom2.appbooking.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.data.model.AppNotification;
import com.nhom2.appbooking.databinding.ActivityMainBinding;
import com.nhom2.appbooking.ui.account.AccountFragment;
import com.nhom2.appbooking.ui.account.NotificationsActivity;
import com.nhom2.appbooking.ui.auth.LoginActivity;
import com.nhom2.appbooking.ui.bookings.MyBookingsFragment;
import com.nhom2.appbooking.ui.chat.ChatFragment;
import com.nhom2.appbooking.ui.home.HomeFragment;
import com.nhom2.appbooking.ui.wishlist.WishlistActivity;

import java.util.List;

/** Màn hình chính với 4 tab: Khách sạn, Đơn của tôi, Hỗ trợ, Tài khoản. */
public class MainActivity extends AppCompatActivity {

    private static final String EXTRA_OPEN_BOOKINGS = "open_bookings";
    private static final String EXTRA_OPEN_ASSISTANT = "open_assistant";
    private static final String STATE_TAB = "tab";

    private ActivityMainBinding binding;
    private int currentTab;
    private BadgeDrawable wishlistBadge;
    private BadgeDrawable notificationBadge;

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
        SessionManager session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (session.isAdmin()) {
            // Admin dùng trang quản trị, như web chuyển admin tới /admin
            startActivity(HomeNavigator.homeIntent(this));
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setUpToolbarActions();
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
            showTitle(currentTab);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBadges();
    }

    /** Tim (yêu thích) và chuông (thông báo) trên thanh trên cùng, như header của web. */
    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void setUpToolbarActions() {
        binding.toolbar.inflateMenu(R.menu.main_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_wishlist) {
                WishlistActivity.start(this);
                return true;
            } else if (item.getItemId() == R.id.action_notifications) {
                NotificationsActivity.start(this);
                return true;
            }
            return false;
        });
        wishlistBadge = BadgeDrawable.create(this);
        notificationBadge = BadgeDrawable.create(this);
        wishlistBadge.setVisible(false);
        notificationBadge.setVisible(false);
        BadgeUtils.attachBadgeDrawable(wishlistBadge, binding.toolbar, R.id.action_wishlist);
        BadgeUtils.attachBadgeDrawable(notificationBadge, binding.toolbar, R.id.action_notifications);
    }

    /** Số khách sạn đã lưu và số thông báo chưa đọc trên hai nút. */
    public void refreshBadges() {
        ApiClient.get(this).getWishlist().enqueue(new ApiCallback<List<Long>>(this) {
            @Override
            protected void onSuccess(List<Long> ids) {
                showCount(wishlistBadge, ids == null ? 0 : ids.size());
            }

            @Override
            protected void onError(String message) {
                // Chỉ là số đếm phụ: lỗi thì để nguyên, lần mở lại màn hình sẽ thử lại
            }
        });
        ApiClient.get(this).getMyNotifications().enqueue(new ApiCallback<List<AppNotification>>(this) {
            @Override
            protected void onSuccess(List<AppNotification> notifications) {
                int unread = 0;
                if (notifications != null) {
                    for (AppNotification notification : notifications) {
                        if (!notification.isRead()) {
                            unread++;
                        }
                    }
                }
                showCount(notificationBadge, unread);
            }

            @Override
            protected void onError(String message) {
                // Như trên
            }
        });
    }

    private static void showCount(BadgeDrawable badge, int count) {
        badge.setNumber(count);
        badge.setVisible(count > 0);
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
        showTitle(tabId);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.tab_enter, R.anim.tab_exit)
                .replace(R.id.container, fragment)
                .commit();
    }

    /** Tab Khách sạn hiện logo như header của web; các tab khác hiện tên tab. */
    private void showTitle(int tabId) {
        if (tabId == R.id.nav_bookings) {
            binding.toolbar.setTitle(R.string.tab_bookings);
        } else if (tabId == R.id.nav_chat) {
            binding.toolbar.setTitle(R.string.tab_chat);
        } else if (tabId == R.id.nav_account) {
            binding.toolbar.setTitle(R.string.tab_account);
        } else {
            binding.toolbar.setTitle(UiUtils.brand(this));
        }
    }
}
