package GUI;

import BUS.LeaveRequestBUS;
import BUS.LeaveTypeBUS;
import BUS.EmployeeBUS;
import DTO.LeaveRequestDTO;
import DTO.LeaveTypeDTO;
import ENUM.BUSOperationResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;

import java.time.LocalDate;
import java.util.ArrayList;

public class LeaveRequestTabController {
    @FXML private TableView<LeaveRequestDTO> tblLeaveRequest;
    @FXML private TableColumn<LeaveRequestDTO, Integer> colId;
    @FXML private TableColumn<LeaveRequestDTO, String> colEmployeeName;
    @FXML private TableColumn<LeaveRequestDTO, LocalDate> colStartDate;
    @FXML private TableColumn<LeaveRequestDTO, LocalDate> colEndDate;
    @FXML private TableColumn<LeaveRequestDTO, String> colLeaveType;
    @FXML private TableColumn<LeaveRequestDTO, String> colStatus;
    @FXML private TableColumn<LeaveRequestDTO, String> colReason;
    @FXML private TableColumn<LeaveRequestDTO, Void> colAction;

    @FXML private Button btnAdd, btnRefresh, btnApprove, btnReject;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;

    private final LeaveRequestBUS leaveRequestBUS = LeaveRequestBUS.getInstance();
    private final LeaveTypeBUS leaveTypeBUS = LeaveTypeBUS.getInstance();
    private final SessionManagerService sessionManager = SessionManagerService.getInstance();

    private int currentEmployeeId;
    private int currentEmployeeRoleId;
    private ObservableList<LeaveRequestDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        currentEmployeeId = sessionManager.employeeLoginId();
        currentEmployeeRoleId = sessionManager.employeeRoleId();

        setupTable();
        setupStatusFilter();
        setupListeners();
        setupPermissions();
        loadLeaveRequests();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colLeaveType.setCellValueFactory(new PropertyValueFactory<>("leaveTypeName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("content"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Duyệt");
            private final Button rejectBtn = new Button("Từ chối");
            private final HBox pane = new HBox(5, approveBtn, rejectBtn);

            {
                approveBtn.getStyleClass().add("btn-approve");
                rejectBtn.getStyleClass().add("btn-reject");
                pane.setStyle("-fx-alignment: CENTER;");

                approveBtn.setOnAction(e -> {
                    LeaveRequestDTO data = getTableView().getItems().get(getIndex());
                    handleStatusUpdate(data.getId(), true);
                });

                rejectBtn.setOnAction(e -> {
                    LeaveRequestDTO data = getTableView().getItems().get(getIndex());
                    handleStatusUpdate(data.getId(), false);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || sessionManager.employeeRoleId() != 1) {
                    setGraphic(null);
                } else {
                    LeaveRequestDTO data = getTableView().getItems().get(getIndex());
                    // Chỉ hiện nút duyệt/từ chối nếu đơn đang ở trạng thái Chờ duyệt (20)
                    if (data != null && data.getStatusId() == 20) {
                        setGraphic(pane);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    private void setupStatusFilter() {
        cbStatus.setItems(FXCollections.observableArrayList("Tất cả", "Chờ duyệt", "Đã duyệt", "Từ chối"));
        cbStatus.getSelectionModel().selectFirst();
    }

    private void setupListeners() {
        if (btnAdd != null) btnAdd.setOnAction(e -> handleAdd());
        if (btnRefresh != null) btnRefresh.setOnAction(e -> loadLeaveRequests());
        
        // Cũ: Duyệt theo nút rời bên ngoài (Chống lỗi Null nếu FXML mới không có)
        if (btnApprove != null) btnApprove.setOnAction(e -> handleStatusTransition(true));
        if (btnReject != null) btnReject.setOnAction(e -> handleStatusTransition(false));

        if (cbStatus != null) cbStatus.setOnAction(e -> applyFilters());
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void setupPermissions() {
        boolean isAdmin = (currentEmployeeRoleId == 1);
        if (btnApprove != null) btnApprove.setVisible(isAdmin);
        if (btnReject != null) btnReject.setVisible(isAdmin);
    }

    // HÀM BỔ SUNG: Xử lý cập nhật trạng thái từ nút bấm trong TableCell
    private void handleStatusUpdate(int id, boolean isApprove) {
        BUSOperationResult result = isApprove ? leaveRequestBUS.approve(id) : leaveRequestBUS.reject(id);
        handleBUSResult(result, isApprove ? "Duyệt đơn thành công" : "Đã từ chối đơn");
    }

    private void loadLeaveRequests() {
        ArrayList<LeaveRequestDTO> leaves;
        if (currentEmployeeRoleId == 1) {
            leaves = leaveRequestBUS.getAll();
        } else {
            leaves = leaveRequestBUS.getByEmployeeId(currentEmployeeId);
        }
        masterData.setAll(leaves);
        applyFilters();
    }

    // HÀM BỔ SUNG: Bộ lọc kết hợp giữa Search và ComboBox Status
    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        String statusFilter = cbStatus.getValue();

        FilteredList<LeaveRequestDTO> filteredList = masterData.filtered(item -> {
            // 1. Lọc theo trạng thái
            boolean matchesStatus = true;
            if (statusFilter != null && !"Tất cả".equals(statusFilter)) {
                if ("Chờ duyệt".equals(statusFilter)) matchesStatus = item.getStatusId() == 20;
                else if ("Đã duyệt".equals(statusFilter)) matchesStatus = item.getStatusId() == 21;
                else if ("Từ chối".equals(statusFilter)) matchesStatus = item.getStatusId() == 22;
            }

            // 2. Lọc theo từ khóa tìm kiếm (Tên nhân viên hoặc nội dung)
            boolean matchesSearch = true;
            if (!keyword.isEmpty()) {
                String empName = item.getEmployeeName() != null ? item.getEmployeeName().toLowerCase() : "";
                String reason = item.getContent() != null ? item.getContent().toLowerCase() : "";
                matchesSearch = empName.contains(keyword) || reason.contains(keyword);
            }

            return matchesStatus && matchesSearch;
        });

        tblLeaveRequest.setItems(filteredList);
    }

    // Giữ nguyên các hàm handle đã có
    private void handleStatusTransition(boolean isApprove) {
        LeaveRequestDTO selected = tblLeaveRequest.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn đơn", "Cảnh báo");
            return;
        }
        handleStatusUpdate(selected.getId(), isApprove);
    }

    private void handleBUSResult(BUSOperationResult result, String successMsg) {
        if (result.isSuccess()) {
            NotificationUtils.showInfoAlert(successMsg, "Thành công");
            loadLeaveRequests();
        } else {
            String msg = switch (result) {
                case NOT_FOUND -> "Không tìm thấy đơn.";
                case CONFLICT -> "Đơn đã được xử lý trước đó.";
                case UNAUTHORIZED -> "Bạn không có quyền.";
                default -> "Lỗi hệ thống database.";
            };
            NotificationUtils.showErrorAlert(msg, "Lỗi");
        }
    }

    private void handleAdd() {
        showLeaveRequestDialog(null);
    }

    // Hàm bổ sung các logic lọc còn thiếu bạn yêu cầu
    private void filterByStatus() { applyFilters(); }
    private void filterBySearch(String newVal) { applyFilters(); }

    private void showLeaveRequestDialog(LeaveRequestDTO editingLeave) {
        Dialog<LeaveRequestDTO> dialog = new Dialog<>();
        dialog.setTitle(editingLeave == null ? "Tạo đơn" : "Sửa đơn");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));

        ComboBox<LeaveTypeDTO> cbType = new ComboBox<>(FXCollections.observableArrayList(leaveTypeBUS.getAll()));
        DatePicker dpStart = new DatePicker(editingLeave != null ? editingLeave.getStartDate() : LocalDate.now());
        DatePicker dpEnd = new DatePicker(editingLeave != null ? editingLeave.getEndDate() : LocalDate.now());
        TextArea taReason = new TextArea(editingLeave != null ? editingLeave.getContent() : "");
        taReason.setPrefRowCount(3);

        if (editingLeave != null) {
            cbType.getItems().stream()
                  .filter(t -> t.getId() == editingLeave.getLeaveTypeId())
                  .findFirst()
                  .ifPresent(cbType::setValue);
        }

        grid.add(new Label("Loại nghỉ:"), 0, 0); grid.add(cbType, 1, 0);
        grid.add(new Label("Từ ngày:"), 0, 1); grid.add(dpStart, 1, 1);
        grid.add(new Label("Đến ngày:"), 0, 2); grid.add(dpEnd, 1, 2);
        grid.add(new Label("Lý do:"), 0, 3); grid.add(taReason, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (cbType.getValue() == null) return null;
                LeaveRequestDTO dto = (editingLeave != null) ? editingLeave : new LeaveRequestDTO();
                dto.setEmployeeId(currentEmployeeId);
                dto.setLeaveTypeId(cbType.getValue().getId());
                dto.setStartDate(dpStart.getValue());
                dto.setEndDate(dpEnd.getValue());
                dto.setContent(taReason.getText());
                return dto;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dto -> {
            BUSOperationResult res = (editingLeave == null) ? 
                    leaveRequestBUS.insert(dto) : leaveRequestBUS.update(dto);
            handleBUSResult(res, editingLeave == null ? "Tạo đơn thành công" : "Cập nhật thành công");
        });
    }
}