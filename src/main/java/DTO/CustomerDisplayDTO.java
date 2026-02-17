package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO hiển thị khách hàng với thông tin JOIN từ status
 * Dùng cho TableView mà không cần gọi BUS lẻ lẻ từng dòng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDisplayDTO {
    private Integer id; // ID khách hàng
    private String fullName; // Họ đệm + Tên (firstName + lastName)
    private String phone; // Số điện thoại
    private String address; // Địa chỉ
    private LocalDate dateOfBirth; // Ngày sinh
    private Integer statusId; // ID trạng thái
    private String statusDescription; // Mô tả trạng thái (JOIN từ status table)
    private LocalDateTime updatedAt; // Ngày cập nhật
}
