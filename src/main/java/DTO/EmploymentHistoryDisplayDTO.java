package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO để hiển thị thông tin lịch sử điều chuyển trong bảng quản lý
 * Bao gồm đầy đủ thông tin: ID, Employee, Department, Position, Approver,
 * Status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmploymentHistoryDisplayDTO {
    private int id; // ID quyết định
    private int employeeId; // Mã nhân viên
    private String employeeName; // Tên nhân viên

    private Integer departmentId; // ID phòng ban mới
    private String departmentName; // Tên phòng ban mới

    private Integer positionId; // ID vị trí mới
    private String positionName; // Tên vị trí mới

    private LocalDate effectiveDate; // Ngày hiệu lực

    private Integer approverId; // ID người lập quyết định
    private String approverName; // Tên người lập

    private int statusId; // ID trạng thái
    private String statusName; // Tên trạng thái
    private String statusDescription; // Mô tả trạng thái

    private String reason; // Lý do điều chuyển
    private LocalDateTime createdAt; // Ngày tạo
}
