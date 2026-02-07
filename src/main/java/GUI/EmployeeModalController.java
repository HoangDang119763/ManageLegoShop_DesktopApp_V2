package GUI;

import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.RoleBUS;
import BUS.SalaryBUS;
import BUS.StatusBUS;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.RoleDTO;
import DTO.SalaryDTO;
import DTO.StatusDTO;
import ENUM.StatusType;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Controller cho modal thêm/sửa/xem thông tin nhân viên
 * typeModal: 0 = Add, 1 = Edit, 2 = View (readonly)
 */
public class EmployeeModalController {
    @FXML
    private Label modalName;
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
    private ComboBox<DepartmentDTO> cbDepartment;
    @FXML
    private ComboBox<RoleDTO> cbRole;
    @FXML
    private TextField txtBaseSalary;
    @FXML
    private TextField txtCoefficient;
    @FXML
    private TextField txtNumDependents;
    @FXML
    private ComboBox<StatusDTO> cbStatus;
    @FXML
    private TextField txtHealthInsCode;
    @FXML
    private CheckBox cbSocialIns;
    @FXML
    private CheckBox cbUnemploymentIns;
    @FXML
    private CheckBox cbPersonalTax;
    @FXML
    private CheckBox cbTransportSupport;
    @FXML
    private CheckBox cbAccommodationSupport;
    @FXML
    private CheckBox cbSocialIns1; // Health insurance checkbox
    @FXML
    private TextField txtCreatedAt;
    @FXML
    private TextField txtUpdatedAt;
    @FXML
    private Button saveBtn;
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

    @FXML
    public void initialize() {
        // Load BUS instances
        roleBUS = RoleBUS.getInstance();
        salaryBUS = SalaryBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        departmentBUS = DepartmentBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        session = SessionManagerService.getInstance();

        // Load initial data
        loadGenderComboBox();
        loadDepartmentComboBox();
        loadRoleComboBox();
        loadStatusComboBox();

        // Setup listeners
        setupListeners();
    }

    private void loadGenderComboBox() {
        cbGender.getItems().addAll("Nam", "Nữ", "Khác");
        cbGender.getSelectionModel().selectFirst();
    }

    private void loadDepartmentComboBox() {
        if (departmentBUS.isLocalEmpty()) {
            departmentBUS.loadLocal();
        }
        cbDepartment.getItems().addAll(departmentBUS.getAllLocal());
        cbDepartment.setConverter(new javafx.util.StringConverter<DepartmentDTO>() {
            @Override
            public String toString(DepartmentDTO dept) {
                return dept != null ? dept.getName() : "";
            }

            @Override
            public DepartmentDTO fromString(String string) {
                return null;
            }
        });
    }

    private void loadRoleComboBox() {
        if (roleBUS.isLocalEmpty()) {
            roleBUS.loadLocal();
        }
        cbRole.getItems().addAll(roleBUS.getAllLocal());
        cbRole.setConverter(new javafx.util.StringConverter<RoleDTO>() {
            @Override
            public String toString(RoleDTO role) {
                return role != null ? role.getName() : "";
            }

            @Override
            public RoleDTO fromString(String string) {
                return null;
            }
        });

        // Auto-update salary when role changes
        cbRole.setOnAction(event -> handleRoleSelectChange());
    }

    /**
     * Khi chọn Role, auto-update Lương cơ bản và Hệ số từ Salary table
     */
    private void handleRoleSelectChange() {
        RoleDTO selectedRole = cbRole.getSelectionModel().getSelectedItem();
        if (selectedRole != null && selectedRole.getSalaryId() != null) {
            SalaryDTO salary = salaryBUS.getByIdLocal(selectedRole.getSalaryId());
            if (salary != null) {
                txtBaseSalary.setText(salary.getBase() != null ? salary.getBase().toString() : "0");
                txtCoefficient.setText(salary.getCoefficient() != null ? salary.getCoefficient().toString() : "0");
            }
        }
    }

    private void loadStatusComboBox() {
        ArrayList<StatusDTO> statusList = statusBUS.getAllByTypeLocal(StatusType.EMPLOYEE);
        cbStatus.getItems().addAll(statusList);
        cbStatus.setConverter(new javafx.util.StringConverter<StatusDTO>() {
            @Override
            public String toString(StatusDTO status) {
                return status != null ? status.getDescription() : "";
            }

            @Override
            public StatusDTO fromString(String string) {
                return null;
            }
        });
    }

    private void setupListeners() {
        saveBtn.setOnAction(e -> handleSave());
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

    public void setEmployee(EmployeeDTO emp) {
        this.employee = emp;

        // Display employee data
        txtEmployeeId.setText(String.valueOf(emp.getId()));
        txtFirstName.setText(emp.getFirstName() != null ? emp.getFirstName() : "");
        txtLastName.setText(emp.getLastName() != null ? emp.getLastName() : "");
        dpDateOfBirth.setValue(emp.getDateOfBirth());
        cbGender.getSelectionModel().select(emp.getGender() != null ? emp.getGender() : "Nam");
        txtPhone.setText(emp.getPhone() != null ? emp.getPhone() : "");
        txtEmail.setText(emp.getEmail() != null ? emp.getEmail() : "");
        txtHealthInsCode.setText(emp.getHealthInsCode() != null ? emp.getHealthInsCode() : "");
        txtNumDependents.setText("0"); // Will update when we have tax DTO

        // Set Department
        if (emp.getDepartmentId() != null) {
            DepartmentDTO dept = departmentBUS.getByIdLocal(emp.getDepartmentId());
            cbDepartment.getSelectionModel().select(dept);
        }

        // Set Role (this will trigger auto-update of salary)
        RoleDTO role = roleBUS.getByIdLocal(emp.getRoleId());
        if (role != null) {
            cbRole.getSelectionModel().select(role);
            handleRoleSelectChange(); // Trigger salary update
        }

        // Set Status
        StatusDTO status = statusBUS.getByIdLocal(emp.getStatusId());
        if (status != null) {
            cbStatus.getSelectionModel().select(status);
        }

        // Set Insurance checkboxes
        cbSocialIns.setSelected(emp.isSocialInsurance());
        cbUnemploymentIns.setSelected(emp.isUnemploymentInsurance());
        cbPersonalTax.setSelected(emp.isPersonalIncomeTax());
        cbTransportSupport.setSelected(emp.isTransportationSupport());
        cbAccommodationSupport.setSelected(emp.isAccommodationSupport());
        cbSocialIns1.setSelected(emp.isHealthInsurance());

        // Display metadata
        if (emp.getCreatedAt() != null) {
            txtCreatedAt.setText(emp.getCreatedAt()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }
        if (emp.getUpdatedAt() != null) {
            txtUpdatedAt.setText(emp.getUpdatedAt()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        }
    }

    private void setReadOnly() {
        txtFirstName.setDisable(true);
        txtLastName.setDisable(true);
        dpDateOfBirth.setDisable(true);
        cbGender.setDisable(true);
        txtPhone.setDisable(true);
        txtEmail.setDisable(true);
        cbDepartment.setDisable(true);
        cbRole.setDisable(true);
        txtNumDependents.setDisable(true);
        cbStatus.setDisable(true);
        txtHealthInsCode.setDisable(true);
        cbSocialIns.setDisable(true);
        cbUnemploymentIns.setDisable(true);
        cbPersonalTax.setDisable(true);
        cbTransportSupport.setDisable(true);
        cbAccommodationSupport.setDisable(true);
        cbSocialIns1.setDisable(true);
        saveBtn.setDisable(true);
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

        // Save to database
        int result;
        if (typeModal == 0) {
            // Insert new employee
            result = employeeBUS.insert(employee, session.employeeRoleId(), session.employeeLoginId());
            if (result == 1) {
                NotificationUtils.showInfoAlert("Thêm nhân viên thành công", AppMessages.DIALOG_TITLE);
                isSaved = true;
                handleClose();
            } else {
                NotificationUtils.showErrorAlert("Thêm nhân viên thất bại", AppMessages.DIALOG_TITLE);
            }
        } else if (typeModal == 1) {
            // Update existing employee
            result = employeeBUS.update(employee, session.employeeRoleId(), session.employeeLoginId());
            if (result == 1) {
                NotificationUtils.showInfoAlert("Cập nhật nhân viên thành công", AppMessages.DIALOG_TITLE);
                isSaved = true;
                handleClose();
            } else {
                NotificationUtils.showErrorAlert("Cập nhật nhân viên thất bại", AppMessages.DIALOG_TITLE);
            }
        }
    }

    private void handleClose() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}