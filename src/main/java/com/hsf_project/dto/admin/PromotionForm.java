package com.hsf_project.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Form tạo / sửa voucher (Promotion) trong khu admin. */
public class PromotionForm {

    @NotBlank(message = "Vui lòng nhập mã voucher")
    @Size(max = 20, message = "Mã voucher tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Mã chỉ gồm chữ, số, gạch ngang/gạch dưới")
    private String code;

    @NotBlank(message = "Vui lòng nhập tên voucher")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    @NotBlank(message = "Vui lòng chọn loại giảm giá")
    @Pattern(regexp = "PERCENT|FIXED", message = "Loại giảm giá không hợp lệ")
    private String discountType;

    @NotNull(message = "Vui lòng nhập giá trị giảm")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @NotNull(message = "Vui lòng chọn thời gian bắt đầu")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @NotNull(message = "Vui lòng chọn thời gian kết thúc")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @Min(value = 1, message = "Giới hạn lượt dùng phải từ 1 trở lên")
    private Integer usageLimit; // null = không giới hạn

    @NotBlank(message = "Vui lòng chọn trạng thái")
    @Pattern(regexp = "ACTIVE|INACTIVE", message = "Trạng thái không hợp lệ")
    private String status = "ACTIVE";

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
