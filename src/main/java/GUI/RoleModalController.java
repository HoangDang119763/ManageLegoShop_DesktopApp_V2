package GUI;

import BUS.RoleBUS;
import DTO.BUSResult;
import DTO.RoleDTO;
import ENUM.BUSOperationResult;
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;

public class RoleModalController {
    @FXML
    private Label modalName;
    @FXML
    private TextField txtRoleName;
    @FXML
    private TextField txtDescription;
    @FXML
    private Button saveBtn, closeBtn;
    @Getter
    private boolean isSaved;
    private int typeModal; // 0: add, 1: edit
    private RoleDTO role;

    @FXML
    public void initialize() {
        setupListeners();
    }

    private void setupListeners() {
        saveBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
    }

    public void setTypeModal(int type) {
        if (type != 0 && type != 1) {
            handleClose();
            return;
        }
        typeModal = type;
        if (typeModal == 0) {
            modalName.setText("Thêm chức vụ");
        } else {
            modalName.setText("Sửa chức vụ");
            if (role != null) {
                bindRoleToForm();
            }
        }
    }

    public void setRole(RoleDTO role) {
        this.role = role;
        if (typeModal == 1) {
            bindRoleToForm();
        }
    }

    private void bindRoleToForm() {
        txtRoleName.setText(role.getName());
        txtDescription.setText(role.getDescription() == null ? "" : role.getDescription());
    }

    private boolean isValidInput() {
        boolean isValid = true;
        String roleName = txtRoleName.getText().trim();
        String description = txtDescription.getText().trim();

        ValidationUtils validator = ValidationUtils.getInstance();

        if (roleName.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên chức vụ không được để trống.", "Thông báo");
            txtRoleName.requestFocus();
            isValid = false;
        } else if (!validator.validateVietnameseText50(roleName)) {
            NotificationUtils.showErrorAlert(
                    "Tên chức vụ không hợp lệ (tối đa 50 ký tự, không chứa ký tự đặc biệt).",
                    "Thông báo");
            txtRoleName.requestFocus();
            isValid = false;
        }

        if (isValid && !description.isEmpty() && !validator.validateVietnameseText255(description)) {
            NotificationUtils.showErrorAlert(
                    "Mô tả không hợp lệ (tối đa 255 ký tự, không chứa ký tự đặc biệt).",
                    "Thông báo");
            txtDescription.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void handleSave() {
        if (!isValidInput()) {
            return;
        }

        RoleBUS roleBUS = RoleBUS.getInstance();
        BUSResult result;

        if (typeModal == 0) {
            RoleDTO toInsert = new RoleDTO(0, txtRoleName.getText().trim(),
                    txtDescription.getText().trim(), null, null);
            result = roleBUS.insert(toInsert);
        } else {
            RoleDTO toUpdate = new RoleDTO(role.getId(), txtRoleName.getText().trim(),
                    txtDescription.getText().trim(), role.getCreatedAt(), role.getUpdatedAt());
            result = roleBUS.update(toUpdate);
        }

        if (result.getCode() == BUSOperationResult.SUCCESS) {
            isSaved = true;
            handleClose();
        } else {
            NotificationUtils.showErrorAlert(result.getMessage(), "Thông báo");
        }
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }
}

