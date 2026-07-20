package com.hsf_project.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Form đăng ký tài khoản mới (mặc định role CUSTOMER). */
public class RegisterForm {

    // max 50 để khớp cột users.first_name/last_name (VARCHAR 50);
    // để 100 thì Hibernate ném ConstraintViolationException lúc persist -> lỗi 500.
    // \p{L} khớp cả chữ có dấu tiếng Việt; chặn chữ số và ký tự đặc biệt trong tên người.
    private static final String NAME_REGEX = "^[\\p{L}\\s'.-]+$";

    @NotBlank(message = "Vui lòng nhập tên")
    @Size(max = 50, message = "Tên tối đa 50 ký tự")
    @Pattern(regexp = NAME_REGEX, message = "Tên chỉ được chứa chữ, không chứa số hay ký tự đặc biệt")
    private String firstName;

    @NotBlank(message = "Vui lòng nhập họ")
    @Size(max = 50, message = "Họ tối đa 50 ký tự")
    @Pattern(regexp = NAME_REGEX, message = "Họ chỉ được chứa chữ, không chứa số hay ký tự đặc biệt")
    private String lastName;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Pattern(regexp = "^$|^0\\d{9}$", message = "Số điện thoại phải gồm 10 số, bắt đầu bằng 0")
    private String phoneNumber;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, max = 50, message = "Mật khẩu từ 6 đến 50 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng nhập lại mật khẩu")
    private String confirmPassword;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
