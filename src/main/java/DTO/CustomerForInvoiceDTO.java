package DTO;

/**
 * DTO dành cho invoice form
 * Chỉ truyền những thông tin cần thiết cho invoice: id, firstName, lastName,
 * fullName, phone, address
 * Không expose: dateOfBirth, statusId, updatedAt, email, etc.
 */
public class CustomerForInvoiceDTO {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String address;

    public CustomerForInvoiceDTO() {
    }

    public CustomerForInvoiceDTO(int id, String firstName, String lastName, String phone, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.address = address;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get full name
     */
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    @Override
    public String toString() {
        return getFullName() + " - " + phone;
    }
}
