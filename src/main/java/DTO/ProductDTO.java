package DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.ToString;

@ToString
public class ProductDTO {
    private String id;
    private String name;
    private int stockQuantity;
    private BigDecimal sellingPrice;
    private BigDecimal importPrice;
    private int statusId;
    private String description;
    private String imageUrl;
    private int categoryId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductDTO() {
    }

    public ProductDTO(String id, String name, int stockQuantity, BigDecimal sellingPrice, int statusId,
            String description, String imageUrl, int categoryId) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.sellingPrice = sellingPrice;
        this.statusId = statusId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }

    public ProductDTO(String id, String name, int stockQuantity, BigDecimal sellingPrice, BigDecimal importPrice,
            int statusId, String description, String imageUrl, int categoryId) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.sellingPrice = sellingPrice;
        this.importPrice = importPrice;
        this.statusId = statusId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
    }

    public ProductDTO(String id, String name, int stockQuantity, BigDecimal sellingPrice, BigDecimal importPrice,
            int statusId, String description, String imageUrl, int categoryId, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
        this.sellingPrice = sellingPrice;
        this.importPrice = importPrice;
        this.statusId = statusId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    public ProductDTO(ProductDTO product) {
        this.id = product.id;
        this.name = product.name;
        this.stockQuantity = product.stockQuantity;
        this.sellingPrice = product.sellingPrice;
        this.importPrice = product.importPrice;
        this.statusId = product.statusId;
        this.description = product.description;
        this.imageUrl = product.imageUrl;
        this.categoryId = product.categoryId;
        this.createdAt = product.createdAt;
        this.updatedAt = product.updatedAt;
    }

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

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
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

    public int getCategoryId() {
        return categoryId;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
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

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}