package com.hsf_project.dto.response;

import com.hsf_project.entity.PaymentMethod;
import com.hsf_project.entity.ShowTime;

import java.math.BigDecimal;
import java.util.List;

public class PaymentPageData {

    private String bookingCode;
    private long secondsLeft;
    private List<String> selectedSeats;
    private List<SelectedComboDTO> selectedCombos;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private List<PaymentMethod> paymentMethods;
    private ShowTime showtime;

    // Constructor đầy đủ tham số
    public PaymentPageData(String bookingCode, long secondsLeft, List<String> selectedSeats,
                           List<SelectedComboDTO> selectedCombos, BigDecimal totalAmount,
                           BigDecimal discountAmount, BigDecimal finalAmount,
                           List<PaymentMethod> paymentMethods, ShowTime showtime) {
        this.bookingCode = bookingCode;
        this.secondsLeft = secondsLeft;
        this.selectedSeats = selectedSeats;
        this.selectedCombos = selectedCombos;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
        this.paymentMethods = paymentMethods;
        this.showtime = showtime;
    }

    // Getters
    public String getBookingCode() { return bookingCode; }
    public long getSecondsLeft() { return secondsLeft; }
    public List<String> getSelectedSeats() { return selectedSeats; }
    public List<SelectedComboDTO> getSelectedCombos() { return selectedCombos; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public List<PaymentMethod> getPaymentMethods() { return paymentMethods; }
    public ShowTime getShowtime() { return showtime; }

    // Class con
    public static class SelectedComboDTO {
        private String name;
        private int quantity;
        private BigDecimal lineTotal;

        public SelectedComboDTO(String name, int quantity, BigDecimal lineTotal) {
            this.name = name;
            this.quantity = quantity;
            this.lineTotal = lineTotal;
        }

        public String getName() { return name; }
        public int getQuantity() { return quantity; }
        public BigDecimal getLineTotal() { return lineTotal; }
    }
}
