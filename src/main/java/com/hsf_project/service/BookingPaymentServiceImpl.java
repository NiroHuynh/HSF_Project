package com.hsf_project.service;

import com.hsf_project.entity.PaymentMethod;
import com.hsf_project.repository.BookingPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingPaymentServiceImpl implements BookingPaymentService {

    @Autowired
    private BookingPaymentRepository bookingPaymentRepository;

    @Override
    public List<PaymentMethod> getActiveMethods() {
        return bookingPaymentRepository.findByIsActiveTrueOrderByIdAsc();
    }
}