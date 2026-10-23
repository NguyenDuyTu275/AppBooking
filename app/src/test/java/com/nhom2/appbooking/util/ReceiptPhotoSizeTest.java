package com.nhom2.appbooking.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReceiptPhotoSizeTest {

    @Test
    public void decodesCameraPhotosAtTheLargestSampleThatStillKeepsEnoughPixels() {
        // 12MP 4000x3000: 4000/2 = 2000 >= 1600 nhưng 4000/4 = 1000 < 1600
        assertEquals(2, ReceiptPhotoSize.sampleSize(4000, 3000, 1600));
        assertEquals(4, ReceiptPhotoSize.sampleSize(3000, 8000, 1600));
        assertEquals(1, ReceiptPhotoSize.sampleSize(1200, 900, 1600));
    }

    @Test
    public void shrinksToTheMaximumSideKeepingTheShape() {
        assertArrayEquals(new int[]{1600, 1200}, ReceiptPhotoSize.fitWithin(2000, 1500, 1600));
        assertArrayEquals(new int[]{900, 1600}, ReceiptPhotoSize.fitWithin(1800, 3200, 1600));
        assertArrayEquals(new int[]{1200, 900}, ReceiptPhotoSize.fitWithin(1200, 900, 1600));
    }
}
