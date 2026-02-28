package DTO;

import java.math.BigDecimal;

public class ProductDisplayForSellingDTO {
    private String id; // Product ID
    private String name; // Product name
    private String imageUrl; // Image URL
    private BigDecimal sellPrice; // Selling price to display
    private Integer stockQuantity; // Current stock quantity
    private Integer categoryId; // Category ID for filtering
    private String categoryName; // Category name for display
    private String statusName; // Status (ACTIVE, SUSPENDED, etc.)
    private String statusDescription; // Status description

    // Constructors
    public ProductDisplayForSellingDTO() {
    }

    public ProductDisplayForSellingDTO(String id, String name, String imageUrl, BigDecimal sellPrice,
            Integer stockQuantity, Integer categoryId, String categoryName,
            String statusName, String statusDescription) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.sellPrice = sellPrice;
        this.stockQuantity = stockQuantity;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.statusName = statusName;
        this.statusDescription = statusDescription;
    }

    // Getters and Setters
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    // Alias method for controller compatibility
    public BigDecimal getSellingPrice() {
        return sellPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellPrice = sellingPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
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

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }

    @Override
    public String toString() {
        return "ProductDisplayForSellingDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", sellPrice=" + sellPrice +
                ", stockQuantity=" + stockQuantity +
                ", categoryName='" + categoryName + '\'' +
                ", statusName='" + statusName + '\'' +
                '}';
    }
}
