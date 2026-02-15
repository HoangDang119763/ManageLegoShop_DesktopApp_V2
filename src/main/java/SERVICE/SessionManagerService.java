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

    public void forceLogout(String customMessage) {
        Platform.runLater(() -> {
            // 1. Xóa dữ liệu phiên trước
            logout();

            // 2. Xác định tin nhắn hiển thị
            String finalMsg = (customMessage != null && !customMessage.isEmpty())
                    ? customMessage + " - " + AppMessages.REQUEST_RELOGIN
                    : AppMessages.REQUEST_RELOGIN;

            // 3. Hiện Alert - Nó sẽ "treo" ở đây cho đến khi User bấm OK
            NotificationUtils.showInfoAlert(finalMsg, AppMessages.DIALOG_TITLE);

            // 4. Dọn dẹp toàn bộ cửa sổ cũ
            List<Window> windows = new ArrayList<>(Window.getWindows());
            for (Window window : windows) {
                if (window instanceof Stage stage) {
                    stage.close();
                }
            }

            // 5. Mở màn hình Login
            try {
                UiUtils.gI().openStage("/GUI/LoginUI.fxml", "Đăng nhập hệ thống");
                System.out.println(">>> Force Logout thành công.");
            } catch (Exception e) {
                System.out.println("Lỗi khi quay về màn hình Login: " + e.getMessage());
            }
        });
    }

    // Overload để dùng cho các trường hợp logout ép buộc thông thường (không có tin
    // nhắn riêng)
    public void forceLogout() {
        forceLogout(null);
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
