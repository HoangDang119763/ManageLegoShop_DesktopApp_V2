package DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * DTO để hiển thị thông tin lương & bảo hiểm của nhân viên
 * Tab 4: PAYROLL & BENEFITS trong EmployeeModalUI
 */
@Data
@Builder
public class EmployeePayrollInfoDTO {
    private int id;
    private Integer taxId;
    private Integer numDependents;
    private String healthInsCode;
    private String socialInsCode;
    private String unemploymentInsCode;
    private boolean isPersonalIncomeTax;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getTaxId() {
        return taxId;
    }

    public void setTaxId(Integer taxId) {
        this.taxId = taxId;
    }

    public Integer getNumDependents() {
        return numDependents;
    }

    public void setNumDependents(Integer numDependents) {
        this.numDependents = numDependents;
    }

    public String getHealthInsCode() {
        return healthInsCode;
    }

    public void setHealthInsCode(String healthInsCode) {
        this.healthInsCode = healthInsCode;
    }

    public String getSocialInsCode() {
        return socialInsCode;
    }

    public void setSocialInsCode(String socialInsCode) {
        this.socialInsCode = socialInsCode;
    }

    public String getUnemploymentInsCode() {
        return unemploymentInsCode;
    }

    public void setUnemploymentInsCode(String unemploymentInsCode) {
        this.unemploymentInsCode = unemploymentInsCode;
    }

    public boolean isSocialInsurance() {
        return socialInsCode != null && !socialInsCode.isEmpty() && !"0".equals(socialInsCode);
    }

    public void setSocialInsurance(boolean socialInsurance) {
        this.socialInsCode = socialInsurance ? "1" : "0";
    }

    public boolean isUnemploymentInsurance() {
        return unemploymentInsCode != null && !unemploymentInsCode.isEmpty() && !"0".equals(unemploymentInsCode);
    }

    public void setUnemploymentInsurance(boolean unemploymentInsurance) {
        this.unemploymentInsCode = unemploymentInsurance ? "1" : "0";
    }

    public boolean isPersonalIncomeTax() {
        return isPersonalIncomeTax;
    }

    public void setPersonalIncomeTax(boolean personalIncomeTax) {
        isPersonalIncomeTax = personalIncomeTax;
    }

    public boolean isTransportationSupport() {
        return isTransportationSupport;
    }

    public void setTransportationSupport(boolean transportationSupport) {
        isTransportationSupport = transportationSupport;
    }

    public boolean isAccommodationSupport() {
        return isAccommodationSupport;
    }

    public void setAccommodationSupport(boolean accommodationSupport) {
        isAccommodationSupport = accommodationSupport;
    }

    public boolean isHealthInsurance() {
        return healthInsCode != null && !healthInsCode.isEmpty();
    }

    @Override
    public String toString() {
        return "EmployeePayrollInfoDTO{" +
                "id=" + id +
                ", taxId=" + taxId +
                ", numDependents=" + numDependents +
                ", healthInsCode='" + healthInsCode + '\'' +
                ", socialInsCode='" + socialInsCode + '\'' +
                ", unemploymentInsCode='" + unemploymentInsCode + '\'' +
                ", isPersonalIncomeTax=" + isPersonalIncomeTax +
                ", isTransportationSupport=" + isTransportationSupport +
                ", isAccommodationSupport=" + isAccommodationSupport +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
