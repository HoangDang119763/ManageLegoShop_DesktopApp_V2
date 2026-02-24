package DTO;

/**
 * Info class for batch update is_pushed operations
 * Lớp dữ liệu cho batch update trạng thái đã đẩy giá
 */
public class DetailPushedInfoDTO {
    private String productId;
    private boolean isPushed;

    public DetailPushedInfoDTO(String productId, boolean isPushed) {
        this.productId = productId;
        this.isPushed = isPushed;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public boolean getIsPushed() {
        return isPushed;
    }

    public void setIsPushed(boolean isPushed) {
        this.isPushed = isPushed;
    }

    @Override
    public String toString() {
        return "DetailPushedInfoDTO{" +
                "productId='" + productId + '\'' +
                ", isPushed=" + isPushed +
                '}';
    }
}
