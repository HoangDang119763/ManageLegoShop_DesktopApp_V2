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
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;

import java.time.LocalDate;
import java.util.ArrayList;

public class LeaveRequestTabController {
    @FXML
    private TableView<LeaveRequestDTO> tblLeave;
    @FXML
    private TableColumn<LeaveRequestDTO, Integer> colId;
    @FXML
    private TableColumn<LeaveRequestDTO, LocalDate> colStartDate;
    @FXML
    private TableColumn<LeaveRequestDTO, LocalDate> colEndDate;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colType;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colStatus;

    @FXML
    private Button btnCreate, btnApprove, btnReject, btnRefresh;

    @FXML
    private TextArea txtContent;
    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private ComboBox<LeaveTypeDTO> cbLeaveType;
    @FXML
    private Label lblEmployeeName;

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
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusId"));
    }

    private void setupLeaveTypeCombo() {
        ArrayList<LeaveTypeDTO> leaveTypes = leaveTypeBUS.getAll();
        cbLeaveType.setItems(FXCollections.observableArrayList(leaveTypes));
    }

    private void setupListeners() {
        btnCreate.setOnAction(e -> handleCreate());
        btnApprove.setOnAction(e -> handleApprove());
        btnReject.setOnAction(e -> handleReject());
        btnRefresh.setOnAction(e -> loadLeaveRequests());
        tblLeave.setOnMouseClicked(e -> loadSelectedLeave());
    }

    private void setupPermissions() {
        // Chỉ admin (role 1) mới có quyền duyệt/từ chối
        currentEmployeeRoleId = sessionManager.employeeRoleId();
        boolean isAdmin = currentEmployeeRoleId == 1;
        btnApprove.setVisible(isAdmin);
        btnReject.setVisible(isAdmin);
    }

    public void loadEmployeeLeaves(int employeeId) {
        this.currentEmployeeId = employeeId;
        EmployeeDTO emp = employeeBUS.getById(employeeId);
        if (emp != null) {
            lblEmployeeName.setText(emp.getFirstName() + " " + emp.getLastName());
        }
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
        tblLeave.setItems(FXCollections.observableArrayList(leaves));
    }

    private void loadSelectedLeave() {
        LeaveRequestDTO selected = tblLeave.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtContent.setText(selected.getContent());
            dpStartDate.setValue(selected.getStartDate());
            dpEndDate.setValue(selected.getEndDate());
        }
    }

    @FXML
    private void handleCreate() {
        if (!validateLeaveInputs()) {
            NotificationUtils.showErrorAlert("Vui lòng kiểm tra lại dữ liệu", "Lỗi nhập liệu");
            return;
        }

        LeaveRequestDTO leave = new LeaveRequestDTO();
        leave.setEmployeeId(currentEmployeeId);
        leave.setContent(txtContent.getText());
        leave.setStartDate(dpStartDate.getValue());
        leave.setEndDate(dpEndDate.getValue());
        leave.setLeaveTypeId(cbLeaveType.getValue().getId());
        leave.setLeaveTypeName(cbLeaveType.getValue().getName());
        leave.setStatusId(22); // Status chờ duyệt

        int result = leaveRequestBUS.insert(leave, currentEmployeeRoleId, currentEmployeeId);
        if (result == 1) {
            NotificationUtils.showInfoAlert("Tạo đơn nghỉ thành công", "Thành công");
            clearInputs();
            loadLeaveRequests();
        } else {
            NotificationUtils.showErrorAlert("Không thể tạo đơn nghỉ", "Lỗi");
        }
    }

    @FXML
    private void handleApprove() {
        LeaveRequestDTO selected = tblLeave.getSelectionModel().getSelectedItem();
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
        LeaveRequestDTO selected = tblLeave.getSelectionModel().getSelectedItem();
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

    private boolean validateLeaveInputs() {
        if (txtContent.getText().isEmpty()) return false;
        if (dpStartDate.getValue() == null) return false;
        if (dpEndDate.getValue() == null) return false;
        if (cbLeaveType.getValue() == null) return false;
        if (dpEndDate.getValue().isBefore(dpStartDate.getValue())) {
            NotificationUtils.showErrorAlert("Ngày kết thúc phải sau ngày bắt đầu", "Lỗi");
            return false;
        }
        return true;
    }

    private void clearInputs() {
        txtContent.clear();
        dpStartDate.setValue(null);
        dpEndDate.setValue(null);
        cbLeaveType.setValue(null);
        tblLeave.getSelectionModel().clearSelection();
    }
}
