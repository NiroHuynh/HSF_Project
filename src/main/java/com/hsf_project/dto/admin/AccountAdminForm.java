package com.hsf_project.dto.admin;

import jakarta.validation.constraints.*;

public class AccountAdminForm {
    @NotBlank @Pattern(regexp = "^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$", message = "Họ chỉ được chứa chữ cái") private String lastName;
    @NotBlank @Pattern(regexp = "^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$", message = "Tên chỉ được chứa chữ cái") private String firstName;
    @NotBlank @Email @Size(max = 150) private String email;
    @Pattern(regexp = "^$|^(0|\\+84)\\d{9}$", message = "Số điện thoại không hợp lệ") private String phoneNumber;
    @NotBlank @Size(min = 8, max = 72) @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Mật khẩu phải có chữ và số") private String password;
    @NotBlank @Pattern(regexp = "ADMIN|MANAGER|CUSTOMER") private String roleName;
    private Integer cinemaId;
    public String getLastName() { return lastName; } public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFirstName() { return firstName; } public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getEmail() { return email; } public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; } public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getPassword() { return password; } public void setPassword(String password) { this.password = password; }
    public String getRoleName() { return roleName; } public void setRoleName(String roleName) { this.roleName = roleName; }
    public Integer getCinemaId() { return cinemaId; } public void setCinemaId(Integer cinemaId) { this.cinemaId = cinemaId; }
}
