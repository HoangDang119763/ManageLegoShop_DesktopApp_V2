package DTO;

import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ToString
public abstract class BaseInformationDTO {
    protected int id;
    private String phone;
    protected LocalDate dateOfBirth;
    protected LocalDateTime updatedAt;
    protected int statusId;

    public BaseInformationDTO() {
    }

    public BaseInformationDTO(BaseInformationDTO other) {
        if (other != null) {
            this.id = other.id;
            this.dateOfBirth = other.dateOfBirth;
            this.phone = other.phone;
            this.updatedAt = other.updatedAt;
            this.statusId = other.statusId;
        }
    }

    public BaseInformationDTO(int id, LocalDate dateOfBirth, String phone, int statusId, LocalDateTime updatedAt) {
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.updatedAt = updatedAt;
        this.statusId = statusId;
    }

    public BaseInformationDTO(int id, LocalDate dateOfBirth, String phone, int statusId) {
        this.id = id;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.statusId = statusId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }
}
