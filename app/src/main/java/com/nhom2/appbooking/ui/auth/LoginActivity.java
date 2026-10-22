package com.nhom2.appbooking.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiRequests;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.databinding.ActivityLoginBinding;
import com.nhom2.appbooking.ui.ApiCallback;
import com.nhom2.appbooking.ui.MainActivity;
import com.nhom2.appbooking.ui.UiUtils;

/** Màn hình mở app: đăng nhập, hoặc vào thẳng trang chính nếu còn phiên đăng nhập. */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(this);
        if (session.isLoggedIn()) {
            if (!isTaskRoot()) {
                // Bấm icon khi app đang mở dở (vd quay lại từ app ngân hàng): giữ nguyên màn hình đang có
                finish();
            } else {
                openMain();
            }
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonLogin.setOnClickListener(v -> login());
        binding.editPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login();
                return true;
            }
            return false;
        });
        binding.linkRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String username = UiUtils.text(binding.editUsername);
        String password = UiUtils.text(binding.editPassword);
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        ApiClient.get(this)
                .login(new ApiRequests.Login(username, password))
                .enqueue(new ApiCallback<String>(this) {
                    @Override
                    protected void onSuccess(String token) {
                        try {
                            session.save(token);
                            openMain();
                        } catch (IllegalArgumentException e) {
                            onError(getString(R.string.error_generic));
                        }
                    }

                    @Override
                    protected void onError(String message) {
                        setLoading(false);
                        super.onError(message);
                    }
                });
    }

    private void setLoading(boolean loading) {
        binding.buttonLogin.setEnabled(!loading);
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void openMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
