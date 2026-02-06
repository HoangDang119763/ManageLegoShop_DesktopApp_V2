package GUI;

import BUS.CategoryBUS;
import BUS.ProductBUS;
import BUS.StatusBUS;
import DTO.*;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.ExcelService;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;

public class ProductController implements IController {
    @FXML
    private TableView<ProductDTO> tblProduct;
    @FXML
    private TableColumn<ProductDTO, String> tlb_col_id;
    @FXML
    private TableColumn<ProductDTO, String> tlb_col_name;
    @FXML
    private TableColumn<ProductDTO, ImageView> tlb_col_imageUrl;
    @FXML
    private TableColumn<ProductDTO, String> tlb_col_description;
    @FXML
    private TableColumn<ProductDTO, String> tlb_col_categoryName;
    @FXML
    private TableColumn<ProductDTO, Integer> tlb_col_stockQuantity;
    @FXML
    private TableColumn<ProductDTO, String> tlb_col_sellingPrice;
    @FXML
    private TableColumn<ProductDTO, String> tlb_col_status;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn, btnImportExcel, detailBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private TextField txtStartPrice;
    @FXML
    private TextField txtEndPrice;
    @FXML
    private ComboBox<StatusDTO> cbStatusFilter;
    @FXML
    private ComboBox<String> cbSearchBy;
    @FXML
    private ComboBox<CategoryDTO> cbCategoryFilter;
    private String searchBy = "Mã sản phẩm";
    private String keyword = "";
    private CategoryDTO categoryFilter = null;
    private StatusDTO statusFilter = null;
    private BigDecimal startPrice = null;
    private BigDecimal endPrice = null;
    private ProductDTO selectedProduct;

    @FXML
    public void initialize() {
        if (ProductBUS.getInstance().isLocalEmpty())
            ProductBUS.getInstance().loadLocal();
        if (CategoryBUS.getInstance().isLocalEmpty())
            CategoryBUS.getInstance().loadLocal();
        tblProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        // Tránh deprecated
        Platform.runLater(() -> tblProduct.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        setupListeners();

        loadTable();
        applyFilters();
    }

    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã sản phẩm", "Tên sản phẩm");

        // Load Status ComboBox
        StatusBUS statusBUS = StatusBUS.getInstance();
        ArrayList<StatusDTO> statusList = statusBUS.getAllByTypeLocal(StatusType.PRODUCT);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả cả trạng thái");
        cbStatusFilter.getItems().add(allStatus); // "Tất cả" option
        cbStatusFilter.getItems().addAll(statusList);

        // Load Category ComboBox
        CategoryBUS cateBUS = CategoryBUS.getInstance();
        CategoryDTO allCategory = new CategoryDTO(-1, "Tất cả thể loại");
        cbCategoryFilter.getItems().add(allCategory); // "Tất cả" option
        cbCategoryFilter.getItems().addAll(cateBUS.getAllLocal());

        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst(); // "Tất cả"
        cbCategoryFilter.getSelectionModel().selectFirst(); // "Tất cả"
    }

    @Override
    public void loadTable() {
        CategoryBUS cateBUS = CategoryBUS.getInstance();
        StatusBUS statusBUS = StatusBUS.getInstance();

        // Cập nhật dữ liệu vào bảng
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_imageUrl.setCellValueFactory(cellData -> {
            String imageUrl = cellData.getValue().getImageUrl();
            File imageFile = null;
            Image image = null;

            // Kiểm tra nếu có ảnh sản phẩm
            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageFile = new File(imageUrl); // Đường dẫn ảnh người dùng nhập
                // System.out.println("Đường dẫn ảnh: " + imageFile.getAbsolutePath());
            }

            if (imageFile != null && imageFile.exists()) {
                try {
                    image = new Image(imageFile.toURI().toString(), 200, 200, true, true);
                } catch (Exception e) {
                    // System.err.println("Lỗi khi tải ảnh: " + e.getMessage());
                }
            } else {
                URL defaultImageUrl = getClass().getResource("/images/default/default.png");
                if (defaultImageUrl != null) {
                    image = new Image(defaultImageUrl.toExternalForm());
                } else {
                    // System.err.println("Ảnh mặc định không tìm thấy trong resources.");
                }
            }

            if (image != null) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(70);
                imageView.setFitHeight(70);
                return new SimpleObjectProperty<>(imageView);
            } else {
                return new SimpleObjectProperty<>(null);
            }
        });

        tlb_col_description.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription() == null ? "" : cellData.getValue().getDescription()));
        tlb_col_categoryName.setCellValueFactory(cellData -> new SimpleStringProperty(
                cateBUS.getByIdLocal(cellData.getValue().getCategoryId()).getName()));
        tlb_col_sellingPrice.setCellValueFactory(cellData -> new SimpleStringProperty(
                ValidationUtils.getInstance().formatCurrency(cellData.getValue().getSellingPrice())));
        tlb_col_stockQuantity.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(statusBUS
                .getByIdLocal(cellData.getValue().getStatusId()).getDescription()));
        UiUtils.gI().addTooltipToColumn(tlb_col_name, 10);
        UiUtils.gI().addTooltipToColumn(tlb_col_description, 10);
        UiUtils.gI().addTooltipToColumn(tlb_col_categoryName, 10);
    }

    @Override
    public void setupListeners() {
        cbSearchBy.setOnAction(event -> handleSearchByChange());
        cbCategoryFilter.setOnAction(event -> handleCategoryFilterChange());
        cbStatusFilter.setOnAction(event -> handleStatusFilterChange());
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> handleKeywordChange());
        txtStartPrice.textProperty().addListener((observable, oldValue, newValue) -> handlePriceChange());
        txtEndPrice.textProperty().addListener((observable, oldValue, newValue) -> handlePriceChange());
        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert(AppMessages.GENERAL_REFRESH_SUCCESS, AppMessages.DIALOG_TITLE);
        });

        addBtn.setOnAction(e -> handleAdd());
        editBtn.setOnAction(e -> handleEdit());
        deleteBtn.setOnAction(e -> handleDelete());
        detailBtn.setOnAction(e -> handleDetail());
        btnImportExcel.setOnMouseClicked(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            importProductExcel(stage);
        });

    }

    private void handlePriceChange() {
        try {
            String startText = txtStartPrice.getText().trim();
            if (!startText.isEmpty()) {
                startPrice = new BigDecimal(startText);
            } else {
                startPrice = null;
            }
            // System.out.println(startPrice);

            String endText = txtEndPrice.getText().trim();
            if (!endText.isEmpty()) {
                endPrice = new BigDecimal(endText);
            } else {
                endPrice = null;
            }
            // System.out.println(endPrice);

            applyFilters();
        } catch (NumberFormatException e) {
            // System.out.println("Giá không hợp lệ: " + e.getMessage());
        }
    }

    private void handleSearchByChange() {
        searchBy = cbSearchBy.getValue();
        applyFilters();
    }

    private void handleKeywordChange() {
        keyword = txtSearch.getText().trim();
        applyFilters();
    }

    private void handleCategoryFilterChange() {
        categoryFilter = cbCategoryFilter.getValue();
        applyFilters();
    }

    private void handleStatusFilterChange() {
        statusFilter = cbStatusFilter.getValue();
        applyFilters();
    }

    @Override
    public void applyFilters() {
        int statusId = statusFilter == null ? -1 : statusFilter.getId();
        int categoryId = categoryFilter == null ? -1 : categoryFilter.getId();
        tblProduct.setItems(FXCollections.observableArrayList(
                ProductBUS.getInstance().filterProducts(searchBy, keyword, categoryId,
                        statusId, startPrice, endPrice)));
        tblProduct.getSelectionModel().clearSelection();
    }

    @Override
    public void resetFilters() {
        cbSearchBy.getSelectionModel().selectFirst(); // Chọn giá trị đầu tiên
        cbStatusFilter.getSelectionModel().selectFirst(); // "Tất cả"
        cbCategoryFilter.getSelectionModel().selectFirst(); // "Tất cả"
        txtSearch.clear();
        txtStartPrice.clear();
        txtEndPrice.clear();

        // Cập nhật lại các biến bộ lọc
        searchBy = "Mã sản phẩm";
        keyword = "";
        categoryFilter = null;
        statusFilter = null;
        startPrice = null;
        endPrice = null;
        applyFilters(); // Áp dụng lại bộ lọc
    }

    @Override
    public void hideButtonWithoutPermission() {
        boolean canAdd = SessionManagerService.getInstance().hasPermission(PermissionKey.PRODUCT_INSERT);
        boolean canEdit = SessionManagerService.getInstance().hasPermission(PermissionKey.PRODUCT_UPDATE);
        boolean canDelete = SessionManagerService.getInstance().hasPermission(PermissionKey.PRODUCT_DELETE);

        if (!canAdd)
            functionBtns.getChildren().remove(addBtn);
        if (!canEdit)
            functionBtns.getChildren().remove(editBtn);
        if (!canDelete)
            functionBtns.getChildren().remove(deleteBtn);
    }

    private void handleDelete() {
        if (isNotSelectedProduct()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        if (ProductBUS.getInstance().getByIdLocal(selectedProduct.getId()).getStockQuantity() != 0) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_DELETE_WITH_STOCK, AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert(AppMessages.PRODUCT_DELETE_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        int deleteResult = ProductBUS.getInstance().delete(
                selectedProduct.getId(),
                SessionManagerService.getInstance().employeeRoleId(),
                SessionManagerService.getInstance().employeeLoginId());
        switch (deleteResult) {
            case 1 -> {
                NotificationUtils.showInfoAlert(AppMessages.PRODUCT_DELETE_SUCCESS, AppMessages.DIALOG_TITLE);
                resetFilters();
            }
            case 2 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_DELETE_ERROR, AppMessages.DIALOG_TITLE);
            case 3 ->
                NotificationUtils.showErrorAlert(AppMessages.PRODUCT_DELETE_NO_PERMISSION, AppMessages.DIALOG_TITLE);
            case 4 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_DELETE_FAILED, AppMessages.DIALOG_TITLE);
            case 5 -> NotificationUtils.showErrorAlert(AppMessages.PRODUCT_DELETE_WITH_STOCK, AppMessages.DIALOG_TITLE);
            default -> NotificationUtils.showErrorAlert(AppMessages.UNKNOWN_ERROR, AppMessages.DIALOG_TITLE);
        }
    }

    private void handleAdd() {
        ProductModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/ProductModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm sản phẩm");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert(AppMessages.PRODUCT_ADD_SUCCESS, AppMessages.DIALOG_TITLE);
            resetFilters();
        }
    }

    private void handleEdit() {
        if (isNotSelectedProduct()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        ProductModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/ProductModal.fxml",
                controller -> {
                    controller.setTypeModal(1);
                    controller.setProduct(selectedProduct);
                },
                "Sửa sản phẩm");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert(AppMessages.PRODUCT_UPDATE_SUCCESS, AppMessages.DIALOG_TITLE);
            applyFilters();
        }
        tblProduct.refresh();
    }

    private void handleDetail() {
        if (isNotSelectedProduct()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        ProductModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/ProductModal.fxml",
                controller -> {
                    controller.setTypeModal(2);
                    controller.setProduct(selectedProduct);
                },
                "Xem chi tiết sản phẩm");

    }

    private boolean isNotSelectedProduct() {
        selectedProduct = tblProduct.getSelectionModel().getSelectedItem();
        return selectedProduct == null;
    }

    public void importProductExcel(Stage stage) {
        try {
            ExcelService.getInstance().ImportSheet("products", stage);
            applyFilters();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
