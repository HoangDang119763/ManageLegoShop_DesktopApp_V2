package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị phiếu nhập với thông tin JOIN từ status table
 * Dùng cho TableView mà không cần gọi BUS lẻ lẻ từng dòng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportDisplayDTO {
    private int id;
    private LocalDateTime createDate;
    private int employeeId;
    private int supplierId;
    private BigDecimal totalPrice;
    private int statusId;
    private String statusDescription; // Mô tả trạng thái (JOIN từ status table)
}
