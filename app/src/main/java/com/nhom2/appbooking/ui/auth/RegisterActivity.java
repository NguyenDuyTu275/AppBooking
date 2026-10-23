package com.nhom2.appbooking.ui.auth;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.databinding.ActivityRegisterBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.HomeNavigator;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.util.FormRules;

/** Đăng ký 2 bước: nhập thông tin -> backend gửi OTP qua email -> nhập OTP để tạo tài khoản. */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private String pendingEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.textBrand.setText(UiUtils.brand(this));
        binding.divider.textDivider.setText(R.string.or_register_with);

        binding.buttonRegister.setOnClickListener(v -> register());
        binding.buttonGoogle.setOnClickListener(v -> GoogleSignInFlow.start(this, new GoogleSignInFlow.Listener() {
            @Override
            public void onLoading(boolean loading) {
                setLoading(loading);
            }

            @Override
            public void onSignedIn() {
                // Màn đăng nhập vẫn nằm dưới màn này: xóa cả hai để nút Back không quay lại form
                HomeNavigator.openHome(RegisterActivity.this);
            }
        }));
        binding.buttonVerify.setOnClickListener(v -> verifyOtp());
        binding.linkLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String username = UiUtils.text(binding.editUsername);
        String email = UiUtils.text(binding.editEmail);
        String password = UiUtils.text(binding.editPassword);
        String confirm = UiUtils.text(binding.editConfirm);
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!FormRules.isStrongPassword(password)) {
            Toast.makeText(this, R.string.password_policy, Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(confirm)) {
            Toast.makeText(this, R.string.password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        ApiClient.get(this)
                .register(new ApiRequests.Register(username, password, email))
                .enqueue(new ApiCallback<String>(this) {
                    @Override
                    protected void onSuccess(String message) {
                        setLoading(false);
                        pendingEmail = email;
                        binding.formSection.setVisibility(View.GONE);
                        binding.otpSection.setVisibility(View.VISIBLE);
                        binding.textOtpInfo.setText(getString(R.string.otp_sent, email));
                    }

                    @Override
                    protected void onError(String message) {
                        setLoading(false);
                        super.onError(message);
                    }
                });
    }

    private void verifyOtp() {
        String otp = UiUtils.text(binding.editOtp);
        if (otp.isEmpty() || pendingEmail == null) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        ApiClient.get(this)
                .verifyOtp(new ApiRequests.VerifyOtp(pendingEmail, otp))
                .enqueue(new ApiCallback<String>(this) {
                    @Override
                    protected void onSuccess(String message) {
                        Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    protected void onError(String message) {
                        setLoading(false);
                        super.onError(message);
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.buttonRegister.setEnabled(!loading);
        binding.buttonVerify.setEnabled(!loading);
        binding.buttonGoogle.setEnabled(!loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
