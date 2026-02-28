package GUI;

import BUS.LeaveRequestBUS;
import BUS.LeaveTypeBUS;
import BUS.EmployeeBUS;
import DTO.LeaveRequestDTO;
import DTO.LeaveTypeDTO;
import DTO.EmployeeDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;

import java.time.LocalDate;
import java.util.ArrayList;

public class LeaveRequestTabController {
    @FXML
    private TableView<LeaveRequestDTO> tblLeaveRequest;
    @FXML
    private TableColumn<LeaveRequestDTO, Integer> colId;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colEmployeeName;
    @FXML
    private TableColumn<LeaveRequestDTO, LocalDate> colStartDate;
    @FXML
    private TableColumn<LeaveRequestDTO, LocalDate> colEndDate;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colLeaveType;
    @FXML
    private TableColumn<LeaveRequestDTO, Integer> colDays;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colStatus;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colReason;
    @FXML
    private TableColumn<LeaveRequestDTO, Void> colAction;

    @FXML
    private Button btnAdd, btnRefresh;
    
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbStatus;

    private LeaveRequestBUS leaveRequestBUS;
    private LeaveTypeBUS leaveTypeBUS;
    private EmployeeBUS employeeBUS;
    private SessionManagerService sessionManager;
    private int currentEmployeeId;
    private int currentEmployeeRoleId;

    @FXML
    public void initialize() {
        leaveRequestBUS = LeaveRequestBUS.getInstance();
        leaveTypeBUS = LeaveTypeBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        sessionManager = SessionManagerService.getInstance();

        setupTable();
        setupLeaveTypeCombo();
        setupListeners();
        setupPermissions();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colLeaveType.setCellValueFactory(new PropertyValueFactory<>("leaveTypeName"));
        colDays.setCellValueFactory(new PropertyValueFactory<>("dayCount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("content"));
        
        // Setup action column with Edit/Delete buttons
        colAction.setCellFactory(param -> new TableCell<LeaveRequestDTO, Void>() {
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(5);

            {
                editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px;");
                pane.setAlignment(javafx.geometry.Pos.CENTER);
                pane.getChildren().addAll(editBtn, deleteBtn);

                editBtn.setOnAction(event -> {
                    LeaveRequestDTO leave = getTableView().getItems().get(getIndex());
                    showLeaveRequestDialog(leave);
                });
                deleteBtn.setOnAction(event -> {
                    LeaveRequestDTO leave = getTableView().getItems().get(getIndex());
                    handleDeleteLeave(leave);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupLeaveTypeCombo() {
        // Setup status filter combo
        ArrayList<String> statuses = new ArrayList<>();
        statuses.add("Tất cả trạng thái");
        statuses.add("Chờ duyệt");
        statuses.add("Đã duyệt");
        statuses.add("Từ chối");
        cbStatus.setItems(FXCollections.observableArrayList(statuses));
        cbStatus.getSelectionModel().selectFirst();
    }

    private void setupListeners() {
        btnAdd.setOnAction(e -> handleAdd());
        btnRefresh.setOnAction(e -> loadLeaveRequests());
        cbStatus.setOnAction(e -> filterByStatus());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterBySearch(newVal));
        tblLeaveRequest.setOnMouseClicked(e -> loadSelectedLeave());
    }

    private void setupPermissions() {
        // Load initial data
        currentEmployeeRoleId = sessionManager.employeeRoleId();
        if (currentEmployeeRoleId == 1) {
            // Admin can see and manage all
            loadLeaveRequests();
        } else {
            // Employee sees only own requests
            currentEmployeeId = sessionManager.employeeLoginId();
            loadLeaveRequests();
        }
    }

    public void loadEmployeeLeaves(int employeeId) {
        this.currentEmployeeId = employeeId;
        loadLeaveRequests();
    }

    private void loadLeaveRequests() {
        ArrayList<LeaveRequestDTO> leaves;
        if (currentEmployeeRoleId == 1) {
            // Admin xem hết
            leaves = leaveRequestBUS.getAll();
        } else {
            // Nhân viên chỉ xem của bản thân
            leaves = leaveRequestBUS.getByEmployeeId(currentEmployeeId);
        }
        
        // Populate employee names
        for (LeaveRequestDTO leave : leaves) {
            if (leave.getEmployeeId() > 0) {
                EmployeeDTO emp = employeeBUS.getById(leave.getEmployeeId());
                if (emp != null) {
                    leave.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
                }
            }
        }
        
        tblLeaveRequest.setItems(FXCollections.observableArrayList(leaves));
    }

    private void loadSelectedLeave() {
        LeaveRequestDTO selected = tblLeaveRequest.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Just select the row, further details could be shown in a dialog
        }
    }

    @FXML
    private void handleAdd() {
        showLeaveRequestDialog(null);
    }

    private void showLeaveRequestDialog(LeaveRequestDTO editingLeave) {
        Dialog<LeaveRequestDTO> dialog = new Dialog<>();
        dialog.setTitle(editingLeave == null ? "Tạo đơn nghỉ phép" : "Cập nhật đơn nghỉ phép");
        dialog.setHeaderText(editingLeave == null ? "Nhập thông tin đơn nghỉ" : "Sửa thông tin đơn nghỉ");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(15));

        ComboBox<LeaveTypeDTO> cbLeaveType = new ComboBox<>();
        ArrayList<LeaveTypeDTO> leaveTypes = leaveTypeBUS.getAll();
        cbLeaveType.setItems(FXCollections.observableArrayList(leaveTypes));
        cbLeaveType.setCellFactory(param -> new ListCell<LeaveTypeDTO>() {
            @Override
            protected void updateItem(LeaveTypeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        cbLeaveType.setButtonCell(new ListCell<LeaveTypeDTO>() {
            @Override
            protected void updateItem(LeaveTypeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        DatePicker dpStartDate = new DatePicker();
        DatePicker dpEndDate = new DatePicker();
        TextArea txtReason = new TextArea();
        txtReason.setPrefRowCount(3);
        txtReason.setWrapText(true);

        if (editingLeave != null) {
            cbLeaveType.setValue(leaveTypes.stream()
                .filter(lt -> lt.getId() == editingLeave.getLeaveTypeId())
                .findFirst().orElse(null));
            dpStartDate.setValue(editingLeave.getStartDate());
            dpEndDate.setValue(editingLeave.getEndDate());
            txtReason.setText(editingLeave.getContent());
        }

        grid.add(new Label("Loại nghỉ:"), 0, 0);
        grid.add(cbLeaveType, 1, 0);
        grid.add(new Label("Ngày bắt đầu:"), 0, 1);
        grid.add(dpStartDate, 1, 1);
        grid.add(new Label("Ngày kết thúc:"), 0, 2);
        grid.add(dpEndDate, 1, 2);
        grid.add(new Label("Lý do:"), 0, 3);
        grid.add(txtReason, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (cbLeaveType.getValue() == null || dpStartDate.getValue() == null || dpEndDate.getValue() == null) {
                    NotificationUtils.showErrorAlert("Vui lòng điền đầy đủ thông tin", "Lỗi");
                    return null;
                }

                if (dpEndDate.getValue().isBefore(dpStartDate.getValue())) {
                    NotificationUtils.showErrorAlert("Ngày kết thúc phải sau ngày bắt đầu", "Lỗi");
                    return null;
                }

                LeaveRequestDTO leave = editingLeave != null ? editingLeave : new LeaveRequestDTO();
                leave.setEmployeeId(currentEmployeeId);
                leave.setLeaveTypeId(cbLeaveType.getValue().getId());
                leave.setLeaveTypeName(cbLeaveType.getValue().getName());
                leave.setStartDate(dpStartDate.getValue());
                leave.setEndDate(dpEndDate.getValue());
                leave.setContent(txtReason.getText());
                leave.setDayCount((int) (java.time.temporal.ChronoUnit.DAYS.between(dpStartDate.getValue(), dpEndDate.getValue()) + 1));
                
                if (editingLeave == null) {
                    leave.setStatusId(22); // Chờ duyệt
                }

                return leave;
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            LeaveRequestDTO leave = result.get();
            int resultCode;
            if (editingLeave == null) {
                resultCode = leaveRequestBUS.insert(leave, currentEmployeeRoleId, currentEmployeeId);
            } else {
                resultCode = leaveRequestBUS.update(leave, currentEmployeeRoleId, currentEmployeeId);
            }
            
            if (resultCode == 1) {
                NotificationUtils.showInfoAlert(editingLeave == null ? "Tạo đơn thành công" : "Cập nhật đơn thành công", "Thành công");
                loadLeaveRequests();
            } else {
                NotificationUtils.showErrorAlert("Có lỗi xảy ra", "Lỗi");
            }
        }
    }

    @FXML
    private void handleApprove() {
        LeaveRequestDTO selected = tblLeaveRequest.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn đơn để duyệt", "Cảnh báo");
            return;
        }

        selected.setStatusId(20); // Duyệt & trừ lương
        int result = leaveRequestBUS.update(selected, currentEmployeeRoleId, currentEmployeeId);
        if (result == 1) {
            NotificationUtils.showInfoAlert("Duyệt đơn và áp dụng trừ lương", "Thành công");
            loadLeaveRequests();
        }
    }

    @FXML
    private void handleReject() {
        LeaveRequestDTO selected = tblLeaveRequest.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn đơn để từ chối", "Cảnh báo");
            return;
        }

        selected.setStatusId(21); // Từ chối
        int result = leaveRequestBUS.update(selected, currentEmployeeRoleId, currentEmployeeId);
        if (result == 1) {
            NotificationUtils.showInfoAlert("Từ chối đơn nghỉ", "Thành công");
            loadLeaveRequests();
        }
    }

    private void handleDeleteLeave(LeaveRequestDTO leave) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Xác nhận xóa");
        dialog.setHeaderText("Bạn có chắc chắn muốn xóa đơn này?");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        dialog.setResultConverter(dialogButton -> dialogButton == ButtonType.YES);

        var result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            boolean deleteResult = leaveRequestBUS.delete(leave.getId(), currentEmployeeRoleId, currentEmployeeId);
            if (deleteResult) {
                NotificationUtils.showInfoAlert("Xóa đơn nghỉ thành công", "Thành công");
                loadLeaveRequests();
            } else {
                NotificationUtils.showErrorAlert("Xóa đơn nghỉ thất bại", "Lỗi");
            }
        }
    }

    private void filterByStatus() {
        String selectedStatus = cbStatus.getValue();
        ArrayList<LeaveRequestDTO> leaves;
        
        if (currentEmployeeRoleId == 1) {
            leaves = leaveRequestBUS.getAll();
        } else {
            leaves = leaveRequestBUS.getByEmployeeId(currentEmployeeId);
        }
        
        if ("Tất cả trạng thái".equals(selectedStatus)) {
            // Show all
        } else {
            leaves.removeIf(leave -> {
                if ("Chờ duyệt".equals(selectedStatus)) return leave.getStatusId() != 22;
                if ("Đã duyệt".equals(selectedStatus)) return leave.getStatusId() != 20;
                if ("Từ chối".equals(selectedStatus)) return leave.getStatusId() != 21;
                return true;
            });
        }
        
        // Populate employee names
        for (LeaveRequestDTO leave : leaves) {
            if (leave.getEmployeeId() > 0) {
                EmployeeDTO emp = employeeBUS.getById(leave.getEmployeeId());
                if (emp != null) {
                    leave.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
                }
            }
        }
        
        tblLeaveRequest.setItems(FXCollections.observableArrayList(leaves));
    }

    private void filterBySearch(String keyword) {
        ArrayList<LeaveRequestDTO> leaves;
        
        if (currentEmployeeRoleId == 1) {
            leaves = leaveRequestBUS.getAll();
        } else {
            leaves = leaveRequestBUS.getByEmployeeId(currentEmployeeId);
        }
        
        if (keyword == null || keyword.isEmpty()) {
            // No search, show all filtered by status
            filterByStatus();
            return;
        }
        
        final String searchKeyword = keyword.toLowerCase();
        leaves.removeIf(leave -> {
            EmployeeDTO emp = employeeBUS.getById(leave.getEmployeeId());
            String empName = emp != null ? (emp.getFirstName() + " " + emp.getLastName()).toLowerCase() : "";
            String reason = (leave.getContent() != null ? leave.getContent() : "").toLowerCase();
            return !empName.contains(searchKeyword) && !reason.contains(searchKeyword);
        });
        
        // Populate employee names
        for (LeaveRequestDTO leave : leaves) {
            if (leave.getEmployeeId() > 0) {
                EmployeeDTO emp = employeeBUS.getById(leave.getEmployeeId());
                if (emp != null) {
                    leave.setEmployeeName(emp.getFirstName() + " " + emp.getLastName());
                }
            }
        }
        
        tblLeaveRequest.setItems(FXCollections.observableArrayList(leaves));
    }
}
