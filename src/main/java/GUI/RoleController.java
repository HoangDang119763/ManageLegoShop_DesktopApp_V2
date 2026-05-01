package GUI;

import BUS.EmployeeBUS;
import BUS.RoleBUS;
import BUS.RolePermissionBUS;
import DTO.BUSResult;
import DTO.RoleDTO;
import ENUM.BUSOperationResult;
import ENUM.PermissionKey;
import INTERFACE.IController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class RoleController implements IController {
    @FXML
    private AnchorPane mainContent;
    @FXML
    private TableView<RoleDTO> tblRole;
    @FXML
    private TableColumn<RoleDTO, String> tlb_col_id;
    @FXML
    private TableColumn<RoleDTO, String> tlb_col_name;
    @FXML
    private TableColumn<RoleDTO, String> tlb_col_description;
    @FXML
    private TableColumn<RoleDTO, String> tlb_col_salaryCoefficient;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn, authorizeBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbSearchBy;

    private String searchBy = "Mã vai trò";
    private String keyword = "";
    private RoleDTO selectedRole;
    private boolean canView = true;

    @FXML
    public void initialize() {
        tblRole.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblRole.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        if (!canView)
            return;

        loadComboBox();
        setupListeners();

        loadTable();
        applyFilters();
    }

    @Override
    public void loadTable() {
        tlb_col_id.setCellValueFactory(cellData -> new SimpleStringProperty(
                String.valueOf(cellData.getValue().getId())));
        tlb_col_name.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getName()));
        tlb_col_description.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription() == null ? "" : cellData.getValue().getDescription()));

        // Cột "Số quyền": đếm số quyền được gán cho từng chức vụ
        tlb_col_salaryCoefficient.setCellValueFactory(cellData -> {
            int roleId = cellData.getValue().getId();
            int count = RolePermissionBUS.getInstance().countByRoleId(roleId);
            return new SimpleStringProperty(String.valueOf(count));
        });
    }

    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã vai trò", "Tên vai trò");
        cbSearchBy.getSelectionModel().selectFirst();
    }

    @Override
    public void setupListeners() {
        cbSearchBy.setOnAction(event -> handleSearchByChange());
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> handleKeywordChange());

        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert("Làm mới thành công", "Thông báo");
        });

        authorizeBtn.setOnAction(event -> handleAuthorize());
        addBtn.setOnAction(e -> handleAdd());
        editBtn.setOnAction(e -> handleEdit());
        deleteBtn.setOnAction(e -> handleDelete());

        tblRole.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedRole = newSel;
        });
    }

    private void handleSearchByChange() {
        searchBy = cbSearchBy.getValue();
        applyFilters();
    }

    private void handleKeywordChange() {
        keyword = txtSearch.getText().trim();
        applyFilters();
    }

    @Override
    public void applyFilters() {
        TaskUtil.executeSecure(null, PermissionKey.ROLE_LIST_VIEW,
                () -> new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS,
                        RoleBUS.getInstance().getAllForUI()),
                result -> applyRoleFilters(result.getData()));
    }

    private void applyRoleFilters(ArrayList<RoleDTO> allRoles) {
        if (allRoles == null) {
            allRoles = new ArrayList<>();
        }

        String kw = keyword.toLowerCase(Locale.ROOT);

        List<RoleDTO> filtered = allRoles.stream().filter(r -> {
            if (kw.isEmpty()) {
                return true;
            }
            if ("Mã vai trò".equals(searchBy)) {
                return String.valueOf(r.getId()).contains(kw);
            }
            String name = r.getName() != null ? r.getName().toLowerCase(Locale.ROOT) : "";
            return name.contains(kw);
        }).collect(Collectors.toList());

        tblRole.setItems(FXCollections.observableArrayList(filtered));
        tblRole.getSelectionModel().clearSelection();
        selectedRole = null;
    }

    @Override
    public void resetFilters() {
        cbSearchBy.getSelectionModel().selectFirst();
        txtSearch.clear();
        searchBy = "Mã vai trò";
        keyword = "";
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();
        canView = session.hasPermission(PermissionKey.ROLE_LIST_VIEW);
        if (!canView) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }

        boolean canAdd = session.hasPermission(PermissionKey.ROLE_INSERT);
        boolean canEdit = session.hasPermission(PermissionKey.ROLE_UPDATE);
        boolean canDelete = session.hasPermission(PermissionKey.ROLE_DELETE);
        boolean canAuthorize = session.hasPermission(PermissionKey.PERMISSION_UPDATE);

        if (!canAdd) {
            functionBtns.getChildren().remove(addBtn);
        }
        if (!canEdit) {
            functionBtns.getChildren().remove(editBtn);
        }
        if (!canDelete) {
            functionBtns.getChildren().remove(deleteBtn);
        }
        if (!canAuthorize) {
            authorizeBtn.setVisible(false);
            authorizeBtn.setManaged(false);
        }
    }

    private boolean isNotSelectedRole() {
        if (selectedRole == null) {
            selectedRole = tblRole.getSelectionModel().getSelectedItem();
        }
        return selectedRole == null;
    }

    private void handleAuthorize() {
        if (isNotSelectedRole()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn vai trò", "Thông báo");
            return;
        }

        if (selectedRole.getId() == SessionManagerService.getInstance().employeeRoleId()) {
            NotificationUtils.showErrorAlert("Bạn không thể cập nhật phân quyền của chính mình.", "Thông báo");
            return;
        }

        TaskUtil.executeSecure(null, PermissionKey.PERMISSION_UPDATE,
                () -> new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS),
                result -> {
                    AuthorizeModalController modalController = UiUtils.gI().openStageWithController(
                            "/GUI/AuthorizeModal.fxml",
                            controller -> controller.setRole(selectedRole),
                            "Phân quyền");

                    if (modalController != null && modalController.isSaved()) {
                        NotificationUtils.showInfoAlert("Cập nhật phân quyền thành công", "Thông báo");
                        resetFilters();
                    }
                });
    }

    private void handleDelete() {
        if (isNotSelectedRole()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn vai trò", "Thông báo");
            return;
        }

        if (selectedRole.getId() == SessionManagerService.getInstance().employeeRoleId()) {
            NotificationUtils.showErrorAlert("Bạn không thể xóa vai trò của chính mình.", "Thông báo");
            return;
        }

        int employeeCount = EmployeeBUS.getInstance().countByRoleId(selectedRole.getId());
        if (employeeCount > 0) {
            String ask = "Hiện có " + employeeCount + " nhân viên sở hữu vai trò này!";
            if (!UiUtils.gI().showConfirmAlert("Bạn chắc muốn xóa vai trò này? " + ask, "Thông báo xác nhận")) {
                return;
            }
        }

        TaskUtil.executeSecure(null, PermissionKey.ROLE_DELETE,
                () -> RoleBUS.getInstance().delete(selectedRole.getId()),
                result -> {
                    if (result.getCode() == BUSOperationResult.SUCCESS) {
                        NotificationUtils.showInfoAlert("Xóa chức vụ thành công.", "Thông báo");
                        resetFilters();
                    } else {
                        String message = result.getMessage() != null ? result.getMessage() : "Có lỗi khi xóa chức vụ.";
                        NotificationUtils.showErrorAlert(message, "Thông báo");
                    }
                });
    }

    private void handleAdd() {
        TaskUtil.executeSecure(null, PermissionKey.ROLE_INSERT,
                () -> new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS),
                result -> {
                    RoleModalController modalController = UiUtils.gI().openStageWithController(
                            "/GUI/RoleModal.fxml",
                            controller -> controller.setTypeModal(0),
                            "Thêm chức vụ");
                    if (modalController != null && modalController.isSaved()) {
                        NotificationUtils.showInfoAlert("Thêm chức vụ thành công", "Thông báo");
                        resetFilters();
                    }
                });
    }

    private void handleEdit() {
        if (isNotSelectedRole()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn vai trò", "Thông báo");
            return;
        }

        TaskUtil.executeSecure(null, PermissionKey.ROLE_UPDATE,
                () -> new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS),
                result -> {
                    RoleModalController modalController = UiUtils.gI().openStageWithController(
                            "/GUI/RoleModal.fxml",
                            controller -> {
                                controller.setRole(selectedRole);
                                controller.setTypeModal(1);
                            },
                            "Sửa chức vụ");

                    if (modalController != null && modalController.isSaved()) {
                        NotificationUtils.showInfoAlert("Sửa chức vụ thành công", "Thông báo");
                        applyFilters();
                    }
                });
    }
}

// package GUI;

// import BUS.AccountBUS;
// import BUS.EmployeeBUS;
// import BUS.RoleBUS;
// import BUS.RolePermissionBUS;
// import DTO.RoleDTO;
// import DTO.RolePermissionDTO;
// import INTERFACE.IController;
// import SERVICE.RolePermissionService;
// import SERVICE.SessionManagerService;
// import UTILS.NotificationUtils;
// import UTILS.UiUtils;
// import UTILS.ValidationUtils;
// import javafx.application.Platform;
// import javafx.beans.property.SimpleStringProperty;
// import javafx.collections.FXCollections;
// import javafx.fxml.FXML;
// import javafx.scene.Node;
// import javafx.scene.control.*;
// import javafx.scene.control.cell.PropertyValueFactory;
// import javafx.scene.layout.HBox;

// public class RoleController implements IController {
// @FXML
// private TableView<RoleDTO> tblRole;
// @FXML
// private TableColumn<RoleDTO, Integer> tlb_col_id;
// @FXML
// private TableColumn<RoleDTO, String> tlb_col_name;
// @FXML
// private TableColumn<RoleDTO, String> tlb_col_description;
// @FXML
// private TableColumn<RoleDTO, String> tlb_col_salaryCoefficient;
// @FXML
// private HBox functionBtns;
// @FXML
// private Button addBtn, editBtn, deleteBtn, refreshBtn, authorizeBtn;
// @FXML
// private TextField txtSearch;
// @FXML
// private ComboBox<String> cbSearchBy;
// private String searchBy = "Mã chức vụ";
// private String keyword = "";
// private RoleDTO selectedRole;
// @FXML
// public void initialize() {
// if (AccountBUS.getInstance().isLocalEmpty())
// AccountBUS.getInstance().loadLocal();
// tblRole.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
// // Tránh deprecated
// Platform.runLater(() -> tblRole.getSelectionModel().clearSelection());
// hideButtonWithoutPermission();
// loadComboBox();
// setupListeners();

// loadTable();
// applyFilters();
// }

// @Override
// public void loadTable() {
// tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
// tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
// tlb_col_description.setCellValueFactory(cellData -> new
// SimpleStringProperty(cellData.getValue().getDescription() == null ? "" :
// cellData.getValue().getDescription()));
// tlb_col_salaryCoefficient.setCellValueFactory(cellData ->
// new
// SimpleStringProperty(ValidationUtils.getInstance().formatCurrency(cellData.getValue().getSalaryCoefficient())));
// tblRole.setItems(FXCollections.observableArrayList(RoleBUS.getInstance().getAllLocal()));
// }

// private void loadComboBox() {
// cbSearchBy.getItems().addAll("Mã chức vụ", "Tên chức vụ");
// cbSearchBy.getSelectionModel().selectFirst();
// }

// @Override
// public void setupListeners() {
// cbSearchBy.setOnAction(event -> handleSearchByChange());
// txtSearch.textProperty().addListener((observable, oldValue, newValue) ->
// handleKeywordChange());
// refreshBtn.setOnAction(event -> {
// resetFilters();
// NotificationUtils.showInfoAlert("Làm mới thành công", "Thông báo");
// });
// authorizeBtn.setOnAction(event -> handleAuthorize());
// addBtn.setOnAction(e -> handleAdd());
// editBtn.setOnAction(e -> handleEdit());
// deleteBtn.setOnAction(e -> handleDelete());
// }

// private void handleSearchByChange() {
// searchBy = cbSearchBy.getValue();
// applyFilters();
// }

// private void handleKeywordChange() {
// keyword = txtSearch.getText().trim();
// applyFilters();
// }

// @Override
// public void applyFilters() {
// tblRole.setItems(FXCollections.observableArrayList(
// RoleBUS.getInstance().filterRoles(searchBy, keyword)
// ));
// tblRole.getSelectionModel().clearSelection();

// }

// @Override
// public void resetFilters() {
// cbSearchBy.getSelectionModel().selectFirst();
// txtSearch.clear();

// searchBy = "Mã chức vụ";
// keyword = "";
// applyFilters();
// }

// @Override
// public void hideButtonWithoutPermission() {
// boolean canAdd = SessionManagerService.getInstance().hasPermission(23);
// boolean canEdit = SessionManagerService.getInstance().hasPermission(25);
// boolean canDelete = SessionManagerService.getInstance().hasPermission(24);

// if (!canAdd) functionBtns.getChildren().remove(addBtn);
// if (!canEdit) functionBtns.getChildren().remove(editBtn);
// if (!canDelete) functionBtns.getChildren().remove(deleteBtn);
// }

// private void handleAuthorize() {
// if (isNotSelectedRole()) {
// NotificationUtils.showErrorAlert("Vui lòng chọn chức vụ", "Thông báo");
// return;
// }
// if (selectedRole.getId() ==
// SessionManagerService.getInstance().employeeRoleId()) {
// NotificationUtils.showErrorAlert("Bạn không thể cập nhật phân quyền của chính
// mình.", "Thông báo");
// return;
// }
// AuthorizeModalController modalController =
// UiUtils.gI().openStageWithController(
// "/GUI/AuthorizeModal.fxml",
// controller -> controller.setRole(selectedRole),
// "Phân quyền"
// );

// if (modalController != null && modalController.isSaved()) {
// NotificationUtils.showInfoAlert("Cập nhật phân quyền thành công", "Thông
// báo");
// resetFilters();
// }
// }

// private void handleDelete() {
// if (isNotSelectedRole()) {
// NotificationUtils.showErrorAlert("Vui lòng chọn chức vụ", "Thông báo");
// return;
// }
// if (selectedRole.getId() ==
// SessionManagerService.getInstance().employeeRoleId()) {
// NotificationUtils.showErrorAlert("Bạn không thể xóa chức vụ của chính mình.",
// "Thông báo");
// return;
// }

// int numEmployeeHasRole =
// EmployeeBUS.getInstance().numEmployeeHasRoleId(selectedRole.getId());
// if (numEmployeeHasRole != 0) {
// String ask = "Hiện có " + numEmployeeHasRole + " nhân viên sở hữu chức vụ
// này!";
// if (!UiUtils.gI().showConfirmAlert("Bạn chắc muốn xóa chức vụ này? " + ask,
// "Thông báo xác nhận")) return;
// }

// int deleteResult =
// RolePermissionService.getInstance().deleteRoleWithPermissions(selectedRole.getId(),SessionManagerService.getInstance().employeeRoleId(),
// SessionManagerService.getInstance().employeeLoginId());

// switch (deleteResult) {
// case 1 ->
// {
// NotificationUtils.showInfoAlert("Xóa chức vụ thành công.", "Thông báo");
// resetFilters();
// }
// case 2 ->
// NotificationUtils.showErrorAlert("Có lỗi khi xóa chức vụ. Vui lòng thử lại.",
// "Thông báo");
// case 3 ->
// NotificationUtils.showErrorAlert("Bạn không thể xóa chức vụ của chính mình.",
// "Thông báo");
// case 4 ->
// NotificationUtils.showErrorAlert("Bạn không có quyền \"Xóa chức vụ\" để thực
// hiện thao tác này.", "Thông báo");
// case 5 ->
// NotificationUtils.showErrorAlert("Bạn không thể xóa chức vụ ngang quyền",
// "Thông báo");
// case 6 ->
// NotificationUtils.showErrorAlert("Xóa chức vụ thất bại. Vui lòng thử lại
// sau.", "Thông báo");
// case 7 ->
// NotificationUtils.showErrorAlert("Chức vụ không hợp lệ hoặc đã bị xóa.",
// "Thông báo");
// default ->
// NotificationUtils.showErrorAlert("Lỗi không xác định, vui lòng thử lại sau.",
// "Thông báo");
// }

// }

// private void handleAdd() {
// RoleModalController modalController = UiUtils.gI().openStageWithController(
// "/GUI/RoleModal.fxml",
// controller -> controller.setTypeModal(0),
// "Thêm chức vụ"
// );
// if (modalController != null && modalController.isSaved()) {
// NotificationUtils.showInfoAlert("Thêm chức vụ thành công", "Thông báo");
// resetFilters();
// }
// }

// private void handleEdit() {
// if (isNotSelectedRole()) {
// NotificationUtils.showErrorAlert("Vui lòng chọn chức vụ", "Thông báo");
// return;
// }
// RoleModalController modalController = UiUtils.gI().openStageWithController(
// "/GUI/RoleModal.fxml",
// controller -> {
// controller.setTypeModal(1);
// controller.setRole(selectedRole);
// },
// "Sửa chức vụ"
// );
// if (modalController != null && modalController.isSaved()) {
// NotificationUtils.showInfoAlert("Sửa chức vụ thành công", "Thông báo");
// applyFilters();
// }
// }

// private boolean isNotSelectedRole() {
// selectedRole = tblRole.getSelectionModel().getSelectedItem();
// return selectedRole == null;
// }
// }
