package DTO;

import lombok.ToString;

import java.time.LocalDateTime;

@ToString
public class RoleDTO {
    private int id;
    private String name;
    private String description;
    private int startExperience;
    private int endExperience;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer salaryId;

    public RoleDTO() {
    }

    public RoleDTO(int id, String name, String description, int startExperience, int endExperience,
            LocalDateTime createdAt, LocalDateTime updatedAt, Integer salaryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startExperience = startExperience;
        this.endExperience = endExperience;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.salaryId = salaryId;
    }

    public RoleDTO(RoleDTO other) {
        if (other != null) {
            this.id = other.id;
            this.name = other.name;
            this.description = other.description;
            this.startExperience = other.startExperience;
            this.endExperience = other.endExperience;
            this.createdAt = other.createdAt;
            this.updatedAt = other.updatedAt;
            this.salaryId = other.salaryId;
        }
    }

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

    public int getStartExperience() {
        return startExperience;
    }

    public void setStartExperience(int startExperience) {
        this.startExperience = startExperience;
    }

    public int getEndExperience() {
        return endExperience;
    }

    public void setEndExperience(int endExperience) {
        this.endExperience = endExperience;
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

    public int getStatusId() {
        return salaryId;
    }

    public Integer getSalaryId() {
        return salaryId;
    }

    public void setSalaryId(Integer salaryId) {
        this.salaryId = salaryId;
    }
}
