package DTO;

import java.math.BigDecimal;
import lombok.Builder;

/**
 * DTO để hiển thị thông tin nhân viên trong bảng (Table) và form chi tiết
 * Được sử dụng để render dữ liệu từ join của bảng employee, role, salary, tax
 */
@Builder
public class EmployeeDetailDTO {
    private int employeeId;

    // Core employee info
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;

    // Role info
    private int roleId;
    private String roleName;

    // Salary info
    private int salaryId;
    private BigDecimal baseSalary;
    private BigDecimal salaryCoefficient;

    // Account info
    private int accountId;
    private String username;
    private int accountStatusId;
    private String accountStatus;

    // Employee status
    private int statusId;
    private String statusDescription;

    // Tax info
    private int taxId;
    private int numDependents;

    public EmployeeDetailDTO() {
    }

    public EmployeeDetailDTO(int employeeId, String firstName, String lastName,
            String email, String phone, String gender,
            int roleId, String roleName,
            int salaryId, BigDecimal baseSalary, BigDecimal salaryCoefficient,
            int accountId, String username, int accountStatusId, String accountStatus,
            int statusId, String statusDescription,
            int taxId, int numDependents) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.roleId = roleId;
        this.roleName = roleName;
        this.salaryId = salaryId;
        this.baseSalary = baseSalary;
        this.salaryCoefficient = salaryCoefficient;
        this.accountId = accountId;
        this.username = username;
        this.accountStatusId = accountStatusId;
        this.accountStatus = accountStatus;
        this.statusId = statusId;
        this.statusDescription = statusDescription;
        this.taxId = taxId;
        this.numDependents = numDependents;
    }

    // ===== GETTERS AND SETTERS =====
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

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
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

    public int getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(int salaryId) {
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

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAccountStatusId() {
        return accountStatusId;
    }

    public void setAccountStatusId(int accountStatusId) {
        this.accountStatusId = accountStatusId;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public int getTaxId() {
        return taxId;
    }

    public void setTaxId(int taxId) {
        this.taxId = taxId;
    }

    public Integer getNumDependents() {
        return numDependents;
    }

    public void setNumDependents(Integer numDependents) {
        this.numDependents = numDependents;
    }

    @Override
    public String toString() {
        return "EmployeeDetailDTO{" +
                "employeeId=" + employeeId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", gender='" + gender + '\'' +
                ", roleName='" + roleName + '\'' +
                ", baseSalary=" + baseSalary +
                ", salaryCoefficient=" + salaryCoefficient +
                ", username='" + username + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                ", numDependents=" + numDependents +
                '}';
    }
}
