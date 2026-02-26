package GUI;

import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.UiUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class HrMainController {
    private static final Logger log = LoggerFactory.getLogger(HrMainController.class);
    @FXML
    private Button closeBtn, minimizeBtn, logoutBtn;
    @FXML
    private Pane mainContent;
    @FXML
    private Label employeeLoginFullName, employeeRoleName;
    @FXML
    private VBox groupBtn;

    private Button selectedButton = null;

    // Mapping Module ID → FXML Path for HR-related modules
    private static final Map<Integer, String> MODULE_FXML_MAP = Map.ofEntries(
            Map.entry(0, "/GUI/EmployeeInfoUI.fxml"), // Thông tin cá nhân (luôn có)
            Map.entry(1, "/GUI/EmployeeUI.fxml"), // Quản lý NV & TK
            Map.entry(12, "/GUI/EmploymentHistory.fxml")); // Quản lý Nhân sự

    @FXML
    public void initialize() {
        setupEventHandlers();
        loadSessionData();
        loadAllowedModules();
    }

    public void loadSessionData() {
        // [TỐI ƯU] Lấy trực tiếp từ Session, không chọc DB
        SessionManagerService session = SessionManagerService.getInstance();
        if (session != null) {
            employeeLoginFullName.setText(session.getLoggedName());
            employeeRoleName.setText(session.getPositionName());
        }
    }

    public void setupEventHandlers() {
        logoutBtn.setOnMouseClicked(e -> {
            if (UiUtils.gI().showConfirmAlert(AppMessages.LOGOUT_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
                // Sử dụng forceLogout để dọn dẹp sạch sẽ toàn bộ App
                SessionManagerService.getInstance().normalLogout();
            }
        });

        closeBtn.setOnMouseClicked(e -> handleClose());
        minimizeBtn.setOnMouseClicked(e -> ((Stage) ((Node) e.getSource()).getScene().getWindow()).setIconified(true));
    }

    private void handleClose() {
        UiUtils.gI().openStage(
                "/GUI/NavigatePermission.fxml",
                "Danh sách chức năng");
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }

    }

    record ModuleMetadata(int id, String name, String icon) {
    }

    private void loadAllowedModules() {
        groupBtn.getChildren().clear();
        List<Button> buttons = new ArrayList<>();
        SessionManagerService sessionService = SessionManagerService.getInstance();

        // 1. Định nghĩa các Module liên quan đến HR
        List<ModuleMetadata> hrModules = Arrays.asList(
                new ModuleMetadata(0, "Cá nhân", "employee_info.png"), // ID 0: Luôn cho phép
                new ModuleMetadata(1, "Nhân viên", "employee.png"), // ID 1
                new ModuleMetadata(12, "Điều chuyển", "employment_history.png"));

        // 2. Lọc và tạo Button dựa trên quyền thực tế trong Session
        for (ModuleMetadata meta : hrModules) {
            // Module 0 luôn hiện, các module khác phải có ID trong allowedModules của DTO
            if (meta.id() == 0 || sessionService.hasModuleAccess(meta.id())) {
                Button btn = createModuleButton(meta.name(), meta.icon(), () -> {
                    handleModuleClick(meta.id(), meta.name());
                });

                // Gắn ID vào UserData để handleModuleClick biết là module nào
                btn.setUserData(meta.id());
                buttons.add(btn);
            }
        }

        // 3. Đưa các nút đã lọc vào Menu bên trái
        groupBtn.getChildren().addAll(buttons);

        // 4. Mặc định mở module đầu tiên khi vừa vào App
        if (!buttons.isEmpty()) {
            buttons.getFirst().fire();
        }
    }

    private Button createModuleButton(String text, String iconPath, Runnable action) {
        // 1. Khởi tạo Button với khoảng trống để text không dính vào icon
        Button btn = new Button("   " + text);
        btn.setPrefSize(194, 40);
        btn.getStyleClass().add("nav-btn"); // Class trong main.css của bạn

        // 2. Tải Icon từ thư mục resources
        String imagePath = "/images/icon/" + iconPath;
        try {
            InputStream is = getClass().getResourceAsStream(imagePath);
            if (is != null) {
                Image image = new Image(is);
                ImageView iconView = new ImageView(image);
                iconView.setFitWidth(22); // Kích thước chuẩn cho Sidebar
                iconView.setFitHeight(22);
                btn.setGraphic(iconView);
            } else {
                log.warn("Không tìm thấy icon tại: {}", imagePath);
                // Có thể set một icon mặc định ở đây nếu muốn
            }
        } catch (Exception e) {
            log.error("Lỗi khi nạp ảnh icon: " + iconPath, e);
        }

        // 3. Thiết lập sự kiện Click
        btn.setOnAction(event -> {
            // Chạy hiệu ứng bấm nút từ UiUtils của bạn cho "xịn"
            UiUtils.gI().applyButtonAnimation(btn);

            // Thực thi hành động (thường là load FXML)
            action.run();
        });

        return btn;
    }

    private void handleModuleClick(int moduleId, String moduleName) {
        // Cập nhật trạng thái Active cho Button
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("nav-btn-active");
        }

        // Tìm button vừa click để highlight
        for (Node node : groupBtn.getChildren()) {
            if (node instanceof Button btn && (int) btn.getUserData() == moduleId) {
                btn.getStyleClass().add("nav-btn-active");
                selectedButton = btn;
                break;
            }
        }

        String fxmlPath = MODULE_FXML_MAP.get(moduleId);
        if (fxmlPath != null) {
            loadFXML(fxmlPath);
        }
    }

    public void loadFXML(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Pane newContent = loader.load();

            // Anchor để nội dung con dãn đều theo mainContent
            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);

            mainContent.getChildren().setAll(newContent);
        } catch (IOException e) {
            log.error("Lỗi tải giao diện: " + fxmlFile, e);
        }
    }
}
