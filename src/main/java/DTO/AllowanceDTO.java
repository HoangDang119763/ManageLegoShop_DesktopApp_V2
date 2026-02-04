package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AllowanceDTO {
    private int id;
    private int employeeId;
    private LocalDate salaryPeriod;
    private BigDecimal attendanceBonus;
    private BigDecimal annualLeaveDays;
    private BigDecimal transportationSupport;
    private BigDecimal accommodationSupport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AllowanceDTO() {
    }

    public AllowanceDTO(int id, int employeeId, LocalDate salaryPeriod, BigDecimal attendanceBonus,
            BigDecimal annualLeaveDays, BigDecimal transportationSupport, BigDecimal accommodationSupport,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.salaryPeriod = salaryPeriod;
        this.attendanceBonus = attendanceBonus;
        this.annualLeaveDays = annualLeaveDays;
        this.transportationSupport = transportationSupport;
        this.accommodationSupport = accommodationSupport;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public AllowanceDTO(AllowanceDTO other) {
        this.id = other.id;
        this.employeeId = other.employeeId;
        this.salaryPeriod = other.salaryPeriod;
        this.attendanceBonus = other.attendanceBonus;
        this.annualLeaveDays = other.annualLeaveDays;
        this.transportationSupport = other.transportationSupport;
        this.accommodationSupport = other.accommodationSupport;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getSalaryPeriod() {
        return salaryPeriod;
    }

    public void setSalaryPeriod(LocalDate salaryPeriod) {
        this.salaryPeriod = salaryPeriod;
    }

    public BigDecimal getAttendanceBonus() {
        return attendanceBonus;
    }

    public void setAttendanceBonus(BigDecimal attendanceBonus) {
        this.attendanceBonus = attendanceBonus;
    }

    public BigDecimal getAnnualLeaveDays() {
        return annualLeaveDays;
    }

    public void setAnnualLeaveDays(BigDecimal annualLeaveDays) {
        this.annualLeaveDays = annualLeaveDays;
    }

    public BigDecimal getTransportationSupport() {
        return transportationSupport;
    }

    public void setTransportationSupport(BigDecimal transportationSupport) {
        this.transportationSupport = transportationSupport;
    }

    public BigDecimal getAccommodationSupport() {
        return accommodationSupport;
    }

    public void setAccommodationSupport(BigDecimal accommodationSupport) {
        this.accommodationSupport = accommodationSupport;
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

    public BigDecimal getTotalAllowance() {
        BigDecimal total = BigDecimal.ZERO;
        if (attendanceBonus != null)
            total = total.add(attendanceBonus);
        if (transportationSupport != null)
            total = total.add(transportationSupport);
        if (accommodationSupport != null)
            total = total.add(accommodationSupport);
        return total;
    }

    @Override
    public String toString() {
        return "AllowanceDTO{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", salaryPeriod=" + salaryPeriod +
                ", attendanceBonus=" + attendanceBonus +
                ", annualLeaveDays=" + annualLeaveDays +
                ", transportationSupport=" + transportationSupport +
                ", accommodationSupport=" + accommodationSupport +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
