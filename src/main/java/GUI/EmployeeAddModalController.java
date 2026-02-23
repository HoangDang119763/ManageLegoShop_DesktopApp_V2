package GUI;

import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.RoleBUS;
import BUS.SalaryBUS;
import BUS.StatusBUS;
import DTO.AccountDTO;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.RoleDTO;
import DTO.SalaryDTO;
import DTO.StatusDTO;
import DTO.TaxDTO;
import ENUM.Gender;
import ENUM.PermissionKey;
import ENUM.Status;
import ENUM.StatusType;
import INTERFACE.IModalController;
import SERVICE.SessionManagerService;
import SERVICE.ImageService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Controller cho modal thêm nhân viên mới
 * Form đơn giản chứa tất cả thông tin cần thiết
 */
public class EmployeeAddModalController implements IModalController {
    // ==================== TOP SECTION ====================
    @FXML
    private Label modalName;
    @FXML
    private Button closeBtn;

    // ==================== BASIC INFO ====================
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

    // ==================== POSITION & SALARY ====================
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

    // ==================== INSURANCE & BENEFITS ====================
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
    private TextField txtNumDependents;
    @FXML
    private TextField txtHealthInsCode;

    // ==================== AVATAR & IMAGE ====================
    @FXML
    private ImageView imgAvatar; // Ảnh đại diện nhân viên
    @FXML
    private Button choseImg; // Nút chọn ảnh
    @FXML
    private Button resetImgBtn; // Nút reset ảnh
    @FXML
    private HBox functionImg; // Container chứa các button ảnh
    private String avatarUrl = null; // Đường dẫn ảnh tạm thời

    // ==================== ACCOUNT INFO ====================
    @FXML
    private TextField txtUsername;
    @FXML
    private ComboBox<StatusDTO> cbAccountStatus;

    // ==================== BUTTONS & OVERLAY ====================
    @FXML
    private Button btnSaveAll;
    @FXML
    private StackPane loadingOverlay;

    @Getter
    private boolean isSaved;
    @Getter
    private String resultMessage = "";

    private ValidationUtils validator = ValidationUtils.getInstance();
    private EmployeeDTO newEmployee;

    @FXML
    public void initialize() {
        // Set avatar ImageView properties
        imgAvatar.setPreserveRatio(false);

        setupListeners();
        loadComboBoxData();
        generateEmployeeId();
        setupInitialValues();
        UiUtils.gI().setReadOnlyItem(cbHealthIns);
    }

    private void setupListeners() {
        closeBtn.setOnAction(e -> handleClose());
        btnSaveAll.setOnAction(e -> handleSaveAll());

        // Auto-fill salary info khi chọn vị trí
        cbDepartment.setOnAction(e -> updateSalaryInfo());
        cbRole.setOnAction(e -> updateSalaryInfo());

        // Cảnh báo khi chọn phòng ban bị vô hiệu hóa
        attachDepartmentWarning(-1);

        // Auto-tick Social Insurance khi Health Insurance Code có giá trị hợp lệ
        txtHealthInsCode.textProperty().addListener((observable, oldValue, newValue) -> {
            cbHealthIns.setSelected(!newValue.trim().isEmpty());
        });

        // Alternative: Trigger khi nhập text (real-time)
        txtHealthInsCode.textProperty().addListener((obs, oldVal, newVal) -> {
            String trimmed = newVal != null ? newVal.trim() : "";
            // Auto-tick nếu có giá trị hợp lệ
            if (!trimmed.isEmpty() && trimmed.length() <= 15) {
                cbSocialIns.setSelected(true);
            }
        });

        // Avatar button listeners
        choseImg.setOnAction(e -> handleChooseAvatar());
        resetImgBtn.setOnAction(e -> handleResetAvatar());
    }

    private void loadComboBoxData() {
        // Load Gender
        for (Gender gender : Gender.values()) {
            cbGender.getItems().add(gender.getDisplayName());
        }
        cbGender.getSelectionModel().selectFirst();

        // Load Departments - với format inactive items
        setupComboBoxData();

        // Load Roles
        ArrayList<RoleDTO> roles = RoleBUS.getInstance().getAll();
        if (SessionManagerService.getInstance().getRoleId() != 1)
            roles.removeIf(role -> role.getId() == 1); // Nếu không phải admin thì không cho chọn role admin
        cbRole.setItems(FXCollections.observableArrayList(roles));
        cbRole.getSelectionModel().selectFirst();

        // Load Status
        ArrayList<StatusDTO> statuses = StatusBUS.getInstance().getAllByType(StatusType.EMPLOYEE);
        cbStatus.setItems(FXCollections.observableArrayList(statuses));

        cbStatus.getSelectionModel().selectFirst();

        // Load Account Status
        ArrayList<StatusDTO> accountStatuses = StatusBUS.getInstance().getAllByType(StatusType.ACCOUNT);
        cbAccountStatus.setItems(FXCollections.observableArrayList(accountStatuses));
        cbAccountStatus.getSelectionModel().selectFirst();
    }

    /**
     * Load department data và format inactive items
     * Áp dụng pattern từ ProductModalController
     */
    private void setupComboBoxData() {
        ArrayList<DepartmentDTO> departments = DepartmentBUS.getInstance().getAll();
        cbDepartment.setItems(FXCollections.observableArrayList(departments));

        // Format display: ghi lên tên phòng ban với style inactive item (xám, in
        // nghiêng)
        int inactiveDeptId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.DEPARTMENT, Status.Department.INACTIVE).getId();
        UiUtils.gI().formatInactiveComboBox(
                cbDepartment,
                DepartmentDTO::getName,
                DepartmentDTO::getStatusId,
                inactiveDeptId);

        if (!departments.isEmpty()) {
            cbDepartment.getSelectionModel().selectFirst();
        }
    }

    /**
     * Cảnh báo thông minh khi chọn phòng ban bị vô hiệu hóa
     * (Tương tự attachCategoryWarning trong ProductModalController)
     */
    private void attachDepartmentWarning(Integer initialDeptStatusId) {
        int inactiveDeptId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.DEPARTMENT, Status.Department.INACTIVE).getId();
        UiUtils.gI().addSmartInactiveWarningListener(
                cbDepartment,
                DepartmentDTO::getId,
                DepartmentDTO::getStatusId,
                inactiveDeptId,
                initialDeptStatusId,
                AppMessages.DEPARTMENT_DELETED_WARNING);
    }

    private void setupInitialValues() {
        // Default values
        dpDateOfBirth.setValue(LocalDate.now().minusYears(25));
        txtNumDependents.setText("0");
        updateSalaryInfo();
    }

    private void generateEmployeeId() {
        // Lấy ID tiếp theo từ BUS
        int nextId = EmployeeBUS.getInstance().nextId();
        txtEmployeeId.setText(String.valueOf(nextId));
    }

    private void updateUsername() {
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();

        if (!firstName.isEmpty() && !lastName.isEmpty()) {
            // Username: firstname.lastname
            String username = firstName.toLowerCase() + "." + lastName.toLowerCase();
            txtUsername.setText(username.replaceAll("\\s+", ""));
        }
    }

    private void updateSalaryInfo() {
        DepartmentDTO dept = cbDepartment.getValue();
        RoleDTO role = cbRole.getValue();

        if (dept != null && role != null) {
            SalaryDTO salary = SalaryBUS.getInstance().getById(cbRole.getValue().getSalaryId());
            if (salary != null) {
                txtBaseSalary.setText(validator.formatCurrency(salary.getBase()));
                txtCoefficient.setText(validator.formatCurrency(salary.getCoefficient()));
            }
        }
    }

    private void handleSaveAll() {
        if (!isValidInput()) {
            return;
        }

        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        LocalDate dob = dpDateOfBirth.getValue();
        String gender = cbGender.getValue() != null ? cbGender.getValue().toString() : null;

        int roleId = cbRole.getValue().getId();
        Integer deptId = cbDepartment.getValue() != null ? cbDepartment.getValue().getId() : null;
        int statusId = cbStatus.getValue().getId();

        // Các trường bảo hiểm từ CheckBox
        boolean isSocial = cbSocialIns.isSelected();
        boolean isUnemployment = cbUnemploymentIns.isSelected();
        boolean isPIT = cbPersonalTax.isSelected();
        boolean isTransport = cbTransportSupport.isSelected();
        boolean isAccommodation = cbAccommodationSupport.isSelected();
        String healthCode = txtHealthInsCode.getText().trim();

        // Xử lý Avatar (tùy chọn - không bắt buộc)
        String finalAvatarUrl = null;
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            try {
                finalAvatarUrl = ImageService.gI().saveEmployeeAvatar(txtEmployeeId.getText().trim(), avatarUrl);
            } catch (IOException ex) {
                NotificationUtils.showErrorAlert("Lỗi lưu ảnh: " + ex.getMessage(), AppMessages.DIALOG_TITLE);
                return;
            }
        }

        // Khởi tạo Object dùng constructor với avatarUrl
        EmployeeDTO tempEmployee = new EmployeeDTO(
                0, // id
                firstName, // first_name
                lastName, // last_name
                phone, // phone
                email, // email
                dob, // date_of_birth
                roleId, // role_id
                deptId, // department_id
                statusId, // status_id
                gender, // gender
                null, // account_id (Sẽ được gán sau khi tạo Account thành công)
                healthCode, // health_ins_code
                isSocial, // is_social_insurance
                isUnemployment, // is_unemployment_insurance
                isPIT, // is_personal_income_tax
                isTransport, // is_transportation_support
                isAccommodation, // is_accommodation_support
                null, // created_at
                null, // updated_at
                finalAvatarUrl // avatar_url
        );
        TaxDTO tempTax = new TaxDTO(-1, -1, Integer.parseInt(txtNumDependents.getText().trim()));
        AccountDTO tempAccount = new AccountDTO(-1, txtUsername.getText().trim(), "",
                cbAccountStatus.getValue().getId());

        // Hiển thị loading
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_INSERT,
                () -> EmployeeBUS.getInstance().insertEmployeeFull(tempEmployee, tempAccount, tempTax), result -> {
                    if (result.isSuccess()) {
                        this.isSaved = true;
                        this.resultMessage = result.getMessage();
                        handleClose();
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    private boolean isValidInput() {
        ValidationUtils validator = ValidationUtils.getInstance();

        // 1. Kiểm tra Họ đệm (Bắt buộc)
        String firstName = txtFirstName.getText().trim();
        if (firstName.isEmpty()) {
            NotificationUtils.showErrorAlert("Họ đệm không được để trống.", "Thông báo");
            clearAndFocus(txtFirstName);
            return false;
        }
        if (!validator.validateVietnameseText100(firstName)) {
            NotificationUtils.showErrorAlert("Họ đệm không hợp lệ (tối đa 100 ký tự).", "Thông báo");
            clearAndFocus(txtFirstName);
            return false;
        }

        // 2. Kiểm tra Tên (Bắt buộc)
        String lastName = txtLastName.getText().trim();
        if (lastName.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên không được để trống.", "Thông báo");
            clearAndFocus(txtLastName);
            return false;
        }
        if (!validator.validateVietnameseText100(lastName)) {
            NotificationUtils.showErrorAlert("Tên không hợp lệ (tối đa 100 ký tự).", "Thông báo");
            clearAndFocus(txtLastName);
            return false;
        }

        // 3. Kiểm tra Số điện thoại (Bắt buộc theo UI của bạn có dấu *)
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            NotificationUtils.showErrorAlert("Số điện thoại không được để trống.", "Thông báo");
            clearAndFocus(txtPhone);
            return false;
        }
        if (!validator.validateVietnamesePhoneNumber(phone)) {
            NotificationUtils.showErrorAlert("Số điện thoại không hợp lệ (10-11 chữ số).", "Thông báo");
            clearAndFocus(txtPhone);
            return false;
        }

        // 4. Kiểm tra Email (BẮT BUỘC ĐIỀN)
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            NotificationUtils.showErrorAlert("Email không được để trống.", "Thông báo");
            clearAndFocus(txtEmail);
            return false;
        }
        if (!validator.validateEmail(email)) {
            NotificationUtils.showErrorAlert("Định dạng email không hợp lệ.", "Thông báo");
            clearAndFocus(txtEmail);
            return false;
        }

        // 5. Kiểm tra Ngày sinh (BẮT BUỘC CHỌN)
        LocalDate dob = dpDateOfBirth.getValue();
        if (dob == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn ngày sinh.", "Thông báo");
            dpDateOfBirth.requestFocus();
            return false;
        }
        // Validate logic nghiệp vụ (Ví dụ: phải đủ 18 tuổi hoặc chỉ cần trước ngày hiện
        // tại)
        if (!validator.validateDateOfBirth(dob)) {
            NotificationUtils.showErrorAlert("Ngày sinh không hợp lệ (phải đủ 18 tuổi và trước ngày hiện tại).",
                    "Thông báo");
            return false;
        }

        // 6. Kiểm tra Số người phụ thuộc (Bắt buộc vì có giá trị mặc định là 0)
        try {
            String depStr = txtNumDependents.getText().trim();
            int dependents = depStr.isEmpty() ? 0 : Integer.parseInt(depStr);
            if (dependents < 0) {
                NotificationUtils.showErrorAlert("Số người phụ thuộc không được âm.", "Thông báo");
                return false;
            }
        } catch (NumberFormatException e) {
            NotificationUtils.showErrorAlert("Số người phụ thuộc phải là số nguyên.", "Thông báo");
            return false;
        }

        // 7. Kiểm tra Mã số BHYT (NẾU CÓ NHẬP - Giả sử tối đa 15 ký tự)
        String healthIns = txtHealthInsCode.getText().trim();
        if (!healthIns.isEmpty() && healthIns.length() > 15) {
            NotificationUtils.showErrorAlert("Mã số BHYT không được vượt quá 15 ký tự.", "Thông báo");
            clearAndFocus(txtHealthInsCode);
            return false;
        }

        // 8. Kiểm tra Username (Bắt buộc)
        String username = txtUsername.getText().trim();
        if (username.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên đăng nhập không được để trống.", "Thông báo");
            clearAndFocus(txtUsername);
            return false;
        } else if (!validator.validateUsername(username, 4, 50)) {
            NotificationUtils.showErrorAlert("Tên đăng nhập không hợp lệ (4-50 ký tự, không chứa khoảng trắng).",
                    "Thông báo");
            clearAndFocus(txtUsername);
            return false;
        }

        return true;
    }

    private void clearAndFocus(TextField textField) {
        textField.requestFocus();
        textField.selectAll();
    }

    @Override
    public void setTypeModal(int type) {
        // Không dùng trong Add Modal
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
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
        if (!UiUtils.gI().showConfirmAlert("Bạn có chắc muốn reset ảnh đại diện?", AppMessages.DIALOG_TITLE_CONFIRM)) {
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
            avatarUrl = null;
            NotificationUtils.showInfoAlert("Ảnh đại diện đã được reset", AppMessages.DIALOG_TITLE);
        }
    }

    /**
     * Load và hiển thị ảnh đại diện
     * (Tham khảo logic từ ProductModalController)
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
