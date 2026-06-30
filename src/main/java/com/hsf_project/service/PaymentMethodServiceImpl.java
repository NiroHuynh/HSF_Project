package com.hsf_project.service;

import com.hsf_project.entity.PaymentMethod;
import com.hsf_project.repository.PaymentMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Override
    public List<PaymentMethod> getActiveMethods() {
        return paymentMethodRepository.findByIsActiveTrueOrderByIdAsc();
    }
}