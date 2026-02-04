package DTO;

import java.time.LocalDateTime;

public class AccountDTO {
    private int id;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private int statusId;

    // Constructors
    public AccountDTO() {
    }

    public AccountDTO(int id, String username, String password, LocalDateTime createdAt,
            LocalDateTime lastLogin, int statusId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.statusId = statusId;
    }

    public AccountDTO(int id, String username, String password, int statusId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.lastLogin = null;
        this.statusId = statusId;
    }

    public AccountDTO(AccountDTO other) {
        if (other != null) {
            this.id = other.id;
            this.username = other.username;
            this.password = other.password;
            this.createdAt = other.createdAt;
            this.lastLogin = other.lastLogin;
            this.statusId = other.statusId;
        }
    }

    // Getters and Setters
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

    public void autoUpdateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}
