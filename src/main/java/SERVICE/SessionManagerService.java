package SERVICE;

import java.util.ArrayList;
import java.util.List;

import DTO.EmployeeSessionDTO;
import ENUM.PermissionKey;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.Window;

public class SessionManagerService {
    private static SessionManagerService instance;
    private EmployeeSessionDTO currentSession; // "Cái hộp" chứa tất cả

    private SessionManagerService() {
    }

    public static SessionManagerService getInstance() {
        if (instance == null)
            instance = new SessionManagerService();
        return instance;
    }

    // Khi Login thành công, gọi hàm này để nạp "thẻ" vào máy
    public void login(EmployeeSessionDTO session) {
        this.currentSession = session;
    }

    public void logout() {
        this.currentSession = null;
    }

    // --- CÁC HÀM CHECK QUYỀN BÂY GIỜ CỰC GỌN ---

    public boolean hasPermission(PermissionKey permissionKey) {
        if (currentSession == null || permissionKey == null)
            return false;
        // Check thẳng trong List Permission của DTO
        return currentSession.getPermissions().contains(permissionKey.name());
    }

    public boolean hasModuleAccess(int moduleId) {
        if (currentSession == null)
            return false;
        // Check trong List ModuleId của DTO
        return currentSession.getAllowedModuleIds().contains(moduleId);
    }

    public int employeeLoginId() {
        return currentSession != null ? currentSession.getEmployeeId() : -1;
    }

    public String getLoggedName() {
        return currentSession != null ? currentSession.getFullName() : "Guest";
    }

    public String getRoleName() {
        return currentSession != null ? currentSession.getRoleName() : "No Role";
    }

    public void forceLogout() {
        // Đảm bảo chạy trên UI Thread của JavaFX
        Platform.runLater(() -> {
            // 1. Xóa dữ liệu phiên
            logout();

            // 2. Dọn dẹp toàn bộ cửa sổ (Stage, Modal, Alert)
            List<Window> windows = new ArrayList<>(Window.getWindows());
            for (Window window : windows) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }

            // 3. Mở lại màn hình Login (Dùng đúng style cũ của bạn trong App.java)
            try {
                // Sử dụng UiUtils của bạn để mở màn hình Login
                // Giả sử bạn để LoginUI.fxml ở đúng đường dẫn
                NotificationUtils.showInfoAlert(AppMessages.FORCE_RELOGIN, AppMessages.DIALOG_TITLE);
                UiUtils.gI().openStage("/GUI/LoginUI.fxml", "Đăng nhập hệ thống");

                System.out.println(">>> Force Logout: Đã dọn dẹp và quay về màn hình đăng nhập.");
            } catch (Exception e) {
                System.err.println("Lỗi khi quay về màn hình Login: " + e.getMessage());
            }
        });
    }

    // Giữ nguyên logic canManage nhưng dùng dữ liệu từ DTO
    public boolean canManage() {
        if (currentSession == null)
            return false;
        for (Integer mid : currentSession.getAllowedModuleIds()) {
            if (mid != 5 && mid != 6)
                return true;
        }
        return false;
    }
}
