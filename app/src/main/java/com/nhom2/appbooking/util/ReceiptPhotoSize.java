package com.nhom2.appbooking.util;

/**
 * Kích thước ảnh hóa đơn gửi lên backend: ảnh máy ảnh 12MP nặng vài MB, vượt giới hạn 5MB của backend
 * và làm Gemini đọc chậm, trong khi cạnh dài 1600px vẫn đủ rõ chữ trên hóa đơn.
 */
public final class ReceiptPhotoSize {

    public static final int MAX_SIDE = 1600;

    private ReceiptPhotoSize() {
    }

    /** inSampleSize lớn nhất (lũy thừa của 2) mà cạnh dài sau khi giải mã vẫn không nhỏ hơn maxSide. */
    public static int sampleSize(int width, int height, int maxSide) {
        int longSide = Math.max(width, height);
        int sample = 1;
        while (longSide / (sample * 2) >= maxSide) {
            sample *= 2;
        }
        return sample;
    }

    /** Thu nhỏ giữ tỉ lệ để cạnh dài không quá maxSide; ảnh nhỏ hơn giữ nguyên. Trả về {rộng, cao}. */
    public static int[] fitWithin(int width, int height, int maxSide) {
        int longSide = Math.max(width, height);
        if (longSide <= maxSide) {
            return new int[]{width, height};
        }
        double scale = (double) maxSide / longSide;
        return new int[]{Math.max(1, (int) Math.round(width * scale)), Math.max(1, (int) Math.round(height * scale))};
    }
}
