package GUI;

import BUS.CategoryBUS;
import BUS.ProductBUS;
import BUS.StatusBUS;
import DTO.CategoryDTO;
import DTO.ProductDTO;
import DTO.StatusDTO;
import ENUM.StatusType;
import SERVICE.ImageService;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;

public class ProductModalController {
    @FXML
    private Label modalName;
    @FXML
    private TextField txtProductId;
    @FXML
    private TextField txtProductName;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtSellingPrice;
    @FXML
    private TextField txtImportPrice;
    @FXML
    private TextField txtStockQuantity;
    @FXML
    private TextField txtCreatedAt, txtUpdatedAt;
    @FXML
    private Button saveBtn, closeBtn, addCategorySubBtn;
    @FXML
    private Button choseImg;
    @FXML
    private ImageView imageView;
    private String imageUrl = null;
    @FXML
    private HBox functionBtns, subCategoryBox;
    @FXML
    private ComboBox<CategoryDTO> cbSelectCategory;
    @FXML
    private ComboBox<StatusDTO> cbSelectStatus;
    @Getter
    private boolean isSaved;
    private int typeModal;
    private ProductDTO product;

    @FXML
    public void initialize() {
        loadComboBox();
        setupListeners();
    }

    public void setupListeners() {
        saveBtn.setOnAction(e -> {
            try {
                handleSave();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        closeBtn.setOnAction(e -> handleClose());
        addCategorySubBtn.setOnAction(e -> handleAddCategorySub());
        choseImg.setOnAction(e -> handleChoseImg());
    }

    private void loadComboBox() {
        StatusBUS statusBus = StatusBUS.getInstance();
        ArrayList<StatusDTO> statusOptions = statusBus.getAllByTypeLocal(StatusType.PRODUCT);
        cbSelectStatus.setItems(FXCollections.observableArrayList(statusOptions));

        CategoryBUS cateBUS = CategoryBUS.getInstance();
        ArrayList<CategoryDTO> categoryOptions = cateBUS.getAllLocal();
        cbSelectCategory.setItems(FXCollections.observableArrayList(categoryOptions));

        cbSelectStatus.getSelectionModel().selectFirst();
        cbSelectCategory.getSelectionModel().selectFirst();
    }

    public void setTypeModal(int type) {
        if (type != 0 && type != 1 && type != 2) {
            handleClose();
        }
        typeModal = type;
        if (typeModal == 0) {
            modalName.setText("Thêm sản phẩm");
            txtProductId.setText(ProductBUS.getInstance().autoId());
        } else if (typeModal == 1) {
            if (product == null) {
                handleClose();
            }
            modalName.setText("Sửa sản phẩm");
        } else if (typeModal == 2) {
            if (product == null) {
                handleClose();
            }
            modalName.setText("Xem chi tiết sản phẩm");
        }
    }

    private void handleSave() throws IOException {
        if (typeModal == 0) {
            insertProduct();
        } else {
            updateProduct();
        }
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
        txtProductName.setText(product.getName());
        CategoryDTO selectedCategory = CategoryBUS.getInstance().getByIdLocal(product.getCategoryId());
        if (selectedCategory != null) {
            cbSelectCategory.getItems().stream()
                    .filter(item -> item != null && item.getId() == selectedCategory.getId())
                    .findFirst()
                    .ifPresent(item -> cbSelectCategory.getSelectionModel().select(item));
        }
        txtProductId.setText(product.getId());

        // Select status based on object
        StatusBUS statusBus = StatusBUS.getInstance();
        StatusDTO statusToSelect = statusBus.getByIdLocal(
                product.getStatusId());
        if (statusToSelect != null) {
            cbSelectStatus.getItems().stream()
                    .filter(item -> item != null && item.getId() == statusToSelect.getId())
                    .findFirst()
                    .ifPresent(item -> cbSelectStatus.getSelectionModel().select(item));
        }

        txtDescription.setText(product.getDescription() == null ? "" : product.getDescription());
        txtSellingPrice.setText(product.getSellingPrice().toString());
        txtImportPrice.setText(product.getImportPrice().toString());
        txtStockQuantity.setText(String.valueOf(product.getStockQuantity()));
        txtCreatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(product.getCreatedAt()));
        txtUpdatedAt.setText(ValidationUtils.getInstance().formatDateTimeWithHour(product.getUpdatedAt()));
        // Only admin can edit selling price ---
        if (SessionManagerService.getInstance().employeeRoleId() == 1) {
            txtSellingPrice.setMouseTransparent(false);
            txtSellingPrice.setFocusTraversable(true);
            txtSellingPrice.setStyle("-fx-background-color: linear-gradient(to bottom, #efefef, #eeeeee)");
        }

        this.imageUrl = product.getImageUrl();

        File imageFile = null;
        Image image = null;

        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageFile = new File(imageUrl);
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

        if (image != null) {
            imageView.setImage(image);
        }

        // Enable read-only mode if viewing details
        if (typeModal == 2) {
            setupReadOnlyMode();
        }
    }

    private void setupReadOnlyMode() {
        // Đối với TextField và TextArea: Chỉ cấm sửa, không cấm tương tác
        txtProductId.setEditable(false);
        txtProductName.setEditable(false);
        txtDescription.setEditable(false);
        txtSellingPrice.setEditable(false);
        txtImportPrice.setEditable(false);
        txtStockQuantity.setEditable(false);

        // Đối với ComboBox: Không có setEditable(false), ta dùng MouseTransparent
        // Kết hợp với setFocusTraversable(false) để không bị chọn bằng phím Tab
        cbSelectCategory.setMouseTransparent(true);
        cbSelectCategory.setFocusTraversable(false);
        cbSelectCategory.setPrefWidth(382);
        cbSelectStatus.setMouseTransparent(true);
        cbSelectStatus.setFocusTraversable(false);

        // Hide action buttons
        functionBtns.getChildren().remove(saveBtn);
        subCategoryBox.getChildren().remove(addCategorySubBtn);

        choseImg.setVisible(false);

        // Change close button text and style
        closeBtn.setText("Đóng");
    }

    private void handleChoseImg() {
        // Open file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            // imageView.setFitWidth(217.5);
            // imageView.setFitHeight(217.5);
            imageUrl = file.toURI().toString();
        }
    }

    private boolean isValidInput() {
        boolean isValid = true;
        String name = txtProductName.getText().trim();
        String description = txtDescription.getText().trim();
        ValidationUtils validator = ValidationUtils.getInstance();

        // Check product name
        if (name.isEmpty()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NAME_EMPTY, AppMessages.DIALOG_TITLE);
            focus(txtProductName);
            isValid = false;
        } else if (!validator.validateVietnameseText255(name)) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NAME_INVALID, AppMessages.DIALOG_TITLE);
            focus(txtProductName);
            isValid = false;
        }

        // Check description
        if (isValid && !description.isEmpty() && !validator.validateVietnameseText65k4(description)) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_DESCRIPTION_INVALID, AppMessages.DIALOG_TITLE);
            focus(txtDescription);
            isValid = false;
        }

        // Check selling price
        String sellingPrice = txtSellingPrice.getText().trim();
        if (isValid && sellingPrice.isEmpty()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_PRICE_EMPTY, AppMessages.DIALOG_TITLE);
            focus(txtSellingPrice);
            isValid = false;
        } else if (isValid) {
            try {
                BigDecimal sellingPriceValue = new BigDecimal(sellingPrice);
                if (!validator.validateBigDecimal(sellingPriceValue, 10, 2, false)) {
                    NotificationUtils.showErrorAlert(AppMessages.PRODUCT_PRICE_INVALID, AppMessages.DIALOG_TITLE);
                    focus(txtSellingPrice);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                NotificationUtils.showErrorAlert(AppMessages.PRODUCT_PRICE_NOT_NUMBER, AppMessages.DIALOG_TITLE);
                focus(txtSellingPrice);
                isValid = false;
            }
        }

        return isValid;
    }

    private void insertProduct() throws IOException {
        ProductBUS proBus = ProductBUS.getInstance();
        if (isValidInput()) {
            StatusDTO selectedStatus = cbSelectStatus.getValue();
            CategoryDTO selectedCategory = cbSelectCategory.getValue();

            if (selectedStatus == null) {
                NotificationUtils.showErrorAlert(AppMessages.PRODUCT_STATUS_REQUIRED, AppMessages.DIALOG_TITLE);
                return;
            }
            if (selectedCategory == null) {
                NotificationUtils.showErrorAlert(AppMessages.PRODUCT_CATEGORY_REQUIRED, AppMessages.DIALOG_TITLE);
                return;
            }

            String newImgUrl = ImageService.gI().saveProductImage(txtProductId.getText().trim(), imageUrl);
            ProductDTO temp = new ProductDTO(
                    txtProductId.getText().trim(),
                    txtProductName.getText().trim(), 0,
                    new BigDecimal(0),
                    selectedStatus.getId(), txtDescription.getText().trim(),
                    newImgUrl, selectedCategory.getId());
            int insertResult = proBus.insert(temp,
                    SessionManagerService.getInstance().employeeRoleId(),
                    SessionManagerService.getInstance().employeeLoginId());
            switch (insertResult) {
                case 1 -> {
                    isSaved = true;
                    handleClose();
                }
                case 2 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_ADD_ERROR, AppMessages.DIALOG_TITLE);
                case 3 ->
                    NotificationUtils.showErrorAlert(AppMessages.PRODUCT_ADD_NO_PERMISSION, AppMessages.DIALOG_TITLE);
                case 4 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_ADD_INVALID_CATEGORY,
                        AppMessages.DIALOG_TITLE);
                case 5 -> {
                    NotificationUtils.showErrorAlert(AppMessages.PRODUCT_ADD_DUPLICATE, AppMessages.DIALOG_TITLE);
                    focus(txtProductName);
                }
                case 6 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_ADD_FAILED, AppMessages.DIALOG_TITLE);
                default -> NotificationUtils.showErrorAlert(AppMessages.UNKNOWN_ERROR, AppMessages.DIALOG_TITLE);
            }
        }
    }

    private void updateProduct() throws IOException {
        ProductBUS proBus = ProductBUS.getInstance();
        if (isValidInput()) {
            StatusDTO selectedStatus = cbSelectStatus.getValue();
            CategoryDTO selectedCategory = cbSelectCategory.getValue();

            if (selectedStatus == null) {
                NotificationUtils.showErrorAlert(AppMessages.PRODUCT_STATUS_REQUIRED, AppMessages.DIALOG_TITLE);
                return;
            }
            if (selectedCategory == null) {
                NotificationUtils.showErrorAlert(AppMessages.PRODUCT_CATEGORY_REQUIRED, AppMessages.DIALOG_TITLE);
                return;
            }

            String newImgUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.equals(product.getImageUrl())) {
                newImgUrl = ImageService.gI().saveProductImage(txtProductId.getText().trim(), imageUrl);
            }

            ProductDTO temp = new ProductDTO(
                    txtProductId.getText().trim(),
                    txtProductName.getText().trim(), 0,
                    new BigDecimal(txtSellingPrice.getText().trim()),
                    selectedStatus.getId(),
                    txtDescription.getText().trim(),
                    newImgUrl, selectedCategory.getId());
            int updateResult = proBus.update(temp,
                    SessionManagerService.getInstance().employeeRoleId(),
                    SessionManagerService.getInstance().employeeLoginId());
            switch (updateResult) {
                case 1 -> {
                    isSaved = true;
                    handleClose();
                }
                case 2 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_UPDATE_ERROR, AppMessages.DIALOG_TITLE);
                case 3 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_UPDATE_NO_PERMISSION,
                        AppMessages.DIALOG_TITLE);
                case 4 ->
                    NotificationUtils.showErrorAlert(AppMessages.PRODUCT_UPDATE_INVALID_DATA, AppMessages.DIALOG_TITLE);
                case 5 -> {
                    NotificationUtils.showErrorAlert(AppMessages.PRODUCT_UPDATE_DUPLICATE, AppMessages.DIALOG_TITLE);
                    focus(txtProductName);
                }
                case 6 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_UPDATE_FAILED, AppMessages.DIALOG_TITLE);
                default -> NotificationUtils.showErrorAlert(AppMessages.UNKNOWN_ERROR, AppMessages.DIALOG_TITLE);
            }
        }
    }

    private void handleAddCategorySub() {
        CategoryModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/CategoryModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm thể loại");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert(AppMessages.OPERATION_SUCCESS,
                    AppMessages.DIALOG_TITLE);
            loadComboBox();
        }
    }

    private void focus(TextField textField) {
        textField.requestFocus();
    }

    private void focus(TextArea textArea) {
        textArea.requestFocus();
    }
}
