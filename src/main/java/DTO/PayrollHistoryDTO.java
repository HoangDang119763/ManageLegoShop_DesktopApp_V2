package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PayrollHistoryDTO {
    private int id;
    private int employeeId;
    private LocalDate salaryPeriod;
    private BigDecimal baseSalary;
    private int standardWorkDays;
    private BigDecimal actualWorkDays;
    private BigDecimal bhxhAmount;
    private BigDecimal bhytAmount;
    private BigDecimal bhtnAmount;
    private BigDecimal totalInsurance;
    private BigDecimal violationAmount;
    private BigDecimal rewardAmount;
    private BigDecimal totalAllowance;
    private BigDecimal overtimeAmount;
    private BigDecimal taxableIncome;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal netSalary;
    private LocalDateTime createdAt;

    public PayrollHistoryDTO() {
    }

    public PayrollHistoryDTO(int id, int employeeId, LocalDate salaryPeriod, BigDecimal baseSalary,
            int standardWorkDays, BigDecimal actualWorkDays, BigDecimal bhxhAmount,
            BigDecimal bhytAmount, BigDecimal bhtnAmount, BigDecimal totalInsurance,
            BigDecimal violationAmount, BigDecimal rewardAmount, BigDecimal totalAllowance,
            BigDecimal overtimeAmount, BigDecimal taxableIncome, BigDecimal taxPercent,
            BigDecimal taxAmount, BigDecimal netSalary, LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.salaryPeriod = salaryPeriod;
        this.baseSalary = baseSalary;
        this.standardWorkDays = standardWorkDays;
        this.actualWorkDays = actualWorkDays;
        this.bhxhAmount = bhxhAmount;
        this.bhytAmount = bhytAmount;
        this.bhtnAmount = bhtnAmount;
        this.totalInsurance = totalInsurance;
        this.violationAmount = violationAmount;
        this.rewardAmount = rewardAmount;
        this.totalAllowance = totalAllowance;
        this.overtimeAmount = overtimeAmount;
        this.taxableIncome = taxableIncome;
        this.taxPercent = taxPercent;
        this.taxAmount = taxAmount;
        this.netSalary = netSalary;
        this.createdAt = createdAt;
    }

    public PayrollHistoryDTO(PayrollHistoryDTO other) {
        this.id = other.id;
        this.employeeId = other.employeeId;
        this.salaryPeriod = other.salaryPeriod;
        this.baseSalary = other.baseSalary;
        this.standardWorkDays = other.standardWorkDays;
        this.actualWorkDays = other.actualWorkDays;
        this.bhxhAmount = other.bhxhAmount;
        this.bhytAmount = other.bhytAmount;
        this.bhtnAmount = other.bhtnAmount;
        this.totalInsurance = other.totalInsurance;
        this.violationAmount = other.violationAmount;
        this.rewardAmount = other.rewardAmount;
        this.totalAllowance = other.totalAllowance;
        this.overtimeAmount = other.overtimeAmount;
        this.taxableIncome = other.taxableIncome;
        this.taxPercent = other.taxPercent;
        this.taxAmount = other.taxAmount;
        this.netSalary = other.netSalary;
        this.createdAt = other.createdAt;
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

    public BigDecimal getTemporarySalary() {
        return baseSalary;
    }

    public void setTemporarySalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public int getStandardWorkDays() {
        return standardWorkDays;
    }

    public void setStandardWorkDays(int standardWorkDays) {
        this.standardWorkDays = standardWorkDays;
    }

    public BigDecimal getActualWorkDays() {
        return actualWorkDays;
    }

    public void setActualWorkDays(BigDecimal actualWorkDays) {
        this.actualWorkDays = actualWorkDays;
    }

    public BigDecimal getBhxhAmount() {
        return bhxhAmount;
    }

    public void setBhxhAmount(BigDecimal bhxhAmount) {
        this.bhxhAmount = bhxhAmount;
    }

    public BigDecimal getBhytAmount() {
        return bhytAmount;
    }

    public void setBhytAmount(BigDecimal bhytAmount) {
        this.bhytAmount = bhytAmount;
    }

    public BigDecimal getBhtnAmount() {
        return bhtnAmount;
    }

    public void setBhtnAmount(BigDecimal bhtnAmount) {
        this.bhtnAmount = bhtnAmount;
    }

    public BigDecimal getTotalInsurance() {
        return totalInsurance;
    }

    public void setTotalInsurance(BigDecimal totalInsurance) {
        this.totalInsurance = totalInsurance;
    }

    public BigDecimal getViolationAmount() {
        return violationAmount;
    }

    public void setViolationAmount(BigDecimal violationAmount) {
        this.violationAmount = violationAmount;
    }

    public BigDecimal getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(BigDecimal rewardAmount) {
        this.rewardAmount = rewardAmount;
    }

    public BigDecimal getTotalAllowance() {
        return totalAllowance;
    }

    public void setTotalAllowance(BigDecimal totalAllowance) {
        this.totalAllowance = totalAllowance;
    }

    public BigDecimal getOvertimeAmount() {
        return overtimeAmount;
    }

    public void setOvertimeAmount(BigDecimal overtimeAmount) {
        this.overtimeAmount = overtimeAmount;
    }

    public BigDecimal getTaxableIncome() {
        return taxableIncome;
    }

    public void setTaxableIncome(BigDecimal taxableIncome) {
        this.taxableIncome = taxableIncome;
    }

    public BigDecimal getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(BigDecimal taxPercent) {
        this.taxPercent = taxPercent;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PayrollHistoryDTO{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", salaryPeriod=" + salaryPeriod +
                ", netSalary=" + netSalary +
                '}';
    }
}
