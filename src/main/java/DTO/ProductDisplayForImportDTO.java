package DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO hiển thị sản phẩm cho nghiệp vụ nhập hàng
 * Chỉ chứa thông tin cần thiết: id, name, image, price, stock, category, status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDisplayForImportDTO {
    private String id; // ID sản phẩm
    private String name; // Tên sản phẩm
    private String imageUrl; // URL hình ảnh
    private BigDecimal importPrice; // Giá bán hiển thị
    private Integer stockQuantity; // Số lượng tồn kho hiện tại
    private Integer categoryId; // ID thể loại (cho filter)
    private String categoryName; // Tên thể loại hiển thị
    private String statusName; // Tên trạng thái (ACTIVE, SUSPENDED, INACTIVE)
    private String statusDescription; // Mô tả trạng thái
}
