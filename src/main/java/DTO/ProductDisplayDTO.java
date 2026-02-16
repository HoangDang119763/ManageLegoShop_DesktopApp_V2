package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO hiển thị sản phẩm với thông tin JOIN từ category và status
 * Dùng cho TableView mà không cần gọi BUS lẻ lẻ từng dòng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDisplayDTO {
    private String id; // ID sản phẩm
    private String name; // Tên sản phẩm
    private String description; // Mô tả
    private String imageUrl; // URL hình ảnh
    private Integer categoryId; // ID thể loại
    private String categoryName; // Tên thể loại (JOIN từ category table)
    private Integer stockQuantity; // Số lượng tồn kho
    private BigDecimal sellingPrice; // Giá bán
    private Integer statusId; // ID trạng thái
    private String statusDescription; // Mô tả trạng thái (JOIN từ status table)
    private LocalDateTime createdAt; // Ngày tạo
    private LocalDateTime updatedAt; // Ngày cập nhật
}
