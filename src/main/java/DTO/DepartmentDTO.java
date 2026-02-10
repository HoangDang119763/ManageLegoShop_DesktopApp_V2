package DTO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    @Override
    public String toString() {
        return name;
    }
}
