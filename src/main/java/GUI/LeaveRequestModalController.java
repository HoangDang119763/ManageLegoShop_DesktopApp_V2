package GUI;

import BUS.LeaveRequestBUS;
import BUS.LeaveTypeBUS;
import DTO.LeaveRequestDTO;
import DTO.LeaveTypeDTO;
import ENUM.BUSOperationResult; // Import Enum mới
import UTILS.NotificationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class LeaveRequestModalController {
    private static final Logger log = LoggerFactory.getLogger(LeaveRequestModalController.class);

    @FXML private ComboBox<LeaveTypeDTO> cbLeaveType;
    @FXML private DatePicker dpFromDate;
    @FXML private DatePicker dpToDate;
    @FXML private TextArea taReason;
    @FXML private Button btnSave, btnCancel;

    private LeaveRequestBUS leaveRequestBUS;
    private LeaveTypeBUS leaveTypeBUS;
    private int employeeId;
    private LeaveRequestTabNestedController parentController;

    @FXML
    public void initialize() {
        leaveRequestBUS = LeaveRequestBUS.getInstance();
        leaveTypeBUS = LeaveTypeBUS.getInstance();

        setupButtons();
        loadLeaveTypes();
    }

    private void setupButtons() {
        btnSave.setOnAction(event -> saveLeaveRequest());
        btnCancel.setOnAction(event -> closeModal());
    }

    private void loadLeaveTypes() {
        new Thread(() -> {
            try {
                ArrayList<LeaveTypeDTO> types = leaveTypeBUS.getAll();
                Platform.runLater(() -> {
                    cbLeaveType.setItems(FXCollections.observableArrayList(types));
                    cbLeaveType.setCellFactory(param -> new LeaveTypeCell());
                    cbLeaveType.setButtonCell(new LeaveTypeCell());
                });
            } catch (Exception e) {
                log.error("Error loading leave types", e);
            }
        }).start();
    }

    private void saveLeaveRequest() {
        // Validation cơ bản tại UI
        if (cbLeaveType.getValue() == null) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn loại nghỉ phép");
            return;
        }
        if (dpFromDate.getValue() == null || dpToDate.getValue() == null) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc");
            return;
        }

        // Tạo DTO - Sử dụng ID trạng thái 20 (Pending) theo DB của bạn
        LeaveRequestDTO leaveRequest = new LeaveRequestDTO();
        leaveRequest.setLeaveTypeId(cbLeaveType.getValue().getId());
        leaveRequest.setLeaveTypeName(cbLeaveType.getValue().getName());
        leaveRequest.setContent(taReason.getText());
        leaveRequest.setStartDate(dpFromDate.getValue());
        leaveRequest.setEndDate(dpToDate.getValue());
        leaveRequest.setStatusId(20); 
        leaveRequest.setEmployeeId(employeeId);

        new Thread(() -> {
            try {
                // VỊ TRÍ SỬA: Gọi hàm insert trả về BUSOperationResult
                BUSOperationResult result = leaveRequestBUS.insert(leaveRequest);
                
                Platform.runLater(() -> {
                    if (result.isSuccess()) {
                        NotificationUtils.showInfoAlert("Thành công", "Gửi đơn nghỉ phép thành công");
                        if (parentController != null) {
                            parentController.loadEmployeeLeaves(employeeId);
                        }
                        closeModal();
                    } else {
                        handleErrorResult(result);
                    }
                });
            } catch (Exception e) {
                log.error("Error saving leave request", e);
                Platform.runLater(() -> NotificationUtils.showErrorAlert("Lỗi", "Lỗi hệ thống: " + e.getMessage()));
            }
        }).start();
    }

    // VỊ TRÍ THÊM: Xử lý lỗi chi tiết dựa trên Enum
    private void handleErrorResult(BUSOperationResult result) {
        switch (result) {
            case INVALID_DATA -> NotificationUtils.showErrorAlert("Lỗi dữ liệu", "Ngày nghỉ không hợp lệ hoặc lý do quá dài.");
            case DB_ERROR -> NotificationUtils.showErrorAlert("Lỗi DB", "Không thể lưu đơn vào cơ sở dữ liệu.");
            case UNAUTHORIZED -> NotificationUtils.showErrorAlert("Từ chối", "Bạn không có quyền thực hiện thao tác này.");
            default -> NotificationUtils.showErrorAlert("Thất bại", "Gửi đơn không thành công.");
        }
    }

    private void closeModal() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    public void setParentController(LeaveRequestTabNestedController parentController) { this.parentController = parentController; }

    private static class LeaveTypeCell extends ListCell<LeaveTypeDTO> {
        @Override
        protected void updateItem(LeaveTypeDTO item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? "" : item.getName());
        }
    }
}