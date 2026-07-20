package com.hsf_project.controller.admin;

import com.hsf_project.dto.admin.request.AdminAccountForm;
import com.hsf_project.dto.admin.response.AdminAccountResponse;
import com.hsf_project.dto.common.ApiResponse;
import com.hsf_project.service.admin.AdminAccountService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint JSON của trang quản lý tài khoản.
 *
 * Là @RestController để GlobalRestExceptionHandler bắt exception và trả JSON
 * {code, message} — nhờ đó lỗi nghiệp vụ (email trùng, số điện thoại trùng...)
 * hiện đúng thông báo trên modal thay vì "Lỗi kết nối mạng".
 */
@RestController
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminAccountRestController {

    AdminAccountService adminAccountService;

    @PostMapping("/toggle-status")
    public ApiResponse<Void> toggleStatus(@RequestParam Long id, @RequestParam String status) {
        adminAccountService.toggleStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<Void> deleteAccount(@PathVariable Long id) {
        adminAccountService.deleteAccount(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/create")
    public ApiResponse<Void> createAccount(@Valid @RequestBody AdminAccountForm form) {
        adminAccountService.createAccount(form);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminAccountResponse> getAccount(@PathVariable Long id) {
        return ApiResponse.success(adminAccountService.getAccountById(id));
    }

    @PostMapping("/update/{id}")
    public ApiResponse<Void> updateAccount(@PathVariable Long id,
                                           @Valid @RequestBody AdminAccountForm form) {
        adminAccountService.updateAccount(id, form);
        return ApiResponse.success(null);
    }
}
