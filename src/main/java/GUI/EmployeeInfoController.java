package GUI;

import BUS.EmployeeBUS;
import BUS.AccountBUS;
import BUS.DepartmentBUS;
import BUS.RoleBUS;
import BUS.StatusBUS;
import DTO.EmployeeDTO;
import DTO.AccountDTO;
import DTO.BUSResult;
import DTO.DepartmentDTO;
import DTO.EmployeeDetailDTO;
import ENUM.*;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;
import SERVICE.SecureExecutor;
import SERVICE.SessionManagerService;
import PROVIDER.EmployeeViewProvider;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

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
    private DatePicker dpDateOfBirth; // Ngày sinh
    @FXML
    private TextField lblPhone; // Điện thoại
    @FXML
    private TextField lblEmail; // Email
    @FXML
    private TextField lblHealthInsCode; // Mã BHYT
    // Role + Department Section
    @FXML
    private TextField lblBaseSalary; // Lương cơ bản
    @FXML
    private TextField lblSalaryCoefficient; // Hệ số lương --- IGNORE ---\
    @FXML
    private TextField lblNumDependents; // Tên tài khoản --- IGNORE ---
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
    private VBox vboxPersonalInfo; // Container thông tin cá nhân

    // ==================== CACHED DATA ====================
    private EmployeeDetailDTO cachedEmployeeDetail; // Cache employee detail để tránh load lại

    // ==================== BUS INSTANCES ====================
    // Gán một lần trong initialize() để tránh gọi getInstance() nhiều lần
    private EmployeeBUS employeeBUS;
    private AccountBUS accountBUS;
    private DepartmentBUS departmentBUS;
    private RoleBUS roleBUS;
    private StatusBUS statusBUS;
    public SessionManagerService sessionManagerService;

    @FXML
    public void initialize() {
        // Khởi tạo BUS instances một lần
        employeeBUS = EmployeeBUS.getInstance();
        accountBUS = AccountBUS.getInstance();
        departmentBUS = DepartmentBUS.getInstance();
        roleBUS = RoleBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        sessionManagerService = SessionManagerService.getInstance();
        // Load local data nếu cần
        if (employeeBUS.isLocalEmpty())
            employeeBUS.loadLocal();
        if (accountBUS.isLocalEmpty())
            accountBUS.loadLocal();
        if (departmentBUS.isLocalEmpty())
            departmentBUS.loadLocal();
        if (roleBUS.isLocalEmpty())
            roleBUS.loadLocal();
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
     * Sử dụng cache để tránh load lại nhiều lần
     */
    private void loadEmployeeInfo() {
        try {
            EmployeeViewProvider provider = EmployeeViewProvider.getInstance();
            EmployeeDTO employee = employeeBUS.getByIdLocal(sessionManagerService.employeeLoginId());

            if (employee == null) {
                hidePersonalInfo();
                NotificationUtils.showErrorAlert("Không tìm thấy thông tin nhân viên", AppMessages.DIALOG_TITLE);
                return;
            }

            // Nếu là IT Admin hệ thống -> ẩn hồ sơ cá nhân
            if (employee.getRoleId() != -1 && employee.getRoleId() == 1) {
                hidePersonalInfo();
                return;
            }

            cachedEmployeeDetail = provider.getDetailById(employee.getId());

            if (cachedEmployeeDetail != null) {
                displayEmployeeInfo();
            } else {
                NotificationUtils.showErrorAlert("Không thể tải thông tin chi tiết nhân viên",
                        AppMessages.DIALOG_TITLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtils.showErrorAlert("Lỗi khi tải thông tin nhân viên", AppMessages.DIALOG_TITLE);
        }
    }

    private void hidePersonalInfo() {
        vboxPersonalInfo.setVisible(false);
        vboxPersonalInfo.setManaged(false);
    }

    /**
     * Hiển thị thông tin nhân viên từ cached data lên UI
     */
    private void displayEmployeeInfo() {
        if (cachedEmployeeDetail == null) {
            return;
        }

        try {
            EmployeeDTO employee = employeeBUS.getByIdLocal(sessionManagerService.employeeLoginId());
            ValidationUtils validationUtils = ValidationUtils.getInstance();

            // === PROFILE INFO SECTION ===
            lblEmployeeId.setText(String.valueOf(cachedEmployeeDetail.getEmployeeId()));
            lblGender.setText(cachedEmployeeDetail.getGender() != null ? cachedEmployeeDetail.getGender() : "");

            // Get department name
            if (employee != null && employee.getDepartmentId() != null) {
                DepartmentDTO department = departmentBUS.getByIdLocal(employee.getDepartmentId());
                lblDepartmentName.setText(department != null ? department.getName() : "");
            } else {
                lblDepartmentName.setText("");
            }

            lblRoleName.setText(cachedEmployeeDetail.getRoleName() != null ? cachedEmployeeDetail.getRoleName() : "");
            lblStatus.setText(
                    cachedEmployeeDetail.getStatusDescription() != null ? cachedEmployeeDetail.getStatusDescription()
                            : "");

            // === CONTACT INFO SECTION ===
            lblFirstName
                    .setText(cachedEmployeeDetail.getFirstName() != null ? cachedEmployeeDetail.getFirstName() : "");
            lblLastName.setText(cachedEmployeeDetail.getLastName() != null ? cachedEmployeeDetail.getLastName() : "");
            dpDateOfBirth.setValue(employee != null ? employee.getDateOfBirth() : LocalDate.now());
            lblPhone.setText(cachedEmployeeDetail.getPhone() != null ? cachedEmployeeDetail.getPhone() : "");
            lblEmail.setText(cachedEmployeeDetail.getEmail() != null ? cachedEmployeeDetail.getEmail() : "");
            lblHealthInsCode.setText(
                    employee != null && employee.getHealthInsCode() != null ? employee.getHealthInsCode() : "");

            // === Salary + Tax SECTION ===
            lblBaseSalary.setText(cachedEmployeeDetail.getBaseSalary() != null
                    ? validationUtils.formatCurrency(cachedEmployeeDetail.getBaseSalary())
                    : "");
            lblSalaryCoefficient.setText(cachedEmployeeDetail.getSalaryCoefficient() != null
                    ? String.valueOf(cachedEmployeeDetail.getSalaryCoefficient())
                    : "");
            lblNumDependents.setText(cachedEmployeeDetail.getNumDependents() != null
                    ? String.valueOf(cachedEmployeeDetail.getNumDependents())
                    : "");

            // === BENEFITS SECTION ===
            if (employee != null) {
                cbHealthIns.setSelected(employee.isHealthInsurance());
                cbSocialIns.setSelected(employee.isSocialInsurance());
                cbUnemploymentIns.setSelected(employee.isUnemploymentInsurance());
                cbIncomeTax.setSelected(employee.isPersonalIncomeTax());
                cbTransportSupport.setSelected(employee.isTransportationSupport());
                cbAccommSupport.setSelected(employee.isAccommodationSupport());

                lblCreatedAt.setText(validationUtils.formatDateTimeWithHour(employee.getCreatedAt()));
                lblUpdatedAt.setText(validationUtils.formatDateTimeWithHour(employee.getUpdatedAt()));
            }
            // === Account ===
            lblUsername.setText(cachedEmployeeDetail.getUsername() != null ? cachedEmployeeDetail.getUsername() : "");
        } catch (Exception e) {
            log.error("Error displaying employee info", e);
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

        AccountDTO account = new AccountDTO(accountBUS.getByIdLocal(sessionManagerService.employeeLoginId()));
        account.setPassword(txtNewPassword.getText().trim());
        BUSResult updateResult = SecureExecutor
                .executePublicBUSResult(
                        () -> accountBUS.changePasswordBySelf(account, txtOldPassword.getText().trim()));

        if (updateResult.isSuccess()) {
            NotificationUtils.showInfoAlert(AppMessages.ACCOUNT_PASSWORD_CHANGE_SUCCESS,
                    AppMessages.DIALOG_TITLE);
            handleClear(); // Xóa form sau khi đổi thành công
        } else {
            NotificationUtils.showErrorAlert(updateResult.getMessage(), AppMessages.DIALOG_TITLE);
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
        boolean isValid = true;
        String oldPassword = txtOldPassword.getText().trim();
        String newPassword = txtNewPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();

        ValidationUtils validator = ValidationUtils.getInstance();

        // 1. Kiểm tra mật khẩu cũ
        if (oldPassword.isEmpty()) {
            NotificationUtils.showErrorAlert("Vui lòng nhập mật khẩu hiện tại.", "Thông báo");
            clearAndFocus(txtOldPassword);
            isValid = false;
        }

        // 2. Kiểm tra mật khẩu mới (Bắt buộc & Định dạng)
        if (isValid && newPassword.isEmpty()) {
            NotificationUtils.showErrorAlert("Mật khẩu mới không được để trống.", "Thông báo");
            clearAndFocus(txtNewPassword);
            isValid = false;
        } else if (isValid && !validator.validatePassword(newPassword, 6, 255)) {
            NotificationUtils.showErrorAlert("Mật khẩu mới không hợp lệ (tối thiểu 6 ký tự).", "Thông báo");
            clearAndFocus(txtNewPassword);
            isValid = false;
        }

        // 3. Kiểm tra xác nhận mật khẩu
        if (isValid && confirmPassword.isEmpty()) {
            NotificationUtils.showErrorAlert("Vui lòng xác nhận mật khẩu mới.", "Thông báo");
            clearAndFocus(txtConfirmPassword);
            isValid = false;
        } else if (isValid && !confirmPassword.equals(newPassword)) {
            NotificationUtils.showErrorAlert("Xác nhận mật khẩu không trùng khớp.", "Thông báo");
            clearAndFocus(txtConfirmPassword);
            isValid = false;
        }

        // 4. Kiểm tra logic nghiệp vụ: Mới phải khác Cũ
        if (isValid && newPassword.equals(oldPassword)) {
            NotificationUtils.showErrorAlert("Mật khẩu mới phải khác mật khẩu cũ.", "Thông báo");
            clearAndFocus(txtNewPassword);
            isValid = false;
        }

        return isValid;
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
    }

    /**
     * PHẦN 4: XỬ LÝ CẬP NHẬT THÔNG TIN NHÂN VIÊN
     * Cập nhật thông tin cơ bản của nhân viên (tên, điện thoại, email, etc.)
     */
    private void handleUpdateInfo() {
        // Validate input trước
        String validationError = validateUpdateInfoFields();
        if (validationError != null) {
            NotificationUtils.showErrorAlert(validationError, AppMessages.DIALOG_TITLE);
            return;
        }

        EmployeeDTO employee = new EmployeeDTO(sessionManagerService.currEmployee());

        // Cập nhật các trường từ UI
        employee.setFirstName(lblFirstName.getText().trim());
        employee.setLastName(lblLastName.getText().trim());
        employee.setDateOfBirth(dpDateOfBirth.getValue());
        employee.setPhone(lblPhone.getText().trim());
        employee.setEmail(lblEmail.getText().trim());
        employee.setGender(lblGender.getText().trim());

        BUSResult updateResult = SecureExecutor
                .executePublicBUSResult(() -> employeeBUS.updatePersonalInfoBySelf(employee));

        if (updateResult.isSuccess()) {
            NotificationUtils.showInfoAlert(updateResult.getMessage(), AppMessages.DIALOG_TITLE);
            sessionManagerService.updateCurrentEmployee();
            loadEmployeeInfo(); // Refresh UI
        } else {
            NotificationUtils.showErrorAlert(updateResult.getMessage(), AppMessages.DIALOG_TITLE);
        }
    }

    /**
     * VALIDATE TỪNG FIELD CỦA FORM CẬP NHẬT THÔNG TIN
     * Kiểm tra và trả về thông báo lỗi cụ thể cho từng field
     * 
     * @return null nếu hợp lệ, message cụ thể nếu lỗi
     */
    private String validateUpdateInfoFields() {
        ValidationUtils validator = ValidationUtils.getInstance();

        // 1. Kiểm tra Họ đệm
        String firstName = lblFirstName.getText().trim();
        if (firstName.isEmpty()) {
            focus(lblFirstName);
            return "Họ đệm không được để trống.";
        }
        if (!validator.validateVietnameseText100(firstName)) {
            focus(lblFirstName);
            return "Họ đệm chỉ chứa chữ cái và không quá 100 ký tự.";
        }

        // 2. Kiểm tra Tên
        String lastName = lblLastName.getText().trim();
        if (lastName.isEmpty()) {
            focus(lblLastName);
            return "Tên không được để trống.";
        }
        if (!validator.validateVietnameseText100(lastName)) {
            focus(lblLastName);
            return "Tên chỉ chứa chữ cái và không quá 100 ký tự.";
        }

        // 3. Kiểm tra Ngày sinh
        if (dpDateOfBirth.getValue() == null) {
            dpDateOfBirth.requestFocus();
            return "Ngày sinh không được để trống.";
        }
        if (!validator.validateDateOfBirth(dpDateOfBirth.getValue())) {
            dpDateOfBirth.requestFocus();
            return "Ngày sinh không hợp lệ hoặc quá nhỏ (tối thiểu 18 tuổi).";
        }

        // 4. Kiểm tra Điện thoại
        String phone = lblPhone.getText().trim();
        if (phone.isEmpty()) {
            focus(lblBaseSalary);
            return "Số điện thoại không được để trống.";
        }
        if (!phone.isEmpty() && !validator.validateVietnamesePhoneNumber(phone)) {
            focus(lblPhone);
            return "Số điện thoại không hợp lệ (VD: 0912345678).";
        }

        // 5. Kiểm tra Email
        String email = lblEmail.getText().trim();
        if (email.isEmpty()) {
            focus(lblEmail);
            return "Email không được để trống.";
        }
        if (!email.isEmpty() && !validator.validateEmail(email)) {
            focus(lblEmail);
            return "Email không hợp lệ (VD: user@example.com).";
        }

        // 6. Kiểm tra Giới tính
        String gender = lblGender.getText().trim();
        if (gender.isEmpty()) {
            focus(lblGender);
            return "Giới tính không được để trống.";
        }
        if (!gender.equals("Nam") && !gender.equals("Nữ") && !gender.equals("Khác")) {
            focus(lblGender);
            return "Giới tính không hợp lệ (Nam, Nữ, hoặc Khác).";
        }

        return null; // Hợp lệ
    }

    private void clearAndFocus(TextField textField) {
        textField.clear();
        textField.requestFocus();
    }

    private void focus(TextField textField) {
        textField.requestFocus();
    }
}
