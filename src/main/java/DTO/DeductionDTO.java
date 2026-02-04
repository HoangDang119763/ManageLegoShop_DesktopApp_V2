package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeductionDTO {
    private int id;
    private int employeeId;
    private LocalDate salaryPeriod;
    private BigDecimal healthInsurance;
    private BigDecimal socialInsurance;
    private BigDecimal unemploymentInsurance;
    private BigDecimal personalIncomeTax;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DeductionDTO() {
    }

    public DeductionDTO(int id, int employeeId, LocalDate salaryPeriod, BigDecimal healthInsurance,
            BigDecimal socialInsurance, BigDecimal unemploymentInsurance, BigDecimal personalIncomeTax,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.salaryPeriod = salaryPeriod;
        this.healthInsurance = healthInsurance;
        this.socialInsurance = socialInsurance;
        this.unemploymentInsurance = unemploymentInsurance;
        this.personalIncomeTax = personalIncomeTax;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public DeductionDTO(DeductionDTO other) {
        this.id = other.id;
        this.employeeId = other.employeeId;
        this.salaryPeriod = other.salaryPeriod;
        this.healthInsurance = other.healthInsurance;
        this.socialInsurance = other.socialInsurance;
        this.unemploymentInsurance = other.unemploymentInsurance;
        this.personalIncomeTax = other.personalIncomeTax;
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

    public BigDecimal getHealthInsurance() {
        return healthInsurance;
    }

    public void setHealthInsurance(BigDecimal healthInsurance) {
        this.healthInsurance = healthInsurance;
    }

    public BigDecimal getSocialInsurance() {
        return socialInsurance;
    }

    public void setSocialInsurance(BigDecimal socialInsurance) {
        this.socialInsurance = socialInsurance;
    }

    public BigDecimal getUnemploymentInsurance() {
        return unemploymentInsurance;
    }

    public void setUnemploymentInsurance(BigDecimal unemploymentInsurance) {
        this.unemploymentInsurance = unemploymentInsurance;
    }

    public BigDecimal getPersonalIncomeTax() {
        return personalIncomeTax;
    }

    public void setPersonalIncomeTax(BigDecimal personalIncomeTax) {
        this.personalIncomeTax = personalIncomeTax;
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

    public BigDecimal getTotalDeduction() {
        BigDecimal total = BigDecimal.ZERO;
        if (healthInsurance != null)
            total = total.add(healthInsurance);
        if (socialInsurance != null)
            total = total.add(socialInsurance);
        if (unemploymentInsurance != null)
            total = total.add(unemploymentInsurance);
        if (personalIncomeTax != null)
            total = total.add(personalIncomeTax);
        return total;
    }

    @Override
    public String toString() {
        return "DeductionDTO{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", salaryPeriod=" + salaryPeriod +
                ", healthInsurance=" + healthInsurance +
                ", socialInsurance=" + socialInsurance +
                ", unemploymentInsurance=" + unemploymentInsurance +
                ", personalIncomeTax=" + personalIncomeTax +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
