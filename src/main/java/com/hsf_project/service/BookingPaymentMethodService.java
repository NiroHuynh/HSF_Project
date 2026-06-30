package com.hsf_project.service;

import com.hsf_project.entity.PaymentMethod;

import java.util.List;

public interface BookingPaymentMethodService {
    List<PaymentMethod> getActiveMethods();
}