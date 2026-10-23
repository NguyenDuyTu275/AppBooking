package com.nhom2.appbooking.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Khách sạn như backend trả về (GET /hotels, GET /hotels/{id}). */
public class Hotel {

    /** Điểm cao trước, chưa có điểm xuống cuối: thứ tự của mục "Khách sạn nổi bật" trên web. */
    public static final Comparator<Hotel> BY_RATING_DESC =
            (a, b) -> Double.compare(scoreOrZero(b), scoreOrZero(a));

    public long id;
    public String name;
    public String address;
    public String city;
    public String description;
    public String imageUrl;
    public Double ratingAvg;
    public Long reviewCount;
    // PUT /hotels/{id} lưu đè cả bản ghi: admin phải gửi lại hai trường này khi sửa khách sạn
    public String status;
    public String createdAt;
    public List<Room> rooms = new ArrayList<>();

    public List<Room> roomsOrEmpty() {
        return rooms == null ? Collections.emptyList() : rooms;
    }

    /** Ảnh đại diện: ảnh khách sạn, thiếu thì lấy ảnh của phòng đầu tiên có ảnh. */
    public String coverImage() {
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            return imageUrl;
        }
        for (Room room : roomsOrEmpty()) {
            String image = room.firstImage();
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    /** Tất cả ảnh để xem: ảnh khách sạn rồi tới ảnh các phòng, bỏ ảnh trùng. */
    public List<String> allImages() {
        Set<String> images = new LinkedHashSet<>();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            images.add(imageUrl);
        }
        for (Room room : roomsOrEmpty()) {
            for (RoomImage image : room.imagesOrEmpty()) {
                if (image != null && image.imageUrl != null && !image.imageUrl.trim().isEmpty()) {
                    images.add(image.imageUrl);
                }
            }
        }
        return new ArrayList<>(images);
    }

    private static double scoreOrZero(Hotel hotel) {
        return hotel.ratingAvg == null ? 0 : hotel.ratingAvg;
    }

    /** Giá một đêm rẻ nhất trong các phòng; null nếu chưa phòng nào có giá. */
    public Double minPrice() {
        Double min = null;
        for (Room room : roomsOrEmpty()) {
            if (room.pricePerNight != null && (min == null || room.pricePerNight < min)) {
                min = room.pricePerNight;
            }
        }
        return min;
    }
}
