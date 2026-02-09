package SERVICE;

import ENUM.BUSOperationResult;
import ENUM.PermissionKey;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * SecureExecutor - Centralized permission checking
 */
@Slf4j
public class SecureExecutor {

    // ==========================
    // 1️⃣ RUN (THROW EXCEPTION - DEV MODE)
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
    public static BUSOperationResult runSafe(PermissionKey key, Runnable action) {
        if (!SessionManagerService.getInstance().hasPermission(key)) {
            return BUSOperationResult.UNAUTHORIZED;
        }

        try {
            action.run();
            return BUSOperationResult.SUCCESS;
        } catch (Exception e) {
            log.error("SecureExecutor error", e);
            return BUSOperationResult.DB_ERROR;
        }
    }

    // ==========================
    // 3️⃣ SAFE RUN WITH RETURN VALUE (🔥 IMPORTANT)
    // ==========================
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
    // 4️⃣ BUSOperationResult SPECIALIZED VERSION
    // ==========================
    public static BUSOperationResult runSafeResult(
            PermissionKey key,
            Supplier<BUSOperationResult> action) {

        if (!SessionManagerService.getInstance().hasPermission(key)) {
            return BUSOperationResult.UNAUTHORIZED;
        }

        try {
            return action.get();
        } catch (Exception e) {
            log.error("SecureExecutor error", e);
            return BUSOperationResult.DB_ERROR;
        }
    }

    // ==========================
    // 5️⃣ PUBLIC EXECUTE
    // ==========================
    public static <T> T executePublic(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            log.error("Public action error", e);
            return null;
        }
    }

    public static BUSOperationResult executePublicResult(Supplier<BUSOperationResult> action) {
        try {
            return action.get();
        } catch (Exception e) {
            log.error("Public action error", e);
            return BUSOperationResult.DB_ERROR;
        }
    }

}
