package com.nhom2.appbooking.ui.account;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.data.model.UserProfile;
import com.nhom2.appbooking.databinding.ActivityProfileBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.ImageLoader;
import com.nhom2.appbooking.ui.ImageParts;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.util.FormRules;

import java.util.Locale;

import okhttp3.MultipartBody;

/** Hồ sơ như trang /profile của web: đổi ảnh đại diện, đổi email qua OTP, đổi mật khẩu. */
public class ProfileActivity extends AppCompatActivity {

    private final ActivityResultLauncher<PickVisualMediaRequest> pickAvatar =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::uploadAvatar);

    private ActivityProfileBinding binding;
    private UserProfile profile;
    /** Địa chỉ đã nhận OTP; xác nhận luôn dùng địa chỉ này dù ô email bị sửa sau đó (như web). */
    private String pendingEmail;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ProfileActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        String username = new SessionManager(this).getUsername();
        binding.editUsername.setText(username);
        showAvatar(username, null);

        binding.layoutAvatar.setOnClickListener(v -> pickAvatar.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        binding.buttonSendCode.setOnClickListener(v -> sendEmailOtp());
        binding.buttonConfirmEmail.setOnClickListener(v -> confirmEmail());
        binding.buttonCancelEmail.setOnClickListener(v -> cancelEmailChange());
        binding.buttonChangePassword.setOnClickListener(v -> changePassword());

        loadProfile();
    }

    private void loadProfile() {
        ApiClient.get(this).getMe().enqueue(new ApiCallback<UserProfile>(this) {
            @Override
            protected void onSuccess(UserProfile body) {
                if (body == null) {
                    onError(getString(R.string.profile_load_failed));
                    return;
                }
                showProfile(body);
            }
        });
    }

    private void showProfile(UserProfile user) {
        profile = user;
        binding.editUsername.setText(user.username);
        binding.editEmail.setText(user.email);
        showAvatar(user.username, user.avatarUrl);
    }

    private void showAvatar(String username, String avatarUrl) {
        boolean hasImage = avatarUrl != null && !avatarUrl.isEmpty();
        String name = username == null ? "" : username;
        binding.textAvatar.setText(name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase(Locale.ROOT));
        binding.textAvatar.setVisibility(hasImage ? View.GONE : View.VISIBLE);
        binding.imageAvatar.setVisibility(hasImage ? View.VISIBLE : View.GONE);
        if (hasImage) {
            ImageLoader.loadAvatar(binding.imageAvatar, avatarUrl);
        }
    }

    private void uploadAvatar(Uri uri) {
        if (uri == null) {
            return;
        }
        setAvatarLoading(true);
        ImageParts.read(this, uri, new ImageParts.Callback() {
            @Override
            public void onReady(MultipartBody.Part part) {
                sendAvatar(part);
            }

            @Override
            public void onError(@StringRes int message) {
                setAvatarLoading(false);
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendAvatar(MultipartBody.Part part) {
        ApiClient.get(this).uploadAvatar(part).enqueue(new ApiCallback<UserProfile>(this) {
            @Override
            protected void onSuccess(UserProfile body) {
                setAvatarLoading(false);
                if (body != null) {
                    showProfile(body);
                }
                Toast.makeText(ProfileActivity.this, R.string.profile_avatar_updated, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onError(String message) {
                setAvatarLoading(false);
                super.onError(message);
            }
        });
    }

    private void setAvatarLoading(boolean loading) {
        binding.layoutAvatar.setEnabled(!loading);
        binding.progressAvatar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void sendEmailOtp() {
        String email = UiUtils.text(binding.editEmail);
        if (!FormRules.isEmail(email)) {
            binding.inputEmail.setError(getString(R.string.email_invalid));
            return;
        }
        binding.inputEmail.setError(null);
        setEmailLoading(true);
        ApiClient.get(this).requestEmailChange(new ApiRequests.ChangeEmail(email, null))
                .enqueue(new ApiCallback<String>(this) {
                    @Override
                    protected void onSuccess(String maskedEmail) {
                        setEmailLoading(false);
                        pendingEmail = email;
                        showEmailOtp(true);
                        binding.textOtpLabel.setText(getString(R.string.profile_otp_sent_to, email));
                        Toast.makeText(ProfileActivity.this,
                                getString(R.string.profile_otp_sent, maskedEmail == null ? email : maskedEmail),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    protected void onError(String message) {
                        setEmailLoading(false);
                        super.onError(message);
                    }
                });
    }

    private void confirmEmail() {
        String otp = UiUtils.text(binding.editEmailOtp);
        if (pendingEmail == null) {
            return;
        }
        if (otp.isEmpty()) {
            Toast.makeText(this, R.string.otp_required, Toast.LENGTH_SHORT).show();
            return;
        }
        setEmailLoading(true);
        ApiClient.get(this).confirmEmailChange(new ApiRequests.ChangeEmail(pendingEmail, otp))
                .enqueue(new ApiCallback<UserProfile>(this) {
                    @Override
                    protected void onSuccess(UserProfile body) {
                        setEmailLoading(false);
                        pendingEmail = null;
                        showEmailOtp(false);
                        if (body != null) {
                            showProfile(body);
                        }
                        Toast.makeText(ProfileActivity.this, R.string.profile_email_changed, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onError(String message) {
                        setEmailLoading(false);
                        super.onError(message);
                    }
                });
    }

    private void cancelEmailChange() {
        pendingEmail = null;
        showEmailOtp(false);
        binding.editEmail.setText(profile == null ? "" : profile.email);
    }

    private void showEmailOtp(boolean visible) {
        binding.layoutEmailOtp.setVisibility(visible ? View.VISIBLE : View.GONE);
        binding.buttonSendCode.setVisibility(visible ? View.GONE : View.VISIBLE);
        binding.inputEmail.setEnabled(!visible);
        binding.editEmailOtp.setText("");
    }

    private void setEmailLoading(boolean loading) {
        binding.buttonSendCode.setEnabled(!loading);
        binding.buttonConfirmEmail.setEnabled(!loading);
        binding.buttonCancelEmail.setEnabled(!loading);
    }

    private void changePassword() {
        String password = UiUtils.text(binding.editPassword);
        if (!FormRules.isStrongPassword(password)) {
            binding.inputPassword.setError(getString(R.string.password_policy));
            return;
        }
        binding.inputPassword.setError(null);
        binding.buttonChangePassword.setEnabled(false);
        ApiClient.get(this).updateMe(new ApiRequests.UpdateProfile(password))
                .enqueue(new ApiCallback<UserProfile>(this) {
                    @Override
                    protected void onSuccess(UserProfile body) {
                        binding.buttonChangePassword.setEnabled(true);
                        binding.editPassword.setText("");
                        Toast.makeText(ProfileActivity.this, R.string.profile_password_changed, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onError(String message) {
                        binding.buttonChangePassword.setEnabled(true);
                        super.onError(message);
                    }
                });
    }
}
