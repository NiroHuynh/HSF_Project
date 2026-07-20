package com.hsf_project.controller.admin;

import com.hsf_project.dto.common.ApiResponse;
import com.hsf_project.dto.customer.response.ActiveCustomerResponse;
import com.hsf_project.dto.customer.response.CustomerDetailResponse;
import com.hsf_project.dto.customer.response.CustomerGrowthResponse;
import com.hsf_project.dto.customer.response.CustomerSummaryResponse;
import com.hsf_project.service.CustomerDashboardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminCustomerController {

    CustomerDashboardService customerDashboardService;

    @GetMapping("/admin/dashboard/customers/summary")
    public ApiResponse<CustomerSummaryResponse> getCustomerSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();
        return ApiResponse.success(customerDashboardService.getCustomerSummary(from, to));
    }

    @GetMapping("/admin/dashboard/customers/growth")
    public ApiResponse<List<CustomerGrowthResponse>> getCustomerGrowth(
            @RequestParam(defaultValue = "month") String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusMonths(11).withDayOfMonth(1);
        if (to == null) to = LocalDate.now();
        return ApiResponse.success(customerDashboardService.getCustomerGrowth(type, from, to));
    }

    @GetMapping("/admin/customers/active")
    public ApiResponse<Page<ActiveCustomerResponse>> getActiveCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookingCount") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String search) {
        if (size > 100) size = 100;
        Sort sortObj = direction.equalsIgnoreCase("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();
        PageRequest pageable = PageRequest.of(page, size, sortObj);

        if (search != null && !search.isBlank()) {
            return ApiResponse.success(customerDashboardService.searchActiveCustomers(search, pageable));
        }
        return ApiResponse.success(customerDashboardService.getActiveCustomers(pageable));
    }

    @GetMapping("/admin/customers/{id}")
    public ApiResponse<CustomerDetailResponse> getCustomerDetail(@PathVariable Long id) {
        return ApiResponse.success(customerDashboardService.getCustomerDetail(id));
    }

    @PostMapping("/admin/customers/{id}/toggle-status")
    public ApiResponse<Void> toggleCustomerStatus(@PathVariable Long id) {
        customerDashboardService.toggleCustomerStatus(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/admin/customers/export")
    public ResponseEntity<byte[]> exportCustomers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "excel") String format) {
        if (from == null) from = LocalDate.now().minusMonths(1);
        if (to == null) to = LocalDate.now();

        byte[] data = customerDashboardService.exportCustomers(from, to, format);
        String filename = "customer-report-" + from + "_to_" + to + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
