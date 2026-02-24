package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ImportDTO {
    private int id;
    private LocalDateTime createdAt;
    private int employeeId;
    private int supplierId;
    private BigDecimal totalPrice;
    private int statusId;

    public ImportDTO() {
    }

    public ImportDTO(int id, LocalDateTime createdAt, int employeeId, int supplierId, BigDecimal totalPrice,
            int statusId) {
        this.id = id;
        this.createdAt = createdAt;
        this.employeeId = employeeId;
        this.supplierId = supplierId;
        this.totalPrice = totalPrice;
        this.statusId = statusId;
    }

    public ImportDTO(ImportDTO other) {
        if (other != null) {
            this.id = other.id;
            this.createdAt = other.createdAt;
            this.employeeId = other.employeeId;
            this.supplierId = other.supplierId;
            this.totalPrice = other.totalPrice;
            this.statusId = other.statusId;
        }
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

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
