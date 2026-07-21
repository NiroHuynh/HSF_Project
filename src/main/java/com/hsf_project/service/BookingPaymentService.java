package com.hsf_project.service;

import com.hsf_project.dto.response.PaymentPageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public interface BookingPaymentService {

    PaymentPageData getPaymentPageData(String bookingCode);

    void cancelBooking(String bookingCode);
}
