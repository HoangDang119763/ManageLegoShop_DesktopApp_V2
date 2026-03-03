package GUI;

import BUS.LeaveRequestBUS;
import BUS.EmployeeBUS;
import DTO.LeaveRequestDTO;
import ENUM.BUSOperationResult; // Import Enum mới
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
import java.util.List;

@Slf4j
public class LeaveRequestTabNestedController {

    @FXML private VBox containerLeaveRequest;
    @FXML private TableView<LeaveRequestDTO> tblLeaveRequest;
    @FXML private TableColumn<LeaveRequestDTO, String> colId, colLeaveType, colFromDate, colToDate, colReason, colStatus, colAction;
    @FXML private Button btnAdd, btnEdit, btnDelete;

    private LeaveRequestBUS leaveRequestBUS;
    private EmployeeBUS employeeBUS;
    private int currentEmployeeId = -1;
    private ObservableList<LeaveRequestDTO> leaveRequestList;

    @FXML
    public void initialize() {
        log.info("Initializing LeaveRequestTabNestedController");
        leaveRequestBUS = LeaveRequestBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        leaveRequestList = FXCollections.observableArrayList();

        setupTable();
        setupButtons();
    }

    private void setupTable() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        colLeaveType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLeaveTypeName()));
        colFromDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStartDate() != null ? cellData.getValue().getStartDate().toString() : ""));
        colToDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEndDate() != null ? cellData.getValue().getEndDate().toString() : ""));
        colReason.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getContent()));
        
        // VỊ TRÍ SỬA 1: Sử dụng statusName thay vì ID để hiển thị thân thiện hơn
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusName()));

        colAction.setCellFactory(param -> createActionCell());
        tblLeaveRequest.setItems(leaveRequestList);
    }

    private TableCell<LeaveRequestDTO, String> createActionCell() {
        return new TableCell<LeaveRequestDTO, String>() {
            private final Button btnView = new Button("Xem");
            private final Button btnDel = new Button("Xóa");

            {
                btnView.setStyle("-fx-padding: 5; -fx-font-size: 11;");
                btnDel.setStyle("-fx-padding: 5; -fx-font-size: 11;");

                btnView.setOnAction(event -> showLeaveDetails(getTableView().getItems().get(getIndex())));
                btnDel.setOnAction(event -> deleteLeave(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    // VỊ TRÍ SỬA 2: Chỉ cho phép xóa nếu đơn ở trạng thái Chờ duyệt (20)
                    LeaveRequestDTO leave = getTableView().getItems().get(getIndex());
                    btnDel.setDisable(leave.getStatusId() != 20);
                    setGraphic(new javafx.scene.layout.HBox(5, btnView, btnDel));
                }
            }
        };
    }

    private void setupButtons() {
        btnAdd.setOnAction(event -> addNewLeaveRequest());
        btnEdit.setOnAction(event -> editSelectedLeave());
        btnDelete.setOnAction(event -> deleteSelectedLeave());
    }

    public void loadEmployeeLeaves(int employeeId) {
        this.currentEmployeeId = employeeId;
        new Thread(() -> {
            try {
                // VỊ TRÍ SỬA 3: BUS hiện tại trả về ArrayList<LeaveRequestDTO>
                List<LeaveRequestDTO> leaves = leaveRequestBUS.getByEmployeeId(employeeId);
                Platform.runLater(() -> leaveRequestList.setAll(leaves));
            } catch (Exception e) {
                log.error("Error loading leave requests", e);
                Platform.runLater(() -> NotificationUtils.showErrorAlert("Lỗi", "Không thể tải danh sách đơn nghỉ."));
            }
        }).start();
    }

    private void deleteLeave(LeaveRequestDTO leave) {
        if (leave == null) return;

        // VỊ TRÍ SỬA 4: Sử dụng NotificationUtils.showConfirmAlert đã viết ở bước trước
        if (NotificationUtils.showConfirmAlert("Bạn có chắc muốn xóa đơn nghỉ phép ID: " + leave.getId() + "?")) {
            new Thread(() -> {
                try {
                    // Gọi BUS trả về BUSOperationResult
                    BUSOperationResult result = leaveRequestBUS.delete(leave.getId());
                    
                    Platform.runLater(() -> {
                        if (result.isSuccess()) {
                            NotificationUtils.showInfoAlert("Thành công", "Xóa đơn nghỉ phép thành công");
                            loadEmployeeLeaves(currentEmployeeId);
                        } else {
                            handleBUSResultError(result);
                        }
                    });
                } catch (Exception e) {
                    log.error("Error deleting leave request", e);
                }
            }).start();
        }
    }

    // VỊ TRÍ THÊM: Xử lý lỗi từ BUS tập trung
    private void handleBUSResultError(BUSOperationResult result) {
        String msg = switch (result) {
            case NOT_FOUND -> "Không tìm thấy đơn nghỉ.";
            case CONFLICT -> "Đơn đã được xử lý, không thể xóa.";
            case UNAUTHORIZED -> "Bạn không có quyền xóa đơn này.";
            default -> "Lỗi cơ sở dữ liệu.";
        };
        NotificationUtils.showErrorAlert("Thất bại", msg);
    }

    private void showLeaveDetails(LeaveRequestDTO leave) {
        if (leave == null) return;
        String details = String.format("ID: %d\nLoại: %s\nTừ: %s\nĐến: %s\nLý do: %s\nTrạng thái: %s",
                leave.getId(), leave.getLeaveTypeName(),
                leave.getStartDate(), leave.getEndDate(),
                leave.getContent(), leave.getStatusName());
        NotificationUtils.showInfoAlert("Chi tiết đơn nghỉ phép", details);
    }

    private void addNewLeaveRequest() {
        if (currentEmployeeId == -1) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn nhân viên trước.");
            return;
        }
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
            log.error("Error opening modal", e);
        }
    }

    private void deleteSelectedLeave() {
        deleteLeave(tblLeaveRequest.getSelectionModel().getSelectedItem());
    }

    private void editSelectedLeave() {
        LeaveRequestDTO selected = tblLeaveRequest.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showInfoAlert("Cảnh báo", "Vui lòng chọn một đơn nghỉ phép");
            return;
        }
        if (selected.getStatusId() != 20) {
            NotificationUtils.showErrorAlert("Lỗi", "Chỉ có thể sửa đơn đang ở trạng thái Chờ duyệt.");
            return;
        }
        // Logic mở Modal sửa tương tự Add nhưng truyền thêm DTO vào modalController
        NotificationUtils.showInfoAlert("Thông báo", "Chức năng chỉnh sửa sẽ sử dụng chung Modal với Thêm mới.");
    }
}