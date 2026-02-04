package DTO;

public class SupplierDTO {
    private int id;
    private int statusId;
    private String name;
    private String phone;
    private String address;
    private String email;

    // Constructor mặc định
    public SupplierDTO() {
    }

    // Constructor đầy đủ
    public SupplierDTO(int id, String name, String phone, String address, String email, int statusId) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.statusId = statusId;
    }

    // Constructor sao chép (clone)
    public SupplierDTO(SupplierDTO other) {
        if (other != null) {
            this.id = other.id;
            this.statusId = other.statusId;
            this.name = other.name;
            this.phone = other.phone;
            this.address = other.address;
            this.email = other.email;
        }
    }

    // Getter và Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isStatus() {
        return statusId == 1; // 1 = ACTIVE
    }

    public void setStatus(boolean status) {
        this.statusId = status ? 1 : 2; // 1 = ACTIVE, 2 = INACTIVE
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
