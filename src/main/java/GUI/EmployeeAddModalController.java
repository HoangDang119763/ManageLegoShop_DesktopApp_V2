package GUI;

import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.PositionBUS;
import BUS.RoleBUS;
import BUS.StatusBUS;
import DTO.AccountDTO;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.PositionDTO;
import DTO.RoleDTO;
import DTO.StatusDTO;
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
    private ComboBox<PositionDTO> cbPosition;
    @FXML
    private ComboBox<StatusDTO> cbStatus;
    @FXML
    private TextField txtBaseSalary;

    // ==================== INSURANCE & BENEFITS ====================
    @FXML
    private CheckBox cbMealSupport;
    @FXML
    private CheckBox cbTransportSupport;
    @FXML
    private CheckBox cbAccommodationSupport;
    @FXML
    private TextField txtNumDependents;
    @FXML
    private TextField txtHealthInsCode;
    @FXML
    private TextField txtSocialInsCode;
    @FXML
    private TextField txtUnemploymentInsCode;

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
    @FXML
    private ComboBox<RoleDTO> cbRole;

    // ==================== BUTTONS & OVERLAY ====================
    @FXML
    private Button btnSaveAll;
    @FXML
    private StackPane loadingOverlay;

    @Getter
    private boolean isSaved;
    @Getter
    private String resultMessage = "";

    private final ValidationUtils validator = ValidationUtils.getInstance();

    @FXML
    public void initialize() {
        // Set avatar ImageView properties
        imgAvatar.setPreserveRatio(false);

        setupListeners();
        loadComboBoxData();
        generateEmployeeId();
        setupInitialValues();
    }

    private void setupListeners() {
        closeBtn.setOnAction(e -> handleClose());
        btnSaveAll.setOnAction(e -> handleSaveAll());

        // Auto-fill salary info khi chọn vị trí
        cbDepartment.setOnAction(e -> updateSalaryInfo());
        cbPosition.setOnAction(e -> updateSalaryInfo());

        // Cảnh báo khi chọn phòng ban bị vô hiệu hóa
        attachDepartmentWarning();

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

        // Load Positions
        ArrayList<PositionDTO> positions = PositionBUS.getInstance().getAll();
        cbPosition.setItems(FXCollections.observableArrayList(positions));
        cbPosition.setConverter(new javafx.util.StringConverter<PositionDTO>() {
            @Override
            public String toString(PositionDTO pos) {
                return pos == null ? "" : pos.getName();
            }

            @Override
            public PositionDTO fromString(String string) {
                return null;
            }
        });
        if (!positions.isEmpty()) {
            cbPosition.getSelectionModel().selectFirst();
        }

        // Load Roles for Account
        ArrayList<RoleDTO> roles = RoleBUS.getInstance().getAll();
        if (SessionManagerService.getInstance().employeeRoleId() != 1)
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
    private void attachDepartmentWarning() {
        int inactiveDeptId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.DEPARTMENT, Status.Department.INACTIVE).getId();
        UiUtils.gI().addSmartInactiveWarningListener(
                cbDepartment,
                DepartmentDTO::getId,
                DepartmentDTO::getStatusId,
                inactiveDeptId,
                -1,
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

    private void updateSalaryInfo() {
        PositionDTO position = cbPosition.getValue();

        if (position != null) {
            txtBaseSalary.setText(validator.formatCurrency(position.getWage()));
        } else {
            txtBaseSalary.setText("");
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

        Integer deptId = cbDepartment.getValue() != null ? cbDepartment.getValue().getId() : null;
        Integer positionId = cbPosition.getValue() != null ? cbPosition.getValue().getId() : null;
        int statusId = cbStatus.getValue().getId();

        // Các trường bảo hiểm từ TextFields và CheckBoxes
        String healthCode = txtHealthInsCode.getText().trim();
        String socialCode = txtSocialInsCode.getText().trim();
        String unemploymentCode = txtUnemploymentInsCode.getText().trim();
        boolean isMeal = cbMealSupport.isSelected();
        boolean isTransport = cbTransportSupport.isSelected();
        boolean isAccommodation = cbAccommodationSupport.isSelected();
        int numDependents = Integer.parseInt(txtNumDependents.getText().trim());

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

        // Khởi tạo EmployeeDTO với constructor đầy đủ
        EmployeeDTO tempEmployee = new EmployeeDTO(
                0, // id
                firstName,
                lastName,
                phone,
                email,
                dob,
                deptId,
                statusId,
                gender,
                null, // account_id
                finalAvatarUrl,
                positionId,
                healthCode,
                socialCode,
                unemploymentCode,
                isMeal,
                isTransport,
                isAccommodation,
                numDependents,
                null, // created_at
                null // updated_at
        );

        AccountDTO tempAccount = new AccountDTO(-1, txtUsername.getText().trim(), "",
                cbAccountStatus.getValue().getId(), cbRole.getValue().getId());

        // Hiển thị loading
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_INSERT,
                () -> EmployeeBUS.getInstance().insertEmployeeFull(tempEmployee, tempAccount), result -> {
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
        if (healthIns.length() > 15) {
            NotificationUtils.showErrorAlert("Mã số BHYT không được vượt quá 15 ký tự.", "Thông báo");
            clearAndFocus(txtHealthInsCode);
            return false;
        }

        // 7. Kiểm tra Mã số xã hội (NẾU CÓ NHẬP - Giả sử tối đa 15 ký tự)
        String socialIns = txtSocialInsCode.getText().trim();
        if (socialIns.length() > 15) {
            NotificationUtils.showErrorAlert("Mã số xã hội không được vượt quá 15 ký tự.", "Thông báo");
            clearAndFocus(txtSocialInsCode);
            return false;
        }

        // 7. Kiểm tra Mã số thất nghiệp (NẾU CÓ NHẬP - Giả sử tối đa 15 ký tự)
        String unemploymentIns = txtUnemploymentInsCode.getText().trim();
        if (unemploymentIns.length() > 15) {
            NotificationUtils.showErrorAlert("Mã số thất nghiệp không được vượt quá 15 ký tự.", "Thông báo");
            clearAndFocus(txtUnemploymentInsCode);
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

}
