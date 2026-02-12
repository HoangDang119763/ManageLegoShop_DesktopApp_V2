package GUI;

import BUS.CategoryBUS;
import BUS.ProductBUS;
import BUS.StatusBUS;
import DTO.*;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.ExcelService;
import SERVICE.SecureExecutor;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.ModalBuilder;
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
import javafx.scene.layout.AnchorPane;
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
    private Button addBtn, editBtn, deleteBtn, refreshBtn, btnImportExcel;
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

    // BUS instances - initialized once
    private ProductBUS productBUS;
    private CategoryBUS categoryBUS;
    private StatusBUS statusBUS;
    @FXML
    private AnchorPane mainContent;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        productBUS = ProductBUS.getInstance();
        if (productBUS.isLocalEmpty())
            productBUS.loadLocal();
        categoryBUS = CategoryBUS.getInstance();
        if (categoryBUS.isLocalEmpty())
            categoryBUS.loadLocal();
        statusBUS = StatusBUS.getInstance();
        if (statusBUS.isLocalEmpty())
            statusBUS.loadLocal();

        tblProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblProduct.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        loadTable();
        setupListeners();
        applyFilters();
    }

    // =====================
    // 2️⃣ UI SETUP (LOAD & CONFIG)
    // =====================
    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã sản phẩm", "Tên sản phẩm");

        ArrayList<StatusDTO> statusList = statusBUS.getAllByTypeLocal(StatusType.PRODUCT);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusList);

        CategoryDTO allCategory = new CategoryDTO(-1, "Tất cả thể loại");
        cbCategoryFilter.getItems().add(allCategory);
        cbCategoryFilter.getItems().addAll(categoryBUS.getAllLocal());

        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
        cbCategoryFilter.getSelectionModel().selectFirst();
    }

    @Override
    public void loadTable() {
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_imageUrl.setCellValueFactory(cellData -> {
            String imageUrl = cellData.getValue().getImageUrl();
            File imageFile = null;
            Image image = null;

            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageFile = new File(imageUrl);
            }

            if (imageFile != null && imageFile.exists()) {
                try {
                    image = new Image(imageFile.toURI().toString(), 200, 200, true, true);
                } catch (Exception e) {
                }
            } else {
                URL defaultImageUrl = getClass().getResource("/images/default/default.png");
                if (defaultImageUrl != null) {
                    image = new Image(defaultImageUrl.toExternalForm());
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
                categoryBUS.getByIdLocal(cellData.getValue().getCategoryId()).getName()));
        tlb_col_sellingPrice.setCellValueFactory(cellData -> new SimpleStringProperty(
                ValidationUtils.getInstance().formatCurrency(cellData.getValue().getSellingPrice())));
        tlb_col_stockQuantity.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(statusBUS
                .getByIdLocal(cellData.getValue().getStatusId()).getDescription()));
        UiUtils.gI().addTooltipToColumn(tlb_col_name, 15);
        UiUtils.gI().addTooltipToColumn(tlb_col_description, 15);
        UiUtils.gI().addTooltipToColumn(tlb_col_categoryName, 15);
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
        tblProduct.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                handleDetail();
        });
        btnImportExcel.setOnMouseClicked(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            importProductExcel(stage);
        });
    }

    // =====================
    // 3️⃣ CRUD HANDLERS (Add/Edit/Detail/Delete)
    // =====================
    private void handleAdd() {
        ProductModalController modalController = new ModalBuilder<ProductModalController>("/GUI/ProductModal.fxml",
                ProductModalController.class)
                .setTitle("Thêm sản phẩm")
                .modeAdd()
                .open();
        if (modalController != null && modalController.isSaved()) {
            resetFilters();
        }
    }

    private void handleEdit() {
        if (isNotSelectedProduct()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        ProductModalController modalController = new ModalBuilder<ProductModalController>("/GUI/ProductModal.fxml",
                ProductModalController.class)
                .setTitle("Sửa sản phẩm")
                .modeEdit()
                .configure(c -> c.setProduct(selectedProduct))
                .open();
        if (modalController != null && modalController.isSaved()) {
            applyFilters();
        }
        tblProduct.refresh();
    }

    private void handleDetail() {
        if (isNotSelectedProduct()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        new ModalBuilder<ProductModalController>("/GUI/ProductModal.fxml", ProductModalController.class)
                .setTitle("Xem chi tiết sản phẩm")
                .modeDetail()
                .configure(c -> c.setProduct(selectedProduct))
                .open();
    }

    private void handleDelete() {
        if (isNotSelectedProduct()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        ProductDTO product = productBUS.getByIdLocal(selectedProduct.getId());
        if (product == null) {
            NotificationUtils.showErrorAlert(AppMessages.UNKNOWN_ERROR, AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert(AppMessages.PRODUCT_DELETE_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        BUSResult updateResult = SecureExecutor.runSafeBUSResult(PermissionKey.PRODUCT_DELETE,
                () -> productBUS.delete(selectedProduct.getId()));

        if (updateResult.isSuccess()) {
            NotificationUtils.showInfoAlert(updateResult.getMessage(), AppMessages.DIALOG_TITLE);
            resetFilters();
        } else {
            NotificationUtils.showErrorAlert(updateResult.getMessage(), AppMessages.DIALOG_TITLE);
        }
    }

    // =====================
    // 4️⃣ FILTER HANDLERS
    // =====================
    private void handlePriceChange() {
        try {
            String startText = txtStartPrice.getText().trim();
            startPrice = startText.isEmpty() ? null : new BigDecimal(startText);

            String endText = txtEndPrice.getText().trim();
            endPrice = endText.isEmpty() ? null : new BigDecimal(endText);

            applyFilters();
        } catch (NumberFormatException e) {
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

    // =====================
    // 5️⃣ INTERFACE METHODS
    // =====================
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
        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
        cbCategoryFilter.getSelectionModel().selectFirst();
        txtSearch.clear();
        txtStartPrice.clear();
        txtEndPrice.clear();

        searchBy = "Mã sản phẩm";
        keyword = "";
        categoryFilter = null;
        statusFilter = null;
        startPrice = null;
        endPrice = null;
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();
        boolean canView = session.hasPermission(PermissionKey.PRODUCT_LIST_VIEW);

        // Block entire view if no permission
        if (!canView) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }

        boolean canAdd = session.hasPermission(PermissionKey.PRODUCT_INSERT);
        boolean canEdit = session.hasPermission(PermissionKey.PRODUCT_UPDATE);
        boolean canDelete = session.hasPermission(PermissionKey.PRODUCT_DELETE);

        if (!canAdd)
            UiUtils.gI().setVisibleItem(addBtn);
        if (!canEdit)
            UiUtils.gI().setVisibleItem(editBtn);
        if (!canDelete)
            UiUtils.gI().setVisibleItem(deleteBtn);
    }

    // =====================
    // 6️⃣ UTILITY METHODS
    // =====================
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
