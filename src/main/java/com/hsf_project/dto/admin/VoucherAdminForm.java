package com.hsf_project.dto.admin;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VoucherAdminForm {
    private Long id;
    @NotBlank @Pattern(regexp = "^[A-Z0-9_-]{3,50}$", message = "Mã chỉ gồm chữ hoa, số, _ hoặc -") private String code;
    @NotBlank @Size(max = 100) private String name;
    @Size(max = 255) private String description;
    @NotBlank @Pattern(regexp = "PERCENT|FIXED") private String discountType;
    @NotNull @DecimalMin("0.01") private BigDecimal discountValue;
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") private LocalDateTime startDate;
    @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") private LocalDateTime endDate;
    @Min(1) private Integer usageLimit;
    @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE") private String status;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCode() { return code; } public void setCode(String code) { this.code = code; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getDiscountType() { return discountType; } public void setDiscountType(String discountType) { this.discountType = discountType; }
    public BigDecimal getDiscountValue() { return discountValue; } public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }
    public LocalDateTime getStartDate() { return startDate; } public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; } public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public Integer getUsageLimit() { return usageLimit; } public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
}
