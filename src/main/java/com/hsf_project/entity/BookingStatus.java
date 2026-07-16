package com.hsf_project.entity;

/**
 * Vòng đời booking: PENDING → CONFIRMED → EXPORTED,
 * hoặc CANCELED khi thanh toán thất bại / hết hạn giữ ghế.
 * Dùng thống nhất enum này, không hard-code chuỗi status.
 */
public enum BookingStatus {

    PENDING,    // Chờ thanh toán
    CONFIRMED,  // Đã thanh toán
    CANCELED,   // Đã hủy
    EXPORTED    // Hoàn thành / Đã xuất vé
}
