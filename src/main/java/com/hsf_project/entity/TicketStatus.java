package com.hsf_project.entity;

/**
 * Vé chỉ có 2 trạng thái: PENDING (đang giữ ghế) và PAID.
 * Hủy vé = soft-delete (isDeleted = true) để giải phóng ghế.
 */
public enum TicketStatus {
    PENDING,
    PAID
}
