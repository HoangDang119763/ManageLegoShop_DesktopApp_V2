package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;

/**
 * DTO để hiển thị thông tin công việc/vị trí của nhân viên
 * Tab 3: JOB INFO trong EmployeeModalUI
 */
@Data
@Builder
@Setter
@Getter
public class EmployeeJobInfoDTO {
    private Integer departmentId;
    private String departmentName;
    private Integer positionId;
    private String positionName;
    private BigDecimal wage;
    private Integer statusId;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "EmployeeJobInfoDTO{" +
                ", departmentId=" + departmentId +
                ", departmentName='" + departmentName + '\'' +
                ", positionId=" + positionId +
                ", positionName='" + positionName + '\'' +
                ", wage=" + wage +
                ", statusId=" + statusId +
                ", statusDescription='" + statusDescription + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
