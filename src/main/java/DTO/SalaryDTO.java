package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalaryDTO {
    private int id;
    private BigDecimal base;
    private BigDecimal coefficient;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SalaryDTO() {
    }

    public SalaryDTO(int id, BigDecimal base, BigDecimal coefficient, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.base = base;
        this.coefficient = coefficient;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public SalaryDTO(SalaryDTO other) {
        if (other != null) {
            this.id = other.id;
            this.base = other.base;
            this.coefficient = other.coefficient != null ? new BigDecimal(other.coefficient.toString()) : null;
            this.createdAt = other.createdAt;
            this.updatedAt = other.updatedAt;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(BigDecimal coefficient) {
        this.coefficient = coefficient;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
