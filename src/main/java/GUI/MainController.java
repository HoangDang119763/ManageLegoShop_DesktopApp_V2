package GUI;

import BUS.*;
import DTO.EmployeeDTO;
import SERVICE.SessionManagerService;
import UTILS.UiUtils;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Slf4j
public class MainController {
    @FXML
    private Button closeBtn, minimizeBtn, logoutBtn;
    @FXML
    private Pane mainContent;
    @FXML
    private Label employeeLoginFullName, employeeRoleName;
    @FXML
    private VBox groupBtn;
    static boolean isLoaded = false;
    private Button selectedButton = null;
    private static final MainController INSTANCE = new MainController();

    public MainController() {
    }

    public static MainController getInstance() {
        return INSTANCE;
    }

    @FXML
    public void initialize() {
        loadSessionData();
        setupEventHandlers();
        loadAllLocalData();
        loadAllowedModules();
    }

    public void loadSessionData() {
        EmployeeDTO currEmployee = SessionManagerService.getInstance().currEmployee();
        employeeLoginFullName.setText(currEmployee.getFirstName() + " " + currEmployee.getLastName());
        employeeRoleName.setText(RoleBUS.getInstance().getByIdLocal(currEmployee.getRoleId()).getName());
    }

    public void setupEventHandlers() {
        logoutBtn.setOnMouseClicked(e -> {
            if (!UiUtils.gI().showConfirmAlert("Bạn chắc muốn thoát?", "Thông báo xác nhận"))
                return;
            // SessionManagerService.getInstance().logout();
            ParallelTransition animation = UiUtils.gI().createButtonAnimation(logoutBtn);
            animation.setOnFinished(event -> logout());
            animation.play();
        });

        closeBtn.setOnMouseClicked(e -> close());
        minimizeBtn.setOnMouseClicked(this::minimize);
    }

    public void loadAllLocalData() {
        if (!isLoaded) {
            EmployeeBUS.getInstance().loadLocal();
            RoleBUS.getInstance().loadLocal();
            RolePermissionBUS.getInstance().loadLocal();
            ModuleBUS.getInstance().loadLocal();
            PermissionBUS.getInstance().loadLocal();
            StatusBUS.getInstance().loadLocal();
            AccountBUS.getInstance().loadLocal();
            isLoaded = true;
        }
    }

    public void minimize(MouseEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).setIconified(true);
    }

    public void close() {
        System.exit(0);
    }

    public void logout() {
        UiUtils.gI().openStage("/GUI/NavigatePermission.fxml", "Danh sách chức năng");
        handleClose();
    }

    public void loadFXML(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Pane newContent = loader.load();
            mainContent.getChildren().setAll(newContent);
        } catch (IOException e) {
            log.error("Lỗi tải FXML: " + fxmlFile, e);
        }
    }

    // Đóng gói Metadata của Module
    private record ModuleMetadata(int id, String name, String icon) {
    }

    // Mapping Module ID → FXML Path
    private static final Map<Integer, String> MODULE_FXML_MAP = Map.ofEntries(
            Map.entry(0, "/GUI/EmployeeInfoUI.fxml"),
            Map.entry(1, "/GUI/EmployeeUI.fxml"),
            Map.entry(2, "/GUI/CustomerUI.fxml"),
            Map.entry(3, "/GUI/ProductUI.fxml"),
            Map.entry(4, "/GUI/SupplierUI.fxml"),
            Map.entry(5, "/GUI/InvoiceUI.fxml"),
            Map.entry(6, "/GUI/ImportUI.fxml"),
            Map.entry(7, "/GUI/CategoryUI.fxml"),
            Map.entry(8, "/GUI/DiscountUI.fxml"),
            Map.entry(9, "/GUI/RoleUI.fxml"),
            Map.entry(10, "/GUI/AccountUI.fxml"),
            Map.entry(11, "/GUI/StatisticUI.fxml"));

    private void loadAllowedModules() {
        // 1. Khởi tạo danh sách Module có thứ tự (ID, Name, Icon)
        List<ModuleMetadata> allModules = Arrays.asList(
                new ModuleMetadata(0, "Thông tin", "employee_info.png"),
                new ModuleMetadata(3, "Sản phẩm", "product.png"),
                new ModuleMetadata(1, "Nhân viên", "employee.png"),
                new ModuleMetadata(2, "Khách hàng", "customer.png"),
                new ModuleMetadata(4, "Nhà cung cấp", "supplier.png"),
                new ModuleMetadata(7, "Thể loại", "category.png"),
                new ModuleMetadata(8, "Khuyến mãi", "discount.png"),
                new ModuleMetadata(5, "Hóa đơn", "invoice.png"),
                new ModuleMetadata(6, "Phiếu nhập", "import.png"),
                new ModuleMetadata(9, "Chức vụ", "role.png"),
                new ModuleMetadata(10, "Tài khoản", "account.png"),
                new ModuleMetadata(11, "Thống kê", "statistical.png"));

        groupBtn.getChildren().clear();
        List<Button> buttons = new ArrayList<>();
        SessionManagerService session = SessionManagerService.getInstance();

        // 2. Duyệt qua danh sách đã đóng gói
        for (ModuleMetadata meta : allModules) {
            // Module 0 (Thông tin nhân viên) luôn được phép truy cập
            if (meta.id() == 0 || session.hasModuleAccess(meta.id())) {
                Button btn = createModuleButton(
                        meta.name(),
                        meta.icon(),
                        () -> handleModuleClick(meta.id(), meta.name()));
                buttons.add(btn);
            }
        }

        groupBtn.getChildren().addAll(buttons);

        if (!buttons.isEmpty()) {
            buttons.getFirst().fire();
        }
    }

    private Button createModuleButton(String text, String iconPath, Runnable action) {
        Button btn = new Button("   " + text);
        btn.setPrefSize(194, 40);
        btn.getStyleClass().add("nav-btn");

        // Kiểm tra CSS trước khi thêm
        URL cssUrl = getClass().getResource("/css/main.css");
        if (cssUrl != null) {
            btn.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("⚠ Lỗi: Không tìm thấy file CSS '/css/main.css'");
        }

        // Kiểm tra và tải icon
        String imagePath = "/images/icon/" + iconPath;
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        Image image;

        if (imageStream != null) {
            image = new Image(imageStream);
        } else {
            System.err.println("⚠ Lỗi: Không tìm thấy ảnh " + imagePath + ", thử dùng ảnh mặc định.");
            InputStream defaultStream = getClass().getResourceAsStream("/images/icon/default.png");
            if (defaultStream != null) {
                image = new Image(defaultStream);
            } else {
                System.err.println("⚠ Lỗi nghiêm trọng: Không tìm thấy ảnh mặc định!");
                image = new Image("https://via.placeholder.com/25"); // Sử dụng ảnh tạm thời từ Internet
            }
        }

        ImageView icon = new ImageView(image);
        icon.setFitWidth(25);
        icon.setFitHeight(25);
        btn.setGraphic(icon);

        btn.setOnAction(event -> {
            UiUtils.gI().applyButtonAnimation(btn);
            action.run();
        });

        return btn;
    }

    private void handleModuleClick(int moduleId, String moduleName) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("nav-btn-active");
        }

        for (Node node : groupBtn.getChildren()) {
            if (node instanceof Button btn && btn.getText().trim().equals(moduleName.trim())) {
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

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }

}
