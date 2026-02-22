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

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public Integer getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(Integer salaryId) {
        this.salaryId = salaryId;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getSalaryCoefficient() {
        return salaryCoefficient;
    }

    public void setSalaryCoefficient(BigDecimal salaryCoefficient) {
        this.salaryCoefficient = salaryCoefficient;
    }

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
