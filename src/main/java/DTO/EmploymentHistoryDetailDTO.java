package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO để hiển thị thông tin lịch sử công tác chi tiết
 * Transform từ EmploymentHistoryDTO + lookup Department và Position name
 */
@Data
@AllArgsConstructor
public class EmploymentHistoryDetailDTO {
    private int employeeId;
    private LocalDate effectiveDate;

    // Department lookup
    private Integer departmentId;
    private String departmentName;

    // Position lookup
    private Integer positionId;
    private String positionName;

    // Additional info
    private String reason;
    private LocalDateTime createdAt;
    private int approverId;
    private String approverName;

    private int statusId;
    private String statusDescription; // Mô tả trạng thái (nếu cần)
}