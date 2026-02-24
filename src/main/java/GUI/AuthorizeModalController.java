package GUI;

import BUS.ModuleBUS;
import BUS.PermissionBUS;
import BUS.RolePermissionBUS;
import DTO.*;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;

public class AuthorizeModalController {
    @Getter
    private boolean isSaved;
    @FXML
    private GridPane permissionGridPane;
    @FXML
    private Button saveBtn, closeBtn;
    @FXML
    private Label roleName;
    private RoleDTO role;
    private ArrayList<RolePermissionDTO> allRolePermissionByRoleId;

    @FXML
    public void initialize() {
        loadRoleData();
        loadCss();
        setupListeners();
    }

    public void loadCss() {
        URL cssUrl = getClass().getResource("/css/base.css");
        if (cssUrl != null) {
            permissionGridPane.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("⚠ Lỗi: Không tìm thấy file CSS '/css/base.css'");
        }
    }

    private void loadRoleData() {
        if (role != null) {
            roleName.setText(role.getName());
        } else
            handleClose();
    }

    private void setupListeners() {
        saveBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
    }

    public void setRole(RoleDTO role) {
        this.role = role;
        loadRolePermissions();
        addCheckboxesToGrid();
    }

    private void loadRolePermissions() {
        if (role == null)
            return;
        roleName.setText(role.getName());
        RolePermissionBUS rolePermissionBus = RolePermissionBUS.getInstance();

        // Lấy danh sách quyền của role trực tiếp từ DB
        allRolePermissionByRoleId = rolePermissionBus.getAllByRoleId(role.getId());
    }

    private void addCheckboxesToGrid() {
        ModuleBUS moduleBus = ModuleBUS.getInstance();
        PermissionBUS permissionBus = PermissionBUS.getInstance();

        // Gọi BUS trực tiếp, không dùng cache local
        ArrayList<ModuleDTO> arrModule = moduleBus.getAll();
        ArrayList<PermissionDTO> arrPermission = permissionBus.getAll();

        permissionGridPane.getChildren().clear(); // Xóa nội dung cũ để load lại

        int rowIndex = 0;
        int colIndex = 0;
        // Duyệt qua các module
        for (ModuleDTO module : arrModule) {
            VBox moduleBox = new VBox();
            moduleBox.setSpacing(5.0);

            // Tạo checkbox đại diện cho module
            CheckBox checkModuleBox = new CheckBox(module.getName());
            checkModuleBox.getStyleClass().add("check-box-s");

            // Kiểm tra xem tất cả các permission của module có được chọn không
            boolean allPermissionsActive = true;
            VBox permissionsBox = new VBox();
            permissionsBox.setSpacing(5.0);
            permissionsBox.setPadding(new Insets(0, 0, 0, 15));

            // Duyệt qua các quyền con của module
            for (PermissionDTO permission : arrPermission) {
                if (permission.getModule_id() == module.getId()) {
                    CheckBox checkBox = new CheckBox(permission.getName());
                    checkBox.setUserData(permission.getId());
                    // Duyệt qua danh sách rolePermissions để kiểm tra xem quyền này có trong phân
                    // quyền của role không

                    for (RolePermissionDTO rp : allRolePermissionByRoleId) {

                        if (rp.getPermissionId() == permission.getId()) {
                            // Nếu bản ghi tồn tại, quyền đã được cấp cho role
                            checkBox.setSelected(true);
                            break;
                        }
                    }

                    checkBox.getStyleClass().add("check-box-m");
                    checkBox.setOnAction(e -> {
                        boolean isSelected = checkBox.isSelected();
                        int permissionId = (int) checkBox.getUserData();

                        // Toggle permission: chọn = thêm, không chọn = xóa
                        RolePermissionBUS rpBus = RolePermissionBUS.getInstance();
                        rpBus.togglePermission(role.getId(), permissionId, isSelected);

                        // Cập nhật trạng thái cho module checkbox
                        updateModuleCheckboxState(moduleBox, permissionsBox);
                    });
                    permissionsBox.getChildren().add(checkBox);
                }
            }

            // Nếu tất cả quyền con của module đều được chọn, đánh dấu moduleBtn là true
            checkModuleBox.setSelected(allPermissionsActive);

            // Thêm sự kiện thay đổi trạng thái của module checkbox
            checkModuleBox.setOnAction(e -> {
                // Nếu module checkbox được tích, tất cả các permission checkbox con sẽ được
                // tích
                boolean isSelected = checkModuleBox.isSelected();
                for (int i = 0; i < permissionsBox.getChildren().size(); i++) {
                    CheckBox permissionCheckBox = (CheckBox) permissionsBox.getChildren().get(i);
                    permissionCheckBox.setSelected(isSelected);

                    // Toggle permission cho mỗi permission
                    int permissionId = (int) permissionCheckBox.getUserData();
                    RolePermissionBUS rpBus = RolePermissionBUS.getInstance();
                    rpBus.togglePermission(role.getId(), permissionId, isSelected);
                }
            });

            moduleBox.getChildren().add(checkModuleBox);
            moduleBox.getChildren().add(permissionsBox);
            permissionGridPane.add(moduleBox, colIndex++, rowIndex);
            if (colIndex == 4) {
                rowIndex++;
                colIndex = 0;
            }
        }
    }

    private void updateModuleCheckboxState(VBox moduleBox, VBox permissionsBox) {
        CheckBox checkModuleBox = (CheckBox) moduleBox.getChildren().get(0);

        boolean allSelected = true;
        for (int i = 0; i < permissionsBox.getChildren().size(); i++) {
            CheckBox permissionCheckBox = (CheckBox) permissionsBox.getChildren().get(i);
            if (!permissionCheckBox.isSelected()) {
                allSelected = false;
                break;
            }
        }

        checkModuleBox.setSelected(allSelected);
    }

    private void handleSave() {
        if (UiUtils.gI().showConfirmAlert("Bạn chắc chắn lưu phân quyền này?", "Thông báo xác nhận")) {
            handleAuthorize();
        }
    }

    private void handleAuthorize() {
        isSaved = true;
        handleClose();
    }

    private boolean handleUpdateError(int updateResult) {
        switch (updateResult) {
            case 2 ->
                NotificationUtils.showErrorAlert("Có lỗi khi cập nhật phân quyền. Vui lòng thử lại.", "Thông báo");
            case 3 ->
                NotificationUtils.showErrorAlert("Bạn không thể cập nhật phân quyền của chính mình.", "Thông báo");
            case 4 -> NotificationUtils.showErrorAlert("Bạn không thể cập nhật phân quyền của chức vụ ngang quyền.",
                    "Thông báo");
            case 5 -> NotificationUtils
                    .showErrorAlert("Bạn không có quyền \"Sửa phân quyền\" để thực hiện thao tác này.", "Thông báo");
            case 6 ->
                NotificationUtils.showErrorAlert("Cập nhật phân quyền thất bại. Vui lòng thử lại sau.", "Thông báo");
            default -> NotificationUtils.showErrorAlert("Lỗi không xác định, vui lòng thử lại sau.", "Thông báo");
        }
        return false;
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }
}
