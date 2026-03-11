package GUI;

import BUS.ModuleBUS;
import BUS.PermissionBUS;
import BUS.RolePermissionBUS;
import DTO.*;
import ENUM.BUSOperationResult;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            // Danh sách checkbox cho từng quyền trong module, dùng để xử lý ràng buộc
            final List<CheckBox> permissionCheckBoxes = new ArrayList<>();
            CheckBox tempViewPermissionCheckBox = null;

            // Xác định module này có áp dụng rule "phải có quyền Xem trước" hay không
            final boolean requireViewBeforeOthers = shouldRequireViewBeforeOthers(module.getName());

            // Duyệt qua các quyền con của module
            for (PermissionDTO permission : arrPermission) {
                if (permission.getModule_id() == module.getId()) {
                    CheckBox checkBox = new CheckBox(permission.getName());
                    checkBox.setUserData(permission.getId());

                    // Hiển thị đầy đủ tên quyền khi hover vào checkbox (kể cả khi bị ẩn bằng "...")
                    Tooltip tooltip = new Tooltip(permission.getName());
                    checkBox.setTooltip(tooltip);
                    Tooltip.install(checkBox, tooltip);

                    // Duyệt qua danh sách rolePermissions để kiểm tra xem quyền này có trong phân
                    // quyền của role không
                    boolean isActive = false;
                    for (RolePermissionDTO rp : allRolePermissionByRoleId) {
                        if (rp.getPermissionId() == permission.getId()) {
                            // Nếu bản ghi tồn tại, quyền đã được cấp cho role
                            checkBox.setSelected(true);
                            isActive = true;
                            break;
                        }
                    }

                    if (!isActive) {
                        allPermissionsActive = false;
                    }

                    checkBox.getStyleClass().add("check-box-m");
                    permissionCheckBoxes.add(checkBox);

                    // Xác định quyền "Xem" (quyền cơ bản bắt buộc trước khi bật các quyền khác)
                    String permissionName = permission.getName() != null ? permission.getName().trim() : "";
                    if (isPrimaryViewPermission(module.getName(), permissionName)) {
                        tempViewPermissionCheckBox = checkBox;
                    }

                    checkBox.setOnAction(e -> {
                        // Chỉ áp dụng rule "phải có quyền Xem" cho các module cấu hình như vậy
                        if (requireViewBeforeOthers) {
                            // Tìm checkbox quyền "Xem" trong cùng module
                            CheckBox finalViewPermissionCheckBox = null;
                            for (CheckBox cb : permissionCheckBoxes) {
                                String text = cb.getText() != null ? cb.getText().trim() : "";
                                if (isPrimaryViewPermission(module.getName(), text)) {
                                    finalViewPermissionCheckBox = cb;
                                    break;
                                }
                            }

                            // Nếu không phải quyền "Xem" thì chỉ cho phép tick khi quyền "Xem" đã được chọn
                            if (finalViewPermissionCheckBox != null
                                    && checkBox != finalViewPermissionCheckBox
                                    && !finalViewPermissionCheckBox.isSelected()) {
                                NotificationUtils.showErrorAlert(
                                        "Vui lòng bật quyền \"Xem\" trước khi chọn các quyền khác.",
                                        "Thông báo");
                                checkBox.setSelected(false);
                                return;
                            }
                        }

                        // Cập nhật trạng thái cho module checkbox (chỉ UI, chưa lưu DB)
                        updateModuleCheckboxState(moduleBox, permissionsBox);
                    });
                    permissionsBox.getChildren().add(checkBox);
                }
            }

            // Nếu tất cả quyền con của module đều được chọn, đánh dấu moduleBtn là true
            checkModuleBox.setSelected(allPermissionsActive);

            // Nếu có quyền "Xem" trong module, chỉ cho phép bật các quyền khác khi "Xem" được chọn
            final CheckBox viewPermissionCheckBox = tempViewPermissionCheckBox;
            if (requireViewBeforeOthers && viewPermissionCheckBox != null) {
                final CheckBox finalViewPermissionCheckBox1 = viewPermissionCheckBox;
                for (CheckBox cb : permissionCheckBoxes) {
                    if (cb != finalViewPermissionCheckBox1 && !finalViewPermissionCheckBox1.isSelected()) {
                        cb.setSelected(false);
                        cb.setDisable(true);
                    }
                }

                finalViewPermissionCheckBox1.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    RolePermissionBUS rpBus = RolePermissionBUS.getInstance();

                    for (CheckBox cb : permissionCheckBoxes) {
                        if (cb == finalViewPermissionCheckBox1) {
                            continue;
                        }

                        if (!isNowSelected) {
                            // Nếu tắt "Xem" thì tắt toàn bộ quyền còn lại
                            if (cb.isSelected()) {
                                cb.setSelected(false);
                                int permissionId = (int) cb.getUserData();
                                rpBus.togglePermission(role.getId(), permissionId, false);
                            }
                            cb.setDisable(true);
                        } else {
                            // Bật "Xem" thì cho phép người dùng tick các quyền khác
                            cb.setDisable(false);
                        }
                    }

                    // Cập nhật trạng thái checkbox cho module (chỉ UI)
                    updateModuleCheckboxState(moduleBox, permissionsBox);
                });
            }

            // Thêm sự kiện thay đổi trạng thái của module checkbox
            checkModuleBox.setOnAction(e -> {
                boolean isSelected = checkModuleBox.isSelected();
                for (int i = 0; i < permissionsBox.getChildren().size(); i++) {
                    CheckBox permissionCheckBox = (CheckBox) permissionsBox.getChildren().get(i);

                    // Nếu module yêu cầu quyền "Xem" trước và có quyền "Xem"
                    if (requireViewBeforeOthers && viewPermissionCheckBox != null) {
                        // Khi chọn module, đảm bảo quyền "Xem" chính được bật trước
                        final CheckBox finalViewPermissionCheckBox2 = viewPermissionCheckBox;
                        if (isSelected && !finalViewPermissionCheckBox2.isSelected()) {
                            finalViewPermissionCheckBox2.setSelected(true);
                        }
                    }

                    permissionCheckBox.setSelected(isSelected);
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

    /**
     * Xác định module nào bắt buộc phải bật quyền "Xem ..." trước khi cho phép chọn
     * các quyền còn lại.
     *
     * - "Quản lý nhân sự & Tài khoản": KHÔNG áp dụng rule này (cho phép chọn tự do).
     * - Các module còn lại, bao gồm "Quản lý chức vụ": áp dụng rule.
     */
    private boolean shouldRequireViewBeforeOthers(String moduleName) {
        if (moduleName == null) {
            return true;
        }
        String trimmed = moduleName.trim();
        // Module Nhân sự & Tài khoản là ngoại lệ
        if (trimmed.equalsIgnoreCase("Quản lý nhân sự & Tài khoản")) {
            return false;
        }
        return true;
    }

    /**
     * Xác định "quyền Xem chính" của mỗi module.
     *
     * - Với module "Quản lý chức vụ": quyền Xem chính là "Xem danh sách chức vụ".
     * - Các module khác: mặc định lấy các quyền bắt đầu bằng "Xem".
     */
    private boolean isPrimaryViewPermission(String moduleName, String permissionName) {
        if (permissionName == null) {
            return false;
        }
        String pn = permissionName.trim();
        if (moduleName != null && moduleName.trim().equalsIgnoreCase("Quản lý chức vụ")) {
            return pn.equalsIgnoreCase("Xem danh sách chức vụ");
        }
        return pn.startsWith("Xem");
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
        if (!UiUtils.gI().showConfirmAlert("Bạn chắc chắn lưu phân quyền này?", "Thông báo xác nhận")) {
            return;
        }

        if (role == null) {
            NotificationUtils.showErrorAlert("Chức vụ không hợp lệ.", "Thông báo");
            return;
        }

        // Thu thập tất cả permissionId đang được tick trong UI
        Set<Integer> selectedPermissionIds = new HashSet<>();

        permissionGridPane.getChildren().forEach(node -> {
            if (node instanceof VBox moduleBox && moduleBox.getChildren().size() >= 2) {
                Object permissionsNode = moduleBox.getChildren().get(1);
                if (permissionsNode instanceof VBox permissionsBox) {
                    permissionsBox.getChildren().forEach(child -> {
                        if (child instanceof CheckBox cb && cb.isSelected()) {
                            Object data = cb.getUserData();
                            if (data instanceof Integer) {
                                selectedPermissionIds.add((Integer) data);
                            }
                        }
                    });
                }
            }
        });

        RolePermissionBUS rpBus = RolePermissionBUS.getInstance();
        BUSResult result = rpBus.updateRolePermissions(role.getId(), selectedPermissionIds);

        if (result.getCode() == BUSOperationResult.SUCCESS) {
            isSaved = true;
            handleClose();
        } else {
            NotificationUtils.showErrorAlert(result.getMessage(), "Thông báo");
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
