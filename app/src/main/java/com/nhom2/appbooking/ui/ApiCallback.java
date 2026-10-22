package com.nhom2.appbooking.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.data.ApiClient;
import com.nhom2.appbooking.data.ApiError;
import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.ui.auth.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback Retrofit dùng chung: bỏ qua kết quả khi màn hình đã đóng, hiện thông báo lỗi của backend,
 * và đưa về màn đăng nhập khi phiên đăng nhập không còn hợp lệ.
 */
public abstract class ApiCallback<T> implements Callback<T> {

    private final Activity activity;
    private final Fragment fragment;

    protected ApiCallback(Activity activity) {
        this.activity = activity;
        this.fragment = null;
    }

    protected ApiCallback(Fragment fragment) {
        this.activity = null;
        this.fragment = fragment;
    }

    protected abstract void onSuccess(T body);

    /** Mặc định hiện thông báo lỗi; ghi đè để xử lý thêm (vd tắt vòng xoay). */
    protected void onError(String message) {
        Context context = context();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public final void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (!isAlive()) {
            return;
        }
        if (response.isSuccessful()) {
            onSuccess(response.body());
            return;
        }
        Context context = context();
        ApiError error = ApiError.from(response);
        if (ApiClient.isOwnApi(call.request()) && isSessionRejected(context, response.code(), error)) {
            backToLogin(context);
            return;
        }
        onError(error.message != null ? error.message : context.getString(R.string.error_generic));
    }

    @Override
    public final void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (!isAlive() || call.isCanceled()) {
            return;
        }
        onError(context().getString(R.string.error_network));
    }

    /**
     * Đang có phiên đăng nhập mà backend trả 401, hoặc 403 mặc định của Spring Security
     * (không kèm mã lỗi nghiệp vụ) = token hết hạn hoặc không còn hợp lệ.
     */
    private static boolean isSessionRejected(Context context, int status, ApiError error) {
        boolean rejected = status == 401 || (status == 403 && error.code == null);
        return rejected && new SessionManager(context).isLoggedIn();
    }

    private static void backToLogin(Context context) {
        new SessionManager(context).clear();
        Toast.makeText(context, R.string.session_expired, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(context, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    private boolean isAlive() {
        if (fragment != null) {
            return fragment.isAdded() && fragment.getView() != null;
        }
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    private Context context() {
        return fragment != null ? fragment.getContext() : activity;
    }
}
