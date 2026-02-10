package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * DTO để hiển thị thông tin nhân viên trong bảng (Table) và form chi tiết
 * Được sử dụng để render dữ liệu từ join của bảng employee, role, salary, tax
 */
@Data
@Builder
public class EmployeeDetailDTO {
    // Core BaseInformation fields
    private int id;
    private LocalDate dateOfBirth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int statusId;

    // Employee display ID
    private int employeeId;

    // Core employee info
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;

    // Department info
    private Integer departmentId;
    private String departmentName;

    // Role info
    private int roleId;
    private String roleName;

    // Account info
    private Integer accountId;
    private String username;
    private int accountStatusId;
    private String accountStatus;

    // Employee status
    private String statusDescription;

    // Salary info
    private int salaryId;
    private BigDecimal baseSalary;
    private BigDecimal salaryCoefficient;

    // Tax info
    private int taxId;
    private Integer numDependents;

    // Health & Support flags
    private String healthInsCode;
    private boolean isSocialInsurance;
    private boolean isUnemploymentInsurance;
    private boolean isPersonalIncomeTax;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "EmployeeDetailDTO{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", departmentName='" + departmentName + '\'' +
                ", roleName='" + roleName + '\'' +
                ", baseSalary=" + baseSalary +
                ", salaryCoefficient=" + salaryCoefficient +
                ", username='" + username + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                ", numDependents=" + numDependents +
                ", healthInsCode='" + healthInsCode + '\'' +
                ", isSocialInsurance=" + isSocialInsurance +
                ", isUnemploymentInsurance=" + isUnemploymentInsurance +
                ", isPersonalIncomeTax=" + isPersonalIncomeTax +
                ", isTransportationSupport=" + isTransportationSupport +
                ", isAccommodationSupport=" + isAccommodationSupport +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
