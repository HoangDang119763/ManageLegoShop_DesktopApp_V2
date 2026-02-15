package GUI;

import BUS.StatusBUS;
import BUS.SupplierBUS;
import DTO.BUSResult;
import DTO.StatusDTO;
import DTO.SupplierDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IModalController;
import SERVICE.SecureExecutor;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;

public class SupplierModalController implements IModalController {
    @FXML
    private Label modalName;
    @FXML
    private TextField txtSupplierId;
    @FXML
    private TextField txtCompanyName;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtAddress;
    @FXML
    private ComboBox<StatusDTO> cbSelectStatus;
    @FXML
    private Button saveBtn, closeBtn;

    @Getter
    private boolean isSaved;
    private int typeModal; // 0: Add, 1: Edit, 2: Detail
    private SupplierDTO supplier;

    private SupplierBUS supplierBUS;
    private StatusBUS statusBUS;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        supplierBUS = SupplierBUS.getInstance();
        statusBUS = StatusBUS.getInstance();

        loadComboBox();
        setupListeners();
    }

    // =====================
    // 2️⃣ UI SETUP
    // =====================
    private void loadComboBox() {
        ArrayList<StatusDTO> statusOptions = statusBUS.getAllByType(StatusType.SUPPLIER);
        cbSelectStatus.setItems(FXCollections.observableArrayList(statusOptions));
        cbSelectStatus.getSelectionModel().selectFirst();
    }

    public void setupListeners() {
        saveBtn.setOnAction(e -> {
            try {
                handleSave();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        closeBtn.setOnAction(e -> handleClose());
    }

    // =====================
    // 3️⃣ MODAL CONFIGURATION
    // =====================
    @Override
    public void setTypeModal(int type) {
        if (type != 0 && type != 1 && type != 2) {
            handleClose();
        }
        typeModal = type;

        if (typeModal == 0) {
            modalName.setText("THÊM NHÀ CUNG CẤP");
            txtSupplierId.setText(String.valueOf(supplierBUS.getAll().size() + 1));
        } else if (typeModal == 1) {
            modalName.setText("SỬA NHÀ CUNG CẤP");
        } else if (typeModal == 2) {
            modalName.setText("CHI TIẾT NHÀ CUNG CẤP");
        }
    }

    public void setSupplier(SupplierDTO supplier) {
        this.supplier = supplier;
        if (supplier == null)
            return;

        txtSupplierId.setText(String.valueOf(supplier.getId()));
        txtCompanyName.setText(supplier.getName());
        txtPhone.setText(supplier.getPhone());
        txtEmail.setText(supplier.getEmail() != null ? supplier.getEmail() : "");
        txtAddress.setText(supplier.getAddress());

        cbSelectStatus.getItems().stream()
                .filter(item -> item.getId() == supplier.getStatusId())
                .findFirst()
                .ifPresent(item -> cbSelectStatus.getSelectionModel().select(item));

        if (typeModal == 2) {
            setupReadOnlyMode();
        }
    }

    private void setupReadOnlyMode() {
        txtCompanyName.setEditable(false);
        txtPhone.setEditable(false);
        txtEmail.setEditable(false);
        txtAddress.setEditable(false);

        cbSelectStatus.setMouseTransparent(true);
        cbSelectStatus.setFocusTraversable(false);

        // Remove save button in detail mode
        UiUtils.gI().setVisibleItem(saveBtn);
    }

    // =====================
    // 4️⃣ EVENT HANDLERS
    // =====================
    private void handleSave() throws IOException {
        if (typeModal == 0) {
            insertSupplier();
        } else {
            updateSupplier();
        }
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }

    // =====================
    // 5️⃣ VALIDATION & BUSINESS LOGIC
    // =====================
    private boolean isValidInput() {
        String name = txtCompanyName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();
        String email = txtEmail.getText().trim();
        ValidationUtils validator = ValidationUtils.getInstance();

        if (name.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên nhà cung cấp không được để trống.", AppMessages.DIALOG_TITLE);
            focus(txtCompanyName);
            return false;
        }

        if (phone.isEmpty()) {
            NotificationUtils.showErrorAlert("Số điện thoại không được để trống.", AppMessages.DIALOG_TITLE);
            focus(txtPhone);
            return false;
        } else if (!validator.validateVietnamesePhoneNumber(phone)) {
            NotificationUtils.showErrorAlert("Số điện thoại không hợp lệ.", AppMessages.DIALOG_TITLE);
            focus(txtPhone);
            return false;
        }

        if (!email.isEmpty() && !validator.validateEmail(email)) {
            NotificationUtils.showErrorAlert("Email không hợp lệ.", AppMessages.DIALOG_TITLE);
            focus(txtEmail);
            return false;
        }

        if (address.isEmpty()) {
            NotificationUtils.showErrorAlert("Địa chỉ không được để trống.", AppMessages.DIALOG_TITLE);
            focus(txtAddress);
            return false;
        }

        if (cbSelectStatus.getValue() == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn trạng thái.", AppMessages.DIALOG_TITLE);
            return false;
        }

        return true;
    }

    private void insertSupplier() {
        // if (isValidInput()) {
        // SupplierDTO temp = new SupplierDTO(
        // -1,
        // txtCompanyName.getText().trim(),
        // txtPhone.getText().trim(),
        // txtAddress.getText().trim(),
        // txtEmail.getText().trim(),
        // cbSelectStatus.getValue().getId() == 1);

        // BUSResult result =
        // SecureExecutor.runSafeBUSResult(PermissionKey.SUPPLIER_INSERT,
        // () -> supplierBUS.insert(temp,
        // SessionManagerService.getInstance().employeeRoleId(),
        // SessionManagerService.getInstance().employeeLoginId()));

        // handleResult(result);
        // }
    }

    private void updateSupplier() {
        // if (isValidInput()) {
        // SupplierDTO temp = new SupplierDTO(
        // supplier.getId(),
        // txtCompanyName.getText().trim(),
        // txtPhone.getText().trim(),
        // txtAddress.getText().trim(),
        // txtEmail.getText().trim(),
        // cbSelectStatus.getValue().getId() == 1);

        // BUSResult result =
        // SecureExecutor.runSafeBUSResult(PermissionKey.SUPPLIER_UPDATE,
        // () -> supplierBUS.update(temp,
        // SessionManagerService.getInstance().employeeRoleId(),
        // SessionManagerService.getInstance().employeeLoginId()));

        // handleResult(result);
        // }
    }

    private void handleResult(BUSResult result) {
        if (result.isSuccess()) {
            NotificationUtils.showInfoAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
            isSaved = true;
            handleClose();
        } else {
            NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
        }
    }

    private void focus(TextField field) {
        field.requestFocus();
    }
}