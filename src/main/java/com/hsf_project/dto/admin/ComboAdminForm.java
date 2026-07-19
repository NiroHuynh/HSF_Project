package com.hsf_project.dto.admin;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ComboAdminForm {
    private Long id;
    @NotBlank @Size(max = 100) private String name;
    @Size(max = 255) private String description;
    @NotNull @DecimalMin("1000") private BigDecimal price;
    @NotNull @Min(0) private Integer quantity;
    @NotBlank @Pattern(regexp = "ACTIVE|INACTIVE") private String status;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; } public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; } public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
}
