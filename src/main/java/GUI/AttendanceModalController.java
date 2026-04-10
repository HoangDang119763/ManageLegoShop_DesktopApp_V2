package GUI;

import BUS.TimeSheetBUS;
import DTO.TimeSheetDTO;
import ENUM.BUSOperationResult;
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
        // 1. Validation dữ liệu đầu vào (Giữ nguyên logic của bạn)
        if (dpAttendanceDate.getValue() == null) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn ngày chấm công");
            return;
        }

        LocalDateTime checkInDateTime = null;
        LocalDateTime checkOutDateTime = null;

        try {
            if (txtCheckIn.getText() != null && !txtCheckIn.getText().isEmpty()) {
                LocalTime time = LocalTime.parse(txtCheckIn.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                checkInDateTime = LocalDateTime.of(dpAttendanceDate.getValue(), time);
            }
            if (txtCheckOut.getText() != null && !txtCheckOut.getText().isEmpty()) {
                LocalTime time = LocalTime.parse(txtCheckOut.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                checkOutDateTime = LocalDateTime.of(dpAttendanceDate.getValue(), time);
            }
        } catch (Exception e) {
            NotificationUtils.showErrorAlert("Lỗi", "Định dạng giờ không hợp lệ (HH:mm)");
            return;
        }

        BigDecimal overtimeHours = BigDecimal.ZERO;
        if (txtOvertimeHours.getText() != null && !txtOvertimeHours.getText().isEmpty()) {
            try {
                overtimeHours = new BigDecimal(txtOvertimeHours.getText());
            } catch (NumberFormatException e) {
                NotificationUtils.showErrorAlert("Lỗi", "Giờ tăng ca không hợp lệ");
                return;
            }
        }

        // 2. Tạo DTO
        TimeSheetDTO timeSheet = new TimeSheetDTO();
        timeSheet.setEmployeeId(employeeId);
        timeSheet.setCheckIn(checkInDateTime);
        timeSheet.setCheckOut(checkOutDateTime);
        timeSheet.setOtHours(overtimeHours);

        // 3. Thực thi lưu dữ liệu (Sử dụng BUSOperationResult)
        new Thread(() -> {
            try {
                // GỌI BUS: Trả về Enum thay vì boolean
                BUSOperationResult result = timeSheetBUS.insert(timeSheet);

                Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        NotificationUtils.showInfoAlert("Thành công", "Thêm chấm công thành công");
                        if (parentController != null) {
                            parentController.loadEmployeeAttendance(employeeId);
                        }
                        closeModal();
                    } else {
                        // Xử lý thông báo lỗi chi tiết dựa trên Enum trả về
                        handleInsertError(result);
                    }
                });
            } catch (Exception e) {
                log.error("Error saving attendance", e);
                Platform.runLater(() -> NotificationUtils.showErrorAlert("Lỗi hệ thống", e.getMessage()));
            }
        }).start();
    }

    // Hàm phụ trợ để hiển thị lỗi chi tiết
    private void handleInsertError(BUSOperationResult result) {
        String message = switch (result) {
            case INVALID_DATA -> "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại giờ vào/ra.";
            case UNAUTHORIZED -> "Bạn không có quyền thực hiện thao tác này.";
            case DB_ERROR -> "Lỗi kết nối cơ sở dữ liệu.";
            default -> "Không thể thêm chấm công. Vui lòng thử lại.";
        };
        NotificationUtils.showErrorAlert("Thất bại", message);
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
