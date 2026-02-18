package DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

/**
 * DTO để hiển thị thông tin tài khoản của nhân viên
 * Tab 2: ACCOUNT INFO trong EmployeeModalUI
 */
@Data
@Builder
public class EmployeeAccountInfoDTO {
    private Integer accountId;
    private String username;
    private Integer accountStatusId;
    private String accountStatus;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "EmployeeAccountInfoDTO{" +
                ", accountId=" + accountId +
                ", username='" + username + '\'' +
                ", accountStatusId=" + accountStatusId +
                ", accountStatus='" + accountStatus + '\'' +
                ", lastLogin=" + lastLogin +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
