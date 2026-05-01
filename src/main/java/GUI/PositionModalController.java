package GUI;

import BUS.PositionBUS;
import DTO.BUSResult;
import DTO.PositionDTO;
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

import java.math.BigDecimal;

public class PositionModalController {
    @FXML
    private Label modalName;
    @FXML
    private TextField txtPositionName;
    @FXML
    private TextField txtWage;
    @FXML
    private Button saveBtn, closeBtn;

    @Getter
    private boolean isSaved;
    private int typeModal; // 0: add, 1: edit
    private PositionDTO position;

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
            modalName.setText("Thêm vị trí");
        } else {
            modalName.setText("Sửa vị trí");
            if (position != null) {
                bindToForm();
            }
        }
    }

    public void setPosition(PositionDTO dto) {
        this.position = dto;
        if (typeModal == 1 && dto != null) {
            bindToForm();
        }
    }

    private void bindToForm() {
        txtPositionName.setText(position.getName());
        BigDecimal wage = position.getWage();
        txtWage.setText(wage != null ? wage.toPlainString() : "");
    }

    private boolean isValidInput() {
        String name = txtPositionName.getText() != null ? txtPositionName.getText().trim() : "";
        String wageText = txtWage.getText() != null ? txtWage.getText().trim() : "";
        ValidationUtils v = ValidationUtils.getInstance();

        if (name.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên vị trí không được để trống.", AppMessages.DIALOG_TITLE);
            txtPositionName.requestFocus();
            return false;
        }
        if (!v.validateVietnameseText100(name)) {
            NotificationUtils.showErrorAlert("Tên vị trí không hợp lệ.", AppMessages.DIALOG_TITLE);
            txtPositionName.requestFocus();
            return false;
        }
        if (wageText.isEmpty()) {
            NotificationUtils.showErrorAlert("Lương cơ bản không được để trống.", AppMessages.DIALOG_TITLE);
            txtWage.requestFocus();
            return false;
        }
        try {
            BigDecimal wage = new BigDecimal(wageText);
            if (!v.validateBigDecimal(wage, 15, 2, false) || wage.compareTo(BigDecimal.ZERO) <= 0) {
                NotificationUtils.showErrorAlert("Lương cơ bản không hợp lệ.", AppMessages.DIALOG_TITLE);
                txtWage.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationUtils.showErrorAlert("Lương cơ bản phải là số.", AppMessages.DIALOG_TITLE);
            txtWage.requestFocus();
            return false;
        }
        return true;
    }

    private void handleSave() {
        if (!isValidInput()) {
            return;
        }

        PositionBUS bus = PositionBUS.getInstance();
        BUSResult result;
        BigDecimal wage = new BigDecimal(txtWage.getText().trim());

        if (typeModal == 0) {
            PositionDTO dto = new PositionDTO();
            dto.setName(txtPositionName.getText().trim());
            dto.setWage(wage);
            result = bus.insert(dto);
        } else {
            position.setName(txtPositionName.getText().trim());
            position.setWage(wage);
            result = bus.update(position);
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
