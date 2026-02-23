package DTO;

import java.math.BigDecimal;

/**
 * Info class for batch stock update with cost price snapshot
 * Lớp dữ liệu cho batch update tồn kho kèm giá vốn tại thời điểm bán (snapshot
 * cho thống kê lợi nhuận)
 */
public class UpdateStockWithCostDTO {
    private String productId;
    private int quantity;
    private BigDecimal costPrice;

    public UpdateStockWithCostDTO(String productId, int quantity, BigDecimal costPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.costPrice = costPrice;
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

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    @Override
    public String toString() {
        return "UpdateStockWithCostDTO{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", costPrice=" + costPrice +
                '}';
    }
}
