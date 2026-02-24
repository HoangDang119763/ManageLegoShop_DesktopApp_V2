package GUI;

import BUS.LeaveRequestBUS;
import BUS.LeaveTypeBUS;
import DTO.LeaveRequestDTO;
import DTO.LeaveTypeDTO;
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

    @FXML
    private ComboBox<LeaveTypeDTO> cbLeaveType;

    @FXML
    private DatePicker dpFromDate;

    @FXML
    private DatePicker dpToDate;

    @FXML
    private TextArea taReason;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private LeaveRequestBUS leaveRequestBUS;
    private LeaveTypeBUS leaveTypeBUS;
    private int employeeId;
    private LeaveRequestTabNestedController parentController;

    @FXML
    public void initialize() {
        log.info("Initializing LeaveRequestModalController");
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
                    ObservableList<LeaveTypeDTO> observableList = FXCollections.observableArrayList(types);
                    cbLeaveType.setItems(observableList);
                    cbLeaveType.setCellFactory(param -> new LeaveTypeCell());
                    cbLeaveType.setButtonCell(new LeaveTypeCell());
                });
            } catch (Exception e) {
                log.error("Error loading leave types", e);
            }
        }).start();
    }

    private void saveLeaveRequest() {
        // Validation
        if (cbLeaveType.getValue() == null) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn loại nghỉ phép");
            return;
        }

        if (dpFromDate.getValue() == null || dpToDate.getValue() == null) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn ngày bắt đầu và kết thúc");
            return;
        }

        if (dpFromDate.getValue().isAfter(dpToDate.getValue())) {
            NotificationUtils.showErrorAlert("Lỗi", "Ngày bắt đầu không được sau ngày kết thúc");
            return;
        }

        // Create DTO
        LeaveRequestDTO leaveRequest = new LeaveRequestDTO(
                0,
                cbLeaveType.getValue().getId(),
                cbLeaveType.getValue().getName(),
                taReason.getText() != null ? taReason.getText() : "",
                dpFromDate.getValue(),
                dpToDate.getValue(),
                1, // Status pending
                employeeId);

        // Save to database
        new Thread(() -> {
            try {
                int result = leaveRequestBUS.insert(leaveRequest, 1, 1);
                Platform.runLater(() -> {
                    if (result == 1) {
                        NotificationUtils.showInfoAlert("Thành công", "Thêm đơn nghỉ phép thành công");
                        if (parentController != null) {
                            parentController.loadEmployeeLeaves(employeeId);
                        }
                        closeModal();
                    } else {
                        NotificationUtils.showErrorAlert("Thất bại", "Không thể thêm đơn nghỉ phép");
                    }
                });
            } catch (Exception e) {
                log.error("Error saving leave request", e);
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

    public void setParentController(LeaveRequestTabNestedController parentController) {
        this.parentController = parentController;
    }

    /**
     * Custom cell for displaying LeaveTypeDTO
     */
    private static class LeaveTypeCell extends ListCell<LeaveTypeDTO> {
        @Override
        protected void updateItem(LeaveTypeDTO item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty || item == null ? "" : item.getName());
        }
    }
}
