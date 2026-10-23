package com.nhom2.appbooking.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;

/**
 * Ghi file tải từ backend (báo cáo Excel, hóa đơn PDF) vào chỗ người dùng chọn qua hộp thoại "Lưu" của hệ thống,
 * nên app không cần quyền bộ nhớ.
 */
public final class DownloadSaver {

    public interface Done {
        /** Gọi trên luồng chính. */
        void onDone(boolean saved);
    }

    private static final String TAG = "DownloadSaver";
    private static final int BUFFER_SIZE = 8192;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private DownloadSaver() {
    }

    /** Thân trả về từ API @Streaming được đọc dần từ mạng, nên ghi ở luồng nền. */
    public static void save(Context context, ResponseBody body, Uri target, Done done) {
        Context appContext = context.getApplicationContext();
        IO.execute(() -> {
            boolean saved = write(appContext, body, target);
            MAIN.post(() -> done.onDone(saved));
        });
    }

    private static boolean write(Context context, ResponseBody body, Uri target) {
        try (ResponseBody source = body;
             InputStream in = source.byteStream();
             OutputStream out = context.getContentResolver().openOutputStream(target)) {
            if (out == null) {
                return false;
            }
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return true;
        } catch (IOException | SecurityException e) {
            Log.w(TAG, "Cannot save the downloaded file", e);
            return false;
        }
    }
}
