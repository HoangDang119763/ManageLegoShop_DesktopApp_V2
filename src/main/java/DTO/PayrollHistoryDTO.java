package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PayrollHistoryDTO {
    private int id;
    private int employeeId;
    private LocalDate salaryPeriod;
    private BigDecimal temporarySalary;
    private BigDecimal overtimeAmount;
    private BigDecimal totalAllowance;
    private BigDecimal totalBonus;
    private BigDecimal totalDeduction;
    private BigDecimal totalFine;
    private BigDecimal netSalary;
    private LocalDateTime paidDate;

    public PayrollHistoryDTO() {
    }

    public PayrollHistoryDTO(int id, int employeeId, LocalDate salaryPeriod, BigDecimal temporarySalary,
            BigDecimal overtimeAmount, BigDecimal totalAllowance, BigDecimal totalBonus,
            BigDecimal totalDeduction, BigDecimal totalFine, BigDecimal netSalary, LocalDateTime paidDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.salaryPeriod = salaryPeriod;
        this.temporarySalary = temporarySalary;
        this.overtimeAmount = overtimeAmount;
        this.totalAllowance = totalAllowance;
        this.totalBonus = totalBonus;
        this.totalDeduction = totalDeduction;
        this.totalFine = totalFine;
        this.netSalary = netSalary;
        this.paidDate = paidDate;
    }

    public PayrollHistoryDTO(PayrollHistoryDTO other) {
        this.id = other.id;
        this.employeeId = other.employeeId;
        this.salaryPeriod = other.salaryPeriod;
        this.temporarySalary = other.temporarySalary;
        this.overtimeAmount = other.overtimeAmount;
        this.totalAllowance = other.totalAllowance;
        this.totalBonus = other.totalBonus;
        this.totalDeduction = other.totalDeduction;
        this.totalFine = other.totalFine;
        this.netSalary = other.netSalary;
        this.paidDate = other.paidDate;
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
        return temporarySalary;
    }

    public void setTemporarySalary(BigDecimal temporarySalary) {
        this.temporarySalary = temporarySalary;
    }

    public BigDecimal getOvertimeAmount() {
        return overtimeAmount;
    }

    public void setOvertimeAmount(BigDecimal overtimeAmount) {
        this.overtimeAmount = overtimeAmount;
    }

    public BigDecimal getTotalAllowance() {
        return totalAllowance;
    }

    public void setTotalAllowance(BigDecimal totalAllowance) {
        this.totalAllowance = totalAllowance;
    }

    public BigDecimal getTotalBonus() {
        return totalBonus;
    }

    public void setTotalBonus(BigDecimal totalBonus) {
        this.totalBonus = totalBonus;
    }

    public BigDecimal getTotalDeduction() {
        return totalDeduction;
    }

    public void setTotalDeduction(BigDecimal totalDeduction) {
        this.totalDeduction = totalDeduction;
    }

    public BigDecimal getTotalFine() {
        return totalFine;
    }

    public void setTotalFine(BigDecimal totalFine) {
        this.totalFine = totalFine;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDateTime paidDate) {
        this.paidDate = paidDate;
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
