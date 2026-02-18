package GUI;

import java.time.LocalDate;
import java.util.ArrayList;

import BUS.CustomerBUS;
import BUS.StatusBUS;
import DTO.CustomerDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IModalController;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

public class CustomerModalController implements IModalController {
    // FXML Controls
    @FXML
    private Label modalName;
    @FXML
    private TextField txtCustomerId;
    @FXML
    private TextField txtFirstName;
    @FXML
    private TextField txtLastName;
    @FXML
    private DatePicker dateOfBirth;
    @FXML
    private TextField txtPhone;
    @FXML
    private TextField txtAddress;
    @FXML
    private TextField txtUpdatedAt;
    @FXML
    private Button closeBtn;
    @FXML
    private Button saveBtn;
    @FXML
    private HBox updatedAt;
    @FXML
    private ComboBox<StatusDTO> cbSelectStatus;
    @FXML
    private StackPane loadingOverlay;

    // State variables
    @Getter
    private boolean isSaved;
    @Getter
    private String resultMessage; // Lưu message để trả về stage cha
    private int typeModal; // 0: Add, 1: Edit
    private StatusBUS statusBus;
    private CustomerBUS customerBus;
    private CustomerDTO customer; // Biến này sẽ lưu thông tin khách hàng đang được sửa (nếu có)
    private ValidationUtils validator;

    @FXML
    public void initialize() {
        statusBus = StatusBUS.getInstance();
        customerBus = CustomerBUS.getInstance();
        validator = ValidationUtils.getInstance();
        loadComboBox();
        setupListeners();
    }

    private void setupListeners() {
        saveBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
    }

    private void loadComboBox() {
        ArrayList<StatusDTO> statusOptions = statusBus.getAllByType(StatusType.CUSTOMER);
        ObservableList<StatusDTO> options = FXCollections.observableArrayList(statusOptions);
        cbSelectStatus.setItems(options);
        cbSelectStatus.getSelectionModel().selectFirst();
    }

    public void setTypeModal(int type) {
        if (type != 0 && type != 1)
            handleClose();
        typeModal = type;
        if (typeModal == 0) {
            modalName.setText("Thêm khách hàng");
            UiUtils.gI().setVisibleItem(updatedAt);
            txtCustomerId.setText(String.valueOf(customerBus.nextId()));
        } else {
            modalName.setText("Sửa khách hàng");
        }
    }

    public void setCustomer(int customerId) {
        if (customerId <= 0) {
            handleClose();
            return;
        }

        this.customer = customerBus.getById(customerId);
        if (this.customer == null) {
            NotificationUtils.showErrorAlert(AppMessages.NOT_FOUND, AppMessages.DIALOG_TITLE);
            handleClose();
            return;
        }
        txtCustomerId.setText(String.valueOf(customer.getId()));
        txtFirstName.setText(customer.getFirstName());
        txtLastName.setText(customer.getLastName());

        if (customer.getDateOfBirth() != null) {
            dateOfBirth.setValue(customer.getDateOfBirth());
        } else {
            dateOfBirth.setValue(null);
        }

        txtPhone.setText(customer.getPhone());
        txtAddress.setText(customer.getAddress());
        StatusDTO statusToSelect = statusBus.getById(customer.getStatusId());
        if (statusToSelect != null) {
            cbSelectStatus.getItems().stream()
                    .filter(item -> item != null && item.getId() == statusToSelect.getId())
                    .findFirst()
                    .ifPresent(item -> cbSelectStatus.getSelectionModel().select(item));
        }
        txtUpdatedAt.setText(customer.getUpdatedAt() != null
                ? ValidationUtils.getInstance().formatDateTimeWithHour(customer.getUpdatedAt())
                : "Chưa cập nhật");

    }

    private boolean isValidInput() {
        boolean isValid = true;
        String firstName = txtFirstName.getText().trim();
        String lastName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();
        String address = txtAddress.getText().trim();

        // Validate Họ
        if (firstName.isEmpty()) {
            NotificationUtils.showErrorAlert("Họ khách hàng không được để trống.", AppMessages.DIALOG_TITLE);
            clearAndFocus(txtFirstName);
            isValid = false;
        } else if (!validator.validateVietnameseText100(firstName)) {
            NotificationUtils.showErrorAlert("Họ không hợp lệ (tối đa 100 ký tự).", AppMessages.DIALOG_TITLE);
            clearAndFocus(txtFirstName);
            isValid = false;
        }

        // Validate Tên
        if (isValid) {
            if (lastName.isEmpty()) {
                NotificationUtils.showErrorAlert("Tên khách hàng không được để trống.", AppMessages.DIALOG_TITLE);
                clearAndFocus(txtLastName);
                isValid = false;
            } else if (!validator.validateVietnameseText100(lastName)) {
                NotificationUtils.showErrorAlert("Tên không hợp lệ (tối đa 100 ký tự).", AppMessages.DIALOG_TITLE);
                clearAndFocus(txtLastName);
                isValid = false;
            }
        }

        // Validate Số điện thoại
        if (isValid) {
            if (phone.isEmpty()) {
                NotificationUtils.showErrorAlert("Số điện thoại không được để trống.", AppMessages.DIALOG_TITLE);
                clearAndFocus(txtPhone);
                isValid = false;
            } else if (!validator.validateVietnamesePhoneNumber(phone)) {
                NotificationUtils.showErrorAlert("Số điện thoại không hợp lệ (Số 0 đứng đầu và theo sau 9 ký tự).",
                        AppMessages.DIALOG_TITLE);
                clearAndFocus(txtPhone);
                isValid = false;
            }
        }

        // Validate Địa chỉ (Chỉ validate nếu có nhập)
        if (isValid && !address.isEmpty()) {
            if (!validator.validateVietnameseText255(address)) {
                NotificationUtils.showErrorAlert("Địa chỉ không hợp lệ (tối đa 255 ký tự).", AppMessages.DIALOG_TITLE);
                clearAndFocus(txtAddress);
                isValid = false;
            }
        }

        // Kiểm tra ngày sinh (Chỉ validate nếu có chọn)
        if (isValid) {
            LocalDate date = dateOfBirth.getValue();
            if (date != null && !validator.validateDateOfBirth(date)) {
                NotificationUtils.showErrorAlert("Ngày sinh không hợp lệ (phải trước ngày hiện tại).",
                        AppMessages.DIALOG_TITLE);
                isValid = false;
            }
        }

        return isValid;
    }

    private void handleSave() {
        if (typeModal == 0) {
            insertCustomer();
        } else {
            updateCustomer();
        }
    }

    private void insertCustomer() {
        if (!isValidInput())
            return;

        String fName = txtFirstName.getText().trim();
        String lName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();
        String addressRaw = txtAddress.getText();
        String address = (addressRaw == null || addressRaw.trim().isEmpty()) ? null : addressRaw.trim();

        CustomerDTO temp = new CustomerDTO(
                -1,
                fName,
                lName,
                phone,
                address,
                dateOfBirth.getValue(),
                cbSelectStatus.getValue().getId());

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.CUSTOMER_INSERT,
                () -> customerBus.insert(temp),
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

    private void updateCustomer() {
        if (!isValidInput())
            return;

        String fName = txtFirstName.getText().trim();
        String lName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();
        String addressRaw = txtAddress.getText();
        String address = (addressRaw == null || addressRaw.trim().isEmpty()) ? null : addressRaw.trim();

        CustomerDTO temp = new CustomerDTO(
                customer.getId(),
                fName,
                lName,
                phone,
                address,
                dateOfBirth.getValue(),
                cbSelectStatus.getValue().getId());

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.CUSTOMER_UPDATE,
                () -> customerBus.update(temp),
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

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }

    private void clearAndFocus(TextField field) {
        field.requestFocus();
    }
}