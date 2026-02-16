package GUI;

import BUS.CategoryBUS;
import BUS.StatusBUS;
import DTO.BUSResult;
import DTO.CategoryDTO;
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
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.ArrayList;

public class CategoryModalController implements IModalController {

    // FXML Controls
    @FXML
    public Label modalName;
    @FXML
    public TextField txtCategoryId;
    @FXML
    public TextField txtCategoryName;
    @FXML
    public VBox containerMetadata; // Nên bọc trong HBox để dễ ẩn/hiện
    @FXML
    public HBox containerUpdatedAt;
    @FXML
    public TextField txtCreatedAt;
    @FXML
    public TextField txtUpdatedAt;
    @FXML
    public Button closeBtn;
    @FXML
    public Button saveBtn;
    @FXML
    public ComboBox<StatusDTO> cbSelectStatus;

    // State variables
    @Getter
    private boolean isSaved;
    private int typeModal; // 0: Add, 1: Edit
    private CategoryDTO category;
    private CategoryBUS categoryBus;
    private StatusBUS statusBus;
    private ValidationUtils validator;

    @FXML
    public void initialize() {
        categoryBus = CategoryBUS.getInstance();
        statusBus = StatusBUS.getInstance();
        validator = ValidationUtils.getInstance();

        loadComboBox();
        setupListeners();
    }

    private void setupListeners() {
        saveBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
    }

    private void loadComboBox() {
        ArrayList<StatusDTO> statusOptions = statusBus.getAllByType(StatusType.CATEGORY);
        cbSelectStatus.setItems(FXCollections.observableArrayList(statusOptions));
        cbSelectStatus.getSelectionModel().selectFirst();
    }

    public void setTypeModal(int type) {
        if (type != 0 && type != 1) {
            handleClose();
            return;
        }
        typeModal = type;

        if (typeModal == 0) {
            modalName.setText("Thêm thể loại mới");
            // txtCategoryId.setText(categoryBus.nextId());
            UiUtils.gI().setVisibleItem(containerMetadata);
        } else {
            if (category == null)
                handleClose();
            modalName.setText("Sửa thể loại");
        }
    }

    public void setCategory(int categoryId) {
        // Fetch full CategoryDTO from BUS bằng ID
        if (categoryId <= 0) {
            handleClose();
            return;
        }

        this.category = categoryBus.getById(categoryId);

        if (this.category == null) {
            NotificationUtils.showErrorAlert("Không tìm thấy thể loại.", AppMessages.DIALOG_TITLE);
            handleClose();
            return;
        }

        // Fill form fields
        txtCategoryId.setText(String.valueOf(category.getId()));
        txtCategoryName.setText(category.getName());

        // Chọn Status tương ứng
        StatusDTO statusToSelect = statusBus.getById(category.getStatusId());
        if (statusToSelect != null) {
            cbSelectStatus.getItems().stream()
                    .filter(item -> item.getId() == statusToSelect.getId())
                    .findFirst()
                    .ifPresent(item -> cbSelectStatus.getSelectionModel().select(item));
        }

        // Hiển thị thời gian (đã format)
        txtCreatedAt.setText(validator.formatDateTimeWithHour(category.getCreatedAt()));
        txtUpdatedAt.setText(validator.formatDateTimeWithHour(category.getUpdatedAt()));
    }

    private boolean isValidInput() {
        String name = txtCategoryName.getText().trim();

        // 1. Kiểm tra trống
        if (name.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên thể loại không được để trống.", AppMessages.DIALOG_TITLE);
            clearAndFocus(txtCategoryName);
            return false;
        }

        // 2. Kiểm tra độ dài và định dạng (Sử dụng trực tiếp chuỗi mô tả lỗi)
        if (!validator.validateVietnameseText100(name)) {
            NotificationUtils.showErrorAlert(
                    "Tên thể loại không hợp lệ (tối đa 100 ký tự và không chứa ký tự đặc biệt).",
                    AppMessages.DIALOG_TITLE);
            clearAndFocus(txtCategoryName);
            return false;
        }
        return true;
    }

    private void handleSave() {
        if (typeModal == 0) {
            insertCategory();
        } else {
            updateCategory();
        }
    }

    private void insertCategory() {
        if (!isValidInput())
            return;

        // Chuẩn hóa dữ liệu UI
        String name = txtCategoryName.getText().trim();
        int statusId = cbSelectStatus.getValue().getId();

        CategoryDTO temp = new CategoryDTO(-1, name, statusId);

        // Gọi qua SecureExecutor và BUSResult (Bỏ Switch-Case số cũ)
        BUSResult result = SecureExecutor.executeSafeBusResult(
                PermissionKey.CATEGORY_INSERT,
                () -> categoryBus.insert(temp));

        processResult(result);
    }

    private void updateCategory() {
        if (!isValidInput())
            return;

        String name = txtCategoryName.getText().trim();
        int statusId = cbSelectStatus.getValue().getId();

        // Giữ nguyên ID cũ để Update
        CategoryDTO temp = new CategoryDTO(category.getId(), name, statusId);

        BUSResult result = SecureExecutor.executeSafeBusResult(
                PermissionKey.CATEGORY_UPDATE,
                () -> categoryBus.update(temp));

        processResult(result);
    }

    // Hàm dùng chung để xử lý kết quả trả về từ BUSResult
    private void processResult(BUSResult result) {
        if (result.isSuccess()) {
            NotificationUtils.showInfoAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
            this.isSaved = true;
            handleClose();
        } else {
            NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
        }
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            ((Stage) closeBtn.getScene().getWindow()).close();
        }
    }

    private void clearAndFocus(TextField field) {
        field.requestFocus();
        field.selectAll();
    }
}