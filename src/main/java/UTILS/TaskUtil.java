package UTILS;

import DTO.BUSResult;
import ENUM.PermissionKey;
import SERVICE.SecureExecutor;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * TaskUtil - Utility class để quản lý background tasks với Loading overlay
 * 
 * ✅ Xử lý Throwable an toàn
 * ✅ Quản lý thread pool với ExecutorService
 * ✅ Tự động show/hide loading overlay
 * ✅ Minimum loading time = 1 giây (UX improvement)
 * ✅ Callback onSuccess và onError linh hoạt
 */
@Slf4j
public class TaskUtil {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(10, r -> {
        Thread t = new Thread(r, "AppTask-" + System.nanoTime());
        t.setDaemon(false);
        return t;
    });

    private static final long MIN_LOADING_TIME_MS = 300; // 0.5 giây

    private static void hideLoadingWithDelay(StackPane overlay, long startTimeMs, Runnable callback) {
        long elapsedMs = System.currentTimeMillis() - startTimeMs;
        long delayMs = Math.max(0, MIN_LOADING_TIME_MS - elapsedMs);

        if (delayMs > 0) {
            // Cần delay thêm
            EXECUTOR.schedule(() -> {
                Platform.runLater(() -> {
                    if (overlay != null)
                        overlay.setVisible(false);
                    callback.run();
                });
            }, delayMs, TimeUnit.MILLISECONDS);
        } else {
            // Đã vượt quá 1 giây, ẩn ngay
            Platform.runLater(() -> {
                if (overlay != null)
                    overlay.setVisible(false);
                callback.run();
            });
        }
    }

    // --- 1. HÀM THỰC THI LÕI (CORE) ---
    // Mọi logic Task đều nằm tại đây để dễ bảo trì
    private static <T> void internalExecute(
            StackPane overlay,
            Callable<T> logic,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {

        long startTime = System.currentTimeMillis();

        if (overlay != null)
            overlay.setVisible(true);

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return logic.call();
            }
        };

        task.setOnSucceeded(e -> {
            hideLoadingWithDelay(overlay, startTime, () -> onSuccess.accept(task.getValue()));
        });

        task.setOnFailed(e -> {
            Throwable throwable = task.getException();
            log.error("Task failed: ", throwable);
            hideLoadingWithDelay(overlay, startTime, () -> {
                if (onError != null)
                    onError.accept(throwable);
            });
        });

        EXECUTOR.submit(task);
    }

    // --- 2. CÁC BIẾN THỂ PUBLIC / SECURE (BUSResult) ---

    // Hàm gốc xử lý BUSResult (Dùng chung cho cả Public và Secure)
    private static void executeBusLogic(
            StackPane overlay,
            Callable<BUSResult> logic,
            Consumer<BUSResult> onSuccess,
            Consumer<Throwable> onError) {

        internalExecute(overlay, logic, result -> {
            if (result != null && result.isSuccess()) {
                onSuccess.accept(result);
            } else {
                String msg = (result != null) ? result.getMessage() : "Kết quả không xác định";
                onError.accept(new Exception(msg));
            }
        }, onError);
    }

    // --- 3. API CÔNG KHAI (Dùng trong Controller) ---

    // Secure: Có check quyền
    public static void executeSecure(StackPane overlay, PermissionKey key, Supplier<BUSResult> action,
            Consumer<BUSResult> onSuccess) {
        executeBusLogic(overlay,
                () -> SecureExecutor.executeSafeBusResult(key, action),
                onSuccess,
                err -> NotificationUtils.showErrorAlert(err.getMessage(), "Lỗi bảo mật"));
    }

    // Public: Không check quyền (Login, v.v.)
    public static void executePublic(StackPane overlay, Supplier<BUSResult> action, Consumer<BUSResult> onSuccess) {
        executeBusLogic(overlay,
                () -> SecureExecutor.executePublicBUSResult(action),
                onSuccess,
                err -> NotificationUtils.showErrorAlert(err.getMessage(), "Lỗi hệ thống"));
    }

    // Async: Tác vụ chạy ngầm bất kỳ (không che màn hình)
    public static <T> void executeAsync(Callable<T> logic, Consumer<T> onSuccess) {
        internalExecute(null, logic, onSuccess, err -> log.error("Async Error: ", err));
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}
