package DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * DTO để hiển thị thông tin cá nhân của nhân viên
 * Tab 1: PERSONAL INFO trong EmployeeModalUI
 */
@Data
@Builder
public class EmployeePersonalInfoDTO {
    private int employeeId;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String avatarUrl; // Đường dẫn ảnh đại diện
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "EmployeePersonalInfoDTO{" +
                ", employeeId=" + employeeId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
