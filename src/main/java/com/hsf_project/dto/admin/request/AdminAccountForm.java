package com.hsf_project.dto.admin.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dữ liệu tạo/sửa tài khoản quản trị từ trang /admin/accounts.
 *
 * Trước đây controller nhận Map<String,String> nên backend không kiểm tra gì cả:
 * form chỉ dựa vào validation mặc định của trình duyệt, gọi thẳng API bằng fetch
 * là tạo được tài khoản tên chứa số, email sai định dạng hay số điện thoại trùng.
 *
 * password để trống khi sửa (không đổi mật khẩu) nên không gắn @NotBlank ở đây —
 * service bắt buộc phải có password khi tạo mới.
 */
public class AdminAccountForm {

    /** \p{L} khớp cả chữ có dấu tiếng Việt; chặn chữ số và ký tự đặc biệt trong tên người. */
    private static final String NAME_REGEX = "^[\\p{L}\\s'.-]+$";

    @NotBlank(message = "Vui lòng nhập họ")
    @Size(max = 50, message = "Họ tối đa 50 ký tự")
    @Pattern(regexp = NAME_REGEX, message = "Họ chỉ được chứa chữ, không chứa số hay ký tự đặc biệt")
    private String lastName;

    @NotBlank(message = "Vui lòng nhập tên")
    @Size(max = 50, message = "Tên tối đa 50 ký tự")
    @Pattern(regexp = NAME_REGEX, message = "Tên chỉ được chứa chữ, không chứa số hay ký tự đặc biệt")
    private String firstName;

    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Size(min = 6, max = 50, message = "Mật khẩu từ 6 đến 50 ký tự")
    private String password;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải gồm 10 số, bắt đầu bằng 0")
    private String phoneNumber;

    @NotNull(message = "Vui lòng chọn vai trò")
    private Integer roleId;

    /** Bắt buộc khi roleId là Manager — kiểm tra trong service vì phụ thuộc roleId. */
    private Integer cinemaId;

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }

    public Integer getCinemaId() { return cinemaId; }
    public void setCinemaId(Integer cinemaId) { this.cinemaId = cinemaId; }
}
