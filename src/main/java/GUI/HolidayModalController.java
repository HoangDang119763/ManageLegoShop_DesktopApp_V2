package GUI;

import BUS.HolidayBUS;
import DTO.HolidayDTO;
import ENUM.PermissionKey;
import INTERFACE.IModalController;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.ValidationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.time.LocalDate;

public class HolidayModalController implements IModalController {

    // FXML Controls
    @FXML
    public Label modalName;
    @FXML
    public TextField txtHolidayId;
    @FXML
    public TextField txtHolidayName;
    @FXML
    public DatePicker dpHolidayDate;
    @FXML
    public Button saveBtn;
    @FXML
    public Button closeBtn;
    @FXML
    public StackPane loadingOverlay;

    // State variables
    @Getter
    private boolean isSaved;
    @Getter
    private String resultMessage;
    private int typeModal; // 0: Add, 1: Edit
    private HolidayDTO holiday;
    private HolidayBUS holidayBus;
    private ValidationUtils validator;

    @FXML
    public void initialize() {
        holidayBus = HolidayBUS.getInstance();
        validator = ValidationUtils.getInstance();

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
            modalName.setText("Thêm ngày lễ mới");
            txtHolidayId.setText(String.valueOf(holidayBus.nextId()));
        } else {
            if (holiday == null) {
                handleClose();
            }
            modalName.setText("Sửa ngày lễ");
        }
    }

    public void setHoliday(int holidayId) {
        // Fetch full HolidayDTO from BUS bằng ID
        if (holidayId <= 0) {
            handleClose();
            return;
        }

        this.holiday = holidayBus.getById(holidayId);

        if (this.holiday == null) {
            NotificationUtils.showErrorAlert(AppMessages.NOT_FOUND, AppMessages.DIALOG_TITLE);
            handleClose();
            return;
        }

        // Fill form fields
        txtHolidayId.setText(String.valueOf(holiday.getId()));
        txtHolidayName.setText(holiday.getName());
        if (holiday.getDate() != null) {
            dpHolidayDate.setValue(holiday.getDate());
        }
    }

    private boolean isValidInput() {
        String name = txtHolidayName.getText().trim();
        LocalDate date = dpHolidayDate.getValue();

        // 1. Kiểm tra trống tên
        if (name.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên ngày lễ không được để trống.", AppMessages.DIALOG_TITLE);
            clearAndFocus(txtHolidayName);
            return false;
        }

        // 2. Kiểm tra độ dài tên
        if (name.length() > 255) {
            NotificationUtils.showErrorAlert("Tên ngày lễ không được vượt quá 255 ký tự.", AppMessages.DIALOG_TITLE);
            clearAndFocus(txtHolidayName);
            return false;
        }

        // 3. Kiểm tra ngày
        if (date == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn ngày.", AppMessages.DIALOG_TITLE);
            dpHolidayDate.requestFocus();
            return false;
        }

        return true;
    }

    private void handleSave() {
        if (typeModal == 0) {
            insertHoliday();
        } else {
            updateHoliday();
        }
    }

    private void insertHoliday() {
        if (!isValidInput()) {
            return;
        }

        // Chuẩn hóa dữ liệu UI
        String name = txtHolidayName.getText().trim();
        LocalDate date = dpHolidayDate.getValue();

        HolidayDTO temp = new HolidayDTO(-1, name, date);

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.HOLIDAY_INSERT,
                () -> holidayBus.insert(temp),
                result -> {
                    if (result.isSuccess()) {
                        isSaved = true;
                        resultMessage = result.getMessage();
                        handleClose();
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    private void updateHoliday() {
        if (!isValidInput()) {
            return;
        }

        String name = txtHolidayName.getText().trim();
        LocalDate date = dpHolidayDate.getValue();

        // Giữ nguyên ID cũ để Update
        HolidayDTO temp = new HolidayDTO(holiday.getId(), name, date);

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.HOLIDAY_UPDATE,
                () -> holidayBus.update(temp),
                result -> {
                    if (result.isSuccess()) {
                        isSaved = true;
                        resultMessage = result.getMessage();
                        handleClose();
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            ((Stage) closeBtn.getScene().getWindow()).close();
        }
    }

    private void clearAndFocus(TextField field) {
        field.requestFocus();
        field.selectAll();
    }
}
