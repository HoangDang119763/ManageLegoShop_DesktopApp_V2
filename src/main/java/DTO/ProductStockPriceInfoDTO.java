package DTO;

import java.math.BigDecimal;

/**
 * Info class for products with stock, selling price, and import price
 * Lớp dữ liệu chứa tồn kho, giá bán và giá vốn của sản phẩm (dùng cho thống kê
 * lợi nhuận hóa đơn)
 */
public class ProductStockPriceInfoDTO {
    private String productId;
    private int stockQuantity;
    private BigDecimal importPrice;

    public ProductStockPriceInfoDTO(String productId, int stockQuantity,
            BigDecimal importPrice) {
        this.productId = productId;
        this.stockQuantity = stockQuantity;
        this.importPrice = importPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    @Override
    public String toString() {
        return "ProductStockPriceInfoDTO{" +
                "productId='" + productId + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", importPrice=" + importPrice +
                '}';
    }
}
