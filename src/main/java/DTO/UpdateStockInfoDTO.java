package DTO;

/**
 * Info class for batch update stock operations
 * Lớp dữ liệu cho batch update tồn kho
 */
public class UpdateStockInfoDTO {
    private String productId;
    private int quantity;

    public UpdateStockInfoDTO(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
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

    @Override
    public String toString() {
        return "UpdateStockInfoDTO{" +
                "productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
