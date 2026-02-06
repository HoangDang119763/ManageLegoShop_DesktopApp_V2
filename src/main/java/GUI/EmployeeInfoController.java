package GUI;

import BUS.EmployeeBUS;
import BUS.AccountBUS;
import DTO.EmployeeDTO;
import DTO.AccountDTO;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import SERVICE.SessionManagerService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    @FXML
    private TextField lblEmployeeId; // Mã nhân viên (readonly)
    @FXML
    private TextField lblFirstName; // Họ đệm (readonly)
    @FXML
    private TextField lblLastName; // Tên (readonly)
    @FXML
    private TextField lblDateOfBirth; // Ngày sinh (readonly)
    @FXML
    private TextField lblRoleName; // Vai trò (readonly)
    @FXML
    private TextField lblSalary; // Lương cơ bản (readonly)
    @FXML
    private TextField lblStatus; // Trạng thái (readonly)
    @FXML
    private TextField lblUsername; // Tên tài khoản (readonly)

    // ==================== PHẦN ĐỔI MẬT KHẨU (Right Panel) ====================
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
        if (AccountBUS.getInstance().isLocalEmpty())
            AccountBUS.getInstance().loadLocal();
        if (EmployeeBUS.getInstance().isLocalEmpty())
            EmployeeBUS.getInstance().loadLocal();
        setupListeners();
        loadEmployeeInfo();
    }

    /**
     * Thiết lập sự kiện cho các nút
     */
    private void setupListeners() {
        btnChangePassword.setOnAction(e -> handleChangePassword());
        btnClear.setOnAction(e -> handleClear());
    }

    /**
     * PHẦN 1: TẢI THÔNG TIN NHÂN VIÊN
     * Lấy thông tin nhân viên hiện tại từ session và hiển thị
     */
    private void loadEmployeeInfo() {
        try {

            // Lấy thông tin nhân viên từ BUS
            EmployeeDTO employee = SessionManagerService.getInstance().currEmployee();

            if (employee != null) {
                // Hiển thị thông tin nhân viên
                lblEmployeeId.setText(String.valueOf(employee.getId()));
                lblFirstName.setText(employee.getFirstName() != null ? employee.getFirstName() : "");
                lblLastName.setText(employee.getLastName() != null ? employee.getLastName() : "");
                lblDateOfBirth.setText(employee.getDateOfBirth() != null ? employee.getDateOfBirth().toString() : "");
                // lblRoleName.setText(employee.getRoleName() != null ? employee.getRoleName() :
                // "");
                // lblSalary.setText(String.valueOf(employee.getSalary()));
                lblStatus.setText(employee.isStatus() ? "Hoạt động" : "Không hoạt động");

                // Lấy tên tài khoản từ AccountBUS
                AccountDTO account = AccountBUS.getInstance().getByIdLocal(employee.getAccountId());
                if (account != null) {
                    lblUsername.setText(account.getUsername());
                }
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
//            // ===== BƯỚC 2: LẤY ID NHÂN VIÊN HIỆN TẠI =====
//            int currentEmployeeId = SessionManagerService.getInstance().getCurrentEmployeeId();
//
//            // ===== BƯỚC 3: LẤY TÀI KHOẢN CỦA NHÂN VIÊN =====
//            AccountDTO account = AccountBUS.getInstance().getByEmployeeId(currentEmployeeId);
//            if (account == null) {
//                NotificationUtils.showErrorAlert("Không tìm thấy tài khoản", AppMessages.DIALOG_TITLE);
//                return;
//            }
//
//            // ===== BƯỚC 4: KIỂM TRA MẬT KHẨU CŨ =====
//            String oldPassword = txtOldPassword.getText();
//            String newPassword = txtNewPassword.getText();
//
//            // Kiểm tra xem mật khẩu cũ có đúng không
//            if (!account.getPassword().equals(oldPassword)) {
//                NotificationUtils.showErrorAlert("Mật khẩu cũ không chính xác!", AppMessages.DIALOG_TITLE);
//                return;
//            }
//
//            // ===== BƯỚC 5: CẬP NHẬT MẬT KHẨU =====
//            account.setPassword(newPassword);
//            int updateResult = AccountBUS.getInstance().update(account);
//
//            if (updateResult > 0) {
//                NotificationUtils.showInfoAlert("Đổi mật khẩu thành công!", AppMessages.DIALOG_TITLE);
//                handleClear(); // Xóa form sau khi thành công
//                log.info("Đổi mật khẩu thành công cho nhân viên ID: {}", currentEmployeeId);
//            } else {
//                NotificationUtils.showErrorAlert("Đổi mật khẩu thất bại. Vui lòng thử lại.", AppMessages.DIALOG_TITLE);
//                log.warn("Đổi mật khẩu thất bại cho nhân viên ID: {}", currentEmployeeId);
//            }
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
}
