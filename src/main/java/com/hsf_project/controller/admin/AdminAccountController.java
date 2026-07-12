package com.hsf_project.controller.admin;

import com.hsf_project.dto.admin.response.AdminAccountResponse;
import com.hsf_project.dto.common.ApiResponse;
import com.hsf_project.service.admin.AdminAccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/accounts")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AdminAccountController {

    AdminAccountService adminAccountService;

    @GetMapping
    public String pageAccounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String role,
            Model model) {

        List<AdminAccountResponse> accounts = adminAccountService.getAccounts(keyword, role);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalCount", adminAccountService.getTotalCount());
        model.addAttribute("activeCount", adminAccountService.getActiveCount());
        model.addAttribute("lockedCount", adminAccountService.getLockedCount());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);

        return "adminManagement";
    }

    @PostMapping("/toggle-status")
    @ResponseBody
    public ApiResponse<Void> toggleStatus(@RequestParam Long id, @RequestParam String status) {
        adminAccountService.toggleStatus(id, status);
        return ApiResponse.success(null);
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ApiResponse<Void> deleteAccount(@PathVariable Long id) {
        adminAccountService.deleteAccount(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/create")
    @ResponseBody
    public ApiResponse<Void> createAccount(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        String roleId = body.get("roleId");
        String phoneNumber = body.get("phoneNumber");

        adminAccountService.createAccount(email, password, firstName, lastName, roleId, phoneNumber);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ApiResponse<AdminAccountResponse> getAccount(@PathVariable Long id) {
        return ApiResponse.success(adminAccountService.getAccountById(id));
    }

    @PostMapping("/update/{id}")
    @ResponseBody
    public ApiResponse<Void> updateAccount(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminAccountService.updateAccount(
                id,
                body.get("email"),
                body.get("firstName"),
                body.get("lastName"),
                body.get("phoneNumber"),
                body.get("roleId")
        );
        return ApiResponse.success(null);
    }
}
