package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FineDTO {
    private int id;
    private String reason;
    private LocalDateTime createdAt;
    private String fineLevel;
    private BigDecimal amount;
    private BigDecimal finePay;
    private int employeeId;

    public FineDTO() {
    }

    public FineDTO(int id, String reason, LocalDateTime createdAt, String fineLevel, BigDecimal amount,
            BigDecimal finePay, int employeeId) {
        this.id = id;
        this.reason = reason;
        this.createdAt = createdAt;
        this.fineLevel = fineLevel;
        this.amount = amount;
        this.finePay = finePay;
        this.employeeId = employeeId;
    }

    public FineDTO(FineDTO other) {
        this.id = other.id;
        this.reason = other.reason;
        this.createdAt = other.createdAt;
        this.fineLevel = other.fineLevel;
        this.amount = other.amount;
        this.finePay = other.finePay;
        this.employeeId = other.employeeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFineLevel() {
        return fineLevel;
    }

    public void setFineLevel(String fineLevel) {
        this.fineLevel = fineLevel;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getFinePay() {
        return finePay;
    }

    public void setFinePay(BigDecimal finePay) {
        this.finePay = finePay;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String toString() {
        return "FineDTO{" +
                "id=" + id +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                ", fineLevel='" + fineLevel + '\'' +
                ", amount=" + amount +
                ", finePay=" + finePay +
                ", employeeId=" + employeeId +
                '}';
    }
}
