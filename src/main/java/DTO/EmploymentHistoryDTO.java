package DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmploymentHistoryDTO {
    private int id;
    private int employeeId;
    private int departmentId;
    private int roleId;
    private LocalDate effectiveDate;
    private Integer approverId; // Nullable
    private int statusId;
    private String reason; // Nullable
    private LocalDateTime createdAt;

    // Constructors
    public EmploymentHistoryDTO() {
    }

    public EmploymentHistoryDTO(int id, int employeeId, int departmentId, int roleId,
            LocalDate effectiveDate, Integer approverId, int statusId, String reason,
            LocalDateTime createdAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.roleId = roleId;
        this.effectiveDate = effectiveDate;
        this.approverId = approverId;
        this.statusId = statusId;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public EmploymentHistoryDTO(int employeeId, int departmentId, int roleId,
            LocalDate effectiveDate, int statusId, String reason) {
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.roleId = roleId;
        this.effectiveDate = effectiveDate;
        this.statusId = statusId;
        this.reason = reason;
    }

    // Copy Constructor
    public EmploymentHistoryDTO(EmploymentHistoryDTO other) {
        if (other != null) {
            this.id = other.id;
            this.employeeId = other.employeeId;
            this.departmentId = other.departmentId;
            this.roleId = other.roleId;
            this.effectiveDate = other.effectiveDate;
            this.approverId = other.approverId;
            this.statusId = other.statusId;
            this.reason = other.reason;
            this.createdAt = other.createdAt;
        }
    }

    // Getters and Setters
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

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Integer getApproverId() {
        return approverId;
    }

    public void setApproverId(Integer approverId) {
        this.approverId = approverId;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "EmploymentHistoryDTO{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", departmentId=" + departmentId +
                ", roleId=" + roleId +
                ", effectiveDate=" + effectiveDate +
                ", approverId=" + approverId +
                ", statusId=" + statusId +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
