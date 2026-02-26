package FACTORY;

import DTO.RoleDTO;
import INTERFACE.Builder;
import java.time.LocalDateTime;

public class RoleBuilder implements Builder<RoleDTO> {
    private int id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RoleBuilder id(int id) {
        this.id = id;
        return this;
    }

    public RoleBuilder name(String name) {
        this.name = name;
        return this;
    }

    public RoleBuilder description(String description) {
        this.description = description;
        return this;
    }

    public RoleBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public RoleBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    @Override
    public RoleDTO build() {
        return new RoleDTO(id, name, description, createdAt, updatedAt);
    }
}
