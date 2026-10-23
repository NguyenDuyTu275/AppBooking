package com.nhom2.appbooking.ui;

import android.content.Context;
import android.content.Intent;

import com.nhom2.appbooking.data.SessionManager;
import com.nhom2.appbooking.ui.admin.AdminActivity;

/**
 * Chọn màn hình chính theo vai trò, như web: admin vào trang quản trị (/admin), khách vào trang đặt phòng.
 * Chặn ở app chỉ để dẫn đường; backend vẫn kiểm tra quyền ở mọi request.
 */
public final class HomeNavigator {

    private HomeNavigator() {
    }

    public static Intent homeIntent(Context context) {
        boolean admin = new SessionManager(context).isAdmin();
        return new Intent(context, admin ? AdminActivity.class : MainActivity.class);
    }

    /** Mở màn hình chính và xóa các màn trước đó (đăng nhập, đăng ký) để nút Back không quay lại. */
    public static void openHome(Context context) {
        context.startActivity(homeIntent(context)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }
}
