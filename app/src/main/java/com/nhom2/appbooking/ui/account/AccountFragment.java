package com.nhom2.appbooking.ui.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nhom2.appbooking.BuildConfig;
import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.LanguageSettings;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.data.model.UserProfile;
import com.nhom2.appbooking.databinding.FragmentAccountBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.ui.auth.LoginActivity;
import com.nhom2.appbooking.ui.wishlist.WishlistActivity;
import com.nhom2.appbooking.util.AppLanguage;

import java.util.Locale;

/** Tab "Tài khoản": thông tin người dùng, hồ sơ, yêu thích, thông báo, ngôn ngữ, mở web, đăng xuất. */
public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private SessionManager session;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        session = new SessionManager(requireContext());
        showUser(session.getUsername(), null, null, session.isAdmin());

        binding.buttonProfile.setOnClickListener(v -> ProfileActivity.start(requireContext()));
        binding.buttonWishlist.setOnClickListener(v -> WishlistActivity.start(requireContext()));
        binding.buttonNotifications.setOnClickListener(v -> NotificationsActivity.start(requireContext()));
        binding.buttonWebsite.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.WEB_BASE_URL))));
        binding.buttonLogout.setOnClickListener(v -> logout());
        setUpLanguageToggle();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại khi quay về từ màn Hồ sơ, vd vừa đổi ảnh đại diện hoặc email
        loadProfile();
    }

    private void setUpLanguageToggle() {
        int checked = LanguageSettings.current() == AppLanguage.EN ? R.id.button_lang_en : R.id.button_lang_vi;
        // Chọn sẵn trước khi gắn listener để không đổi ngôn ngữ lúc mở tab
        binding.toggleLanguage.check(checked);
        binding.toggleLanguage.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            AppLanguage chosen = checkedId == R.id.button_lang_en ? AppLanguage.EN : AppLanguage.VI;
            if (chosen != LanguageSettings.current()) {
                LanguageSettings.apply(chosen);
            }
        });
    }

    private void loadProfile() {
        ApiClient.get(requireContext()).getMe().enqueue(new ApiCallback<UserProfile>(this) {
            @Override
            protected void onSuccess(UserProfile profile) {
                if (profile != null) {
                    showUser(profile.username, profile.email, profile.avatarUrl, "ADMIN".equals(profile.role));
                }
            }

            @Override
            protected boolean signOutWhenRejected() {
                // Backend cũ chỉ cho ADMIN gọi /users/me: giữ phiên, vẫn hiện tên từ token
                return false;
            }

            @Override
            protected void onError(String message) {
                // Không lấy được email thì vẫn dùng được trang tài khoản
            }
        });
    }

    private void showUser(String username, String email, String avatarUrl, boolean admin) {
        String name = username == null ? "" : username;
        boolean hasImage = avatarUrl != null && !avatarUrl.isEmpty();
        binding.textUsername.setText(name);
        binding.textAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT));
        binding.textAvatar.setVisibility(hasImage ? View.GONE : View.VISIBLE);
        binding.imageAvatar.setVisibility(hasImage ? View.VISIBLE : View.GONE);
        if (hasImage) {
            ImageLoader.loadAvatar(binding.imageAvatar, avatarUrl);
        }
        binding.textEmail.setText(email == null || email.isEmpty() ? getString(R.string.profile_no_email) : email);
        binding.textRole.setText(admin ? R.string.role_admin : R.string.role_user);
    }

    private void logout() {
        session.clear();
        Intent intent = new Intent(requireContext(), LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
