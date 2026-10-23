package com.nhom2.appbooking.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ImageUploadsTest {

    @Test
    public void acceptsTheSameImageTypesAsTheWebsite() {
        assertTrue(ImageUploads.isAllowedType("image/jpeg"));
        assertTrue(ImageUploads.isAllowedType("IMAGE/PNG"));
        assertTrue(ImageUploads.isAllowedType("image/webp"));
        assertTrue(ImageUploads.isAllowedType("image/gif"));
        assertFalse(ImageUploads.isAllowedType("image/heic"));
        assertFalse(ImageUploads.isAllowedType("application/pdf"));
        assertFalse(ImageUploads.isAllowedType(null));
    }

    @Test
    public void sizeLimitMatchesTheBackend() {
        assertTrue(ImageUploads.isAllowedSize(ImageUploads.MAX_BYTES));
        assertFalse(ImageUploads.isAllowedSize(ImageUploads.MAX_BYTES + 1));
        assertFalse(ImageUploads.isAllowedSize(0));
    }
}
