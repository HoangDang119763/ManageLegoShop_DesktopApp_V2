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
    private boolean requireRelogin;

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

    public AccountDTO(String username, String password) {
        this.username = username;
        this.password = password;
        this.lastLogin = null;
        this.statusId = 1; // Default status ID for active accounts
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

    public void autoUpdateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}
