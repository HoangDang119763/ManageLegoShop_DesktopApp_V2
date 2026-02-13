package GUI;

import java.time.LocalDate;
import java.util.ArrayList;

import BUS.CustomerBUS;
import BUS.StatusBUS;
import DTO.BUSResult;
import DTO.CustomerDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IModalController;
import SERVICE.SecureExecutor;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
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
import javafx.stage.Stage;
import lombok.Getter;

public class CustomerModalController implements IModalController {
    // FXML Controls
    @FXML
    public Label modalName;
    @FXML
    public TextField txtCustomerId;
    @FXML
    public TextField txtFirstName;
    @FXML
    public TextField txtLastName;
    @FXML
    public DatePicker dateOfBirth;
    @FXML
    public TextField txtPhone;
    @FXML
    public TextField txtAddress;
    @FXML
    public Button closeBtn;
    @FXML
    public Button saveBtn;
    @FXML
    public HBox updatedAt;
    @FXML
    public ComboBox<StatusDTO> cbSelectStatus;

    // State variables
    @Getter
    private boolean isSaved;
    private int typeModal; // 0: Add, 1: Edit
    private CustomerDTO customer;
    private StatusBUS statusBus;
    private CustomerBUS customerBus;
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
        ArrayList<StatusDTO> statusOptions = statusBus.getAllByTypeLocal(StatusType.CUSTOMER);
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
            txtCustomerId.setText(customerBus.nextId());
            UiUtils.gI().setVisibleItem(updatedAt);
        } else {
            if (customer == null)
                handleClose();
            modalName.setText("Sửa khách hàng");
        }
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
        if (customer != null) {
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
            StatusDTO statusToSelect = statusBus.getByIdLocal(customer.getStatusId());
            if (statusToSelect != null) {
                cbSelectStatus.getItems().stream()
                        .filter(item -> item != null && item.getId() == statusToSelect.getId())
                        .findFirst()
                        .ifPresent(item -> cbSelectStatus.getSelectionModel().select(item));
            }
        }
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
            return; // Early return giúp code đỡ bị lồng nhiều khối if

        // 1. Thu thập dữ liệu và xử lý nhanh
        String fName = txtFirstName.getText().trim();
        String lName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();

        // Rút gọn logic xử lý address: trim và chuyển rỗng thành null
        String addressRaw = txtAddress.getText();
        String address = (addressRaw == null || addressRaw.trim().isEmpty()) ? null : addressRaw.trim();

        // 2. Tạo đối tượng Temp (ID -1 là chuẩn cho insert)
        CustomerDTO temp = new CustomerDTO(
                -1,
                fName,
                lName,
                phone,
                address,
                dateOfBirth.getValue(),
                cbSelectStatus.getValue().getId());

        // 3. Thực thi an toàn qua SecureExecutor
        BUSResult insertResult = SecureExecutor.runSafeBUSResult(
                PermissionKey.CUSTOMER_INSERT,
                () -> customerBus.insert(temp));

        // 4. Xử lý phản hồi từ BUS
        if (insertResult.isSuccess()) {
            NotificationUtils.showInfoAlert(insertResult.getMessage(), AppMessages.DIALOG_TITLE);
            isSaved = true;
            handleClose();
        } else {
            // BUS sẽ trả về lỗi nếu "isExistCustomer" (trùng 5 trường) trả về true
            NotificationUtils.showErrorAlert(insertResult.getMessage(), AppMessages.DIALOG_TITLE);
        }
    }

    private void updateCustomer() {
        // 1. Early Return giúp code thoáng hơn
        if (!isValidInput())
            return;

        // 2. Thu thập và làm sạch dữ liệu (UI Clean-up)
        String fName = txtFirstName.getText().trim();
        String lName = txtLastName.getText().trim();
        String phone = txtPhone.getText().trim();

        // Xử lý địa chỉ an toàn: Tránh NPE và đồng bộ logic null với BUS
        String addressRaw = txtAddress.getText();
        String address = (addressRaw == null || addressRaw.trim().isEmpty()) ? null : addressRaw.trim();

        // 3. Khởi tạo DTO với dữ liệu đã làm sạch
        // Sử dụng customer.getId() để đảm bảo Update đúng đối tượng cũ
        CustomerDTO temp = new CustomerDTO(
                customer.getId(),
                fName,
                lName,
                phone,
                address,
                dateOfBirth.getValue(), // Tối giản: getValue() trả về null nếu không chọn
                cbSelectStatus.getValue().getId());

        // 4. Thực thi qua lớp bảo mật SecureExecutor
        BUSResult updateResult = SecureExecutor.runSafeBUSResult(
                PermissionKey.CUSTOMER_UPDATE,
                () -> customerBus.update(temp));

        // 5. Phản hồi người dùng dựa trên kết quả từ BUS
        if (updateResult.isSuccess()) {
            NotificationUtils.showInfoAlert(updateResult.getMessage(), AppMessages.DIALOG_TITLE);
            isSaved = true;
            handleClose();
        } else {
            // Hiển thị lỗi cụ thể (ví dụ: Trùng 5 trường với khách khác - CONFLICT)
            NotificationUtils.showErrorAlert(updateResult.getMessage(), AppMessages.DIALOG_TITLE);
        }
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