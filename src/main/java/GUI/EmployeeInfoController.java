package GUI;

import BUS.EmployeeBUS;
import BUS.EmploymentHistoryBUS;
import BUS.AccountBUS;
import BUS.DepartmentBUS;
import BUS.RoleBUS;
import BUS.StatusBUS;
import DTO.EmployeeDTO;
import DTO.DepartmentDTO;
import DTO.EmployeeDetailDTO;
import DTO.EmploymentHistoryDetailBasicDTO;
import DTO.PagedResponse;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.ValidationUtils;
import SERVICE.SessionManagerService;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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

    // ==================== TAB PANE ====================
    @FXML
    private TabPane tabPaneInfo; // Tab pane chính

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
    @FXML
    private StackPane loadingOverlay;

    // ==================== LƯƠNG & CÔNG TÁC TAB (Salary & Work History)
    // ====================
    @FXML
    private TableView<EmploymentHistoryDetailBasicDTO> tvHistory; // Bảng lịch sử điều chuyển
    @FXML
    private TableColumn<EmploymentHistoryDetailBasicDTO, String> colEffectiveDate; // Cột ngày áp dụng
    @FXML
    private TableColumn<EmploymentHistoryDetailBasicDTO, String> colDepartment; // Cột phòng ban
    @FXML
    private TableColumn<EmploymentHistoryDetailBasicDTO, String> colRole; // Cột chức vụ
    @FXML
    private TableColumn<EmploymentHistoryDetailBasicDTO, String> colCreatedAt; // Cột ngày tạo
    @FXML
    private PaginationController historyPaginationController;
    // ==================== BUS INSTANCES ====================
    // Gán một lần trong initialize() để tránh gọi getInstance() nhiều lần
    private EmployeeBUS employeeBUS;
    private AccountBUS accountBUS;
    private DepartmentBUS departmentBUS;
    private RoleBUS roleBUS;
    private StatusBUS statusBUS;
    public SessionManagerService sessionManagerService;
    public EmployeeDetailDTO CurrEmployeeDetail; // Cache
    private static final int PAGE_SIZE = 10; // Kích thước trang cho lịch sử công tác
    // ==================== 📍 LIFECYCLE & INITIALIZATION ====================

    @FXML
    public void initialize() {
        // Khởi tạo BUS instances một lần
        employeeBUS = EmployeeBUS.getInstance();
        accountBUS = AccountBUS.getInstance();
        departmentBUS = DepartmentBUS.getInstance();
        roleBUS = RoleBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        sessionManagerService = SessionManagerService.getInstance();
        CurrEmployeeDetail = employeeBUS.getDetailById(sessionManagerService.employeeLoginId());

        if (CurrEmployeeDetail == null) {
            NotificationUtils.showErrorAlert(AppMessages.EMPLOYEE_DETAIL_LOAD_ERROR, AppMessages.DIALOG_TITLE);
            return;
        }

        setupListeners();
        loadEmployeeInfo();
        setupTableColumns();
        setupHistoryPagination();
    }

    // ==================== 🎨 UI SETUP & DATA LOADING ====================
    private void setupTableColumns() {
        ValidationUtils vu = ValidationUtils.getInstance();

        // Cột Ngày áp dụng (LocalDate -> hiển thị formatted)
        colEffectiveDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                vu.formatDateTime(cellData.getValue().getEffectiveDate()) != null
                        ? vu.formatDateTime(cellData.getValue().getEffectiveDate())
                        : ""));

        // Cột Phòng ban
        colDepartment.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDepartmentName() != null
                        ? cellData.getValue().getDepartmentName()
                        : ""));

        // Cột Chức vụ
        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRoleName() != null
                ? cellData.getValue().getRoleName()
                : ""));

        // Cột Ngày tạo (LocalDateTime -> hiển thị formatted)
        colCreatedAt.setCellValueFactory(cellData -> new SimpleStringProperty(
                vu.formatDateTimeWithHour(cellData.getValue().getCreatedAt()) != null
                        ? vu.formatDateTimeWithHour(cellData.getValue().getCreatedAt())
                        : ""));
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
     * Tải thông tin nhân viên từ session và hiển thị
     * Sử dụng cache để tránh load lại nhiều lần
     */
    private void loadEmployeeInfo() {
        // Nếu là IT Admin hệ thống -> ẩn hồ sơ cá nhân
        if (CurrEmployeeDetail.getRoleId() != -1 && CurrEmployeeDetail.getRoleId() == 1) {
            hidePersonalInfo();
            return;
        }

        displayEmployeeInfo();
    }

    /**
     * Ẩn thông tin nhân viên khỏi UI
     */
    private void hidePersonalInfo() {
        vboxPersonalInfo.setVisible(false);
        vboxPersonalInfo.setManaged(false);
    }

    /**
     * Hiển thị thông tin nhân viên từ cached data lên UI
     */
    private void displayEmployeeInfo() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        if (CurrEmployeeDetail != null) {
            // === PROFILE INFO SECTION ===
            lblEmployeeId.setText(String.valueOf(CurrEmployeeDetail.getEmployeeId()));
            lblGender.setText(CurrEmployeeDetail.getGender() != null ? CurrEmployeeDetail.getGender() : "");

            // Get department name
            if (CurrEmployeeDetail != null && CurrEmployeeDetail.getDepartmentId() != null) {
                DepartmentDTO department = departmentBUS.getById(CurrEmployeeDetail.getDepartmentId());
                lblDepartmentName.setText(department != null ? department.getName() : "");
            } else {
                lblDepartmentName.setText("");
            }

            lblRoleName.setText(CurrEmployeeDetail.getRoleName() != null ? CurrEmployeeDetail.getRoleName() : "");
            lblStatus.setText(
                    CurrEmployeeDetail.getStatusDescription() != null ? CurrEmployeeDetail.getStatusDescription()
                            : "");

            // === CONTACT INFO SECTION ===
            lblFirstName
                    .setText(CurrEmployeeDetail.getFirstName() != null ? CurrEmployeeDetail.getFirstName() : "");
            lblLastName.setText(CurrEmployeeDetail.getLastName() != null ? CurrEmployeeDetail.getLastName() : "");
            dpDateOfBirth.setValue(CurrEmployeeDetail != null ? CurrEmployeeDetail.getDateOfBirth() : LocalDate.now());
            lblPhone.setText(CurrEmployeeDetail.getPhone() != null ? CurrEmployeeDetail.getPhone() : "");
            lblEmail.setText(CurrEmployeeDetail.getEmail() != null ? CurrEmployeeDetail.getEmail() : "");
            lblHealthInsCode.setText(
                    CurrEmployeeDetail != null && CurrEmployeeDetail.getHealthInsCode() != null
                            ? CurrEmployeeDetail.getHealthInsCode()
                            : "");

            // === Salary + Tax SECTION ===
            lblBaseSalary.setText(CurrEmployeeDetail.getBaseSalary() != null
                    ? validationUtils.formatCurrency(CurrEmployeeDetail.getBaseSalary())
                    : "");
            lblSalaryCoefficient.setText(CurrEmployeeDetail.getSalaryCoefficient() != null
                    ? String.valueOf(CurrEmployeeDetail.getSalaryCoefficient())
                    : "");
            lblNumDependents.setText(CurrEmployeeDetail.getNumDependents() != null
                    ? String.valueOf(CurrEmployeeDetail.getNumDependents())
                    : "");
            // === BENEFITS SECTION ===
            cbHealthIns.setSelected(CurrEmployeeDetail.isHealthInsurance());
            cbSocialIns.setSelected(CurrEmployeeDetail.isSocialInsurance());
            cbUnemploymentIns.setSelected(CurrEmployeeDetail.isUnemploymentInsurance());
            cbIncomeTax.setSelected(CurrEmployeeDetail.isPersonalIncomeTax());
            cbTransportSupport.setSelected(CurrEmployeeDetail.isTransportationSupport());
            cbAccommSupport.setSelected(CurrEmployeeDetail.isAccommodationSupport());

            lblCreatedAt.setText(validationUtils.formatDateTimeWithHour(CurrEmployeeDetail.getCreatedAt()));
            lblUpdatedAt.setText(validationUtils.formatDateTimeWithHour(CurrEmployeeDetail.getUpdatedAt()));
            // === Account ===
            lblUsername.setText(CurrEmployeeDetail.getUsername() != null ? CurrEmployeeDetail.getUsername() : "");
            loadHistoryData(0);
        } else {
            NotificationUtils.showErrorAlert(AppMessages.EMPLOYEE_DETAIL_LOAD_ERROR,
                    AppMessages.DIALOG_TITLE);
        }
    }

    // ==================== 👤 EMPLOYEE INFO HANDLERS ====================

    /**
     * Xử lý cập nhật thông tin cá nhân của nhân viên
     * Validate input trước khi gửi request cập nhật
     */
    private void handleUpdateInfo() {
        // Validate input trước
        String validationError = validateUpdateInfoFields();
        if (validationError != null) {
            NotificationUtils.showErrorAlert(validationError, AppMessages.DIALOG_TITLE);
            return;
        }

        EmployeeDTO employee = new EmployeeDTO();

        // Cập nhật các trường từ UI
        employee.setId(sessionManagerService.employeeLoginId());
        employee.setFirstName(lblFirstName.getText().trim());
        employee.setLastName(lblLastName.getText().trim());
        employee.setDateOfBirth(dpDateOfBirth.getValue());
        employee.setPhone(lblPhone.getText().trim());
        employee.setEmail(lblEmail.getText().trim());

        TaskUtil.executePublic(
                loadingOverlay,
                // 1. Chỉ truyền logic BUS thuần túy
                () -> EmployeeBUS.getInstance().updatePersonalInfoBySelf(employee),

                // 2. Xử lý khi thành công (Chạy trên UI Thread)
                result -> {
                    loadEmployeeInfo();
                    Stage stage = (Stage) btnUpdateInfo.getScene().getWindow();
                    NotificationUtils.showToast(
                            stage,
                            result.getMessage());

                });
    }

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

        return null; // Hợp lệ
    }

    // ==================== 🔐 PASSWORD CHANGE HANDLERS ====================

    private void handleChangePassword() {
        // ===== BƯỚC 1: KIỂM TRA DỮ LIỆU ĐẦU VÀO =====
        if (!validatePasswordInput()) {
            return; // Nếu validation thất bại, dừng lại
        }

        String username = lblUsername.getText();
        String oldPassword = txtOldPassword.getText().trim();
        String newPassword = txtNewPassword.getText().trim();
        TaskUtil.executePublic(
                loadingOverlay,
                // 1. Chỉ truyền logic BUS thuần túy
                () -> accountBUS.changePasswordBySelf(username, oldPassword, newPassword),

                // 2. Xử lý khi thành công (Chạy trên UI Thread)
                result -> {

                    SessionManagerService.getInstance().forceLogout(result.getMessage());
                });
    }

    /**
     * Validate dữ liệu đầu vào cho form đổi mật khẩu
     * Các điều kiện kiểm tra:
     * - Mật khẩu cũ không được để trống
     * - Mật khẩu mới không được để trống và hợp lệ
     * - Xác nhận mật khẩu không được để trống và khớp với mật khẩu mới
     * - Mật khẩu mới phải khác mật khẩu cũ
     * 
     * @return true nếu hợp lệ, false nếu lỗi
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
     * Xóa tất cả dữ liệu trong form đổi mật khẩu
     */
    private void handleClear() {
        txtOldPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
        txtOldPassword.requestFocus();
    }

    // ==================== 🛠️ UTILITY METHODS ====================

    /**
     * Xóa content của TextField và focus vào nó
     */
    private void clearAndFocus(TextField textField) {
        textField.clear();
        textField.requestFocus();
    }

    /**
     * Focus vào một TextField cụ thể
     */
    private void focus(TextField textField) {
        textField.requestFocus();
    }

    // ==================== 📋 HISTORY & PAGINATION ====================

    /**
     * Thiết lập pagination cho lịch sử công tác
     * Load dữ liệu từ BUS và hiển thị trong TableView
     */
    private void setupHistoryPagination() {
        // Init với pageSize = 10
        historyPaginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadHistoryData(pageIndex);
        });
    }

    /**
     * Load dữ liệu lịch sử công tác từ BUS với phân trang
     */
    private void loadHistoryData(int pageIndex) {
        int empId = sessionManagerService.employeeLoginId();

        TaskUtil.executePublic(loadingOverlay,
                () -> EmploymentHistoryBUS.getInstance().getDetailsByEmployeeIdPaged(empId, pageIndex, PAGE_SIZE),
                result -> {
                    // Ép kiểu trực tiếp từ Object về PagedResponse cụ thể
                    // Thêm (PagedResponse<EmploymentHistoryDetailBasicDTO>) ở phía trước
                    PagedResponse<EmploymentHistoryDetailBasicDTO> res = result.getPagedData();

                    // Đổ data vào TableView
                    tvHistory.setItems(javafx.collections.FXCollections.observableArrayList(res.getItems()));

                    // Cập nhật Pagination
                    int totalItems = res.getTotalItems();
                    int pageCount = (int) Math.ceil((double) totalItems / PAGE_SIZE);
                    historyPaginationController.setPageCount(pageCount > 0 ? pageCount : 1);
                });
    }
}
