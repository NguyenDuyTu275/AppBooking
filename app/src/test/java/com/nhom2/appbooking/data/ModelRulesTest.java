package com.nhom2.appbooking.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.nhom2.appbooking.data.model.Booking;
import com.nhom2.appbooking.data.model.Hotel;
import com.nhom2.appbooking.data.model.Room;
import com.nhom2.appbooking.data.model.RoomImage;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModelRulesTest {

    private static Room room(double price, String image) {
        Room room = new Room();
        room.pricePerNight = price;
        if (image != null) {
            RoomImage roomImage = new RoomImage();
            roomImage.imageUrl = image;
            room.images = Collections.singletonList(roomImage);
        }
        return room;
    }

    @Test
    public void coverFallsBackToFirstRoomImage() {
        Hotel hotel = new Hotel();
        hotel.rooms = Arrays.asList(room(2_000_000, null), room(1_200_000, "rooms/a.jpg"));

        assertEquals("rooms/a.jpg", hotel.coverImage());
        hotel.imageUrl = "hotels/h.jpg";
        assertEquals("hotels/h.jpg", hotel.coverImage());
    }

    @Test
    public void allImagesListsHotelThenRoomsWithoutDuplicates() {
        Hotel hotel = new Hotel();
        hotel.imageUrl = "h.jpg";
        hotel.rooms = Arrays.asList(room(1, "a.jpg"), room(2, "a.jpg"), room(3, "b.jpg"));

        assertEquals(Arrays.asList("h.jpg", "a.jpg", "b.jpg"), hotel.allImages());
    }

    @Test
    public void minPriceIsCheapestRoom() {
        Hotel hotel = new Hotel();
        assertNull(hotel.minPrice());
        hotel.rooms = Arrays.asList(room(2_000_000, null), room(1_200_000, null));
        assertEquals(1_200_000, hotel.minPrice(), 0.001);
    }

    private static Hotel hotel(String name, Double rating) {
        Hotel hotel = new Hotel();
        hotel.name = name;
        hotel.ratingAvg = rating;
        return hotel;
    }

    @Test
    public void featuredOrderIsHighestRatingFirstAndUnratedLast() {
        List<Hotel> hotels = new ArrayList<>(Arrays.asList(
                hotel("unrated", null), hotel("ok", 6.0), hotel("good", 7.0), hotel("also ok", 6.0)));

        hotels.sort(Hotel.BY_RATING_DESC);

        List<String> names = new ArrayList<>();
        for (Hotel h : hotels) {
            names.add(h.name);
        }
        // Cùng điểm thì giữ thứ tự backend trả về
        assertEquals(Arrays.asList("good", "ok", "also ok", "unrated"), names);
    }

    @Test
    public void adminActionsFollowTheWebsiteRules() {
        Booking booking = new Booking();
        booking.status = Booking.STATUS_PENDING;
        booking.paymentStatus = Booking.PAYMENT_UNPAID;
        assertTrue(booking.canAdminDecide());
        assertTrue(booking.canAdminMarkPaid());

        booking.status = Booking.STATUS_CONFIRMED;
        assertFalse(booking.canAdminDecide());
        assertTrue(booking.canAdminMarkPaid());

        booking.status = Booking.STATUS_CANCELLED;
        assertFalse(booking.canAdminMarkPaid());
        booking.status = Booking.STATUS_REJECTED;
        assertFalse(booking.canAdminMarkPaid());

        booking.status = Booking.STATUS_CONFIRMED;
        booking.paymentStatus = Booking.PAYMENT_PAID;
        assertFalse(booking.canAdminMarkPaid());
    }

    @Test
    public void onlyUnpaidPendingOrConfirmedBookingsCanBePaid() {
        Booking booking = new Booking();
        booking.id = 47;
        booking.status = Booking.STATUS_CONFIRMED;
        booking.paymentStatus = "UNPAID";
        assertTrue(booking.canPay());
        assertEquals("BK000047", booking.code());

        booking.paymentStatus = Booking.PAYMENT_PAID;
        assertFalse(booking.canPay());

        booking.paymentStatus = "UNPAID";
        booking.status = Booking.STATUS_CANCELLED;
        assertFalse(booking.canPay());
    }
}
