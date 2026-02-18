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
