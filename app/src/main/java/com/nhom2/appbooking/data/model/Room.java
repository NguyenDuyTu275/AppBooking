package com.nhom2.appbooking.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Room {
    public long id;
    public String name;
    public String type;
    public Double pricePerNight;
    public Integer capacity;
    public Integer quantity;
    public String description;
    public List<RoomImage> images = new ArrayList<>();

    public List<RoomImage> imagesOrEmpty() {
        return images == null ? Collections.emptyList() : images;
    }

    /** Ảnh đầu tiên của phòng, null nếu phòng chưa có ảnh. */
    public String firstImage() {
        for (RoomImage image : imagesOrEmpty()) {
            if (image != null && image.imageUrl != null && !image.imageUrl.trim().isEmpty()) {
                return image.imageUrl;
            }
        }
        return null;
    }
}
