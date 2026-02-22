package DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CustomerDTO extends BaseInformationDTO {
    private String firstName;
    private String lastName;
    private String address;

    public CustomerDTO(int id, String firstName, String lastName, String phone, String address,
            LocalDate dateOfBirth, int statusId, LocalDateTime updatedAt) {
        super(id, dateOfBirth, phone, statusId, updatedAt);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public CustomerDTO(int id, String firstName, String lastName, String phone, String address,
                       LocalDate dateOfBirth, int statusId) {
        super(id, dateOfBirth, phone, statusId);
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
    }

    public CustomerDTO(CustomerDTO other) {
        super(other); // Gọi constructor clone của PersonDTO nếu có
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.address = other.address;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
