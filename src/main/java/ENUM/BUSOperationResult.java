package ENUM;

/**
 * Enum để standardize return codes từ tất cả BUS classes
 * Sử dụng duy nhất 1 enum cho consistency và maintainability
 * 
 * Mapping:
 * - SUCCESS (1): Thao tác thành công
 * - NO_CHANGES (1): Thành công nhưng không có thay đổi (data trùng)
 * - INVALID_PARAMS (2): Tham số đầu vào không hợp lệ (null, <= 0, etc)
 * - INVALID_DATA (3): Dữ liệu không đạt requirement validate (phone, email,
 * etc)
 * - DB_ERROR (4): Lỗi cơ sở dữ liệu khi insert/update/delete
 * - UNAUTHORIZED (5): Người dùng không có quyền thực hiện (được check tại
 * Controller/SecureExecutor)
 * - NOT_FOUND (6): Record không tìm thấy
 * - CONFLICT (7): Dữ liệu xung đột (duplicate, foreign key, etc)
 */
public enum BUSOperationResult {
    // Success cases
    SUCCESS,
    NO_CHANGES,
    // Error cases
    INVALID_PARAMS,
    INVALID_DATA,
    DB_ERROR,
    UNAUTHORIZED,
    NOT_FOUND,
    CONFLICT,
    CANNOT_DELETE_SELF,
    CANNOT_DELETE_SYSTEM,
    FAIL;

    /**
     * Kiểm tra xem kết quả có phải thành công không
     * (SUCCESS hoặc NO_CHANGES đều coi là thành công)
     */
    public boolean isSuccess() {
        return this == SUCCESS || this == NO_CHANGES;
    }

    /**
     * Kiểm tra xem kết quả có phải error không
     */
    public boolean isError() {
        return !isSuccess();
    }
}
