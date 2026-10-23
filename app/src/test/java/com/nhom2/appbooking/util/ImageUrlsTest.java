package com.nhom2.appbooking.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ImageUrlsTest {

    private static final String API = "https://api.duytu205.id.vn/";

    @Test
    public void keepsAbsoluteUrls() {
        String url = "https://madpcgneaunzuwvkugtu.supabase.co/storage/v1/object/public/images/hotels/a.jpg";
        assertEquals(url, ImageUrls.resolve(url, API));
    }

    @Test
    public void prefixesRelativeUploadsWithTheBackend() {
        assertEquals("https://api.duytu205.id.vn/uploads/rooms/x.jpg",
                ImageUrls.resolve("/uploads/rooms/x.jpg", API));
        assertEquals("https://api.duytu205.id.vn/uploads/x.jpg", ImageUrls.resolve("uploads/x.jpg", API));
    }

    @Test
    public void blankMeansNoImage() {
        assertNull(ImageUrls.resolve("  ", API));
        assertNull(ImageUrls.resolve(null, API));
    }
}
