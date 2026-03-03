package GUI;

import BUS.LeaveRequestBUS;
import DTO.LeaveRequestDTO;
import ENUM.BUSOperationResult;
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class LeaveRequestController {

    @FXML private TableView<LeaveRequestDTO> tblLeave;
    @FXML private TableColumn<LeaveRequestDTO, Integer> colId;
    @FXML private TableColumn<LeaveRequestDTO, String> colStartDate;
    @FXML private TableColumn<LeaveRequestDTO, String> colEndDate;
    @FXML private TableColumn<LeaveRequestDTO, String> colType;
    @FXML private TableColumn<LeaveRequestDTO, String> colStatus;

    @FXML private Button btnCreate, btnApprove, btnReject;

    private final LeaveRequestBUS leaveBUS = LeaveRequestBUS.getInstance();
    private final SessionManagerService session = SessionManagerService.getInstance();

    @FXML
    public void initialize() {
        setupTable();
        loadData();

        // Phân quyền hiển thị dựa trên Role (Admin = 1)
        boolean isAdmin = session.employeeRoleId() == 1;
        btnApprove.setVisible(isAdmin);
        btnReject.setVisible(isAdmin);
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        
        // Sử dụng leaveTypeName và statusName (đã được DAL Join từ các bảng liên quan)
        colType.setCellValueFactory(new PropertyValueFactory<>("leaveTypeName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusName"));
    }

    private void loadData() {
        if (session.employeeRoleId() != 1) {
            // Nhân viên thường chỉ thấy đơn của mình
            tblLeave.setItems(FXCollections.observableArrayList(
                leaveBUS.getByEmployeeId(session.employeeLoginId())
            ));
        } else {
            // Admin thấy toàn bộ đơn trong hệ thống
            tblLeave.setItems(FXCollections.observableArrayList(leaveBUS.getAll()));
        }
    }

    @FXML
    private void handleApprove() {
        LeaveRequestDTO selected = tblLeave.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn đơn cần duyệt", "Thông báo");
            return;
        }

        BUSOperationResult result = leaveBUS.approve(selected.getId());
        handleResult(result, "Duyệt đơn thành công");
    }

    @FXML
    private void handleReject() {
        LeaveRequestDTO selected = tblLeave.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn đơn cần từ chối", "Thông báo");
            return;
        }

        BUSOperationResult result = leaveBUS.reject(selected.getId());
        handleResult(result, "Từ chối đơn thành công");
    }

    @FXML
    private void handleCreate() {
        // Đây là ví dụ tạo đơn nhanh, thực tế bạn nên mở một Modal để nhập nội dung/ngày tháng
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setEmployeeId(session.employeeLoginId());
        dto.setContent("Yêu cầu nghỉ phép mới");
        dto.setLeaveTypeId(1); // Mặc định loại 1 (Nghỉ có phép)
        dto.setStartDate(java.time.LocalDate.now());
        dto.setEndDate(java.time.LocalDate.now().plusDays(1));

        // Gọi hàm insert đã được đơn giản hóa tham số trong BUS
        BUSOperationResult result = leaveBUS.insert(dto);
        handleResult(result, "Tạo đơn thành công");
    }

    private void handleResult(BUSOperationResult result, String successMsg) {
        if (result.isSuccess()) {
            NotificationUtils.showInfoAlert(successMsg, "Thông báo");
            loadData(); // Làm mới bảng dữ liệu
            return;
        }

        // Xử lý các trường hợp lỗi dựa trên Enum
        switch (result) {
            case INVALID_PARAMS -> NotificationUtils.showErrorAlert("Tham số đầu vào không hợp lệ.", "Lỗi");
            case INVALID_DATA -> NotificationUtils.showErrorAlert("Dữ liệu đơn không hợp lệ hoặc sai định dạng.", "Lỗi");
            case NOT_FOUND -> NotificationUtils.showErrorAlert("Không tìm thấy đơn nghỉ phép này trên hệ thống.", "Lỗi");
            case CONFLICT -> NotificationUtils.showErrorAlert("Đơn này đã được xử lý hoặc hủy, không thể thay đổi.", "Lỗi");
            case UNAUTHORIZED -> NotificationUtils.showErrorAlert("Bạn không có quyền thực hiện thao tác này.", "Lỗi");
            case DB_ERROR -> NotificationUtils.showErrorAlert("Lỗi kết nối cơ sở dữ liệu.", "Lỗi");
            default -> NotificationUtils.showErrorAlert("Thao tác thất bại do lỗi không xác định.", "Lỗi");
        }
    }
}