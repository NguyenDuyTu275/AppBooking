package com.nhom2.appbooking.ui.admin;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.nhom2.appbooking.util.ReceiptPhotoSize;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Đọc ảnh hóa đơn vừa chụp hoặc chọn thành phần "file" của POST /admin/receipts/scan:
 * thu nhỏ về cạnh dài 1600px, xoay theo EXIF của máy ảnh và nén JPEG để gửi nhanh, dưới giới hạn 5MB.
 */
final class ReceiptPhotos {

    interface Callback {
        void onReady(MultipartBody.Part part);

        void onError();
    }

    private static final String TAG = "ReceiptPhotos";
    private static final String FORM_FIELD = "file";
    private static final String FILE_NAME = "receipt.jpg";
    private static final int JPEG_QUALITY = 85;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ReceiptPhotos() {
    }

    /** Xử lý ảnh ở luồng nền; kết quả trả về trên luồng chính. */
    static void read(Context context, Uri uri, Callback callback) {
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        IO.execute(() -> {
            try {
                byte[] jpeg = toJpeg(resolver, uri);
                RequestBody body = RequestBody.create(jpeg, MediaType.parse("image/jpeg"));
                MultipartBody.Part part = MultipartBody.Part.createFormData(FORM_FIELD, FILE_NAME, body);
                MAIN.post(() -> callback.onReady(part));
            } catch (IOException | SecurityException | IllegalArgumentException e) {
                Log.w(TAG, "Cannot read the receipt photo", e);
                MAIN.post(callback::onError);
            }
        });
    }

    private static byte[] toJpeg(ContentResolver resolver, Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = open(resolver, uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IllegalArgumentException("Not an image: " + uri);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = ReceiptPhotoSize.sampleSize(bounds.outWidth, bounds.outHeight, ReceiptPhotoSize.MAX_SIDE);
        Bitmap decoded;
        try (InputStream in = open(resolver, uri)) {
            decoded = BitmapFactory.decodeStream(in, null, options);
        }
        if (decoded == null) {
            throw new IllegalArgumentException("Cannot decode: " + uri);
        }

        Bitmap upright = rotate(decoded, rotationDegrees(resolver, uri));
        int[] size = ReceiptPhotoSize.fitWithin(upright.getWidth(), upright.getHeight(), ReceiptPhotoSize.MAX_SIDE);
        Bitmap scaled = Bitmap.createScaledBitmap(upright, size[0], size[1], true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        return out.toByteArray();
    }

    private static InputStream open(ContentResolver resolver, Uri uri) throws IOException {
        InputStream in = resolver.openInputStream(uri);
        if (in == null) {
            throw new IOException("No stream for " + uri);
        }
        return in;
    }

    /** Máy ảnh thường lưu ảnh nằm ngang kèm cờ xoay EXIF; Gemini đọc chữ tốt hơn khi ảnh đứng đúng chiều. */
    private static int rotationDegrees(ContentResolver resolver, Uri uri) {
        try (InputStream in = open(resolver, uri)) {
            int orientation = new ExifInterface(in)
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                default:
                    return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }

    private static Bitmap rotate(Bitmap bitmap, int degrees) {
        if (degrees == 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
