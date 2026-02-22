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

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAccountStatusId() {
        return accountStatusId;
    }

    public void setAccountStatusId(Integer accountStatusId) {
        this.accountStatusId = accountStatusId;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "EmployeeAccountInfoDTO{" +
                ", accountId=" + accountId +
                ", username='" + username + '\'' +
                ", accountStatusId=" + accountStatusId +
                ", accountStatus='" + accountStatus + '\'' +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
