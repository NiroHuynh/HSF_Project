package com.hsf_project.service.admin;

import com.hsf_project.dto.admin.response.AdminAccountResponse;

import java.util.List;

public interface AdminAccountService {
    List<AdminAccountResponse> getAccounts(String keyword, String roleFilter);

    long getTotalCount();

    long getActiveCount();

    long getLockedCount();

    void toggleStatus(Long id, String newStatus);

    void deleteAccount(Long id);

    void createAccount(String email, String password, String firstName, String lastName, String roleId, String phoneNumber, String cinemaId);

    AdminAccountResponse getAccountById(Long id);

    void updateAccount(Long id, String email, String firstName, String lastName, String phoneNumber, String roleId, String cinemaId);
}
