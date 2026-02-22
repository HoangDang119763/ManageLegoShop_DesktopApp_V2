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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
