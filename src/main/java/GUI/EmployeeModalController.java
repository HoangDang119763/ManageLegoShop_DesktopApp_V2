package GUI;

import BUS.AccountBUS;
import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.EmploymentHistoryBUS;
import BUS.RoleBUS;
import BUS.SalaryBUS;
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
import DTO.SalaryDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import ENUM.Gender;
import ENUM.Status;
import INTERFACE.IModalController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

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
    private TableColumn<EmploymentHistoryDetailDTO, String> colReason1;
    @FXML
    private Button saveJobBtn;

    // ==================== TAB 4: PAYROLL & BENEFITS ====================
    @FXML
    private TextField txtNumDependents;
    @FXML
    private TextField txtHealthInsCode;
    @FXML
    private CheckBox cbSocialIns;
    @FXML
    private CheckBox cbHealthIns;
    @FXML
    private CheckBox cbUnemploymentIns;
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
    private boolean isSaved = false;
    @Getter
    private String resultMessage = "";
    private int typeModal; // 0=Add, 1=Edit, 2=View
    private EmployeeDTO employee;
    private int currentEmployeeId; // Store employee ID for lazy loading
    private RoleBUS roleBUS;
    private SalaryBUS salaryBUS;
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
        salaryBUS = SalaryBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        departmentBUS = DepartmentBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        session = SessionManagerService.getInstance();
        employmentHistoryBUS = EmploymentHistoryBUS.getInstance();
        validationUtils = ValidationUtils.getInstance();
        accountBUS = AccountBUS.getInstance();
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
        cbRole.getItems().addAll(roleBUS.getAll());

        // Auto-update salary when role changes
        cbRole.setOnAction(event -> handleRoleSelectChange());
    }

    /**
     * Khi chọn Role, auto-update Lương cơ bản và Hệ số từ Salary table
     */
    private void handleRoleSelectChange() {
        RoleDTO selectedRole = cbRole.getSelectionModel().getSelectedItem();
        if (selectedRole != null && selectedRole.getSalaryId() != null) {
            SalaryDTO salary = salaryBUS.getById(selectedRole.getSalaryId());
            if (salary != null) {
                txtBaseSalary.setText(salary.getBase() != null ? salary.getBase().toString() : "0");
                txtCoefficient.setText(salary.getCoefficient() != null ? salary.getCoefficient().toString() : "0");
            }
        }
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
                cellData.getValue().getRoleName() != null ? cellData.getValue().getRoleName() : ""));

        colReason.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getReason() != null ? cellData.getValue().getReason() : ""));

        colApprover.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getApproverName() != null ? cellData.getValue().getApproverName() : ""));

        colReason1.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getReason() != null ? cellData.getValue().getReason() : ""));
    }

    private void setupListeners() {
        savePersonalBtn.setOnAction(e -> handleSave());
        saveAccountBtn.setOnAction(e -> handleSave());
        saveJobBtn.setOnAction(e -> handleSave());
        savePayrollBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
    }

    public void setTypeModal(int type) {
        typeModal = type;

        if (typeModal == 0) {
            modalName.setText("Thêm nhân viên");
            txtEmployeeId.setText("Hệ thống tự tạo");
        } else if (typeModal == 1) {
            modalName.setText("Sửa nhân viên");
        } else if (typeModal == 2) {
            modalName.setText("Xem thông tin nhân viên");
            setReadOnly();
        }
    }

    /**
     * Set employee ID and load initial tab (Tab 1: Personal)
     */
    public void setData(int empId) {
        currentEmployeeId = empId;
        // Có quyền nào thì load tab đó đầu tiên, ưu tiên Tab 1 > Tab 2 > Tab 3 > Tab 4
        if (canViewPersonal)
            loadTabPersonal();
        else if (canViewAccount)
            loadTabAccount();
        else if (canViewJob)
            loadTabJob();
        else if (canViewPayroll)
            loadTabPayroll();
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

        // Set role and trigger salary update
        if (jobInfo.getRoleId() != null) {
            RoleDTO role = roleBUS.getById(jobInfo.getRoleId());
            if (role != null) {
                cbRole.getSelectionModel().select(role);
                handleRoleSelectChange();
            }
        }

        // Set status
        if (jobInfo.getStatusId() != null) {
            StatusDTO status = statusBUS.getById(jobInfo.getStatusId());
            if (status != null) {
                cbStatus.getSelectionModel().select(status);
            }
        }

        // Display base salary and coefficient
        txtBaseSalary.setText(jobInfo.getBaseSalary() != null ? jobInfo.getBaseSalary().toString() : "0");
        txtCoefficient
                .setText(jobInfo.getSalaryCoefficient() != null ? jobInfo.getSalaryCoefficient().toString() : "0");

        // Set created/updated time
        lblCreatedAt.setText(validationUtils.formatDateTimeWithHour(jobInfo.getCreatedAt()));
        lblUpdatedAt.setText(validationUtils.formatDateTimeWithHour(jobInfo.getUpdatedAt()));

        // Setup job history table and load separately
        setupJobHistoryTable();
        loadEmploymentHistory(0);

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

        cbHealthIns.setSelected(payrollInfo.isHealthInsurance());
        cbSocialIns.setSelected(payrollInfo.isSocialInsurance());
        cbUnemploymentIns.setSelected(payrollInfo.isUnemploymentInsurance());
        cbPersonalTax.setSelected(payrollInfo.isPersonalIncomeTax());
        cbTransportSupport.setSelected(payrollInfo.isTransportationSupport());
        cbAccommodationSupport.setSelected(payrollInfo.isAccommodationSupport());

        // Set created/updated time
        lblCreatedAt.setText(validationUtils.formatDateTimeWithHour(payrollInfo.getCreatedAt()));
        lblUpdatedAt.setText(validationUtils.formatDateTimeWithHour(payrollInfo.getUpdatedAt()));

        if (!canUpdatePayroll) {
            txtNumDependents.setEditable(false);
            txtHealthInsCode.setEditable(false);
            cbHealthIns.setDisable(true);
            cbSocialIns.setDisable(true);
            cbUnemploymentIns.setDisable(true);
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
        UiUtils.gI().setReadOnlyItem(cbSocialIns);
        UiUtils.gI().setReadOnlyItem(cbHealthIns);
        UiUtils.gI().setReadOnlyItem(cbUnemploymentIns);
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

    private void handleSave() {
        // Validation
        if (txtFirstName.getText().trim().isEmpty() || txtLastName.getText().trim().isEmpty()) {
            NotificationUtils.showErrorAlert("Họ tên không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        if (cbRole.getSelectionModel().getSelectedItem() == null) {
            NotificationUtils.showErrorAlert("Chức vụ không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        if (cbStatus.getSelectionModel().getSelectedItem() == null) {
            NotificationUtils.showErrorAlert("Trạng thái không được để trống", AppMessages.DIALOG_TITLE);
            return;
        }

        // Update employee
        if (employee == null) {
            employee = new EmployeeDTO();
        }

        employee.setFirstName(txtFirstName.getText().trim());
        employee.setLastName(txtLastName.getText().trim());
        employee.setDateOfBirth(dpDateOfBirth.getValue());
        employee.setGender(cbGender.getValue());
        employee.setPhone(txtPhone.getText().trim());
        employee.setEmail(txtEmail.getText().trim());

        DepartmentDTO dept = cbDepartment.getSelectionModel().getSelectedItem();
        if (dept != null) {
            employee.setDepartmentId(dept.getId());
        }

        RoleDTO role = cbRole.getSelectionModel().getSelectedItem();
        if (role != null) {
            employee.setRoleId(role.getId());
        }

        StatusDTO status = cbStatus.getSelectionModel().getSelectedItem();
        if (status != null) {
            employee.setStatusId(status.getId());
        }

        employee.setHealthInsCode(txtHealthInsCode.getText().trim());
        employee.setSocialInsurance(cbSocialIns.isSelected());
        employee.setUnemploymentInsurance(cbUnemploymentIns.isSelected());
        employee.setPersonalIncomeTax(cbPersonalTax.isSelected());
        employee.setTransportationSupport(cbTransportSupport.isSelected());
        employee.setAccommodationSupport(cbAccommodationSupport.isSelected());

        // Save to database with loading overlay
        if (typeModal == 0) {
            // Insert new employee
            // TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_INSERT, () -> {
            // var result = employeeBUS.insert(employee);
            // return result;
            // }, result -> {
            // if (result.isSuccess()) {
            // resultMessage = result.getMessage();
            // isSaved = true;
            // handleClose();
            // } else {
            // NotificationUtils.showErrorAlert(result.getMessage(),
            // AppMessages.DIALOG_TITLE);
            // }
            // });
        } else if (typeModal == 1) {
            // // Update existing employee
            // TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_UPDATE, () -> {
            // var result = employeeBUS.update(employee);
            // return result;
            // }, result -> {
            // if (result.isSuccess()) {
            // resultMessage = result.getMessage();
            // isSaved = true;
            // handleClose();
            // } else {
            // NotificationUtils.showErrorAlert(result.getMessage(),
            // AppMessages.DIALOG_TITLE);
            // }
            // });
        }
    }

    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}