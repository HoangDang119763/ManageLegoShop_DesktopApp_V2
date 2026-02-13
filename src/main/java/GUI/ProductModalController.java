package GUI;

import BUS.CategoryBUS;
import BUS.ProductBUS;
import BUS.StatusBUS;
import DTO.BUSResult;
import DTO.CategoryDTO;
import DTO.ProductDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import SERVICE.ImageService;
import SERVICE.SecureExecutor;
import SERVICE.SessionManagerService;
import INTERFACE.IModalController;
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

public class ProductModalController implements IModalController {
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
    private Button choseImg, resetImgBtn;
    @FXML
    private ImageView imageView;
    private String imageUrl = null;
    @FXML
    private HBox functionBtns, subCategoryBox, functionImg;
    @FXML
    private ComboBox<CategoryDTO> cbSelectCategory;
    @FXML
    private ComboBox<StatusDTO> cbSelectStatus;
    @Getter
    private boolean isSaved;
    private int typeModal;
    private ProductDTO product;
    private ProductBUS productBUS;
    private StatusBUS statusBUS;
    private CategoryBUS categoryBUS;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        productBUS = ProductBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        categoryBUS = CategoryBUS.getInstance();

        loadComboBox();
        setupListeners();
    }

    // =====================
    // 2️⃣ UI SETUP (ComboBox & Listeners)
    // =====================
    private void loadComboBox() {
        ArrayList<StatusDTO> statusOptions = statusBUS.getAllByTypeLocal(StatusType.PRODUCT);
        cbSelectStatus.setItems(FXCollections.observableArrayList(statusOptions));

        ArrayList<CategoryDTO> categoryOptions = categoryBUS.getAllLocal();
        cbSelectCategory.setItems(FXCollections.observableArrayList(categoryOptions));

        cbSelectStatus.getSelectionModel().selectFirst();
        cbSelectCategory.getSelectionModel().selectFirst();
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
        resetImgBtn.setOnAction(e -> handleResetImg());
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
            modalName.setText("Thêm sản phẩm");
            txtProductId.setText(productBUS.autoId());
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

    public void setProduct(ProductDTO product) {
        this.product = product;
        txtProductName.setText(product.getName());

        CategoryDTO selectedCategory = categoryBUS.getByIdLocal(product.getCategoryId());
        if (selectedCategory != null) {
            cbSelectCategory.getItems().stream()
                    .filter(item -> item != null && item.getId() == selectedCategory.getId())
                    .findFirst()
                    .ifPresent(item -> cbSelectCategory.getSelectionModel().select(item));
        }
        txtProductId.setText(product.getId());

        StatusDTO statusToSelect = statusBUS.getByIdLocal(product.getStatusId());
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

        // Only admin can edit selling price
        if (SessionManagerService.getInstance().employeeRoleId() == 1) {
            txtSellingPrice.setMouseTransparent(false);
            txtSellingPrice.setFocusTraversable(true);
            txtSellingPrice.setStyle("-fx-background-color: linear-gradient(to bottom, #efefef, #eeeeee)");
        }

        this.imageUrl = product.getImageUrl();
        loadProductImage();

        // Setup read-only mode if viewing details
        if (typeModal == 2) {
            setupReadOnlyMode();
        }
    }

    private void setupReadOnlyMode() {
        txtProductId.setEditable(false);
        txtProductName.setEditable(false);
        txtDescription.setEditable(false);
        txtSellingPrice.setEditable(false);
        txtImportPrice.setEditable(false);
        txtStockQuantity.setEditable(false);

        cbSelectCategory.setMouseTransparent(true);
        cbSelectCategory.setFocusTraversable(false);
        cbSelectCategory.setPrefWidth(406.4);
        cbSelectStatus.setMouseTransparent(true);
        cbSelectStatus.setFocusTraversable(false);

        functionBtns.getChildren().remove(saveBtn);
        subCategoryBox.getChildren().remove(addCategorySubBtn);
        choseImg.setVisible(false);
        resetImgBtn.setVisible(false);
    }

    // =====================
    // 4️⃣ IMAGE HANDLERS
    // =====================
    private void loadProductImage() {
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
    }

    private void handleChoseImg() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);
            imageUrl = file.toURI().toString();
        }
    }

    private void handleResetImg() {
        if (!UiUtils.gI().showConfirmAlert(AppMessages.PRODUCT_IMAGE_RESET_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
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
            imageView.setImage(image);
            imageUrl = null;
            NotificationUtils.showInfoAlert(AppMessages.PRODUCT_IMAGE_RESET_SUCCESS, AppMessages.DIALOG_TITLE);
        }
    }

    // =====================
    // 5️⃣ EVENT HANDLERS
    // =====================
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

    private void handleAddCategorySub() {
        CategoryModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/CategoryModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm thể loại");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert(AppMessages.OPERATION_SUCCESS, AppMessages.DIALOG_TITLE);
            loadComboBox();
        }
    }

    // =====================
    // 6️⃣ VALIDATION & BUSINESS LOGIC
    // =====================
    private boolean isValidInput() {
        boolean isValid = true;
        String name = txtProductName.getText().trim();
        String description = txtDescription.getText().trim();
        ValidationUtils validator = ValidationUtils.getInstance();

        // Check product name
        if (name.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên sản phẩm không được để trống.", AppMessages.DIALOG_TITLE);
            focus(txtProductName);
            isValid = false;
        } else if (!validator.validateVietnameseText255(name)) {
            NotificationUtils.showErrorAlert(
                    "Tên sản phẩm không hợp lệ (Tối đa 50 ký tự, chỉ chữ và số, \"_\", \"-\", \"/\").",
                    AppMessages.DIALOG_TITLE);
            focus(txtProductName);
            isValid = false;
        }

        // Check description
        if (isValid && !description.isEmpty() && !validator.validateVietnameseText65k4(description)) {
            NotificationUtils.showErrorAlert(
                    "Mô tả không hợp lệ (Tối đa 65.400 ký tự, chỉ chữ và số, \"_\", \"-\", \"/\").",
                    AppMessages.DIALOG_TITLE);
            focus(txtDescription);
            isValid = false;
        }

        // Check selling price
        String sellingPrice = txtSellingPrice.getText().trim();
        if (isValid && sellingPrice.isEmpty()) {
            NotificationUtils.showErrorAlert("Giá bán không được để trống.", AppMessages.DIALOG_TITLE);
            focus(txtSellingPrice);
            isValid = false;
        } else if (isValid) {
            try {
                BigDecimal sellingPriceValue = new BigDecimal(sellingPrice);
                if (!validator.validateBigDecimal(sellingPriceValue, 10, 2, false)) {
                    NotificationUtils.showErrorAlert(
                            "Giá bạn không hợp lệ (tối đa 10 chữ số, 2 số thập phân, không âm hoặc bằng 0).",
                            AppMessages.DIALOG_TITLE);
                    focus(txtSellingPrice);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                NotificationUtils.showErrorAlert("Giá bạn phải là số.", AppMessages.DIALOG_TITLE);
                focus(txtSellingPrice);
                isValid = false;
            }
        }

        return isValid;
    }

    private void insertProduct() throws IOException {
        if (!isValidInput())
            return;

        // 1. Tiền xử lý dữ liệu: Trống -> Null
        String desc = (txtDescription.getText() == null || txtDescription.getText().trim().isEmpty())
                ? null
                : txtDescription.getText().trim();

        // 2. Xử lý ảnh: Nếu imageUrl (từ FileChooser) null thì không lưu, trả về null
        // luôn
        String newImgUrl = null;
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            newImgUrl = ImageService.gI().saveProductImage(txtProductId.getText().trim(), imageUrl);
        }

        // 3. Tạo DTO
        ProductDTO temp = new ProductDTO(
                txtProductId.getText().trim(),
                txtProductName.getText().trim(),
                0,
                new BigDecimal(txtSellingPrice.getText().trim()),
                BigDecimal.ZERO,
                cbSelectStatus.getValue().getId(),
                desc,
                newImgUrl, // Có thể là null
                cbSelectCategory.getValue().getId());

        BUSResult insertResult = SecureExecutor.runSafeBUSResult(
                PermissionKey.PRODUCT_INSERT,
                () -> productBUS.insert(temp));

        handleResult(insertResult);
    }

    private void updateProduct() throws IOException {
        if (!isValidInput())
            return;

        // 1. Xử lý Description
        String desc = (txtDescription.getText() == null || txtDescription.getText().trim().isEmpty())
                ? null
                : txtDescription.getText().trim();

        // 2. Xử lý ImageUrl (Logic 3 trường hợp)
        String finalImgUrl = product.getImageUrl(); // Mặc định giữ ảnh cũ

        if (imageUrl != null && !imageUrl.equals(product.getImageUrl())) {
            // Trường hợp 1: Có chọn ảnh mới và khác ảnh cũ -> Lưu ảnh mới
            finalImgUrl = ImageService.gI().saveProductImage(txtProductId.getText().trim(), imageUrl);
        } else if (imageUrl == null) {
            // Trường hợp 2: UI không có ảnh (người dùng đã xóa hoặc không chọn) -> Set null
            finalImgUrl = null;
        }
        // Trường hợp 3: imageUrl giống product.getImageUrl() -> Giữ nguyên (finalImgUrl
        // không đổi)

        // 3. Tạo DTO cập nhật
        ProductDTO temp = new ProductDTO(
                txtProductId.getText().trim(),
                txtProductName.getText().trim(),
                product.getStockQuantity(),
                new BigDecimal(txtSellingPrice.getText().trim()),
                product.getImportPrice(),
                cbSelectStatus.getValue().getId(),
                desc,
                finalImgUrl,
                cbSelectCategory.getValue().getId());

        BUSResult updateResult = SecureExecutor.runSafeBUSResult(
                PermissionKey.PRODUCT_UPDATE,
                () -> productBUS.update(temp));

        handleResult(updateResult);
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

    // =====================
    // 7️⃣ UTILITY METHODS
    // =====================
    private void focus(TextField textField) {
        textField.requestFocus();
    }

    private void focus(TextArea textArea) {
        textArea.requestFocus();
    }
}
