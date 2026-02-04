package DTO;

import java.math.BigDecimal;

public class DetailInvoiceDTO {
    private int invoiceId;
    private String productId;
    private int quantity;
    private BigDecimal price;
    private BigDecimal costPrice;
    private BigDecimal totalPrice;

    public DetailInvoiceDTO() {
    }

    public DetailInvoiceDTO(int invoiceId, String productId, int quantity, BigDecimal price, BigDecimal costPrice,
            BigDecimal totalPrice) {
        this.invoiceId = invoiceId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.costPrice = costPrice;
        this.totalPrice = totalPrice;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
