package com.nhom2.appbooking.ui.admin;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.nhom2.appbooking.BuildConfig;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.LanguageSettings;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.databinding.ActivityAdminBinding;
import com.nhom2.appbooking.ui.HomeNavigator;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.ui.auth.LoginActivity;

/**
 * Trang quản trị như AdminDashboard của web: các mục ở sidebar trên web thành tab ngang.
 * Chỉ admin vào được (như AuthGuard requiredRole="ADMIN"); backend vẫn kiểm tra quyền ở mọi request.
 */
public class AdminActivity extends AppCompatActivity {

    private static final String STATE_TAB = "tab";
    private static final String WEB_ADMIN_PATH = "/admin";

    /** Cùng thứ tự với MENU trong AdminDashboard.tsx. */
    private static final int[] TAB_TITLES = {
            R.string.admin_menu_overview,
            R.string.admin_menu_users,
            R.string.admin_menu_hotels,
            R.string.admin_menu_bookings,
            R.string.admin_menu_requests,
            R.string.admin_menu_reviews,
            R.string.admin_menu_chat,
    };

    private ActivityAdminBinding binding;
    private SessionManager session;
    private int currentTab = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (!session.isAdmin()) {
            startActivity(HomeNavigator.homeIntent(this));
            finish();
            return;
        }

        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle(UiUtils.brand(this));
        binding.toolbar.setSubtitle(getString(R.string.admin_console, session.getUsername()));
        binding.toolbar.inflateMenu(R.menu.admin_menu);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_language) {
                LanguageSettings.apply(LanguageSettings.current().other());
                return true;
            } else if (item.getItemId() == R.id.action_open_website) {
                openWebsite();
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                logout();
                return true;
            }
            return false;
        });

        for (int title : TAB_TITLES) {
            binding.tabs.addTab(binding.tabs.newTab().setText(title));
        }
        if (savedInstanceState != null) {
            // Fragment đã được khôi phục: chỉ chọn lại tab, không thay fragment
            currentTab = savedInstanceState.getInt(STATE_TAB, 0);
            TabLayout.Tab tab = binding.tabs.getTabAt(currentTab);
            if (tab != null) {
                tab.select();
            }
        }
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        if (savedInstanceState == null) {
            showTab(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_TAB, currentTab);
    }

    private void showTab(int position) {
        if (position == currentTab) {
            return;
        }
        currentTab = position;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.tab_enter, R.anim.tab_exit)
                .replace(R.id.container, fragmentFor(position))
                .commit();
    }

    private static Fragment fragmentFor(int position) {
        switch (position) {
            case 1:
                return new AdminUsersFragment();
            case 2:
                return new AdminHotelsFragment();
            case 3:
                return new AdminBookingsFragment();
            case 4:
                return new AdminRequestsFragment();
            case 5:
                return new AdminReviewsFragment();
            case 6:
                return new AdminChatFragment();
            default:
                return new AdminOverviewFragment();
        }
    }

    private void openWebsite() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.WEB_BASE_URL + WEB_ADMIN_PATH)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_browser, Toast.LENGTH_LONG).show();
        }
    }

    private void logout() {
        session.clear();
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
