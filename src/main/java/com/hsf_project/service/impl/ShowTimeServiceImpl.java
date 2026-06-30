package com.hsf_project.service.impl;

import com.hsf_project.entity.ShowTime;
import com.hsf_project.repository.ShowTimeRepository;
import com.hsf_project.service.ShowTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowTimeServiceImpl implements ShowTimeService {

    @Autowired
    private ShowTimeRepository showTimeRepository;

    @Override
    public ShowTime getShowTimeById(Long id) {
        return showTimeRepository.findById(id).get();
    }
}
