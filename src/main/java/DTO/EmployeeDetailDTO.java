package DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO để hiển thị thông tin nhân viên trong bảng (Table) và form chi tiết
 * Được sử dụng để render dữ liệu từ join của bảng employee, role, salary, tax
 */
@Data
@Builder
@Getter
@Setter
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

    // Position info
    private Integer positionId;
    private String positionName;
    private BigDecimal wage;

    private Integer numDependents;

    // Health & Support flags
    private String healthInsCode;
    private String socialInsCode;
    private String unemploymentInsCode;
    private boolean isMealSupport;
    private boolean isTransportationSupport;
    private boolean isAccommodationSupport;

    public boolean isHealthInsurance() {
        return healthInsCode != null && !healthInsCode.isEmpty() && !"0".equals(healthInsCode);
    }

    public boolean isSocialInsurance() {
        return socialInsCode != null && !socialInsCode.isEmpty() && !"0".equals(socialInsCode);
    }

    public boolean isUnemploymentInsurance() {
        return unemploymentInsCode != null && !unemploymentInsCode.isEmpty() && !"0".equals(unemploymentInsCode);
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
                ", positionId=" + positionId +
                ", positionName='" + positionName + '\'' +
                ", wage=" + wage +
                ", username='" + username + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", statusDescription='" + statusDescription + '\'' +
                ", numDependents=" + numDependents +
                ", healthInsCode='" + healthInsCode + '\'' +
                ", socialInsCode='" + socialInsCode + '\'' +
                ", unemploymentInsCode='" + unemploymentInsCode + '\'' +
                ", isMealSupport=" + isMealSupport +
                ", isTransportationSupport=" + isTransportationSupport +
                ", isAccommodationSupport=" + isAccommodationSupport +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
