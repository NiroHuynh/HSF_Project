package com.hsf_project.service;

import com.hsf_project.dto.customer.response.ActiveCustomerResponse;
import com.hsf_project.dto.customer.response.CustomerDetailResponse;
import com.hsf_project.dto.customer.response.CustomerGrowthResponse;
import com.hsf_project.dto.customer.response.CustomerSummaryResponse;
import com.hsf_project.dto.customer.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CustomerDashboardService {

    CustomerSummaryResponse getCustomerSummary(LocalDate from, LocalDate to);

    List<CustomerGrowthResponse> getCustomerGrowth(String type, LocalDate from, LocalDate to);

    Page<ActiveCustomerResponse> getActiveCustomers(Pageable pageable);

    Page<ActiveCustomerResponse> searchActiveCustomers(String keyword, Pageable pageable);

    CustomerDetailResponse getCustomerDetail(Long id);

    List<TransactionResponse> getRecentTransactions(Long userId, int limit);

    byte[] exportCustomers(LocalDate from, LocalDate to, String format);
}
