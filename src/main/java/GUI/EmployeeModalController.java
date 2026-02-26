package GUI;

import BUS.AccountBUS;
import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.EmploymentHistoryBUS;
import BUS.RoleBUS;
import BUS.StatusBUS;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.EmployeePersonalInfoDTO;
import DTO.EmploymentHistoryDetailDTO;
import DTO.EmployeeAccountInfoDTO;
import DTO.EmployeeJobInfoDTO;
import DTO.EmployeePayrollInfoDTO;
import DTO.PagedResponse;
import DTO.RoleDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import ENUM.Gender;
import ENUM.Status;
import INTERFACE.IModalController;
import SERVICE.SessionManagerService;
import SERVICE.ImageService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Controller cho modal thêm/sửa/xem thông tin nhân viên
 * typeModal: 0 = Add, 1 = Edit, 2 = View (readonly)
 */
public class EmployeeModalController implements IModalController {
    // ==================== TOP SECTION ====================
    @FXML
    private Label modalName;

    // ==================== TAB PANE ====================
    @FXML
    private TabPane tabPaneEmployee;
    @FXML
    private Tab tabPersonal;
    @FXML
    private Tab tabAccount;
    @FXML
    private Tab tabJob;
    @FXML
    private Tab tabPayroll;

    // ==================== TAB 1: PERSONAL INFO ====================
    @FXML
    private TextField txtEmployeeId;
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private DatePicker dpDateOfBirth;
    @FXML
    private ComboBox<String> cbGender;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private Button savePersonalBtn;

    // ==================== TAB 1: AVATAR ====================
    @FXML
    private ImageView imgAvatar; // Ảnh đại diện nhân viên
    @FXML
    private Button choseImg; // Nút chọn ảnh
    @FXML
    private Button resetImgBtn; // Nút reset ảnh
    private String avatarUrl = null; // Đường dẫn ảnh tạm thời

    // ==================== TAB 2: ACCOUNT INFO ====================
    @FXML
    private TextField txtUsername;
    @FXML
    private ComboBox<StatusDTO> cbAccountStatus;
    @FXML
    private Label lblLastLogin;
    @FXML
    private Button btnResetPassword;
    @FXML
    private Button saveAccountBtn;
    @FXML
    private HBox detailPassword;

    // ==================== TAB 3: JOB INFO ====================
    @FXML
    private ComboBox<DepartmentDTO> cbDepartment;
    @FXML
    private ComboBox<RoleDTO> cbRole;
    @FXML
    private ComboBox<StatusDTO> cbStatus;
    @FXML
    private TextField txtBaseSalary;
    @FXML
    private TextField txtCoefficient;
    @FXML
    private VBox jobHistorySection;
    @FXML
    private HBox paginationSection;
    @FXML
    private TableView<EmploymentHistoryDetailDTO> tblJobHistory;
    @FXML
    private TableColumn<EmploymentHistoryDetailDTO, String> colEffectiveDate;
    @FXML
    private TableColumn<EmploymentHistoryDetailDTO, String> colDepartment;
    @FXML
    private TableColumn<EmploymentHistoryDetailDTO, String> colRole;
    @FXML
    private TableColumn<EmploymentHistoryDetailDTO, String> colReason;
    @FXML
    private TableColumn<EmploymentHistoryDetailDTO, String> colApprover;
    @FXML
    private TableColumn<EmploymentHistoryDetailDTO, String> colStatus;
    @FXML
    private TableColumn<EmploymentHistoryDetailDTO, String> colCreatedAt;
    @FXML
    private Button saveJobBtn;

    // ==================== TAB 4: PAYROLL & BENEFITS ====================
    @FXML
    private TextField txtNumDependents;
    @FXML
    private TextField txtHealthInsCode;
    @FXML
    private TextField txtSocialInsCode;
    @FXML
    private TextField txtUnemploymentInsCode;
    @FXML
    private CheckBox cbPersonalTax;
    @FXML
    private CheckBox cbTransportSupport;
    @FXML
    private CheckBox cbAccommodationSupport;
    @FXML
    private Button savePayrollBtn;

    // ==================== BOTTOM SECTION ====================
    @FXML
    private Label lblCreatedAt;
    @FXML
    private Label lblUpdatedAt;
    @FXML
    private Button closeBtn;
    @FXML
    private StackPane loadingOverlay;
    @FXML
    private PaginationController historyPaginationController;
    private static final int PAGE_SIZE = 10;

    @Getter
    private String resultMessage = "";
    private int typeModal; // 0=Add, 1=Edit, 2=View
    private EmployeeDTO employee;
    private int currentEmployeeId; // Store employee ID for lazy loading
    private RoleBUS roleBUS;
    private StatusBUS statusBUS;
    private DepartmentBUS departmentBUS;
    private EmployeeBUS employeeBUS;
    private SessionManagerService session;
    private EmploymentHistoryBUS employmentHistoryBUS;
    private ValidationUtils validationUtils;
    private AccountBUS accountBUS;
    // Permission flags per tab
    private boolean canViewPersonal;
    private boolean canUpdatePersonal;
    private boolean canViewAccount;
    private boolean canUpdateAccount;
    private boolean canResetPassword;
    private boolean canViewJob;
    private boolean canUpdateJob;
    private boolean canViewPayroll;
    private boolean canUpdatePayroll;

    @FXML
    public void initialize() {
        // Load BUS instances
        roleBUS = RoleBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        departmentBUS = DepartmentBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        session = SessionManagerService.getInstance();
        employmentHistoryBUS = EmploymentHistoryBUS.getInstance();
        validationUtils = ValidationUtils.getInstance();
        accountBUS = AccountBUS.getInstance();

        // Set avatar ImageView properties
        imgAvatar.setPreserveRatio(false);

        // Load initial data
        loadGenderComboBox();
        setupDepartmentComboBox();
        loadRoleComboBox();
        loadStatusComboBox();
        loadAccountStatusComboBox();
        setupPermissions();
        hideTabWithoutPermission();
        // Setup listeners
        setupListeners();
        setupHistoryPagination();
        setupTabLoadingListeners();
    }

    /**
     * Setup permission flags for each tab
     */
    private void setupPermissions() {
        canViewPersonal = session.hasPermission(PermissionKey.EMPLOYEE_PERSONAL_VIEW);
        canUpdatePersonal = session.hasPermission(PermissionKey.EMPLOYEE_PERSONAL_UPDATE);
        canViewAccount = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_VIEW);
        canUpdateAccount = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_UPDATE_STATUS);
        canResetPassword = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_RESET_PASSWORD);
        canViewJob = session.hasPermission(PermissionKey.EMPLOYEE_JOB_VIEW);
        canUpdateJob = session.hasPermission(PermissionKey.EMPLOYEE_JOB_UPDATE);
        canViewPayroll = session.hasPermission(PermissionKey.EMPLOYEE_PAYROLLINFO_VIEW);
        canUpdatePayroll = session.hasPermission(PermissionKey.EMPLOYEE_PAYROLLINFO_UPDATE);
    }

    /**
     * Thiết lập listener cho tab selection để lazy load dữ liệu
     * Mỗi tab chỉ tải dữ liệu khi người dùng click vào tab
     */
    private void setupTabLoadingListeners() {
        tabPaneEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == null || currentEmployeeId <= 0)
                return;

            // Tab 1: Thông tin cá nhân (Personal Info)
            if (newTab == tabPersonal) {
                loadTabPersonal();
            }

            // Tab 2: Tài khoản hệ thống (Account System)
            else if (newTab == tabAccount) {
                loadTabAccount();
            }

            // Tab 3: Đơn vị công tác (Job/Work Unit)
            else if (newTab == tabJob) {
                loadTabJob();
            }

            // Tab 4: Bảo hiểm & Phúc lợi (Payroll & Benefits)
            else if (newTab == tabPayroll) {
                loadTabPayroll();
            }
        });
    }

    /**
     * Ẩn các tab mà user không có quyền xem
     */
    private void hideTabWithoutPermission() {
        // Tab 1: Personal Info
        if (!canViewPersonal) {
            tabPaneEmployee.getTabs().remove(tabPersonal);
        }

        // Tab 2: Account System
        if (!canViewAccount) {
            tabPaneEmployee.getTabs().remove(tabAccount);
        }

        // Tab 3: Job/Work Unit
        if (!canViewJob) {
            tabPaneEmployee.getTabs().remove(tabJob);
        }

        // Tab 4: Payroll & Benefits
        if (!canViewPayroll) {
            tabPaneEmployee.getTabs().remove(tabPayroll);
        }
    }

    private void loadGenderComboBox() {
        for (Gender gender : Gender.values()) {
            cbGender.getItems().add(gender.getDisplayName());
        }
        cbGender.getSelectionModel().selectFirst();
    }

    /**
     * Setup Department ComboBox với formatting cho inactive items
     */
    private void setupDepartmentComboBox() {
        ArrayList<DepartmentDTO> departmentOptions = departmentBUS.getAll();
        cbDepartment.setItems(FXCollections.observableArrayList(departmentOptions));

        // Format inactive departments (màu xám, in nghiêng)
        int inactiveDeptId = statusBUS
                .getByTypeAndStatusName(StatusType.DEPARTMENT, Status.Department.INACTIVE).getId();

        UiUtils.gI().formatInactiveComboBox(
                cbDepartment,
                DepartmentDTO::getName,
                DepartmentDTO::getStatusId,
                inactiveDeptId);
    }

    /**
     * Attach warning listener cho Department khi chọn inactive item
     */
    private void attachDepartmentWarning(int initialDeptId) {
        int inactiveDeptId = statusBUS
                .getByTypeAndStatusName(StatusType.DEPARTMENT, Status.Department.INACTIVE).getId();

        UiUtils.gI().addSmartInactiveWarningListener(
                cbDepartment,
                DepartmentDTO::getId,
                DepartmentDTO::getStatusId,
                inactiveDeptId,
                initialDeptId,
                AppMessages.DEPARTMENT_DELETED_WARNING);
    }

    private void loadRoleComboBox() {
        ArrayList<RoleDTO> roles = RoleBUS.getInstance().getAll();
        if (SessionManagerService.getInstance().employeeRoleId() != 1)
            roles.removeIf(role -> role.getId() == 1);
        cbRole.getItems().addAll(roles);
    }

    private void loadStatusComboBox() {
        ArrayList<StatusDTO> statusList = statusBUS.getAllByType(StatusType.EMPLOYEE);
        cbStatus.getItems().addAll(statusList);
    }

    private void loadAccountStatusComboBox() {
        ArrayList<StatusDTO> statusList = statusBUS.getAllByType(StatusType.ACCOUNT);
        cbAccountStatus.getItems().addAll(statusList);
    }

    /**
     * Setup job history table columns
     */
    private void setupJobHistoryTable() {

        colEffectiveDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEffectiveDate() != null
                        ? validationUtils.formatDateTime(cellData.getValue().getEffectiveDate())
                        : ""));

        colDepartment.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartmentName() != null ? cellData.getValue().getDepartmentName() : ""));

        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPositionName() != null ? cellData.getValue().getPositionName() : ""));

        colReason.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getReason() != null ? cellData.getValue().getReason() : ""));

        colApprover.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getApproverName() != null ? cellData.getValue().getApproverName() : ""));

        colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatusDescription() != null ? cellData.getValue().getStatusDescription() : ""));

        colCreatedAt.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCreatedAt() != null
                        ? validationUtils.formatDateTime(cellData.getValue().getCreatedAt())
                        : ""));
    }

    private void setupListeners() {
        savePersonalBtn.setOnAction(e -> handleSavePersonal());
        saveAccountBtn.setOnAction(e -> handleSaveAccount());
        saveJobBtn.setOnAction(e -> handleSaveJob());
        savePayrollBtn.setOnAction(e -> handleSavePayroll());
        btnResetPassword.setOnAction(e -> handleResetPassword());
        closeBtn.setOnAction(e -> handleClose());

        // Avatar button listeners
        choseImg.setOnAction(e -> handleChooseAvatar());
        resetImgBtn.setOnAction(e -> handleResetAvatar());
    }

    public void setTypeModal(int type) {
        typeModal = type;

        if (typeModal == 0) {
            modalName.setText("Thêm nhân viên");
            txtEmployeeId.setText("Hệ thống tự tạo");
        } else if (typeModal == 1) {
            modalName.setText("Sửa nhân viên");
            // Ẩn job history section khi sửa (không cần thiết)
            jobHistorySection.setVisible(false);
            jobHistorySection.setManaged(false);
            paginationSection.setVisible(false);
            paginationSection.setManaged(false);
        } else if (typeModal == 2) {
            modalName.setText("Xem thông tin nhân viên");
            setReadOnly();
        }
    }

    /**
     * Set employee ID and select initial tab dựa vào quyền
     * Ưu tiên: Tab 1 (Personal) > Tab 2 (Account) > Tab 3 (Job) > Tab 4 (Payroll)
     */
    public void setData(int empId) {
        currentEmployeeId = empId;

        // Chọn tab đầu tiên mà user có quyền xem
        if (canViewPersonal) {
            tabPaneEmployee.getSelectionModel().select(tabPersonal);
            loadTabPersonal();
        } else if (canViewAccount) {
            tabPaneEmployee.getSelectionModel().select(tabAccount);
            loadTabAccount();
        } else if (canViewJob) {
            tabPaneEmployee.getSelectionModel().select(tabJob);
            loadTabJob();
        } else if (canViewPayroll) {
            tabPaneEmployee.getSelectionModel().select(tabPayroll);
            loadTabPayroll();
        }
    }

    /**
     * Load dữ liệu cho Tab 1: Thông tin cá nhân (Async)
     */
    private void loadTabPersonal() {
        if (!canViewPersonal) {
            return;
        }

        TaskUtil.executePublic(
                loadingOverlay,
                () -> employeeBUS.getPersonalInfo(currentEmployeeId),
                result -> {
                    EmployeePersonalInfoDTO personalInfo = result.getData();
                    displayPersonalInfo(personalInfo);
                });
    }

    /**
     * Load dữ liệu cho Tab 2: Tài khoản hệ thống (Async)
     */
    private void loadTabAccount() {
        if (!canViewAccount) {
            return;
        }

        TaskUtil.executePublic(
                loadingOverlay,
                () -> employeeBUS.getAccountInfo(currentEmployeeId),
                result -> {
                    EmployeeAccountInfoDTO accountInfo = result.getData();
                    displayAccountInfo(accountInfo);
                });
    }

    /**
     * Load dữ liệu cho Tab 3: Đơn vị công tác (Async)
     */
    private void loadTabJob() {
        if (!canViewJob) {
            return;
        }

        TaskUtil.executePublic(
                loadingOverlay,
                () -> employeeBUS.getJobInfo(currentEmployeeId),
                result -> {
                    EmployeeJobInfoDTO jobInfo = result.getData();
                    displayJobInfo(jobInfo);
                });
    }

    /**
     * Load dữ liệu cho Tab 4: Bảo hiểm & Phúc lợi (Async)
     */
    private void loadTabPayroll() {
        if (!canViewPayroll) {
            return;
        }

        TaskUtil.executePublic(
                loadingOverlay,
                () -> employeeBUS.getPayrollInfo(currentEmployeeId),
                result -> {
                    EmployeePayrollInfoDTO payrollInfo = result.getData();
                    displayPayrollInfo(payrollInfo);
                });
    }

    /**
     * Display personal info on Tab 1
     */
    private void displayPersonalInfo(EmployeePersonalInfoDTO personalInfo) {
        if (personalInfo == null)
            return;

        txtEmployeeId.setText(String.valueOf(personalInfo.getEmployeeId()));
        txtFirstName.setText(personalInfo.getFirstName() != null ? personalInfo.getFirstName() : "");
        txtLastName.setText(personalInfo.getLastName() != null ? personalInfo.getLastName() : "");
        dpDateOfBirth.setValue(personalInfo.getDateOfBirth() != null ? personalInfo.getDateOfBirth() : null);
        cbGender.getSelectionModel().select(personalInfo.getGender() != null ? personalInfo.getGender() : "");
        txtPhone.setText(personalInfo.getPhone() != null ? personalInfo.getPhone() : "");
        txtEmail.setText(personalInfo.getEmail() != null ? personalInfo.getEmail() : "");

        // Load avatar
        avatarUrl = personalInfo.getAvatarUrl();
        loadEmployeeAvatar(avatarUrl);

        lblCreatedAt.setText(validationUtils.formatDateTimeWithHour(personalInfo.getCreatedAt()));
        lblUpdatedAt.setText(validationUtils.formatDateTimeWithHour(personalInfo.getUpdatedAt()));

        if (!canUpdatePersonal) {
            txtFirstName.setEditable(false);
            txtLastName.setEditable(false);
            dpDateOfBirth.setDisable(true);
            cbGender.setDisable(true);
            txtPhone.setEditable(false);
            txtEmail.setEditable(false);
            savePersonalBtn.setDisable(true);
            choseImg.setDisable(true);
            resetImgBtn.setDisable(true);
        } else if (typeModal != 1) {
            // Chỉ cho sửa avatar khi typeModal = 1 (Edit)
            choseImg.setDisable(true);
            resetImgBtn.setDisable(true);
        }
    }

    /**
     * Display account info on Tab 2
     */
    private void displayAccountInfo(EmployeeAccountInfoDTO accountInfo) {
        if (accountInfo == null)
            return;

        txtUsername.setText(accountInfo.getUsername() != null ? accountInfo.getUsername() : "");
        lblLastLogin.setText(accountInfo.getLastLogin() != null
                ? validationUtils.formatDateTimeWithHour(accountInfo.getLastLogin())
                : "Chưa có lần đăng nhập");

        // Set account status
        if (accountInfo.getAccountStatusId() != null && accountInfo.getAccountStatusId() > 0) {
            StatusDTO accountStatus = statusBUS.getById(accountInfo.getAccountStatusId());
            if (accountStatus != null) {
                cbAccountStatus.getSelectionModel().select(accountStatus);
            }
        }

        // Set created/updated time
        lblCreatedAt.setText(validationUtils.formatDateTimeWithHour(accountInfo.getCreatedAt()));
        lblUpdatedAt.setText(validationUtils.formatDateTimeWithHour(accountInfo.getUpdatedAt()));

        if (!canUpdateAccount && !canResetPassword) {
            cbAccountStatus.setDisable(true);
            btnResetPassword.setDisable(true);
            saveAccountBtn.setDisable(true);
        } else if (!canResetPassword) {
            btnResetPassword.setDisable(true);
        }
    }

    /**
     * Display job info on Tab 3
     */
    private void displayJobInfo(EmployeeJobInfoDTO jobInfo) {
        if (jobInfo == null)
            return;

        // Set department and attach warning listener
        int initialDeptId = jobInfo.getDepartmentId() != null ? jobInfo.getDepartmentId() : -1;
        attachDepartmentWarning(initialDeptId);

        if (jobInfo.getDepartmentId() != null) {
            DepartmentDTO dept = departmentBUS.getById(jobInfo.getDepartmentId());
            if (dept != null) {
                cbDepartment.getSelectionModel().select(dept);
            }
        }

        // Set status
        if (jobInfo.getStatusId() != null) {
            StatusDTO status = statusBUS.getById(jobInfo.getStatusId());
            if (status != null) {
                cbStatus.getSelectionModel().select(status);
            }
        }

        // // Display position name
        // txtPositionName.setText(jobInfo.getPositionName() != null ?
        // jobInfo.getPositionName() : "");

        // // Display base salary
        // txtBaseSalary.setText(jobInfo.getWage() != null ?
        // jobInfo.getWage().toString() : "0");

        // Set created/updated time
        lblCreatedAt.setText(validationUtils.formatDateTimeWithHour(jobInfo.getCreatedAt()));
        lblUpdatedAt.setText(validationUtils.formatDateTimeWithHour(jobInfo.getUpdatedAt()));

        // Setup job history table chỉ khi ở chế độ thêm mới (typeModal = 0)
        // Khi edit (typeModal = 1) hoặc view (typeModal = 2) không load history
        if (typeModal == 2) {
            setupJobHistoryTable();
            loadEmploymentHistory(0);
        }

        if (!canUpdateJob) {
            cbDepartment.setDisable(true);
            cbRole.setDisable(true);
            cbStatus.setDisable(true);
            saveJobBtn.setDisable(true);
        }
    }

    /**
     * Display payroll info on Tab 4
     */
    private void displayPayrollInfo(EmployeePayrollInfoDTO payrollInfo) {
        if (payrollInfo == null)
            return;

        txtNumDependents
                .setText(payrollInfo.getNumDependents() != null ? payrollInfo.getNumDependents().toString() : "0");
        txtHealthInsCode.setText(payrollInfo.getHealthInsCode() != null ? payrollInfo.getHealthInsCode() : "");
        txtSocialInsCode.setText(payrollInfo.getSocialInsCode() != null ? payrollInfo.getSocialInsCode() : "");
        txtUnemploymentInsCode
                .setText(payrollInfo.getUnemploymentInsCode() != null ? payrollInfo.getUnemploymentInsCode() : "");
        cbPersonalTax.setSelected(payrollInfo.isMealSupport());
        cbTransportSupport.setSelected(payrollInfo.isTransportationSupport());
        cbAccommodationSupport.setSelected(payrollInfo.isAccommodationSupport());

        // Set created/updated time
        lblCreatedAt.setText(validationUtils.formatDateTimeWithHour(payrollInfo.getCreatedAt()));
        lblUpdatedAt.setText(validationUtils.formatDateTimeWithHour(payrollInfo.getUpdatedAt()));

        if (!canUpdatePayroll) {
            txtNumDependents.setEditable(false);
            txtHealthInsCode.setEditable(false);
            txtSocialInsCode.setEditable(false);
            txtUnemploymentInsCode.setEditable(false);
            cbPersonalTax.setDisable(true);
            cbTransportSupport.setDisable(true);
            cbAccommodationSupport.setDisable(true);
            savePayrollBtn.setDisable(true);
        }
    }

    private void setReadOnly() {
        // Disable all input controls
        txtFirstName.setEditable(false);
        txtLastName.setEditable(false);
        txtPhone.setEditable(false);
        txtEmail.setEditable(false);
        txtNumDependents.setEditable(false);
        txtHealthInsCode.setEditable(false);

        UiUtils.gI().setReadOnlyComboBox(cbAccountStatus);
        UiUtils.gI().setReadOnlyItem(dpDateOfBirth);
        UiUtils.gI().setReadOnlyComboBox(cbGender);
        UiUtils.gI().setReadOnlyComboBox(cbDepartment);
        UiUtils.gI().setReadOnlyComboBox(cbRole);
        UiUtils.gI().setReadOnlyComboBox(cbStatus);
        UiUtils.gI().setReadOnlyItem(txtHealthInsCode);
        UiUtils.gI().setReadOnlyItem(txtSocialInsCode);
        UiUtils.gI().setReadOnlyItem(txtUnemploymentInsCode);
        UiUtils.gI().setReadOnlyItem(cbPersonalTax);
        UiUtils.gI().setReadOnlyItem(cbTransportSupport);
        UiUtils.gI().setReadOnlyItem(cbAccommodationSupport);
        UiUtils.gI().setVisibleItem(detailPassword);
        // Hide all save buttons
        UiUtils.gI().setReadOnlyItem(savePersonalBtn);
        UiUtils.gI().setReadOnlyItem(saveAccountBtn);
        UiUtils.gI().setReadOnlyItem(saveJobBtn);
        UiUtils.gI().setReadOnlyItem(savePayrollBtn);
        UiUtils.gI().setReadOnlyItem(btnResetPassword);
        choseImg.setVisible(false);
        resetImgBtn.setVisible(false);
    }

    private void setupHistoryPagination() {
        // Init với pageSize = 10
        // Gọi overloaded method loadTabJobHistory(pageIndex) khi người dùng chuyển
        // trang
        historyPaginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadEmploymentHistory(pageIndex);
        });
    }

    /**
     * Load employment history for the job history table
     */
    private void loadEmploymentHistory(int pageIndex) {
        if (currentEmployeeId <= 0)
            return;

        TaskUtil.executePublic(loadingOverlay,
                () -> EmploymentHistoryBUS.getInstance().getDetailsFullByEmployeeIdPaged(currentEmployeeId, pageIndex,
                        PAGE_SIZE),
                result -> {
                    PagedResponse<EmploymentHistoryDetailDTO> res = result.getPagedData();

                    // Đổ data vào TableView
                    tblJobHistory.setItems(javafx.collections.FXCollections.observableArrayList(res.getItems()));

                    // Cập nhật Pagination
                    int totalItems = res.getTotalItems();
                    int pageCount = (int) Math.ceil((double) totalItems / PAGE_SIZE);
                    historyPaginationController.setPageCount(pageCount > 0 ? pageCount : 1);
                });

    }

    /**
     * Handle save personal info (Tab 1)
     * Cập nhật: firstName, lastName, dateOfBirth, gender, phone, email
     */
    private void handleSavePersonal() {
        // Validation
        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
            NotificationUtils.showErrorAlert("Họ tên không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        if (dpDateOfBirth.getValue() == null) {
            NotificationUtils.showErrorAlert("Ngày sinh không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        if (cbGender.getSelectionModel().getSelectedItem() == null) {
            NotificationUtils.showErrorAlert("Giới tính không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        if (txtPhone.getText().trim().isEmpty()) {
            NotificationUtils.showErrorAlert("Số điện thoại không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        // Xử lý Avatar (tùy chọn - không bắt buộc)
        String finalAvatarUrl = null;
        if (typeModal == 1 && avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            try {
                // Nếu avatarUrl khác với đường dẫn cũ (có chọn ảnh mới)
                if (!avatarUrl.equals(txtEmployeeId.getText()) && !avatarUrl.startsWith("images/")) {
                    finalAvatarUrl = ImageService.gI().saveEmployeeAvatar(String.valueOf(currentEmployeeId), avatarUrl);
                } else {
                    // Giữ nguyên avatar cũ hoặc reset thành null
                    finalAvatarUrl = avatarUrl.startsWith("images/") ? avatarUrl : null;
                }
            } catch (IOException ex) {
                NotificationUtils.showErrorAlert("Lỗi lưu ảnh: " + ex.getMessage(), AppMessages.DIALOG_TITLE);
                return;
            }
        }

        // Build EmployeeDTO with personal info
        EmployeeDTO personal = new EmployeeDTO();
        personal.setId(currentEmployeeId);
        personal.setFirstName(txtFirstName.getText().trim());
        personal.setLastName(txtLastName.getText().trim());
        personal.setDateOfBirth(dpDateOfBirth.getValue());
        personal.setGender(cbGender.getValue());
        personal.setPhone(txtPhone.getText().trim());
        personal.setEmail(txtEmail.getText().trim());
        personal.setAvatarUrl(finalAvatarUrl);

        // Save to database
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_PERSONAL_UPDATE,
                () -> employeeBUS.updatePersonalInfoByAdmin(personal),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) savePersonalBtn.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    /**
     * Handle save account info (Tab 2)
     * Cập nhật: account status, reset password
     */
    private void handleSaveAccount() {
        if (currentEmployeeId <= 0) {
            NotificationUtils.showErrorAlert("Thông tin nhân viên không hợp lệ", AppMessages.DIALOG_TITLE);
            return;
        }

        StatusDTO selectedStatus = cbAccountStatus.getSelectionModel().getSelectedItem();
        if (selectedStatus == null) {
            NotificationUtils.showErrorAlert("Trạng thái tài khoản không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        // Update account status
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_ACCOUNT_UPDATE_STATUS,
                () -> employeeBUS.updateAccountStatus(currentEmployeeId, selectedStatus.getId()),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) saveAccountBtn.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    /**
     * Handle save job info (Tab 3)
     * Cập nhật: department, role, status
     */
    private void handleSaveJob() {
        // Validation
        DepartmentDTO selectedDept = cbDepartment.getSelectionModel().getSelectedItem();
        if (selectedDept == null) {
            NotificationUtils.showErrorAlert("Phòng ban không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        RoleDTO selectedRole = cbRole.getSelectionModel().getSelectedItem();
        if (selectedRole == null) {
            NotificationUtils.showErrorAlert("Chức vụ không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        StatusDTO selectedStatus = cbStatus.getSelectionModel().getSelectedItem();
        if (selectedStatus == null) {
            NotificationUtils.showErrorAlert("Trạng thái không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        // Build EmployeeDTO with job info
        EmployeeDTO jobInfo = new EmployeeDTO();
        jobInfo.setId(currentEmployeeId);
        jobInfo.setDepartmentId(selectedDept.getId());
        jobInfo.setStatusId(selectedStatus.getId());

        // Save to database
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_JOB_UPDATE,
                () -> employeeBUS.updateJobInfo(jobInfo),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) saveJobBtn.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    /**
     * Handle save payroll info (Tab 4)
     * Cập nhật: healthInsCode, socialInsCode, unemploymentInsCode, insurance flags
     * (tax,
     * transport, accommodation)
     */
    private void handleSavePayroll() {
        if (currentEmployeeId <= 0) {
            NotificationUtils.showErrorAlert("Thông tin nhân viên không hợp lệ", AppMessages.DIALOG_TITLE);
            return;
        }

        // Build EmployeeDTO with payroll info
        EmployeeDTO payrollInfo = new EmployeeDTO();
        payrollInfo.setId(currentEmployeeId);
        payrollInfo.setHealthInsCode(txtHealthInsCode.getText().trim());
        payrollInfo.setSocialInsCode(txtSocialInsCode.getText().trim());
        payrollInfo.setUnemploymentInsCode(txtUnemploymentInsCode.getText().trim());
        payrollInfo.setMealSupport(cbPersonalTax.isSelected());
        payrollInfo.setTransportationSupport(cbTransportSupport.isSelected());
        payrollInfo.setAccommodationSupport(cbAccommodationSupport.isSelected());
        int numDependents = txtNumDependents.getText().trim().isEmpty() ? 0
                : Integer.parseInt(txtNumDependents.getText().trim());
        // Save to database
        TaskUtil.executeSecure(loadingOverlay,
                PermissionKey.EMPLOYEE_PAYROLLINFO_UPDATE,
                () -> employeeBUS.updatePayrollInfo(payrollInfo, numDependents),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) savePayrollBtn.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    /**
     * Handle reset password (Button on Tab 2)
     * Reset mật khẩu thành 123456 và ép user phải đăng nhập lại
     */
    private void handleResetPassword() {
        if (currentEmployeeId <= 0) {
            NotificationUtils.showErrorAlert("Thông tin nhân viên không hợp lệ", AppMessages.DIALOG_TITLE);
            return;
        }

        // Confirm before reset
        boolean confirmed = UiUtils.gI().showConfirmAlert(
                "Bạn chắc chắn muốn reset mật khẩu nhân viên này? - Mật khẩu sẽ được reset về 123456",
                AppMessages.DIALOG_TITLE);

        if (!confirmed)
            return;

        // Reset password
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_ACCOUNT_RESET_PASSWORD,
                () -> employeeBUS.resetAccountPassword(currentEmployeeId),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) btnResetPassword.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    // ==================== 🖼️ AVATAR HANDLERS ====================
    /**
     * Chọn ảnh đại diện từ hệ thống tệp
     */
    private void handleChooseAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imgAvatar.setImage(image);
            // Force fill ImageView bằng cách reload properties
            imgAvatar.setPreserveRatio(false);
            avatarUrl = file.toURI().toString();
        }
    }

    /**
     * Reset ảnh đại diện về mặc định
     */
    private void handleResetAvatar() {
        if (!UiUtils.gI().showConfirmAlert("Bạn có chắc muốn xóa ảnh đại diện?", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        Image image = null;
        URL resource = getClass().getResource("/images/default/default.png");
        if (resource != null) {
            image = new Image(resource.toExternalForm());
        } else {
            System.err.println("Resource not found: /images/default/default.png");
        }

        if (image != null) {
            imgAvatar.setImage(image);
            // Force fill ImageView bằng cách reload properties
            imgAvatar.setPreserveRatio(false);
            avatarUrl = null; // Set null để xóa avatar
            NotificationUtils.showInfoAlert("Ảnh đại diện đã được xóa", AppMessages.DIALOG_TITLE);
        }
    }

    /**
     * Load và hiển thị ảnh đại diện
     */
    private void loadEmployeeAvatar(String avatarUrlPath) {
        File imageFile = null;
        Image image = null;

        if (avatarUrlPath != null && !avatarUrlPath.isEmpty()) {
            imageFile = new File(avatarUrlPath);
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