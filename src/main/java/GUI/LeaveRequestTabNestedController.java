package GUI;

import BUS.LeaveRequestBUS;
import BUS.EmployeeBUS;
import DTO.LeaveRequestDTO;
import DTO.EmployeeDTO;
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Nested controller for Leave Request within HROperationsTab
 * Displays leave requests for a selected employee
 */
@Slf4j
public class LeaveRequestTabNestedController {

    @FXML
    private VBox containerLeaveRequest;

    @FXML
    private TableView<LeaveRequestDTO> tblLeaveRequest;

    @FXML
    private TableColumn<LeaveRequestDTO, String> colId;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colLeaveType;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colFromDate;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colToDate;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colReason;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colStatus;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colAction;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;

    // BUS instances
    private LeaveRequestBUS leaveRequestBUS;
    private EmployeeBUS employeeBUS;
    private ValidationUtils validationUtils;

    private int currentEmployeeId = -1;
    private ObservableList<LeaveRequestDTO> leaveRequestList;

    /**
     * Initialize Leave Request nested controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing LeaveRequestTabNestedController");
        leaveRequestBUS = LeaveRequestBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        validationUtils = ValidationUtils.getInstance();
        leaveRequestList = FXCollections.observableArrayList();

        setupTable();
        setupButtons();
    }

    /**
     * Setup table columns
     */
    private void setupTable() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getId())));
        colLeaveType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getLeaveTypeName() != null && !cellData.getValue().getLeaveTypeName().isEmpty() ?
                        cellData.getValue().getLeaveTypeName() : ""));
        colFromDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStartDate() != null ?
                        cellData.getValue().getStartDate().toString() : ""));
        colToDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getEndDate() != null ?
                        cellData.getValue().getEndDate().toString() : ""));
        colReason.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getContent() != null ? cellData.getValue().getContent() : ""));
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getStatusId())));

        // Action column
        colAction.setCellFactory(param -> createActionCell());

        tblLeaveRequest.setItems(leaveRequestList);
    }

    /**
     * Create action cell with Edit/Delete buttons
     */
    private TableCell<LeaveRequestDTO, String> createActionCell() {
        return new TableCell<LeaveRequestDTO, String>() {
            private final Button btnView = new Button("Xem");
            private final Button btnDel = new Button("Xóa");

            {
                btnView.setStyle("-fx-padding: 5; -fx-font-size: 11;");
                btnDel.setStyle("-fx-padding: 5; -fx-font-size: 11;");

                btnView.setOnAction(event -> {
                    LeaveRequestDTO leave = getTableView().getItems().get(getIndex());
                    showLeaveDetails(leave);
                });

                btnDel.setOnAction(event -> {
                    LeaveRequestDTO leave = getTableView().getItems().get(getIndex());
                    deleteLeave(leave);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new javafx.scene.layout.HBox(5, btnView, btnDel));
            }
        };
    }

    /**
     * Setup button actions
     */
    private void setupButtons() {
        btnAdd.setOnAction(event -> addNewLeaveRequest());
        btnEdit.setOnAction(event -> editSelectedLeave());
        btnDelete.setOnAction(event -> deleteSelectedLeave());
    }

    /**
     * Load leave requests for specified employee
     */
    public void loadEmployeeLeaves(int employeeId) {
        currentEmployeeId = employeeId;
        log.info("Loading leave requests for employee: {}", employeeId);

        new Thread(() -> {
            try {
                List<LeaveRequestDTO> leaves = leaveRequestBUS.getByEmployeeId(employeeId);
                Platform.runLater(() -> {
                    if (leaves != null && !leaves.isEmpty()) {
                        leaveRequestList.setAll(leaves);
                    } else {
                        leaveRequestList.clear();
                    }
                });
            } catch (Exception e) {
                log.error("Error loading leave requests", e);
                Platform.runLater(() -> {
                    NotificationUtils.showErrorAlert("Lỗi tải đơn nghỉ phép",
                            "Chi tiết: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Show leave request details
     */
    private void showLeaveDetails(LeaveRequestDTO leave) {
        if (leave == null) return;

        String details = String.format("ID: %d\nNgày bắt đầu: %s\nNgày kết thúc: %s\nLý do: %s\nTrạng thái: %d",
                leave.getId(),
                leave.getStartDate() != null ? leave.getStartDate().toString() : "N/A",
                leave.getEndDate() != null ? leave.getEndDate().toString() : "N/A",
                leave.getContent() != null ? leave.getContent() : "Không có",
                leave.getStatusId());

        NotificationUtils.showInfoAlert("Chi tiết đơn nghỉ phép", details);
    }

    /**
     * Delete leave request
     */
    private void deleteLeave(LeaveRequestDTO leave) {
        if (leave == null) return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa đơn nghỉ phép");
        confirmDialog.setContentText("Bạn có chắc muốn xóa đơn nghỉ phép này?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    if (leaveRequestBUS.delete(leave.getId()) > 0) {
                        Platform.runLater(() -> {
                            NotificationUtils.showInfoAlert("Thành công", "Xóa đơn nghỉ phép thành công");
                            loadEmployeeLeaves(currentEmployeeId);
                        });
                    } else {
                        Platform.runLater(() -> {
                            NotificationUtils.showErrorAlert("Thất bại", "Không thể xóa đơn nghỉ phép");
                        });
                    }
                } catch (Exception e) {
                    log.error("Error deleting leave request", e);
                    Platform.runLater(() -> {
                        NotificationUtils.showErrorAlert("Lỗi", "Chi tiết: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void deleteSelectedLeave() {
        LeaveRequestDTO selected = tblLeaveRequest.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteLeave(selected);
        } else {
            NotificationUtils.showInfoAlert("Cảnh báo", "Vui lòng chọn một đơn nghỉ phép");
        }
    }

    private void addNewLeaveRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/LeaveRequestModal.fxml"));
            VBox modalRoot = loader.load();
            LeaveRequestModalController modalController = loader.getController();
            modalController.setEmployeeId(currentEmployeeId);
            modalController.setParentController(this);

            Stage modalStage = new Stage();
            modalStage.setTitle("Thêm Đơn Nghỉ Phép");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();
        } catch (IOException e) {
            log.error("Error opening leave request modal", e);
            NotificationUtils.showErrorAlert("Lỗi", "Không thể mở form thêm đơn nghỉ phép");
        }
    }

    private void editSelectedLeave() {
        LeaveRequestDTO selected = tblLeaveRequest.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showInfoAlert("Cảnh báo", "Vui lòng chọn một đơn nghỉ phép");
        } else {
            NotificationUtils.showInfoAlert("Thông báo", "Chức năng chỉnh sửa đơn nghỉ phép sẽ được thêm vào");
        }
    }
}
