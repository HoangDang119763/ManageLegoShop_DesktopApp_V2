package GUI;

import BUS.EmployeeBUS;
import BUS.AccountBUS;
import BUS.DepartmentBUS;
import BUS.RoleBUS;
import BUS.StatusBUS;
import DTO.EmployeeDTO;
import DTO.AccountDTO;
import DTO.DepartmentDTO;
import DTO.RoleDTO;
import DTO.StatusDTO;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;
import SERVICE.SessionManagerService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller quản lý màn hình thông tin nhân viên (Employee Info)
 * Chức năng:
 * - Hiển thị thông tin cá nhân của nhân viên (bên trái)
 * - Cho phép đổi mật khẩu (bên phải)
 */
@Slf4j
public class EmployeeInfoController {

    // ==================== PHẦN HIỂN THỊ THÔNG TIN (Left Panel)
    // ====================
    // Profile Info Section
    @FXML
    private TextField lblEmployeeId; // Mã nhân viên (readonly)
    @FXML
    private TextField lblGender; // Giới tính (readonly)
    @FXML
    private TextField lblDepartmentName; // Phòng ban (readonly)
    @FXML
    private TextField lblRoleName; // Vai trò (readonly)
    @FXML
    private TextField lblStatus; // Trạng thái (readonly)

    // Contact Info Section
    @FXML
    private TextField lblFirstName; // Họ đệm
    @FXML
    private TextField lblLastName; // Tên
    @FXML
    private TextField lblDateOfBirth; // Ngày sinh
    @FXML
    private TextField lblPhone; // Điện thoại
    @FXML
    private TextField lblEmail; // Email
    @FXML
    private TextField lblHealthInsCode; // Mã BHYT

    // Benefits Section
    @FXML
    private CheckBox cbHealthIns; // Bảo hiểm y tế
    @FXML
    private CheckBox cbSocialIns; // Bảo hiểm xã hội
    @FXML
    private CheckBox cbUnemploymentIns; // Bảo hiểm thất nghiệp
    @FXML
    private CheckBox cbIncomeTax; // Thuế TN cá nhân
    @FXML
    private CheckBox cbTransportSupport; // Hỗ trợ đi lại
    @FXML
    private CheckBox cbAccommSupport; // Hỗ trợ chỗ ở

    // Metadata
    @FXML
    private Label lblCreatedAt; // Ngày tạo hồ sơ (readonly)
    @FXML
    private Label lblUpdatedAt; // Cập nhật cuối (readonly)
    @FXML
    private Button btnUpdateInfo; // Nút lưu thông tin

    // ==================== PHẦN ĐỔI MẬT KHẨU (Right Panel) ====================
    @FXML
    private TextField lblUsername; // Tên tài khoản (readonly)
    @FXML
    private PasswordField txtOldPassword; // Mật khẩu cũ
    @FXML
    private PasswordField txtNewPassword; // Mật khẩu mới
    @FXML
    private PasswordField txtConfirmPassword; // Xác nhận mật khẩu mới
    @FXML
    private Button btnChangePassword; // Nút đổi mật khẩu
    @FXML
    private Button btnClear; // Nút xóa form

    @FXML
    public void initialize() {
        EmployeeBUS employeeBUS = EmployeeBUS.getInstance();
        if (employeeBUS.isLocalEmpty())
            employeeBUS.loadLocal();

        AccountBUS accountBUS = AccountBUS.getInstance();
        if (accountBUS.isLocalEmpty())
            accountBUS.loadLocal();

        DepartmentBUS departmentBUS = DepartmentBUS.getInstance();
        if (departmentBUS.isLocalEmpty())
            departmentBUS.loadLocal();

        RoleBUS roleBUS = RoleBUS.getInstance();
        if (roleBUS.isLocalEmpty())
            roleBUS.loadLocal();

        StatusBUS statusBUS = StatusBUS.getInstance();
        if (statusBUS.isLocalEmpty())
            statusBUS.loadLocal();

        setupListeners();
        loadEmployeeInfo();
    }

    /**
     * Thiết lập sự kiện cho các nút
     */
    private void setupListeners() {
        btnChangePassword.setOnAction(e -> handleChangePassword());
        btnClear.setOnAction(e -> handleClear());
        btnUpdateInfo.setOnAction(e -> handleUpdateInfo());
    }

    /**
     * PHẦN 1: TẢI THÔNG TIN NHÂN VIÊN
     * Lấy thông tin nhân viên hiện tại từ session và hiển thị
     */
    private void loadEmployeeInfo() {
        try {
            EmployeeBUS employeeBUS = EmployeeBUS.getInstance();
            AccountBUS accountBUS = AccountBUS.getInstance();
            DepartmentBUS departmentBUS = DepartmentBUS.getInstance();
            RoleBUS roleBUS = RoleBUS.getInstance();
            StatusBUS statusBUS = StatusBUS.getInstance();
            ValidationUtils validationUtils = ValidationUtils.getInstance();
            // Lấy thông tin nhân viên từ session
            EmployeeDTO employee = SessionManagerService.getInstance().currEmployee();

            if (employee != null) {
                // === PROFILE INFO SECTION ===
                lblEmployeeId.setText(String.valueOf(employee.getId()));
                lblGender.setText(employee.getGender() != null ? employee.getGender() : "");

                // Get department name
                if (employee.getDepartmentId() != null) {
                    DepartmentDTO department = departmentBUS.getByIdLocal(employee.getDepartmentId());
                    lblDepartmentName.setText(department != null ? department.getName() : "");
                } else {
                    lblDepartmentName.setText("");
                }

                RoleDTO role = roleBUS.getByIdLocal(SessionManagerService.getInstance().employeeRoleId());
                lblRoleName.setText(role != null ? role.getName() : "");
                StatusDTO status = statusBUS.getByIdLocal(employee.getStatusId());
                lblStatus.setText(status != null ? status.getDescription() : "");

                // === CONTACT INFO SECTION ===
                lblFirstName.setText(employee.getFirstName() != null ? employee.getFirstName() : "");
                lblLastName.setText(employee.getLastName() != null ? employee.getLastName() : "");
                lblDateOfBirth.setText(validationUtils.formatDateTime(employee.getDateOfBirth()));
                lblPhone.setText(employee.getPhone() != null ? employee.getPhone() : "");
                lblEmail.setText(employee.getEmail() != null ? employee.getEmail() : "");
                lblHealthInsCode.setText(employee.getHealthInsCode() != null ? employee.getHealthInsCode() : "");

                // === BENEFITS SECTION ===
                cbHealthIns.setSelected(employee.isHealthInsurance());
                cbSocialIns.setSelected(employee.isSocialInsurance());
                cbUnemploymentIns.setSelected(employee.isUnemploymentInsurance());
                cbIncomeTax.setSelected(employee.isPersonalIncomeTax());
                cbTransportSupport.setSelected(employee.isTransportationSupport());
                cbAccommSupport.setSelected(employee.isAccommodationSupport());

                lblCreatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(employee.getCreatedAt()));
                lblUpdatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(employee.getUpdatedAt()));
                // === Account ===
                AccountDTO account = accountBUS.getByIdLocal(employee.getAccountId());
                lblUsername.setText(account.getUsername());

            } else {
                NotificationUtils.showErrorAlert("Không thể tải thông tin nhân viên", AppMessages.DIALOG_TITLE);
            }
        } catch (Exception e) {
            NotificationUtils.showErrorAlert("Lỗi khi tải thông tin nhân viên. Vui lòng thử lại.",
                    AppMessages.DIALOG_TITLE);
        }
    }

    /**
     * PHẦN 2: XỬ LÝ ĐỔI MẬT KHẨU
     * Kiểm tra và cập nhật mật khẩu tài khoản
     */
    private void handleChangePassword() {
        // ===== BƯỚC 1: KIỂM TRA DỮ LIỆU ĐẦU VÀO =====
        if (!validatePasswordInput()) {
            return; // Nếu validation thất bại, dừng lại
        }

        try {
            // // ===== BƯỚC 2: LẤY ID NHÂN VIÊN HIỆN TẠI =====
            // int currentEmployeeId =
            // SessionManagerService.getInstance().getCurrentEmployeeId();
            //
            // // ===== BƯỚC 3: LẤY TÀI KHOẢN CỦA NHÂN VIÊN =====
            // AccountDTO account =
            // AccountBUS.getInstance().getByEmployeeId(currentEmployeeId);
            // if (account == null) {
            // NotificationUtils.showErrorAlert("Không tìm thấy tài khoản",
            // AppMessages.DIALOG_TITLE);
            // return;
            // }
            //
            // // ===== BƯỚC 4: KIỂM TRA MẬT KHẨU CŨ =====
            // String oldPassword = txtOldPassword.getText();
            // String newPassword = txtNewPassword.getText();
            //
            // // Kiểm tra xem mật khẩu cũ có đúng không
            // if (!account.getPassword().equals(oldPassword)) {
            // NotificationUtils.showErrorAlert("Mật khẩu cũ không chính xác!",
            // AppMessages.DIALOG_TITLE);
            // return;
            // }
            //
            // // ===== BƯỚC 5: CẬP NHẬT MẬT KHẨU =====
            // account.setPassword(newPassword);
            // int updateResult = AccountBUS.getInstance().update(account);
            //
            // if (updateResult > 0) {
            // NotificationUtils.showInfoAlert("Đổi mật khẩu thành công!",
            // AppMessages.DIALOG_TITLE);
            // handleClear(); // Xóa form sau khi thành công
            // log.info("Đổi mật khẩu thành công cho nhân viên ID: {}", currentEmployeeId);
            // } else {
            // NotificationUtils.showErrorAlert("Đổi mật khẩu thất bại. Vui lòng thử lại.",
            // AppMessages.DIALOG_TITLE);
            // log.warn("Đổi mật khẩu thất bại cho nhân viên ID: {}", currentEmployeeId);
            // }
        } catch (Exception e) {
            log.error("Lỗi khi đổi mật khẩu", e);
            NotificationUtils.showErrorAlert("Có lỗi xảy ra khi đổi mật khẩu. Vui lòng thử lại.",
                    AppMessages.DIALOG_TITLE);
        }
    }

    /**
     * KIỂM TRA DỮ LIỆU ĐẦU VÀO CHO FORM ĐỔI MẬT KHẨU
     * Các điều kiện cần kiểm tra:
     * - Mật khẩu cũ không được để trống
     * - Mật khẩu mới không được để trống
     * - Xác nhận mật khẩu không được để trống
     * - Mật khẩu mới và xác nhận phải giống nhau
     * - Mật khẩu mới phải khác mật khẩu cũ
     */
    private boolean validatePasswordInput() {
        String oldPassword = txtOldPassword.getText().trim();
        String newPassword = txtNewPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();

        // Kiểm tra: Mật khẩu cũ không được để trống
        if (oldPassword.isEmpty()) {
            NotificationUtils.showErrorAlert("Vui lòng nhập mật khẩu cũ", AppMessages.DIALOG_TITLE);
            txtOldPassword.requestFocus();
            return false;
        }

        // Kiểm tra: Mật khẩu mới không được để trống
        if (newPassword.isEmpty()) {
            NotificationUtils.showErrorAlert("Vui lòng nhập mật khẩu mới", AppMessages.DIALOG_TITLE);
            txtNewPassword.requestFocus();
            return false;
        }

        // Kiểm tra: Xác nhận mật khẩu không được để trống
        if (confirmPassword.isEmpty()) {
            NotificationUtils.showErrorAlert("Vui lòng xác nhận mật khẩu mới", AppMessages.DIALOG_TITLE);
            txtConfirmPassword.requestFocus();
            return false;
        }

        // Kiểm tra: Mật khẩu mới và xác nhận phải giống nhau
        if (!newPassword.equals(confirmPassword)) {
            NotificationUtils.showErrorAlert("Mật khẩu mới và xác nhận không trùng khớp!", AppMessages.DIALOG_TITLE);
            txtConfirmPassword.requestFocus();
            return false;
        }

        // Kiểm tra: Mật khẩu mới phải khác mật khẩu cũ
        if (oldPassword.equals(newPassword)) {
            NotificationUtils.showErrorAlert("Mật khẩu mới phải khác mật khẩu cũ!", AppMessages.DIALOG_TITLE);
            txtNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * PHẦN 3: XỬ LÝ XÓA FORM
     * Xóa tất cả dữ liệu trong form đổi mật khẩu
     */
    private void handleClear() {
        txtOldPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
        txtOldPassword.requestFocus();
        log.info("Xóa form đổi mật khẩu");
    }

    /**
     * PHẦN 4: XỬ LÝ CẬP NHẬT THÔNG TIN NHÂN VIÊN
     * Cập nhật thông tin cơ bản của nhân viên (tên, điện thoại, email, etc.)
     */
    private void handleUpdateInfo() {
        try {
            SessionManagerService session = SessionManagerService.getInstance();
            EmployeeBUS employeeBUS = EmployeeBUS.getInstance();

            EmployeeDTO employee = session.currEmployee();
            if (employee == null) {
                NotificationUtils.showErrorAlert("Không tìm thấy thông tin nhân viên", AppMessages.DIALOG_TITLE);
                return;
            }

            // Cập nhật các trường từ UI
            employee.setFirstName(lblFirstName.getText().trim());
            employee.setLastName(lblLastName.getText().trim());
            employee.setPhone(lblPhone.getText().trim());
            employee.setEmail(lblEmail.getText().trim());
            employee.setHealthInsCode(lblHealthInsCode.getText().trim());

            // Cập nhật các quyền lợi/trợ cấp

            int updateResult = employeeBUS.update(employee,
                    session.employeeRoleId(),
                    session.employeeLoginId());

            if (updateResult == 1) {
                NotificationUtils.showInfoAlert("Cập nhật thông tin nhân viên thành công!", AppMessages.DIALOG_TITLE);
                loadEmployeeInfo(); // Refresh UI
            } else {
                NotificationUtils.showErrorAlert("Cập nhật thông tin thất bại. Vui lòng thử lại.",
                        AppMessages.DIALOG_TITLE);
            }
        } catch (Exception e) {
            NotificationUtils.showErrorAlert("Lỗi khi cập nhật thông tin nhân viên: " + e.getMessage(),
                    AppMessages.DIALOG_TITLE);
            log.error("Error updating employee info", e);
        }
    }
}
