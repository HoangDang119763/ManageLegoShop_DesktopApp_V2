package DTO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDTO {
    private int id;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private int statusId;
    private int roleId;
    private boolean requireRelogin;

    // Constructors
    public AccountDTO() {
    }

    public AccountDTO(int id, String username, String password, LocalDateTime createdAt,
            LocalDateTime lastLogin, int statusId, int roleId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.statusId = statusId;
        this.roleId = roleId;
    }

    public AccountDTO(int id, String username, String password, int statusId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.lastLogin = null;
        this.statusId = statusId;
    }

    public AccountDTO(String username, String password) {
        this.username = username;
        this.password = password;
        this.lastLogin = null;
    }

    public AccountDTO(AccountDTO other) {
        if (other != null) {
            this.id = other.id;
            this.username = other.username;
            this.roleId = other.roleId;
            this.password = other.password;
            this.createdAt = other.createdAt;
            this.lastLogin = other.lastLogin;
            this.statusId = other.statusId;
        }
    }

    public void autoUpdateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public boolean isRequireRelogin() {
        return requireRelogin;
    }

    public void setRequireRelogin(boolean requireRelogin) {
        this.requireRelogin = requireRelogin;
    }
}
