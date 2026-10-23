package com.nhom2.appbooking.data.model;

/** Số liệu tổng quan cho admin (GET /admin/statistics/overview). */
public class AdminOverview {
    public long totalUsers;
    public long totalAdmins;
    public long totalHotels;
    public long totalRooms;
    public long totalBookings;
    public long pendingBookings;
    public long confirmedBookings;
    public long rejectedBookings;
    public long cancelledBookings;
    public long pendingRequests;
    /** Tổng tiền các đơn đã thanh toán; null khi chưa có đơn nào được trả. */
    public Double paidRevenue;

    /** Đơn đã kết thúc mà không ở lại: hủy hoặc bị từ chối (gộp chung một phần như biểu đồ trên web). */
    public long closedBookings() {
        return cancelledBookings + rejectedBookings;
    }
}
