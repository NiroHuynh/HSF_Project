package com.hsf_project.service;

import com.hsf_project.entity.Combo;

import java.util.List;

public interface BookingComboService {
    List<Combo> getActiveCombos();
    Combo getById(Long id);
}
