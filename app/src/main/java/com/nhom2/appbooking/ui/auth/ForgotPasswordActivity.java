package com.nhom2.appbooking.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.databinding.ActivityForgotPasswordBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.UiUtils;
import com.nhom2.appbooking.util.FormRules;

/** Quên mật khẩu 3 bước như trang /forgot-password của web: tên đăng nhập -> OTP + mật khẩu mới -> xong. */
public class ForgotPasswordActivity extends AppCompatActivity {

    private static final int STEP_USERNAME = 0;
    private static final int STEP_RESET = 1;
    private static final int STEP_DONE = 2;
    private static final int OTP_LENGTH = 6;

    private ActivityForgotPasswordBinding binding;
    private String username;
    private String maskedEmail;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ForgotPasswordActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.textBrand.setText(UiUtils.brand(this));

        binding.buttonSendCode.setOnClickListener(v -> sendCode());
        binding.editUsername.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                sendCode();
                return true;
            }
            return false;
        });
        binding.linkBackToLogin.setOnClickListener(v -> finish());
        binding.buttonReset.setOnClickListener(v -> resetPassword());
        binding.buttonBack.setOnClickListener(v -> showStep(STEP_USERNAME));
        binding.buttonLoginNow.setOnClickListener(v -> finish());

        showStep(STEP_USERNAME);
    }

    private void sendCode() {
        String name = UiUtils.text(binding.editUsername);
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        ApiClient.get(this).forgotPassword(new ApiRequests.ForgotPassword(name))
                .enqueue(new ApiCallback<String>(this) {
                    @Override
                    protected void onSuccess(String email) {
                        setLoading(false);
                        username = name;
                        maskedEmail = email == null ? "" : email;
                        showStep(STEP_RESET);
                        Toast.makeText(ForgotPasswordActivity.this, R.string.forgot_code_sent, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onError(String message) {
                        setLoading(false);
                        super.onError(message);
                    }
                });
    }

    private void resetPassword() {
        String otp = UiUtils.text(binding.editOtp);
        String password = UiUtils.text(binding.editPassword);
        String confirm = UiUtils.text(binding.editConfirm);
        if (otp.length() < OTP_LENGTH) {
            Toast.makeText(this, R.string.forgot_otp_invalid, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean strong = FormRules.isStrongPassword(password);
        binding.inputPassword.setError(strong ? null : getString(R.string.password_policy));
        boolean matches = password.equals(confirm);
        binding.inputConfirm.setError(matches ? null : getString(R.string.password_mismatch));
        if (!strong || !matches) {
            return;
        }
        setLoading(true);
        ApiClient.get(this).resetPassword(new ApiRequests.ResetPassword(username, otp, password))
                .enqueue(new ApiCallback<String>(this) {
                    @Override
                    protected void onSuccess(String message) {
                        setLoading(false);
                        showStep(STEP_DONE);
                        Toast.makeText(ForgotPasswordActivity.this, R.string.forgot_reset_success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void onError(String message) {
                        setLoading(false);
                        super.onError(message);
                    }
                });
    }

    private void showStep(int step) {
        binding.sectionUsername.setVisibility(step == STEP_USERNAME ? View.VISIBLE : View.GONE);
        binding.sectionReset.setVisibility(step == STEP_RESET ? View.VISIBLE : View.GONE);
        binding.sectionDone.setVisibility(step == STEP_DONE ? View.VISIBLE : View.GONE);

        binding.textSubtitle.setVisibility(step == STEP_DONE ? View.GONE : View.VISIBLE);
        binding.textSubtitle.setText(step == STEP_RESET
                ? getString(R.string.forgot_step1_subtitle, maskedEmail)
                : getString(R.string.forgot_step0_subtitle));

        markStep(binding.stepUsername, STEP_USERNAME, step);
        markStep(binding.stepPassword, STEP_RESET, step);
        markStep(binding.stepDone, STEP_DONE, step);
    }

    /** Bước đã qua hoặc đang làm tô màu chính, bước sau để xám. */
    private void markStep(TextView label, int labelStep, int currentStep) {
        int color = labelStep <= currentStep ? R.color.primary : R.color.text_muted;
        label.setTextColor(ContextCompat.getColor(this, color));
    }

    private void setLoading(boolean loading) {
        binding.buttonSendCode.setEnabled(!loading);
        binding.buttonReset.setEnabled(!loading);
        binding.buttonBack.setEnabled(!loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
