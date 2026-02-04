package FACTORY;

import DTO.CustomerDTO;
import INTERFACE.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CustomerBuilder implements Builder<CustomerDTO> {
    private int id;
    private int statusId;
    private LocalDate dateOfBirth;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    public CustomerBuilder id(int id) {
        this.id = id;
        return this;
    }

    public CustomerBuilder statusId(int statusId) {
        this.statusId = statusId;
        return this;
    }

    public CustomerBuilder dateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        return this;
    }

    public CustomerBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public CustomerBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public CustomerBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public CustomerBuilder address(String address) {
        this.address = address;
        return this;
    }

    @Override
    public CustomerDTO build() {
        return new CustomerDTO(id, firstName, lastName, phone, address, dateOfBirth, statusId);
    }
}
