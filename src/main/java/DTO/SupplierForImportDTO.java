package DTO;

/**
 * DTO cho ImportProduct form - chỉ chứa thông tin cơ bản của nhà cung cấp
 * Giới hạn quyền xem thông tin chi tiết để bảo vệ dữ liệu nhạy cảm
 */
public class SupplierForImportDTO {
    private int id;
    private String name;
    private String phone;
    private String address;

    // Constructor mặc định
    public SupplierForImportDTO() {
    }

    // Constructor đầy đủ
    public SupplierForImportDTO(int id, String name, String phone, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    // Getters & Setters
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

    @Override
    public String toString() {
        return name + " - " + phone;
    }
}
