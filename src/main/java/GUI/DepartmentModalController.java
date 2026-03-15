package GUI;

import BUS.DepartmentBUS;
import DTO.BUSResult;
import DTO.DepartmentDTO;
import ENUM.BUSOperationResult;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;

public class DepartmentModalController {
    @FXML
    private Label modalName;
    @FXML
    private TextField txtDepartmentName;
    @FXML
    private TextField txtDescription;
    @FXML
    private Button saveBtn, closeBtn;

    @Getter
    private boolean isSaved;
    private int typeModal; // 0: add, 1: edit
    private DepartmentDTO department;

    @FXML
    public void initialize() {
        saveBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
    }

    public void setTypeModal(int type) {
        if (type != 0 && type != 1) {
            handleClose();
            return;
        }
        this.typeModal = type;
        if (type == 0) {
            modalName.setText("Thêm phòng ban");
        } else {
            modalName.setText("Sửa phòng ban");
            if (department != null) {
                bindToForm();
            }
        }
    }

    public void setDepartment(DepartmentDTO dto) {
        this.department = dto;
        if (typeModal == 1 && dto != null) {
            bindToForm();
        }
    }

    private void bindToForm() {
        txtDepartmentName.setText(department.getName());
        txtDescription.setText(department.getDescription() == null ? "" : department.getDescription());
    }

    private boolean isValidInput() {
        String name = txtDepartmentName.getText() != null ? txtDepartmentName.getText().trim() : "";
        String description = txtDescription.getText() != null ? txtDescription.getText().trim() : "";
        ValidationUtils v = ValidationUtils.getInstance();

        if (name.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên phòng ban không được để trống.", AppMessages.DIALOG_TITLE);
            txtDepartmentName.requestFocus();
            return false;
        }
        if (!v.validateVietnameseText100(name)) {
            NotificationUtils.showErrorAlert("Tên phòng ban không hợp lệ.", AppMessages.DIALOG_TITLE);
            txtDepartmentName.requestFocus();
            return false;
        }
        if (!description.isEmpty() && !v.validateVietnameseText255(description)) {
            NotificationUtils.showErrorAlert("Mô tả phòng ban không hợp lệ.", AppMessages.DIALOG_TITLE);
            txtDescription.requestFocus();
            return false;
        }
        return true;
    }

    private void handleSave() {
        if (!isValidInput()) {
            return;
        }
        DepartmentBUS bus = DepartmentBUS.getInstance();
        BUSResult result;
        if (typeModal == 0) {
            DepartmentDTO dto = new DepartmentDTO();
            dto.setName(txtDepartmentName.getText().trim());
            dto.setDescription(txtDescription.getText().trim());
            result = bus.insert(dto);
        } else {
            department.setName(txtDepartmentName.getText().trim());
            department.setDescription(txtDescription.getText().trim());
            result = bus.update(department);
        }

        if (result.getCode() == BUSOperationResult.SUCCESS) {
            isSaved = true;
            handleClose();
        } else {
            String message = result.getMessage() != null ? result.getMessage() : AppMessages.UNKNOWN_ERROR;
            NotificationUtils.showErrorAlert(message, AppMessages.DIALOG_TITLE);
        }
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }
}

