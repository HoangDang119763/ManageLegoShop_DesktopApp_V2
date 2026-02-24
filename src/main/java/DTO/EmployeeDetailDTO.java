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
    private String socialInsCode;
    private String unemploymentInsCode;
    private boolean isPersonalIncomeTax;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;

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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

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

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isHealthInsurance() {
        return healthInsCode != null && !healthInsCode.isEmpty();
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
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
