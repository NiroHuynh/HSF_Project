package com.hsf_project.dto;

import jakarta.validation.constraints.*;

public class RegisterForm {
    @NotBlank(message = "Vui lòng nhập họ và tên")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    @Pattern(regexp = "^[\\p{L}]+(?:[ '\\-][\\p{L}]+)*$", message = "Họ tên chỉ được chứa chữ cái")
    private String fullName;
    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 150)
    private String email;
    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Mật khẩu phải có cả chữ và số")
    private String password;
    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String confirmPassword;
    private boolean agreeTerms;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public boolean isAgreeTerms() { return agreeTerms; }
    public void setAgreeTerms(boolean agreeTerms) { this.agreeTerms = agreeTerms; }
}
