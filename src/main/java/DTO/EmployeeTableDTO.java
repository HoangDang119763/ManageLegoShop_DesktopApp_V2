package DTO;

import java.math.BigDecimal;

/**
 * DTO để hiển thị thông tin nhân viên trong bảng (Table)
 * Được sử dụng để render dữ liệu từ join của bảng employee, role, salary
 */
public class EmployeeTableDTO {
    private int employeeId;
    private String fullName; // firstName + " " + lastName
    private String roleName;
    private BigDecimal baseSalary; // từ salary.base
    private BigDecimal salaryCoefficient; // từ salary.coefficient
    private String phone;
    private String email;
    private String gender;
    private String statusDescription;

    public EmployeeTableDTO() {
    }

    public EmployeeTableDTO(int employeeId, String fullName, String roleName,
            BigDecimal baseSalary, BigDecimal salaryCoefficient,
            String phone, String email, String gender, String statusDescription) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.roleName = roleName;
        this.baseSalary = baseSalary;
        this.salaryCoefficient = salaryCoefficient;
        this.phone = phone;
        this.email = email;
        this.gender = gender;
        this.statusDescription = statusDescription;
    }

    // Getters and Setters
    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public String toString() {
        return "EmployeeTableDTO{" +
                "employeeId=" + employeeId +
                ", fullName='" + fullName + '\'' +
                ", roleName='" + roleName + '\'' +
                ", baseSalary=" + baseSalary +
                ", salaryCoefficient=" + salaryCoefficient +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                '}';
    }
}
