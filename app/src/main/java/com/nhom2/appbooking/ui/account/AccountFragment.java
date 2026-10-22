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
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.data.model.UserProfile;
import com.nhom2.appbooking.databinding.FragmentAccountBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.auth.LoginActivity;

import java.util.Locale;

/** Tab "Tài khoản": thông tin người dùng, mở web, đăng xuất. */
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
        showUser(session.getUsername(), null, session.isAdmin());

        binding.buttonWebsite.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.WEB_BASE_URL))));
        binding.buttonLogout.setOnClickListener(v -> logout());

        ApiClient.get(requireContext()).getMe().enqueue(new ApiCallback<UserProfile>(this) {
            @Override
            protected void onSuccess(UserProfile profile) {
                if (profile != null) {
                    showUser(profile.username, profile.email, "ADMIN".equals(profile.role));
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

    private void showUser(String username, String email, boolean admin) {
        String name = username == null ? "" : username;
        binding.textUsername.setText(name);
        binding.textAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT));
        binding.textEmail.setText(email == null ? "" : email);
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
