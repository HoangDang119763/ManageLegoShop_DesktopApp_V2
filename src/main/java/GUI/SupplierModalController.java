package GUI;

import BUS.StatusBUS;
import BUS.SupplierBUS;
import DTO.StatusDTO;
import DTO.SupplierDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IModalController;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
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
    @FXML
    public StackPane loadingOverlay;

    @Getter
    private boolean isSaved;
    @Getter
    private String resultMessage;

    private int typeModal; // 0: Add, 1: Edit,
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
        if (type != 0 && type != 1) {
            handleClose();
        }
        typeModal = type;

        if (typeModal == 0) {
            modalName.setText("Thêm nhà cung cấp");
            txtSupplierId.setText(String.valueOf(supplierBUS.nextId()));
        } else if (typeModal == 1) {
            modalName.setText("Sửa nhà cung cấp");
        }
    }

    public void setSupplier(int id) {
        this.supplier = supplierBUS.getById(id);
        if (supplier == null)
            return;
        if (this.supplier == null) {
            NotificationUtils.showErrorAlert(AppMessages.NOT_FOUND, AppMessages.DIALOG_TITLE);
            handleClose();
            return;
        }
        txtSupplierId.setText(String.valueOf(supplier.getId()));
        txtCompanyName.setText(supplier.getName());
        txtPhone.setText(supplier.getPhone());
        txtEmail.setText(supplier.getEmail() != null ? supplier.getEmail() : "");
        txtAddress.setText(supplier.getAddress());

        cbSelectStatus.getItems().stream()
                .filter(item -> item.getId() == supplier.getStatusId())
                .findFirst()
                .ifPresent(item -> cbSelectStatus.getSelectionModel().select(item));
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
        ValidationUtils validator = ValidationUtils.getInstance();

        // 1. Kiểm tra Tên (Sai là đá luôn)
        String name = txtCompanyName.getText().trim();
        if (name.isEmpty()) {
            showError("Tên nhà cung cấp không được để trống.", txtCompanyName);
            return false;
        }
        if (!validator.validateVietnameseText100(name)) {
            showError("Tên không hợp lệ (tối đa 100 ký tự).", txtCompanyName);
            return false;
        }

        // 2. Kiểm tra Số điện thoại (Sai là đá luôn)
        String phone = txtPhone.getText().trim();
        if (phone.isEmpty()) {
            showError("Số điện thoại không được để trống.", txtPhone);
            return false;
        }
        if (!validator.validateVietnamesePhoneNumber(phone)) {
            showError("Số điện thoại phải bắt đầu bằng 0 và đủ 10 số.", txtPhone);
            return false;
        }

        // 3. Kiểm tra Email (Nếu có nhập thì phải đúng, sai là đá)
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !validator.validateEmail(email)) {
            showError("Định dạng Email không hợp lệ.", txtEmail);
            return false;
        }

        // 4. Kiểm tra Địa chỉ
        String address = txtAddress.getText().trim();
        if (address.isEmpty()) {
            showError("Địa chỉ không được để trống.", txtAddress);
            return false;
        }

        return true; // Đi qua hết các "cửa ải" trên thì mới là đúng
    }

    // Hàm phụ để viết code cho gọn, đỡ lặp lại NotificationUtils
    private void showError(String message, TextField field) {
        NotificationUtils.showErrorAlert(message, "Thông báo");
        field.requestFocus();
        field.selectAll(); // Bôi đen luôn để người dùng gõ đè lên cho nhanh
    }

    private void insertSupplier() {
        if (!isValidInput())
            return;
        String name = txtCompanyName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        int statusId = cbSelectStatus.getValue().getId();
        SupplierDTO temp = new SupplierDTO(
                -1,
                name,
                phone,
                address,
                email,
                statusId);

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.SUPPLIER_INSERT,
                () -> supplierBUS.insert(temp),
                result -> {
                    if (result.isSuccess()) {
                        isSaved = true;
                        resultMessage = result.getMessage();
                        handleClose();
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });

    }

    private void updateSupplier() {
        if (!isValidInput())
            return;
        String name = txtCompanyName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String address = txtAddress.getText().trim();
        int statusId = cbSelectStatus.getValue().getId();
        SupplierDTO temp = new SupplierDTO(
                supplier.getId(),
                name,
                phone,
                address,
                email,
                statusId);

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.SUPPLIER_UPDATE,
                () -> supplierBUS.update(temp),
                result -> {
                    if (result.isSuccess()) {
                        isSaved = true;
                        resultMessage = result.getMessage();
                        handleClose();
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    private void focus(TextField field) {
        field.requestFocus();
    }
}