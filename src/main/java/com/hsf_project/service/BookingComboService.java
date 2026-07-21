package com.hsf_project.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface BookingComboService {
    void saveBookingCombos(String bookingCode, Map<String, String> allParams);
}
