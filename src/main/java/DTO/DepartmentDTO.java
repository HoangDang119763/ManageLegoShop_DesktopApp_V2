package DTO;

import java.time.LocalDateTime;

public class DepartmentDTO {
    private int id;
    private String name;
    private String description;
    private int statusId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public DepartmentDTO() {
    }

    public DepartmentDTO(int id, String name, String description, int statusId, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.statusId = statusId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public DepartmentDTO(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public DepartmentDTO(int id, String name, int statusId) {
        this.id = id;
        this.name = name;
        this.statusId = statusId;
    }

    // Copy Constructor
    public DepartmentDTO(DepartmentDTO other) {
        if (other != null) {
            this.id = other.id;
            this.name = other.name;
            this.description = other.description;
            this.statusId = other.statusId;
            this.createdAt = other.createdAt;
            this.updatedAt = other.updatedAt;
        }
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "DepartmentDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", statusId=" + statusId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
