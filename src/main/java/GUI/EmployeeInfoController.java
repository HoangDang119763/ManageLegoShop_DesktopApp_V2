package GUI;

import BUS.EmployeeBUS;
import BUS.EmploymentHistoryBUS;
import BUS.AccountBUS;
import BUS.DepartmentBUS;
import BUS.RoleBUS;
import BUS.StatusBUS;
import DTO.EmployeeDTO;
import DTO.EmployeeAccountInfoDTO;
import DTO.EmployeeJobHistoryBundle;
import DTO.EmployeeJobInfoDTO;
import DTO.EmployeePayrollInfoDTO;
import DTO.EmployeePersonalInfoBundle;
import DTO.EmployeePersonalInfoDTO;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;

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
    @FXML
    private Tab tabPersonalInfo; // Tab Hồ sơ nhân viên
    @FXML
    private Tab tabJobHistory; // Tab Lương & Công tác
    @FXML
    private Tab tabAccountSecurity; // Tab Bảo mật tài khoản

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
    private TextField txtSocialInsCode; // Mã BHXH
    @FXML
    private TextField txtUnemploymentInsCode; // Mã BHTN
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
    private ImageView imgAvatar; // Ảnh đại diện nhân viên
    @FXML
    private Label lblLastLogin;
    @FXML
    private StackPane loadingOverlay;

    // ==================== TAB CONTROLLERS ====================
    @FXML
    private AllowanceTabController allowanceTabController;
    @FXML
    private DeductionTabController deductionTabController;
    @FXML
    private PayrollTabController payrollTabController;
    @FXML
    private LeaveRequestTabController leaveRequestTabController;
    @FXML
    private AttendanceTabController attendanceTabController;

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
    // Gán một lần trong initialize() để tránh gọi getInstance() nhiều lần
    private EmployeeBUS employeeBUS;
    private AccountBUS accountBUS;
    private DepartmentBUS departmentBUS;
    private RoleBUS roleBUS;
    private StatusBUS statusBUS;
    public SessionManagerService sessionManagerService;

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

        // Set avatar ImageView properties
        imgAvatar.setPreserveRatio(false);

        setupListeners();
        setupTabLoadingListeners();

        if (sessionManagerService.employeeRoleId() == 1) {
            hideInfo();
            loadTabAccountSecurity();
        } else {
            loadTabPersonalInfo();
        }
    }

    /**
     * Thiết lập listener cho tab selection để lazy load dữ liệu
     * Mỗi tab chỉ tải dữ liệu khi người dùng click vào tab
     */
    private void setupTabLoadingListeners() {
        tabPaneInfo.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null)
                return;

            // Tab 1: Hồ sơ nhân viên (Personal Info)
            if (newTab == tabPersonalInfo) {
                loadTabPersonalInfo();
            }

            // Tab 2: Lương & Công tác (Job History)
            else if (newTab == tabJobHistory) {
                loadTabJobSalaryInfo();
            }

            // Tab 3: Bảo mật tài khoản (Account Security)
            else if (newTab == tabAccountSecurity) {
                loadTabAccountSecurity();
            }
        });
    }

    /**
     * Load dữ liệu cho Tab 1: Hồ sơ nhân viên (Async)
     */
    private void loadTabPersonalInfo() {
        int empId = sessionManagerService.employeeLoginId();

        TaskUtil.executePublic(
                loadingOverlay,
                () -> employeeBUS.getPersonalInfoComplete(empId),
                bundleResult -> {
                    EmployeePersonalInfoBundle bundle = bundleResult.getData();
                    EmployeePersonalInfoDTO personalInfo = bundle.getPersonalInfo();
                    EmployeeJobInfoDTO jobInfo = bundle.getJobInfo();
                    EmployeePayrollInfoDTO payrollInfo = bundle.getPayrollInfo();

                    // Nếu là IT Admin hệ thống -> ẩn hồ sơ cá nhân
                    if (jobInfo.getRoleId() != null && jobInfo.getRoleId() == 1) {
                        hideInfo();
                        return;
                    }
                    displayPersonalInfo(personalInfo, jobInfo, payrollInfo);
                });
    }

    /**
     * Load dữ liệu cho Tab 2: Thông tin lương & công tác (Async)
     * Luôn lấy dữ liệu mới từ DB để đảm bảo dữ liệu không bị cũ
     */
    private void loadTabJobSalaryInfo() {
        int empId = sessionManagerService.employeeLoginId();

        TaskUtil.executePublic(
                loadingOverlay,
                () -> employeeBUS.getJobAndPayrollInfo(empId),
                bundleResult -> {
                    EmployeeJobHistoryBundle bundle = bundleResult.getData();
                    EmployeeJobInfoDTO jobInfo = bundle.getJobInfo();
                    EmployeePayrollInfoDTO payrollInfo = bundle.getPayrollInfo();

                    setupTableColumns();
                    displayJobAndSalaryInfo(jobInfo, payrollInfo);
                });
    }

    /**
     * Overload: Load dữ liệu lịch sử công tác với phân trang (Async)
     * Gọi từ pagination callback khi người dùng chuyển trang
     */
    private void loadTabJobHistory(int pageIndex) {
        loadHistoryData(pageIndex);
    }

    /**
     * Load dữ liệu cho Tab 3: Bảo mật tài khoản (Async)
     * Luôn lấy dữ liệu mới từ DB để đảm bảo dữ liệu không bị cũ
     */
    private void loadTabAccountSecurity() {
        int empId = sessionManagerService.employeeLoginId();

        TaskUtil.executePublic(
                loadingOverlay,
                () -> employeeBUS.getAccountInfo(empId),
                accountInfoResult -> {
                    EmployeeAccountInfoDTO accountInfo = accountInfoResult.getData();
                    displayAccountSecurityInfo(accountInfo);
                });
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
     * Ẩn thông tin nhân viên khỏi UI
     */
    private void hideInfo() {
        tabPaneInfo.getTabs().remove(tabPersonalInfo);
        tabPaneInfo.getTabs().remove(tabJobHistory);
    }

    // Tab 1: Hiển thị thông tin nhân viên
    private void displayPersonalInfo(EmployeePersonalInfoDTO personalInfo, EmployeeJobInfoDTO jobInfo,
            EmployeePayrollInfoDTO payrollInfo) {
        if (personalInfo == null)
            return;

        // Profile Info
        lblEmployeeId.setText(String.valueOf(personalInfo.getEmployeeId()));
        lblGender.setText(personalInfo.getGender() != null ? personalInfo.getGender() : "");
        lblDepartmentName.setText(jobInfo != null ? jobInfo.getDepartmentName() : "");
        lblRoleName.setText(jobInfo.getRoleName() != null ? jobInfo.getRoleName() : "");
        lblStatus.setText(jobInfo.getStatusDescription() != null ? jobInfo.getStatusDescription() : "");
        lblHealthInsCode.setText(payrollInfo != null ? payrollInfo.getHealthInsCode() : "");

        // Contact Info
        lblFirstName.setText(personalInfo.getFirstName() != null ? personalInfo.getFirstName() : "");
        lblLastName.setText(personalInfo.getLastName() != null ? personalInfo.getLastName() : "");
        dpDateOfBirth.setValue(
                personalInfo.getDateOfBirth() != null ? personalInfo.getDateOfBirth() : null);
        lblPhone.setText(personalInfo.getPhone() != null ? personalInfo.getPhone() : "");
        lblEmail.setText(personalInfo.getEmail() != null ? personalInfo.getEmail() : "");

        // Avatar
        loadEmployeeAvatar(personalInfo.getAvatarUrl());

        // Benefits (TextFields & CheckBoxes)
        if (payrollInfo != null) {
            txtSocialInsCode.setText(payrollInfo.getSocialInsCode() != null ? payrollInfo.getSocialInsCode() : "");
            txtUnemploymentInsCode
                    .setText(payrollInfo.getUnemploymentInsCode() != null ? payrollInfo.getUnemploymentInsCode() : "");
            cbIncomeTax.setSelected(payrollInfo.isPersonalIncomeTax());
            cbTransportSupport.setSelected(payrollInfo.isTransportationSupport());
            cbAccommSupport.setSelected(payrollInfo.isAccommodationSupport());
        }

        // Metadata
        lblCreatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(personalInfo.getCreatedAt()));
        lblUpdatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(personalInfo.getUpdatedAt()));
    }

    private void displayJobAndSalaryInfo(EmployeeJobInfoDTO jobInfo, EmployeePayrollInfoDTO payrollInfo) {
        if (jobInfo == null)
            return;

        ValidationUtils vu = ValidationUtils.getInstance();

        // Salary Info
        lblBaseSalary.setText(jobInfo.getBaseSalary() != null
                ? vu.formatCurrency(jobInfo.getBaseSalary())
                : "");

        lblSalaryCoefficient.setText(jobInfo.getSalaryCoefficient() != null
                ? String.valueOf(jobInfo.getSalaryCoefficient())
                : "");

        lblNumDependents.setText(payrollInfo != null && payrollInfo.getNumDependents() != null
                ? String.valueOf(payrollInfo.getNumDependents())
                : "0");

        setupHistoryPagination();
    }

    private void displayAccountSecurityInfo(EmployeeAccountInfoDTO accountInfo) {
        if (accountInfo == null) {
            lblUsername.setText("");
            return;
        }

        lblUsername.setText(accountInfo.getUsername() != null ? accountInfo.getUsername() : "");

        // Nếu DTO Account có trường lastLogin, hiển thị tại đây
        lblLastLogin.setText(ValidationUtils.getInstance().formatDateTimeWithHour(accountInfo.getLastLogin()));
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
                    loadTabPersonalInfo(); // Reload personal info tab
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
        // Gọi overloaded method loadTabJobHistory(pageIndex) khi người dùng chuyển
        // trang
        historyPaginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadTabJobHistory(pageIndex);
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

    // ==================== 🖼️ AVATAR LOADER ====================
    /**
     * Load và hiển thị ảnh đại diện của nhân viên
     * Tham khảo logic từ ProductModalController
     */
    private void loadEmployeeAvatar(String avatarUrl) {
        File imageFile = null;
        Image image = null;

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            imageFile = new File(avatarUrl);
        }

        if (imageFile != null && imageFile.exists()) {
            image = new Image(imageFile.toURI().toString());
        } else {
            URL resource = getClass().getResource("/images/default/default.png");
            if (resource != null) {
                image = new Image(resource.toExternalForm());
            } else {
                System.err.println("Resource not found: /images/default/default.png");
            }
        }

        if (image != null && imgAvatar != null) {
            imgAvatar.setImage(image);
            // Force fill ImageView bằng cách reload properties
            imgAvatar.setPreserveRatio(false);
        }
    }

}
