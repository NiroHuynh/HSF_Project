package com.hsf_project.service;

import com.hsf_project.entity.Combo;
import com.hsf_project.repository.BookingComboRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingComboServiceImpl implements BookingComboService {

    @Autowired
    private BookingComboRepository bookingComboRepository;

    @Override
    public List<Combo> getActiveCombos() {
        return bookingComboRepository.findByStatusAndIsDeletedFalseOrderByIdAsc("ACTIVE");
    }

    @Override
    public Combo getById(Long id) {
        return bookingComboRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Combo không tồn tại: " + id));
    }
}
