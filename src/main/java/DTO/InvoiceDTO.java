package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceDTO {
    private int id;
    private LocalDateTime createdAt;
    private int employeeId;
    private int customerId;
    private String discountCode;
    private BigDecimal discountAmount;
    private BigDecimal totalPrice;
    private int statusId;

    public InvoiceDTO() {
    }

    public InvoiceDTO(int id, LocalDateTime createdAt, int employeeId, int customerId, String discountCode,
            BigDecimal discountAmount, BigDecimal totalPrice, int statusId) {
        this.id = id;
        this.createdAt = createdAt;
        this.employeeId = employeeId;
        this.customerId = customerId;
        this.discountCode = discountCode;
        this.discountAmount = discountAmount;
        this.totalPrice = totalPrice;
        this.statusId = statusId;
    }

    public InvoiceDTO(InvoiceDTO other) {
        this.id = other.id;
        this.createdAt = other.createdAt;
        this.employeeId = other.employeeId;
        this.customerId = other.customerId;
        this.discountCode = other.discountCode;
        this.discountAmount = other.discountAmount;
        this.totalPrice = other.totalPrice;
        this.statusId = other.statusId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}