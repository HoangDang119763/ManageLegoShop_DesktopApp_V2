package SERVICE;

import DTO.BUSResult;
import ENUM.BUSOperationResult;
import ENUM.PermissionKey;
import UTILS.AppMessages;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * SecureExecutor - Centralized permission checking
 */
@Slf4j
public class SecureExecutor {

    // ==========================
    // 1️⃣ DEV MODE THROW EXCEPTION
    // ==========================
    public static void run(PermissionKey key, Runnable action) {
        SessionManagerService session = SessionManagerService.getInstance();
        int empId = session.employeeLoginId();

        if (!session.hasPermission(key)) {
            String msg = "Không có quyền: " + key;
            log.warn("Security violation - User {} tried {}", empId, key);
            throw new SecurityException(msg);
        }

        action.run();
    }

    // ==========================
    // 2️⃣ SAFE RUN (NO RETURN)
    // ==========================
    // Dùng khi BUS chỉ thực hiện hành động (void)
    // Ví dụ: delete, reset password, log, refresh cache
    // Trả về BUSResult để UI xử lý thông báo
    /*
     * BUSResult res = SecureExecutor.runSafe(
     * PermissionKey.PRODUCT_DELETE,
     * () -> productBUS.delete(id)
     * );
     */
    public static BUSResult runSafe(PermissionKey key, Runnable action) {
        if (!SessionManagerService.getInstance().hasPermission(key)) {
            return new BUSResult(BUSOperationResult.UNAUTHORIZED, AppMessages.UNAUTHORIZED);
        }

        try {
            action.run();
            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
        } catch (Exception e) {
            log.error("SecureExecutor error", e);
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }

    // ==========================
    // 3️⃣ SAFE RUN GENERIC RETURN (T)
    // ==========================
    // Dùng khi BUS trả dữ liệu DTO hoặc List
    // Nếu không có quyền → trả unauthorizedValue (null hoặc empty list)
    /*
     * List<EmployeeDTO> list = SecureExecutor.runSafeResult(
     * PermissionKey.EMPLOYEE_VIEW,
     * () -> employeeBUS.getAll(),
     * Collections.emptyList()
     * );
     */
    public static <T> T runSafeResult(PermissionKey key, Supplier<T> action, T unauthorizedValue) {
        if (!SessionManagerService.getInstance().hasPermission(key)) {
            return unauthorizedValue;
        }

        try {
            return action.get();
        } catch (Exception e) {
            log.error("SecureExecutor error", e);
            return unauthorizedValue;
        }
    }

    // ==========================
    // 4️⃣ SAFE RUN BUSRESULT (🔥 MAIN)
    // ==========================
    // Dùng khi BUS trả BUSResult (code + message)
    // Đây là method CHUẨN cho update/insert/delete business logic
    /*
     * BUSResult res = SecureExecutor.runSafeBUSResult(
     * PermissionKey.EMPLOYEE_UPDATE,
     * () -> employeeBUS.update(emp)
     * );
     */
    public static BUSResult runSafeBUSResult(
            PermissionKey key,
            Supplier<BUSResult> action) {

        if (!SessionManagerService.getInstance().hasPermission(key)) {
            return new BUSResult(BUSOperationResult.UNAUTHORIZED, AppMessages.UNAUTHORIZED);
        }

        try {
            return action.get();
        } catch (Exception e) {
            log.error("SecureExecutor error", e);
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }

    // ==========================
    // 5️⃣ PUBLIC EXECUTE
    // ==========================
    // Dùng cho các chức năng public hoặc personal (update profile, login, register)
    // Không check permission
    /*
     * BUSResult res = SecureExecutor.executePublicBUSResult(
     * () -> employeeBUS.updatePersonalInfo(emp)
     * );
     */
    public static <T> T executePublic(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            log.error("Public action error", e);
            return null;
        }
    }

    public static BUSResult executePublicBUSResult(Supplier<BUSResult> action) {
        try {
            return action.get();
        } catch (Exception e) {
            log.error("Public action error", e);
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }
}
