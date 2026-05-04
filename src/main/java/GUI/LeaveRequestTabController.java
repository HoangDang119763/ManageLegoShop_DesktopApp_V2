package GUI;

import BUS.LeaveRequestBUS;
import BUS.LeaveTypeBUS;
import DTO.LeaveRequestDTO;
import DTO.LeaveTypeDTO;
import ENUM.BUSOperationResult;
import ENUM.PermissionKey;
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
import java.time.temporal.ChronoUnit;
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
    @FXML private TableColumn<LeaveRequestDTO, Long> colDays;

    @FXML private Button btnAdd, btnRefresh, btnApprove, btnReject;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;

    private final LeaveRequestBUS leaveRequestBUS = LeaveRequestBUS.getInstance();
    private final LeaveTypeBUS leaveTypeBUS = LeaveTypeBUS.getInstance();
    private final SessionManagerService sessionManager = SessionManagerService.getInstance();

    private int currentEmployeeId;
    private ObservableList<LeaveRequestDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        currentEmployeeId = sessionManager.employeeLoginId();

        setupTable();
        setupStatusFilter();
        setupListeners();
        loadLeaveRequests();
        hideButtonWithoutPermission();
    }

    private void hideButtonWithoutPermission() {
        boolean canView = sessionManager.hasPermission(PermissionKey.EMPLOYEE_LEAVE_REQUEST_VIEW);

        if (!canView) {
            tblLeaveRequest.setVisible(false);
            tblLeaveRequest.setManaged(false);
            NotificationUtils.showErrorAlert("Bạn không có quyền truy cập", "Unauthorized");
            return;
        }

        if (!sessionManager.hasPermission(PermissionKey.EMPLOYEE_LEAVE_REQUEST_CREATE)) {
            btnAdd.setVisible(false);
        }

        if (!sessionManager.hasPermission(PermissionKey.EMPLOYEE_LEAVE_REQUEST_MANAGE)) {
            if (btnApprove != null) btnApprove.setVisible(false);
            if (btnReject != null) btnReject.setVisible(false);
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colLeaveType.setCellValueFactory(new PropertyValueFactory<>("leaveTypeName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("content"));

        colDays.setCellValueFactory(cellData -> {
            LeaveRequestDTO dto = cellData.getValue();
            if (dto.getStartDate() != null && dto.getEndDate() != null) {
                long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
                return new javafx.beans.property.SimpleLongProperty(days).asObject();
            }
            return new javafx.beans.property.SimpleLongProperty(0).asObject();
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Duyệt");
            private final Button rejectBtn = new Button("Từ chối");
            private final HBox pane = new HBox(5, approveBtn, rejectBtn);

            {
                pane.setStyle("-fx-alignment: CENTER;");
                approveBtn.setOnAction(e -> handleStatusUpdate(getCurrentId(), true));
                rejectBtn.setOnAction(e -> handleStatusUpdate(getCurrentId(), false));
            }

            private int getCurrentId() {
                return getTableView().getItems().get(getIndex()).getId();
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || !sessionManager.hasPermission(PermissionKey.EMPLOYEE_LEAVE_REQUEST_MANAGE)) {
                    setGraphic(null);
                    return;
                }

                LeaveRequestDTO data = getTableView().getItems().get(getIndex());
                setGraphic(data.getStatusId() == 20 ? pane : null);
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

        if (btnApprove != null) btnApprove.setOnAction(e -> handleStatusTransition(true));
        if (btnReject != null) btnReject.setOnAction(e -> handleStatusTransition(false));

        if (cbStatus != null) cbStatus.setOnAction(e -> applyFilters());

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, o, n) -> applyFilters());
        }
    }

    private void handleStatusUpdate(int id, boolean isApprove) {
        BUSOperationResult result = isApprove
                ? leaveRequestBUS.approve(id)
                : leaveRequestBUS.reject(id);

        handleBUSResult(result, isApprove ? "Duyệt thành công" : "Đã từ chối");
    }

    private void loadLeaveRequests() {
        ArrayList<LeaveRequestDTO> leaves;

        if (sessionManager.hasPermission(PermissionKey.EMPLOYEE_LEAVE_REQUEST_MANAGE)) {
            leaves = leaveRequestBUS.getAll();
        } else {
            leaves = leaveRequestBUS.getByEmployeeId(currentEmployeeId);
        }

        masterData.setAll(leaves);
        applyFilters();
    }

    private void applyFilters() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase();
        String statusFilter = cbStatus.getValue();

        FilteredList<LeaveRequestDTO> filtered = masterData.filtered(item -> {
            boolean matchStatus = true;

            if (!"Tất cả".equals(statusFilter)) {
                matchStatus = switch (statusFilter) {
                    case "Chờ duyệt" -> item.getStatusId() == 20;
                    case "Đã duyệt" -> item.getStatusId() == 21;
                    case "Từ chối" -> item.getStatusId() == 22;
                    default -> true;
                };
            }

            boolean matchSearch = item.getEmployeeName().toLowerCase().contains(keyword)
                    || item.getContent().toLowerCase().contains(keyword);

            return matchStatus && matchSearch;
        });

        tblLeaveRequest.setItems(filtered);
    }

    private void handleStatusTransition(boolean isApprove) {
        LeaveRequestDTO selected = tblLeaveRequest.getSelectionModel().getSelectedItem();

        if (selected == null) {
            NotificationUtils.showErrorAlert("Chọn đơn", "Cảnh báo");
            return;
        }

        handleStatusUpdate(selected.getId(), isApprove);
    }

    private void handleBUSResult(BUSOperationResult result, String successMsg) {
        if (result.isSuccess()) {
            NotificationUtils.showInfoAlert(successMsg, "Thành công");
            loadLeaveRequests();
        } else {
            NotificationUtils.showErrorAlert("Lỗi hệ thống", "Lỗi");
        }
    }

    private void handleAdd() {
        showLeaveRequestDialog(null);
    }

    private void showLeaveRequestDialog(LeaveRequestDTO editingLeave) {

        Dialog<LeaveRequestDTO> dialog = new Dialog<>();
        dialog.setTitle(editingLeave == null ? "Tạo đơn" : "Sửa đơn");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<LeaveTypeDTO> cbType = new ComboBox<>(
                FXCollections.observableArrayList(leaveTypeBUS.getAll())
        );

        cbType.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(LeaveTypeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        cbType.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(LeaveTypeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        DatePicker dpStart = new DatePicker(LocalDate.now());
        DatePicker dpEnd = new DatePicker(LocalDate.now());
        TextArea taReason = new TextArea();

        if (editingLeave != null) {
            dpStart.setValue(editingLeave.getStartDate());
            dpEnd.setValue(editingLeave.getEndDate());
            taReason.setText(editingLeave.getContent());

            cbType.getItems().stream()
                    .filter(t -> t.getId() == editingLeave.getLeaveTypeId())
                    .findFirst()
                    .ifPresent(cbType::setValue);
        }

        grid.add(new Label("Loại nghỉ:"), 0, 0);
        grid.add(cbType, 1, 0);
        grid.add(new Label("Từ ngày:"), 0, 1);
        grid.add(dpStart, 1, 1);
        grid.add(new Label("Đến ngày:"), 0, 2);
        grid.add(dpEnd, 1, 2);
        grid.add(new Label("Lý do:"), 0, 3);
        grid.add(taReason, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;

            if (cbType.getValue() == null) return null;
            if (dpStart.getValue().isAfter(dpEnd.getValue())) return null;

            LeaveRequestDTO dto = editingLeave != null ? editingLeave : new LeaveRequestDTO();
            dto.setEmployeeId(currentEmployeeId);
            dto.setLeaveTypeId(cbType.getValue().getId());
            dto.setStartDate(dpStart.getValue());
            dto.setEndDate(dpEnd.getValue());
            dto.setContent(taReason.getText());

            return dto;
        });

        dialog.showAndWait().ifPresent(dto -> {
            BUSOperationResult res = editingLeave == null
                    ? leaveRequestBUS.insert(dto)
                    : leaveRequestBUS.update(dto);

            handleBUSResult(res, "Thành công");
        });
    }
}