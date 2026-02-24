package GUI;

import BUS.LeaveRequestBUS;
import DTO.LeaveRequestDTO;
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class LeaveRequestController {
    @FXML
    private TableView<LeaveRequestDTO> tblLeave;
    @FXML
    private TableColumn<LeaveRequestDTO, Integer> colId;
    @FXML
    private TableColumn<LeaveRequestDTO, String> colStartDate, colEndDate, colType, colStatus;
    @FXML
    private Button btnCreate, btnApprove, btnReject;

    private LeaveRequestBUS leaveBUS = LeaveRequestBUS.getInstance();
    private SessionManagerService session = SessionManagerService.getInstance();

    @FXML
    public void initialize() {
        setupTable();
        loadData();

        // Phân quyền: Chỉ Admin mới thấy nút Duyệt/Từ chối
        boolean isAdmin = session.employeeRoleId() == 1; // Giả định Role 1 là Admin
        btnApprove.setVisible(isAdmin);
        btnReject.setVisible(isAdmin);
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colType.setCellValueFactory(new PropertyValueFactory<>("typeName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusDescription"));
    }

    private void loadData() {
        // Nếu là nhân viên thường, chỉ lấy đơn của chính mình
        if (session.employeeRoleId() != 1) {
            tblLeave.setItems(FXCollections.observableArrayList(
                    leaveBUS.getByEmployeeId(session.employeeLoginId())));
        } else {
            tblLeave.setItems(FXCollections.observableArrayList(leaveBUS.getAll()));
        }
    }

    @FXML
    private void handleApprove() {
        LeaveRequestDTO selected = tblLeave.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Update status_id = 20 (Duyệt & Trừ lương theo chính sách)
            // NOTE: Use LeaveRequestTabController instead - update() method returns int
            selected.setStatusId(20);
            int result = leaveBUS.update(selected, 1, 1);
            if (result == 1) {
                NotificationUtils.showInfoAlert("Đã duyệt đơn và áp dụng trừ lương phí nghỉ.", "Thông báo");
                loadData();
            }
        }
    }
}