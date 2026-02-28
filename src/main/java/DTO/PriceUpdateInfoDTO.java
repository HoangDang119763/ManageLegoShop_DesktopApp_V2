package DTO;

import java.math.BigDecimal;

/**
 * Info class for batch update price operations
 * Lớp dữ liệu cho batch update giá bán
 */
public class PriceUpdateInfoDTO {
    private String productId;
    private BigDecimal importPrice;
    private BigDecimal sellingPrice;

    public PriceUpdateInfoDTO(String productId, BigDecimal importPrice, BigDecimal sellingPrice) {
        this.productId = productId;
        this.importPrice = importPrice;
        this.sellingPrice = sellingPrice;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BigDecimal getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(BigDecimal importPrice) {
        this.importPrice = importPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    @Override
    public String toString() {
        return "PriceUpdateInfoDTO{" +
                "productId='" + productId + '\'' +
                ", importPrice=" + importPrice +
                ", sellingPrice=" + sellingPrice +
                '}';
    }
}
