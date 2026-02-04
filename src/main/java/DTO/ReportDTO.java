package DTO;

import java.time.LocalDateTime;

public class ReportDTO {
    private int id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private String level;
    private String category;
    private int employeeId;

    public ReportDTO() {
    }

    public ReportDTO(int id, String title, String description, LocalDateTime createdAt, String level, String category,
            int employeeId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.level = level;
        this.category = category;
        this.employeeId = employeeId;
    }

    public ReportDTO(ReportDTO other) {
        this.id = other.id;
        this.title = other.title;
        this.description = other.description;
        this.createdAt = other.createdAt;
        this.level = other.level;
        this.category = other.category;
        this.employeeId = other.employeeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String toString() {
        return "ReportDTO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", level='" + level + '\'' +
                ", category='" + category + '\'' +
                ", employeeId=" + employeeId +
                '}';
    }
}
