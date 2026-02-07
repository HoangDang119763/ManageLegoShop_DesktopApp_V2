package GUI;

import BUS.CategoryBUS;
import BUS.StatusBUS;
import DTO.CategoryDTO;
import DTO.StatusDTO;
import ENUM.StatusType;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.ArrayList;

public class CategoryModalController {

    // FXML Controller
    public Label modalName;
    public TextField txtCategoryId;
    public TextField txtCategoryName;
    public TextField txtCreatedAt;
    public TextField txtUpdatedAt;
    public Button closeBtn;
    public Button saveBtn;
    public ComboBox<StatusDTO> cbSelectStatus;

    // State variables
    @Getter
    private boolean isSaved;
    private int typeModal; // 0: Add, 1: Edit, 2: View
    private CategoryDTO category;

    @FXML
    public void initialize() {
        loadComboBox();
        setupListener();
    }

    private void setupListener() {
        saveBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
    }

    private void loadComboBox() {
        StatusBUS statusBus = StatusBUS.getInstance();
        ArrayList<StatusDTO> statusOptions = statusBus.getAllByTypeLocal(StatusType.CATEGORY);
        cbSelectStatus.setItems(FXCollections.observableArrayList(statusOptions));
        cbSelectStatus.getSelectionModel().selectFirst();
    }

    public void setTypeModal(int type) {
        if (type != 0 && type != 1 && type != 2)
            handleClose();
        typeModal = type;

        if (typeModal == 0) {
            modalName.setText("Thêm thể loại");
            txtCategoryId.setText(String.valueOf(CategoryBUS.getInstance().getAllLocal().size() + 1));
        } else if (typeModal == 1) {
            if (category == null)
                handleClose();
            modalName.setText("Sửa thể loại");
        }
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
        if (category != null) {
            txtCategoryId.setText(String.valueOf(category.getId()));
            txtCategoryName.setText(String.valueOf(category.getName()));

            // Select status based on object
            StatusBUS statusBus = StatusBUS.getInstance();
            StatusDTO statusToSelect = statusBus.getByIdLocal(
                    category.getStatusId());
            if (statusToSelect != null) {
                cbSelectStatus.getItems().stream()
                        .filter(item -> item != null && item.getId() == statusToSelect.getId())
                        .findFirst()
                        .ifPresent(item -> cbSelectStatus.getSelectionModel().select(item));
            }

            txtCreatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(category.getCreatedAt()));
            txtUpdatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(category.getUpdatedAt()));
        }
    }

    private boolean isValidInput() {
        boolean isValid = true;
        String categoryName = txtCategoryName.getText().trim();

        ValidationUtils validator = ValidationUtils.getInstance();

        // Kiểm tra empty
        if (categoryName.isEmpty()) {
            NotificationUtils.showErrorAlert(AppMessages.CATEGORY_NAME_EMPTY, AppMessages.DIALOG_TITLE);
            clearAndFocus(txtCategoryName);
            isValid = false;
        }
        // Kiểm tra vượt 50 ký tự
        else if (!validator.validateVietnameseText50(categoryName)) {
            NotificationUtils.showErrorAlert(AppMessages.CATEGORY_NAME_INVALID, AppMessages.DIALOG_TITLE);
            clearAndFocus(txtCategoryName);
            isValid = false;
        }

        if (cbSelectStatus.getSelectionModel().getSelectedItem() == null) {
            NotificationUtils.showErrorAlert(AppMessages.CATEGORY_STATUS_REQUIRED, AppMessages.DIALOG_TITLE);
            isValid = false;
        }

        return isValid;
    }

    private void handleSave() {
        if (typeModal == 0) {
            insertCategory();
        } else if (typeModal == 1) {
            updateCategory();
        }
    }

    private void insertCategory() {
        CategoryBUS categoryBus = CategoryBUS.getInstance();
        if (isValidInput()) {
            StatusDTO selectedStatus = cbSelectStatus.getSelectionModel().getSelectedItem();
            CategoryDTO temp = new CategoryDTO(-1, txtCategoryName.getText().trim(),
                    selectedStatus.getId());
            int insertResult = categoryBus.insert(temp,
                    SessionManagerService.getInstance().employeeRoleId(),
                    SessionManagerService.getInstance().employeeLoginId());
            switch (insertResult) {
                case 1 -> {
                    isSaved = true;
                    handleClose();
                }
                case 2 -> NotificationUtils.showErrorAlert(AppMessages.CATEGORY_DATA_INVALID, AppMessages.DIALOG_TITLE);
                case 3 ->
                    NotificationUtils.showErrorAlert(AppMessages.CATEGORY_ADD_DUPLICATE, AppMessages.DIALOG_TITLE);
                case 4 ->
                    NotificationUtils.showErrorAlert(AppMessages.CATEGORY_ADD_NO_PERMISSION, AppMessages.DIALOG_TITLE);
                case 5 ->
                    NotificationUtils.showErrorAlert(AppMessages.DATABASE_CONNECTION_ERROR, AppMessages.DIALOG_TITLE);
                default -> NotificationUtils.showErrorAlert(AppMessages.UNKNOWN_ERROR, AppMessages.DIALOG_TITLE);
            }
        }
    }

    private void updateCategory() {
        CategoryBUS categoryBus = CategoryBUS.getInstance();
        if (isValidInput()) {
            StatusDTO selectedStatus = cbSelectStatus.getSelectionModel().getSelectedItem();
            CategoryDTO temp = new CategoryDTO(category.getId(), txtCategoryName.getText().trim(),
                    selectedStatus.getId());

            int updateResult = categoryBus.update(temp,
                    SessionManagerService.getInstance().employeeRoleId(),
                    SessionManagerService.getInstance().employeeLoginId());
            switch (updateResult) {
                case 1 -> {
                    isSaved = true;
                    handleClose();
                }
                case 2 -> NotificationUtils.showErrorAlert(AppMessages.CATEGORY_DATA_INVALID, AppMessages.DIALOG_TITLE);
                case 3 ->
                    NotificationUtils.showErrorAlert(AppMessages.CATEGORY_UPDATE_DUPLICATE, AppMessages.DIALOG_TITLE);
                case 4 -> NotificationUtils.showErrorAlert(AppMessages.CATEGORY_UPDATE_NO_PERMISSION,
                        AppMessages.DIALOG_TITLE);
                case 5 -> NotificationUtils.showErrorAlert(AppMessages.CATEGORY_UPDATE_ERROR, AppMessages.DIALOG_TITLE);
                case 6 -> NotificationUtils.showErrorAlert(AppMessages.CATEGORY_CANNOT_DELETE_DEFAULT,
                        AppMessages.DIALOG_TITLE);
                default -> NotificationUtils.showErrorAlert(AppMessages.UNKNOWN_ERROR, AppMessages.DIALOG_TITLE);
            }
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