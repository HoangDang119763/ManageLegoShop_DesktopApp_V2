package DTO;


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
    private boolean isSocialInsurance;
    private boolean isUnemploymentInsurance;
    private boolean isPersonalIncomeTax;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;

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

    public boolean isSocialInsurance() {
        return isSocialInsurance;
    }

    public void setSocialInsurance(boolean socialInsurance) {
        isSocialInsurance = socialInsurance;
    }

    public boolean isUnemploymentInsurance() {
        return isUnemploymentInsurance;
    }

    public void setUnemploymentInsurance(boolean unemploymentInsurance) {
        isUnemploymentInsurance = unemploymentInsurance;
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
                ", isSocialInsurance=" + isSocialInsurance +
                ", isUnemploymentInsurance=" + isUnemploymentInsurance +
                ", isPersonalIncomeTax=" + isPersonalIncomeTax +
                ", isTransportationSupport=" + isTransportationSupport +
                ", isAccommodationSupport=" + isAccommodationSupport +
                '}';
    }
}
