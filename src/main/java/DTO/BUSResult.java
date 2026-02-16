package DTO;

import ENUM.BUSOperationResult;

/**
 * Result object trả về từ BUS layer
 * Chứa operation code và message tương ứng
 * UI sẽ dùng message này để hiển thị mà không cần switch/case
 */
public class BUSResult {
    private final BUSOperationResult code;
    private final String message; // message runtime (lấy từ AppMessages hoặc custom)
    private Object data;

    /**
     * Tạo BUSResult với custom message
     * Dùng khi UI cần thông tin chi tiết (ví dụ: "Khách hàng 'Nguyễn Văn A' đã tồn
     * tại")
     */
    public BUSResult(BUSOperationResult code) {
        this.code = code;
        this.message = null;
    }

    public BUSResult(BUSOperationResult code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BUSResult(BUSOperationResult code, String customMessage) {
        this.code = code;
        this.message = customMessage;
    }

    public BUSOperationResult getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return code.isSuccess();
    }

    public boolean isError() {
        return code.isError();
    }

    @Override
    public String toString() {
        return "BUSResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

    @SuppressWarnings("unchecked")
    public <T> PagedResponse<T> getPagedData() {
        if (this.data instanceof PagedResponse) {
            return (PagedResponse<T>) this.data;
        }
        return new PagedResponse<>(new java.util.ArrayList<>(), 0, 0, 0);
    }
}
