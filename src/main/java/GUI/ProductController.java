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
import UTILS.TaskUtil;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

public class ProductController implements IController {
    @FXML
    private TableView<ProductDisplayDTO> tblProduct;
    @FXML
    private TableColumn<ProductDisplayDTO, String> tlb_col_id;
    @FXML
    private TableColumn<ProductDisplayDTO, String> tlb_col_name;
    @FXML
    private TableColumn<ProductDisplayDTO, ImageView> tlb_col_imageUrl;
    @FXML
    private TableColumn<ProductDisplayDTO, String> tlb_col_description;
    @FXML
    private TableColumn<ProductDisplayDTO, String> tlb_col_categoryName;
    @FXML
    private TableColumn<ProductDisplayDTO, Integer> tlb_col_stockQuantity;
    @FXML
    private TableColumn<ProductDisplayDTO, String> tlb_col_sellingPrice;
    @FXML
    private TableColumn<ProductDisplayDTO, String> tlb_col_status;
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
    private ComboBox<CategoryDTO> cbCategoryFilter;
    @FXML
    private PaginationController paginationController;

    private final int PAGE_SIZE = 10;
    private String keyword = "";
    private CategoryDTO categoryFilter = null;
    private StatusDTO statusFilter = null;
    private BigDecimal startPrice = null;
    private BigDecimal endPrice = null;
    private ProductDisplayDTO selectedProduct;

    // BUS instances - initialized once
    private ProductBUS productBUS;
    private CategoryBUS categoryBUS;
    private StatusBUS statusBUS;
    @FXML
    private AnchorPane mainContent;
    @FXML
    private StackPane loadingOverlay;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        productBUS = ProductBUS.getInstance();
        categoryBUS = CategoryBUS.getInstance();
        statusBUS = StatusBUS.getInstance();

        tblProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblProduct.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        loadTable();
        setupListeners();
        setupPagination();
        applyFilters();
    }

    // =====================
    // 2️⃣ UI SETUP (LOAD & CONFIG)
    // =====================
    private void loadComboBox() {
        ArrayList<StatusDTO> statusList = statusBUS.getAllByType(StatusType.PRODUCT);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusList);

        CategoryDTO allCategory = new CategoryDTO(-1, "Tất cả thể loại");
        cbCategoryFilter.getItems().add(allCategory);
        cbCategoryFilter.getItems().addAll(categoryBUS.getAll());

        cbStatusFilter.getSelectionModel().selectFirst();
        cbCategoryFilter.getSelectionModel().selectFirst();
    }

    @Override
    public void loadTable() {
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_imageUrl.setCellValueFactory(cellData -> {
            String url = cellData.getValue().getImageUrl();
            return new SimpleObjectProperty<>(UiUtils.gI().createImageView(url, 70, 70));
        });

        tlb_col_description.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription() == null ? "" : cellData.getValue().getDescription()));

        // Không cần gọi BUS - categoryName đã có từ JOIN
        tlb_col_categoryName.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCategoryName() != null ? cellData.getValue().getCategoryName() : ""));

        tlb_col_sellingPrice.setCellValueFactory(cellData -> new SimpleStringProperty(
                ValidationUtils.getInstance().formatCurrency(cellData.getValue().getSellingPrice())));
        tlb_col_stockQuantity.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        // Không cần gọi BUS - statusDescription đã có từ JOIN
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatusDescription() != null ? cellData.getValue().getStatusDescription() : ""));

        UiUtils.gI().addTooltipToColumn(tlb_col_name, 15);
        UiUtils.gI().addTooltipToColumn(tlb_col_description, 15);
        UiUtils.gI().addTooltipToColumn(tlb_col_categoryName, 15);
    }

    private void setupPagination() {
        // Thiết lập callback: Khi trang đổi -> gọi hàm load dữ liệu
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex);
        });
    }

    private void loadPageData(int pageIndex) {
        String keyword = txtSearch.getText().trim();
        int statusId = (cbStatusFilter.getValue() == null) ? -1 : cbStatusFilter.getValue().getId();
        int categoryId = (cbCategoryFilter.getValue() == null) ? -1 : cbCategoryFilter.getValue().getId();

        // Sử dụng method DISPLAY version - JOIN category & status, không cần gọi BUS lẻ
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.PRODUCT_LIST_VIEW,
                () -> productBUS.filterProductsPagedForManageDisplay(keyword, categoryId, statusId, startPrice,
                        endPrice,
                        pageIndex,
                        PAGE_SIZE),
                result -> {
                    // Lấy dữ liệu ProductDisplayDTO đã được JOIN
                    PagedResponse<ProductDisplayDTO> res = result.getPagedData();

                    tblProduct.setItems(FXCollections.observableArrayList(res.getItems()));

                    // Cập nhật tổng số trang dựa trên COUNT(*) từ DB
                    int totalPages = (int) Math.ceil((double) res.getTotalItems() / PAGE_SIZE);
                    paginationController.setPageCount(totalPages > 0 ? totalPages : 1);

                    tblProduct.getSelectionModel().clearSelection();
                });
    }

    @Override
    public void setupListeners() {
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
            Stage currentStage = (Stage) addBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
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
                .configure(c -> c.setProduct(selectedProduct.getId()))
                .open();
        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) editBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            resetFilters();
        }
    }

    private void handleDetail() {
        if (isNotSelectedProduct()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        new ModalBuilder<ProductModalController>("/GUI/ProductModal.fxml", ProductModalController.class)
                .setTitle("Xem chi tiết sản phẩm")
                .modeDetail()
                .configure(c -> c.setProduct(selectedProduct.getId()))
                .open();
    }

    private void handleDelete() {
        if (isNotSelectedProduct()) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert(AppMessages.PRODUCT_DELETE_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        BUSResult updateResult = SecureExecutor.executeSafeBusResult(PermissionKey.PRODUCT_DELETE,
                () -> productBUS.delete(selectedProduct.getId()));

        if (updateResult.isSuccess()) {
            Stage currentStage = (Stage) deleteBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, updateResult.getMessage());
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
        if (paginationController.getCurrentPage() == 0) {
            loadPageData(0); // Trường hợp đang ở trang 0 rồi thì phải gọi thủ công
        } else {
            paginationController.setCurrentPage(0);
        }
    }

    @Override
    public void resetFilters() {
        cbStatusFilter.getSelectionModel().selectFirst();
        cbCategoryFilter.getSelectionModel().selectFirst();
        txtSearch.clear();
        txtStartPrice.clear();
        txtEndPrice.clear();

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
