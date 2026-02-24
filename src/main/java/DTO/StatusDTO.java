package DTO;

public class StatusDTO {
    private int id;
    private String name;
    private String description;
    private String type;

    public StatusDTO() {
    }

    public StatusDTO(int id, String name, String description, String type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public StatusDTO(int id, String description) {
        this.id = id;
        this.description = description;
    }

    public StatusDTO(StatusDTO other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.type = other.type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return description;
    }
}
