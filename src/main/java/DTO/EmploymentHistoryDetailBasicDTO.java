package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO chứa thông tin cơ bản lịch sử công tác của nhân viên
 * Gồm: departmentId, departmentName, roleId, roleName, effectiveDate, createdAt
 * Dùng cho display trong TableView
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentHistoryDetailBasicDTO {
    private Integer departmentId; // ID phòng ban
    private String departmentName; // Tên phòng ban
    private Integer roleId; // ID chức vụ
    private String roleName; // Tên chức vụ
    private LocalDate effectiveDate; // Ngày áp dụng
    private LocalDateTime createdAt; // Ngày tạo bản ghi
}
