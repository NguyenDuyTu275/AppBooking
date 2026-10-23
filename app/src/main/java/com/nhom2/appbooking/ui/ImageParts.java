package com.nhom2.appbooking.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.StringRes;

import com.nhom2.appbooking.R;
import com.nhom2.appbooking.util.ImageUploads;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/** Đọc ảnh vừa chọn thành phần "file" của request multipart (ảnh khách sạn, phòng, ảnh đại diện). */
public final class ImageParts {

    public interface Callback {
        void onReady(MultipartBody.Part part);

        void onError(@StringRes int message);
    }

    private static final String TAG = "ImageParts";
    private static final String FORM_FIELD = "file";
    private static final String DEFAULT_NAME = "image";
    private static final int BUFFER_SIZE = 8192;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ImageParts() {
    }

    /** Đọc file ở luồng nền; kết quả trả về trên luồng chính. */
    public static void read(Context context, Uri uri, Callback callback) {
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        IO.execute(() -> {
            String mimeType = resolver.getType(uri);
            if (!ImageUploads.isAllowedType(mimeType)) {
                MAIN.post(() -> callback.onError(R.string.admin_image_type_invalid));
                return;
            }
            try (InputStream in = resolver.openInputStream(uri)) {
                if (in == null) {
                    MAIN.post(() -> callback.onError(R.string.admin_image_read_failed));
                    return;
                }
                // Đọc tối đa 1 byte quá giới hạn là đủ biết ảnh có quá lớn hay không
                byte[] bytes = readAtMost(in, ImageUploads.MAX_BYTES + 1);
                if (!ImageUploads.isAllowedSize(bytes.length)) {
                    MAIN.post(() -> callback.onError(R.string.admin_image_too_large));
                    return;
                }
                RequestBody body = RequestBody.create(bytes, MediaType.parse(mimeType));
                MultipartBody.Part part = MultipartBody.Part.createFormData(
                        FORM_FIELD, fileName(resolver, uri, mimeType), body);
                MAIN.post(() -> callback.onReady(part));
            } catch (IOException | SecurityException e) {
                Log.w(TAG, "Cannot read the picked image", e);
                MAIN.post(() -> callback.onError(R.string.admin_image_read_failed));
            }
        });
    }

    private static byte[] readAtMost(InputStream in, long limit) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        long total = 0;
        int read;
        while (total < limit && (read = in.read(buffer, 0, (int) Math.min(buffer.length, limit - total))) != -1) {
            out.write(buffer, 0, read);
            total += read;
        }
        return out.toByteArray();
    }

    private static String fileName(ContentResolver resolver, Uri uri, String mimeType) {
        try (Cursor cursor = resolver.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(0);
                if (name != null && !name.trim().isEmpty()) {
                    return name;
                }
            }
        }
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        return extension == null ? DEFAULT_NAME : DEFAULT_NAME + "." + extension;
    }
}
