package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO để hiển thị thông tin lịch sử công tác chi tiết
 * Transform từ EmploymentHistoryDTO + lookup Department và Role name
 */
@Data
@AllArgsConstructor
public class EmploymentHistoryDetailDTO {
    private int id;
    private int employeeId;
    private LocalDate effectiveDate;

    // Department lookup
    private Integer departmentId;
    private String departmentName;

    // Role lookup
    private Integer roleId;
    private String roleName;

    // Additional info
    private String reason;
    private LocalDateTime createdAt;
    private int approverId;
}
