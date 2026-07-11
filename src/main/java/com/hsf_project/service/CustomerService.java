package com.hsf_project.service;

import com.hsf_project.dto.response.CustomerRowDTO;

import java.util.List;

public interface CustomerService {

    // Lấy danh sách khách hàng + thống kê theo cinema của staff
    List<CustomerRowDTO> getCustomersByCinema(Integer cinemaId);
}