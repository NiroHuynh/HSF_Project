package com.hsf_project.service;

import com.hsf_project.entity.Combo;
import com.hsf_project.repository.ComboRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComboServiceImpl implements ComboService {

    @Autowired
    private ComboRepository comboRepository;

    @Override
    public List<Combo> getActiveCombos() {
        return comboRepository.findByStatusAndIsDeletedFalseOrderByIdAsc("ACTIVE");
    }

    @Override
    public Combo getById(Long id) {
        return comboRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Combo không tồn tại: " + id));
    }
}
