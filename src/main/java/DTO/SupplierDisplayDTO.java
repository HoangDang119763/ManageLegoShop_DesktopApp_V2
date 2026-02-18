package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO hiển thị nhà cung cấp với thông tin JOIN từ status
 * Dùng cho TableView mà không cần gọi BUS lẻ lẻ từng dòng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDisplayDTO {
    private Integer id; // ID nhà cung cấp
    private String name; // Tên nhà cung cấp
    private String phone; // Số điện thoại
    private String address; // Địa chỉ
    private String email; // Email
    private Integer statusId; // ID trạng thái
    private String statusDescription; // Mô tả trạng thái (JOIN từ status table)
}
