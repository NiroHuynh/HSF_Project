package com.hsf_project.service.admin;

import com.hsf_project.dto.admin.request.AdminAccountForm;
import com.hsf_project.dto.admin.response.AdminAccountResponse;

import java.util.List;

public interface AdminAccountService {
    List<AdminAccountResponse> getAccounts(String keyword, String roleFilter);

    long getTotalCount();

    long getActiveCount();

    long getLockedCount();

    void toggleStatus(Long id, String newStatus);

    void deleteAccount(Long id);

    void createAccount(AdminAccountForm form);

    AdminAccountResponse getAccountById(Long id);

    void updateAccount(Long id, AdminAccountForm form);
}
