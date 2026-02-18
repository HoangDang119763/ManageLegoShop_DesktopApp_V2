package DTO;

import java.math.BigDecimal;
import DTO.PagedResponse;

import lombok.*;

/**
 * DTO để hiển thị thông tin công việc/vị trí của nhân viên
 * Tab 3: JOB INFO trong EmployeeModalUI
 */
@Data
@Builder
@Setter
@Getter
public class EmployeeJobInfoDTO {
    private Integer departmentId;
    private String departmentName;
    private Integer roleId;
    private String roleName;
    private Integer statusId;
    private String statusDescription;
    private Integer salaryId;
    private BigDecimal baseSalary;
    private BigDecimal salaryCoefficient;

    @Override
    public String toString() {
        return "EmployeeJobInfoDTO{" +
                ", departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", statusId=" + statusId +
                ", statusDescription='" + statusDescription + '\'' +
                ", salaryId=" + salaryId +
                ", baseSalary=" + baseSalary +
                ", salaryCoefficient=" + salaryCoefficient +
                '}';
    }
}
