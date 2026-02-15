package GUI;

import BUS.AccountBUS;
import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.EmploymentHistoryBUS;
import BUS.RoleBUS;
import BUS.SalaryBUS;
import BUS.StatusBUS;
import DTO.AccountDTO;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.EmployeeDetailDTO;
import DTO.EmploymentHistoryDetailDTO;
import DTO.RoleDTO;
import DTO.SalaryDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IModalController;
import PROVIDER.EmploymentHistoryViewProvider;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
    private TableColumn<EmploymentHistoryDetailDTO, String> colCreatedAt;
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

    @Getter
    private boolean isSaved = false;
    private int typeModal; // 0=Add, 1=Edit, 2=View
    private EmployeeDTO employee;
    private RoleBUS roleBUS;
    private SalaryBUS salaryBUS;
    private StatusBUS statusBUS;
    private DepartmentBUS departmentBUS;
    private EmployeeBUS employeeBUS;
    private SessionManagerService session;
    private EmploymentHistoryBUS employmentHistoryBUS;
    private ValidationUtils validationUtils;
    private AccountBUS accountBUS;

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
        loadDepartmentComboBox();
        loadRoleComboBox();
        loadStatusComboBox();
        loadAccountStatusComboBox();
        hideTabWithoutPermission();
        // Setup listeners
        setupListeners();
    }

    public void hideTabWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();

        boolean canViewPersonal = session.hasPermission(PermissionKey.EMPLOYEE_PERSONAL_VIEW);
        boolean canUpdatePersonal = session.hasPermission(PermissionKey.EMPLOYEE_PERSONAL_UPDATE);
        boolean canViewJob = session.hasPermission(PermissionKey.EMPLOYEE_JOB_VIEW);
        boolean canUpdateJob = session.hasPermission(PermissionKey.EMPLOYEE_JOB_UPDATE);
        boolean canViewPayrollInfo = session.hasPermission(PermissionKey.EMPLOYEE_PAYROLLINFO_VIEW);
        boolean canUpdatePayrollInfo = session.hasPermission(PermissionKey.EMPLOYEE_PAYROLLINFO_UPDATE);
        boolean canviewAccount = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_VIEW);
        boolean canResetAccountPassword = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_RESET_PASSWORD);
        boolean canUpdateAccountStatus = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_UPDATE_STATUS);
        boolean canDelete = session.hasPermission(PermissionKey.EMPLOYEE_DELETE);
        boolean canViewDetail = canViewPersonal || canViewJob || canViewPayrollInfo || canviewAccount;

        // Có 4 tab edit riêng biệt

    }

    private void loadGenderComboBox() {
        cbGender.getItems().addAll("Nam", "Nữ", "Khác");
        cbGender.getSelectionModel().selectFirst();
    }

    private void loadDepartmentComboBox() {
        cbDepartment.getItems().addAll(departmentBUS.getAll());
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
                validationUtils.formatDateTime(cellData.getValue().getCreatedAt()) != null
                        ? validationUtils.formatDateTime(cellData.getValue().getCreatedAt())
                        : ""));

        colDepartment.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartmentName()));

        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getRoleName()));

        colCreatedAt.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatDateTimeWithHour(cellData.getValue().getCreatedAt()) != null
                        ? validationUtils.formatDateTimeWithHour(cellData.getValue().getCreatedAt())
                        : ""));
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

    public void setEmployee(EmployeeDetailDTO empDetail) {
        // Display employee data from EmployeeDetailDTO
        // tab1 Personal Info
        txtEmployeeId.setText(String.valueOf(empDetail.getEmployeeId()));
        txtFirstName.setText(empDetail.getFirstName() != null ? empDetail.getFirstName() : "");
        txtLastName.setText(empDetail.getLastName() != null ? empDetail.getLastName() : "");
        dpDateOfBirth.setValue(empDetail.getDateOfBirth());
        cbGender.getSelectionModel().select(empDetail.getGender() != null ? empDetail.getGender() : "");
        txtPhone.setText(empDetail.getPhone() != null ? empDetail.getPhone() : "");
        txtEmail.setText(empDetail.getEmail() != null ? empDetail.getEmail() : "");
        // tab2 Account Info
        txtUsername.setText(empDetail.getUsername() != null ? empDetail.getUsername() : "");
        if (empDetail.getAccountStatusId() > 0) {
            StatusDTO accountStatus = statusBUS.getById(empDetail.getAccountStatusId());
            if (accountStatus != null) {
                cbAccountStatus.getItems().stream()
                        .filter(item -> item != null && item.getId() == accountStatus.getId())
                        .findFirst()
                        .ifPresent(item -> cbAccountStatus.getSelectionModel().select(item));
            }
        }
        AccountDTO account = accountBUS.getById(empDetail.getAccountId());
        if (account != null) {
            lblLastLogin.setText(
                    account.getLastLogin() != null ? validationUtils.formatDateTimeWithHour(account.getLastLogin())
                            : "Chưa có lần đăng nhập");
        } else {
            lblLastLogin.setText("Chưa có lần đăng nhập");
        }
        // tab3 Job Info
        if (empDetail.getDepartmentId() != null)

        {
            DepartmentDTO dept = departmentBUS.getById(empDetail.getDepartmentId());
            cbDepartment.getSelectionModel().select(dept);
        }

        // Set Role (this will trigger auto-update of salary)
        RoleDTO role = roleBUS.getById(empDetail.getRoleId());
        if (role != null) {
            cbRole.getSelectionModel().select(role);
            handleRoleSelectChange(); // Trigger salary update
        }

        // Load Job History with department and role names
        ArrayList<EmploymentHistoryDetailDTO> jobHistory = EmploymentHistoryViewProvider.getInstance()
                .getJobHistoryByEmployeeIdDecrease(empDetail.getEmployeeId());

        setupJobHistoryTable();
        if (jobHistory != null)
            tblJobHistory.setItems(FXCollections.observableArrayList(jobHistory));

        // Set Status
        StatusDTO status = statusBUS.getById(empDetail.getStatusId());
        if (status != null) {
            cbStatus.getItems().stream()
                    .filter(item -> item != null && item.getId() == status.getId())
                    .findFirst()
                    .ifPresent(item -> cbStatus.getSelectionModel().select(item));
        }

        // Tab4 Payroll & Benefits
        cbSocialIns.setSelected(empDetail.isSocialInsurance());
        cbHealthIns.setSelected(empDetail.isHealthInsurance());
        cbUnemploymentIns.setSelected(empDetail.isUnemploymentInsurance());
        cbPersonalTax.setSelected(empDetail.isPersonalIncomeTax());
        cbTransportSupport.setSelected(empDetail.isTransportationSupport());
        cbAccommodationSupport.setSelected(empDetail.isAccommodationSupport());

        txtNumDependents.setText(empDetail.getNumDependents() != null ? empDetail.getNumDependents().toString() : "0");
        txtHealthInsCode.setText(empDetail.getHealthInsCode() != null ? empDetail.getHealthInsCode() : "");

        // Display metadata
        if (empDetail.getCreatedAt() != null) {
            lblCreatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(empDetail.getCreatedAt()));
        }
        if (empDetail.getUpdatedAt() != null) {
            lblUpdatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(empDetail.getUpdatedAt()));
        }

        UiUtils.gI().addTooltipToComboBoxValue(cbAccountStatus, 25, as -> as.getDescription());
        UiUtils.gI().addTooltipToComboBoxValue(cbDepartment, 15, d -> d.getName());
        UiUtils.gI().addTooltipToComboBoxValue(cbRole, 15, r -> r.getName());
        UiUtils.gI().addTooltipToComboBoxValue(cbStatus, 15, s -> s.getDescription());
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

    // ...existing code...

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

        // Save to database
        int result;
        if (typeModal == 0) {
            // Insert new employee
            // result = employeeBUS.insert(employee, session.employeeRoleId(),
            // session.employeeLoginId());
            // if (result == 1) {
            // NotificationUtils.showInfoAlert("Thêm nhân viên thành công",
            // AppMessages.DIALOG_TITLE);
            // isSaved = true;
            // handleClose();
            // } else {
            // NotificationUtils.showErrorAlert("Thêm nhân viên thất bại",
            // AppMessages.DIALOG_TITLE);
            // }
        } else if (typeModal == 1) {
            // Update existing employee
            // result = employeeBUS.update(employee, session.employeeRoleId(),
            // session.employeeLoginId());
            // if (result == 1) {
            // NotificationUtils.showInfoAlert("Cập nhật nhân viên thành công",
            // AppMessages.DIALOG_TITLE);
            // isSaved = true;
            // handleClose();
            // } else {
            // NotificationUtils.showErrorAlert("Cập nhật nhân viên thất bại",
            // AppMessages.DIALOG_TITLE);
            // }
        }
    }

    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}