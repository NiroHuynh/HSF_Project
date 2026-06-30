package com.hsf_project.service;

import com.hsf_project.entity.PaymentMethod;
import com.hsf_project.repository.BookingPaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingPaymentMethodServiceImpl implements BookingPaymentMethodService {

    @Autowired
    private BookingPaymentMethodRepository bookingPaymentMethodRepository;

    @Override
    public List<PaymentMethod> getActiveMethods() {
        return bookingPaymentMethodRepository.findByIsActiveTrueOrderByIdAsc();
    }
}