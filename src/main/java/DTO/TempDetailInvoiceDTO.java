package DTO;

import java.math.BigDecimal;

public class TempDetailInvoiceDTO {
    private String productId;
    private String name;
    private int quantity;
    private int stockQuantity;
    private BigDecimal sellingPrice;
    private BigDecimal totalPrice;

    // Constructor không tham số
    public TempDetailInvoiceDTO() {
    }

    // Constructor đầy đủ tham số
    public TempDetailInvoiceDTO(String productId, String name, int quantity, int stockQuantity, BigDecimal sellingPrice,
            BigDecimal totalPrice) {
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
        this.stockQuantity = stockQuantity;
        this.sellingPrice = sellingPrice;
        this.totalPrice = totalPrice;
    }

    // Constructor clone
    public TempDetailInvoiceDTO(TempDetailInvoiceDTO other) {
        this.productId = other.productId;
        this.name = other.name;
        this.quantity = other.quantity;
        this.stockQuantity = other.stockQuantity;
        this.sellingPrice = other.sellingPrice;
        this.totalPrice = other.totalPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
