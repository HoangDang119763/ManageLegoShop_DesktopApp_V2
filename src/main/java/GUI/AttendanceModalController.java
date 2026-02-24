package GUI;

import BUS.TimeSheetBUS;
import DTO.TimeSheetDTO;
import UTILS.NotificationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AttendanceModalController {
    private static final Logger log = LoggerFactory.getLogger(AttendanceModalController.class);

    @FXML
    private DatePicker dpAttendanceDate;

    @FXML
    private TextField txtCheckIn;

    @FXML
    private TextField txtCheckOut;

    @FXML
    private TextField txtOvertimeHours;

    @FXML
    private TextArea taNotes;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private TimeSheetBUS timeSheetBUS;
    private int employeeId;
    private AttendanceTabNestedController parentController;

    @FXML
    public void initialize() {
        log.info("Initializing AttendanceModalController");
        timeSheetBUS = TimeSheetBUS.getInstance();

        setupButtons();
        dpAttendanceDate.setValue(LocalDate.now());
    }

    private void setupButtons() {
        btnSave.setOnAction(event -> saveAttendance());
        btnCancel.setOnAction(event -> closeModal());
    }

    private void saveAttendance() {
        // Validation
        if (dpAttendanceDate.getValue() == null) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn ngày chấm công");
            return;
        }

        LocalDateTime checkInDateTime = null;
        LocalDateTime checkOutDateTime = null;

        // Parse check-in time
        if (txtCheckIn.getText() != null && !txtCheckIn.getText().isEmpty()) {
            try {
                LocalTime checkInTime = LocalTime.parse(txtCheckIn.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                checkInDateTime = LocalDateTime.of(dpAttendanceDate.getValue(), checkInTime);
            } catch (Exception e) {
                NotificationUtils.showErrorAlert("Lỗi", "Giờ vào không hợp lệ (HH:mm)");
                return;
            }
        }

        // Parse check-out time
        if (txtCheckOut.getText() != null && !txtCheckOut.getText().isEmpty()) {
            try {
                LocalTime checkOutTime = LocalTime.parse(txtCheckOut.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                checkOutDateTime = LocalDateTime.of(dpAttendanceDate.getValue(), checkOutTime);
            } catch (Exception e) {
                NotificationUtils.showErrorAlert("Lỗi", "Giờ ra không hợp lệ (HH:mm)");
                return;
            }
        }

        // Parse overtime hours
        BigDecimal overtimeHours = BigDecimal.ZERO;
        if (txtOvertimeHours.getText() != null && !txtOvertimeHours.getText().isEmpty()) {
            try {
                overtimeHours = new BigDecimal(txtOvertimeHours.getText());
            } catch (NumberFormatException e) {
                NotificationUtils.showErrorAlert("Lỗi", "Giờ tăng ca không hợp lệ");
                return;
            }
        }

        // Create DTO using TimeSheetDTO
        TimeSheetDTO timeSheet = new TimeSheetDTO();
        timeSheet.setEmployeeId(employeeId);
        timeSheet.setCheckIn(checkInDateTime);
        timeSheet.setCheckOut(checkOutDateTime);
        timeSheet.setOtHours(overtimeHours);

        // Save to database
        new Thread(() -> {
            try {
                if (timeSheetBUS.insert(timeSheet, 1, 1)) {
                    Platform.runLater(() -> {
                        NotificationUtils.showInfoAlert("Thành công", "Thêm chấm công thành công");
                        if (parentController != null) {
                            parentController.loadEmployeeAttendance(employeeId);
                        }
                        closeModal();
                    });
                } else {
                    Platform.runLater(() -> {
                        NotificationUtils.showErrorAlert("Thất bại", "Không thể thêm chấm công");
                    });
                }
            } catch (Exception e) {
                log.error("Error saving attendance", e);
                Platform.runLater(() -> {
                    NotificationUtils.showErrorAlert("Lỗi", "Chi tiết: " + e.getMessage());
                });
            }
        }).start();
    }

    private void closeModal() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setParentController(AttendanceTabNestedController parentController) {
        this.parentController = parentController;
    }
}
