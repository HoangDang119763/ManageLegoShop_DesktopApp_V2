package GUI;

import BUS.CategoryBUS;
import BUS.ImportBUS;
import BUS.ProductBUS;
import DTO.*;
import ENUM.PermissionKey;
import INTERFACE.IController;
import SERVICE.SessionManagerService;
import SERVICE.TemplateGeneratorService;
import SERVICE.ExcelService;
import UTILS.ModalBuilder;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;

/**
 * Controller cho ImportProduct.fxml
 * Hiển thị danh sách sản phẩm có tồn kho > 0 với pagination
 * Hỗ trợ tìm kiếm theo tên + lọc theo thể loại
 */
public class ImportProductController implements IController {

    // ==================== FILTER & SEARCH CONTROLS ====================
    @FXML
    private TextField txtProductNameSearch;
    @FXML
    private ComboBox<CategoryDTO> cbCategoryFilter;
    @FXML
    private ComboBox<String> cbPriceSortOrder;
    @FXML
    private Button btnClearProduct;

    // ==================== DISPLAY CONTROLS ====================
    @FXML
    private GridPane gpShowProductWrapper;
    @FXML
    private StackPane loadingOverlay;
    @FXML
    private ScrollPane scrollPane;

    // ==================== TABLE COLUMNS ====================
    @FXML
    private TableView<TempDetailImportDTO> tbvDetailImportProduct;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tlb_col_productId;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tlb_col_productName;
    @FXML
    private TableColumn<TempDetailImportDTO, Integer> tlb_col_quantity;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tlb_col_profitPercent;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tlb_col_importPrice;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tlb_col_totalPrice;

    // ==================== PAGINATION ====================
    @FXML
    private PaginationController paginationController;

    // ==================== BUTTONS ====================
    @FXML
    private Button btnExitImportingForm;
    @FXML
    private Button btnGetSupInfo;
    @FXML
    private Button btnImportListProductRemove;
    @FXML
    private Button btnImportListProductClear, btnSubmitImport;
    @FXML
    private Button btnImportFromExcel, btnDownloadTemplate;

    // ==================== LABELS & FIELDS ====================
    @FXML
    private TextField lbTotalImportPrice;

    // ==================== BUS INSTANCES ====================
    private ProductBUS productBUS;
    private CategoryBUS categoryBUS;
    private SessionManagerService session;
    private ImportBUS importBUS;
    private final int PAGE_SIZE = 12; // 3 cột x 3 hàng

    // ==================== FILTER STATE ====================
    private String keyword = "";
    private int selectedCategoryId = -1;
    private String priceOrder = ""; // "" = không sort, "ASC" = tăng, "DESC" = giảm
    private ValidationUtils validationUtils = ValidationUtils.getInstance();
    private boolean isResetting = false;

    // ==================== DETAIL DATA ====================
    private ObservableList<TempDetailImportDTO> detailImportList = FXCollections.observableArrayList();
    private SupplierForImportDTO selectedSupplier = null;
    private List<ProductDisplayForImportDTO> currentDisplayedProducts = new ArrayList<>();
    @FXML
    private TextField txtImportId, txtEmployeeFullName, txtSupplierName;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        productBUS = ProductBUS.getInstance();
        categoryBUS = CategoryBUS.getInstance();
        session = SessionManagerService.getInstance();
        importBUS = ImportBUS.getInstance();

        updateImportId();
        txtEmployeeFullName.setText(session.getLoggedName());
        // Setup UI
        loadCategoryFilter();
        loadPriceSortFilter();
        loadTable();
        setupDetailTable();
        setupPagination();
        setupListeners();

        // Load initial data trang 0
        applyFilters();
    }

    private void updateImportId() {
        int nextId = importBUS.nextId();
        txtImportId.setText(String.valueOf(nextId));
    }

    /**
     * Load category combo box cho filter
     */
    private void loadCategoryFilter() {
        // Add "Tất cả thể loại" option
        CategoryDTO allCategories = new CategoryDTO();
        allCategories.setId(-1);
        allCategories.setName("Tất cả thể loại");

        cbCategoryFilter.getItems().add(allCategories);
        cbCategoryFilter.getItems().addAll(categoryBUS.getAll());
        cbCategoryFilter.getSelectionModel().selectFirst();
    }

    /**
     * Load price sort combo box cho filter
     */
    private void loadPriceSortFilter() {
        cbPriceSortOrder.getItems().addAll(
                "Không sắp xếp",
                "Giá thấp đến cao",
                "Giá cao đến thấp");
        cbPriceSortOrder.getSelectionModel().selectFirst();
    }

    /**
     * Setup GridPane layout (3 cột)
     * Chỉ cấu hình cấu trúc, dữ liệu sẽ được đổ vào khi tải
     */
    public void loadTable() {
        gpShowProductWrapper.getChildren().clear();
    }

    /**
     * Setup TableView để hiển thị danh sách chi tiết sản phẩm nhập
     */
    private void setupDetailTable() {
        tlb_col_productId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        tlb_col_productName.setCellValueFactory(new PropertyValueFactory<>("name"));
        UiUtils.gI().addTooltipToColumn(tlb_col_productName, 20); // Tooltip nếu tên dài hơn 20 ký tự
        // Setup editable quantity column
        tlb_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tlb_col_quantity.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        tlb_col_quantity.setOnEditCommit(event -> {
            TempDetailImportDTO item = event.getRowValue();
            if (event.getNewValue() != null && event.getNewValue() > 0) {
                item.setQuantity(event.getNewValue());
                recalculateTotalPrice(item);
                tbvDetailImportProduct.refresh();
            }
        });

        // Setup editable profitPercent column
        tlb_col_profitPercent.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatPercent(cellData.getValue().getProfitPercent())));
        tlb_col_profitPercent
                .setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.StringConverter<String>() {
                    @Override
                    public String toString(String value) {
                        return value;
                    }

                    @Override
                    public String fromString(String string) {
                        return string;
                    }
                }));
        tlb_col_profitPercent.setOnEditCommit(event -> {
            TempDetailImportDTO item = event.getRowValue();
            try {
                String input = event.getNewValue();
                String cleanInput = input.replace("%", "").trim();
                BigDecimal newPercent = new BigDecimal(cleanInput);

                if (newPercent.compareTo(BigDecimal.ZERO) >= 0 && newPercent.compareTo(new BigDecimal(1000)) <= 0) {
                    item.setProfitPercent(newPercent);
                    recalculateTotalPrice(item);
                    tbvDetailImportProduct.refresh();
                }
            } catch (NumberFormatException e) {
                tbvDetailImportProduct.refresh();
            }
        });

        // Setup import price column (editable)
        tlb_col_importPrice.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getImportPrice())));
        tlb_col_importPrice.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.StringConverter<String>() {
            @Override
            public String toString(String value) {
                return value;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        }));
        tlb_col_importPrice.setOnEditCommit(event -> {
            TempDetailImportDTO item = event.getRowValue();
            try {
                String input = event.getNewValue();
                String cleanInput = input.replaceAll("[^0-9.,]", "").replace(",", "").trim();
                BigDecimal newPrice = new BigDecimal(cleanInput);

                if (newPrice.compareTo(BigDecimal.ZERO) > 0) {
                    item.setImportPrice(newPrice);
                    recalculateTotalPrice(item);
                    tbvDetailImportProduct.refresh();
                }
            } catch (NumberFormatException e) {
                tbvDetailImportProduct.refresh();
            }
        });

        tlb_col_totalPrice.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));

        tbvDetailImportProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tbvDetailImportProduct.setEditable(true);
        tbvDetailImportProduct.setItems(detailImportList);
    }

    /**
     * Setup pagination controller
     */
    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex, true);
        });
    }

    /**
     * Setup button & filter listeners
     */
    public void setupListeners() {
        cbCategoryFilter.setOnAction(event -> handleCategoryFilterChange());

        cbPriceSortOrder.setOnAction(event -> handlePriceSortChange());

        UiUtils.gI().applySearchDebounce(txtProductNameSearch, 500, () -> handleKeywordChange());

        btnClearProduct.setOnAction(e -> {
            resetFilters();
            Stage currentStage = (Stage) btnClearProduct.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Làm mới danh sách sản phẩm");
        });

        btnExitImportingForm.setOnAction(e -> {

            Stage stage = (Stage) btnExitImportingForm.getScene().getWindow();
            stage.close();
            UiUtils.gI().openStage1("/GUI/MainUI.fxml", "Lego Store");
        });

        btnGetSupInfo.setOnAction(e -> openSupplierModalForSelection());

        btnImportListProductRemove.setOnAction(e -> handleRemoveSelectedItem());

        btnImportListProductClear.setOnAction(e -> handleClearAllData());
        btnSubmitImport.setOnAction(e -> confirmImport());
        btnImportFromExcel.setOnAction(e -> handleImportFromExcel());
        btnDownloadTemplate.setOnAction(e -> handleDownloadTemplate());
    }

    /**
     * Load data cho trang hiện tại
     * Gom lại tham số filter, gọi BUS, cập nhật GridPane
     */
    private void loadPageData(int pageIndex, boolean showOverlay) {
        StackPane overlay = showOverlay ? loadingOverlay : null;

        // Chạy task ngầm
        TaskUtil.executeSecure(overlay, PermissionKey.IMPORT_INSERT,
                () -> productBUS.filterProductsPagedForImport(keyword, selectedCategoryId, priceOrder, pageIndex,
                        PAGE_SIZE),
                result -> {
                    PagedResponse<ProductDisplayForImportDTO> res = result.getPagedData();
                    currentDisplayedProducts = res.getItems();
                    // Cập nhật GridPane with product cards
                    displayProductCards(currentDisplayedProducts);

                    // Cập nhật pagination
                    int totalPages = (int) Math.ceil((double) res.getTotalItems() / PAGE_SIZE);
                    paginationController.setPageCount(totalPages > 0 ? totalPages : 1);
                });
    }

    /**
     * Display product cards trong GridPane
     * Mỗi card là VBox chứa: image, name, stock, price
     * GridPane có 3 cột, auto-wrap
     */
    private void displayProductCards(List<ProductDisplayForImportDTO> products) {
        gpShowProductWrapper.getChildren().clear();

        if (products.isEmpty()) {
            // Show empty state
            Label emptyLabel = new Label("Không tìm thấy sản phẩm");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-font-size: 14;");
            gpShowProductWrapper.add(emptyLabel, 0, 0);
            return;
        }

        int row = 0, col = 0;
        for (ProductDisplayForImportDTO product : products) {
            HBox productCard = createProductCard(product);
            gpShowProductWrapper.add(productCard, col, row);

            // Move to next position (3 columns)
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Tạo HBox card cho một sản phẩm - design mới (ngang)
     * Chứa: ImageView (80x80 bên trái) + Content VBox (tên, kho, giá, trạng thái
     * bên phải)
     */
    private HBox createProductCard(ProductDisplayForImportDTO product) {
        // Main container - HBox ngang
        HBox card = new HBox();
        // Trạng thái bình thường: Border xanh nhạt, hòa quyện với màu của Line trong
        // FXML
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-color:  #30bac4; " + // Đổi từ #eee sang xanh nhạt
                "-fx-border-width: 1.5; " + // Tăng độ dày một chút để thấy màu
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 5, 0, 0, 2);");

        // Thêm hiệu ứng Hover để card "nổi" lên khi nhân viên rê chuột vào
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #1f7d84; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " + // Đảm bảo cursor vẫn là hand
                "-fx-effect: dropshadow(three-pass-box, rgba(31,125,132,0.2), 10, 0, 0, 0);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; " +
                "-fx-border-color:  #30bac4; " + // Đổi từ #eee sang xanh nhạt
                "-fx-border-width: 1.5; " + // Tăng độ dày một chút để thấy màu
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 5, 0, 0, 2);"));
        card.setSpacing(10);
        card.setPadding(new Insets(8));
        card.setMinHeight(120);

        // Image container (80x80)
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-background-color: #f1f2f6; -fx-background-radius: 5;");
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);
        imageContainer.setMinWidth(85);
        imageContainer.setPrefWidth(85);
        imageContainer.setMinHeight(85);

        // Load image hoặc show placeholder
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                File imageFile = new File(product.getImageUrl());
                if (imageFile.exists()) {
                    ImageView imageView = new ImageView(new Image(imageFile.toURI().toString()));
                    imageView.setFitHeight(85);
                    imageView.setFitWidth(85);
                    // imageView.setPreserveRatio(true);
                    imageContainer.getChildren().add(imageView);
                } else {
                    imageContainer.getChildren().add(UiUtils.gI().createPlaceholderIcon());
                }
            } catch (Exception e) {
                imageContainer.getChildren().add(UiUtils.gI().createPlaceholderIcon());
            }
        } else {
            imageContainer.getChildren().add(UiUtils.gI().createPlaceholderIcon());
        }

        // Content VBox
        VBox contentBox = new VBox();
        contentBox.setSpacing(4);
        HBox.setHgrow(contentBox, javafx.scene.layout.Priority.ALWAYS);

        // Product name - với tooltip
        Label nameLabel = new Label(product.getId() + "-" + product.getName());
        nameLabel.setMaxWidth(130);
        nameLabel.setWrapText(false);
        nameLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 12;");
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        UiUtils.gI().addTooltipToLabel(nameLabel, 20); // Tooltip nếu tên dài hơn 20 ký tự

        // Status badge - color based on status
        Label statusLabel = new Label(product.getStatusDescription() != null ? product.getStatusDescription() : "N/A");
        statusLabel.setStyle("-fx-padding: 2 6; -fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-radius: 3;");

        // Determine color based on status
        String statusColor = getStatusColor(product.getStatusName());
        statusLabel.setStyle(statusLabel.getStyle() + " -fx-background-color: " + statusColor + ";");

        // Stock quantity
        Label stockLabel = new Label("Kho: " + product.getStockQuantity());
        stockLabel.setStyle("-fx-text-fill: #16a085; -fx-font-weight: bold; -fx-font-size: 11;");

        // Spacer để đẩy giá xuống dưới
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Price box - ở dưới cùng, right align
        HBox priceBox = new HBox();
        priceBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        String priceText = validationUtils.formatCurrency(product.getImportPrice());
        Label priceLabel = new Label(priceText);
        priceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14;");
        UiUtils.gI().addTooltipToLabel(priceLabel, 8); // Tooltip nếu giá dài (e.g., "1.234.567đ")

        priceBox.getChildren().add(priceLabel);

        // Add to content
        contentBox.getChildren().addAll(nameLabel, statusLabel, stockLabel, spacer, priceBox);

        // Add to card
        card.getChildren().addAll(imageContainer, contentBox);

        // Add click event
        card.setOnMouseClicked(e -> handleProductCardClick(product));
        card.setStyle(card.getStyle() + "; -fx-cursor: hand;");

        return card;
    }

    /**
     * Determine status color based on statusName
     * ACTIVE -> #27ae60 (green)
     * SUSPENDED -> #f39c12 (yellow/orange)
     * Default -> #95a5a6 (gray)
     */
    private String getStatusColor(String statusName) {
        if (statusName == null)
            return "#95a5a6";

        switch (statusName.toUpperCase()) {
            case "ACTIVE":
                return "#27ae60"; // Green
            case "SUSPENDED":
                return "#f39c12"; // Yellow/Orange
            default:
                return "#95a5a6"; // Gray
        }
    }

    /**
     * Handle product card click
     * Add product to detail import list or increment quantity if already exists
     */
    private void handleProductCardClick(ProductDisplayForImportDTO product) {
        // Check if product already exists in the list
        for (TempDetailImportDTO item : detailImportList) {
            if (item.getProductId().equals(product.getId())) {
                // If exists, increment quantity
                item.setQuantity(item.getQuantity() + 1);
                recalculateTotalPrice(item);
                tbvDetailImportProduct.refresh();
                Stage currentStage = (Stage) btnClearProduct.getScene().getWindow();
                NotificationUtils.showToast(currentStage, "Tăng SL: " + product.getName());
                return;
            }
        }

        // If not exists, create new
        TempDetailImportDTO newDetail = new TempDetailImportDTO(
                product.getId(),
                product.getName(),
                1, // Default quantity
                BigDecimal.ZERO, // Default profit percent
                product.getImportPrice(),
                product.getImportPrice() // Initial totalPrice = importPrice * 1
        );
        detailImportList.add(newDetail);

        // Update table and total price
        tbvDetailImportProduct.refresh();
        updateTotalImportPrice();

        Stage currentStage = (Stage) btnClearProduct.getScene().getWindow();
        NotificationUtils.showToast(currentStage, "Thêm: " + product.getName());
    }

    /**
     * Handle keyword change từ text field
     */
    private void handleKeywordChange() {
        // 1. Chặn nếu đang trong quá trình reset
        if (isResetting)
            return;

        String newKeyword = txtProductNameSearch.getText().trim();

        // 2. Chặn nếu keyword không thay đổi (tránh debounce trigger nhầm)
        if (newKeyword.equals(keyword))
            return;

        keyword = newKeyword;
        applyFilters();
    }

    /**
     * Handle category filter change từ combo box
     */
    private void handleCategoryFilterChange() {
        selectedCategoryId = (cbCategoryFilter.getValue() == null) ? -1 : cbCategoryFilter.getValue().getId();
        applyFilters();
    }

    /**
     * Handle price sort change từ combo box
     */
    private void handlePriceSortChange() {
        String selected = cbPriceSortOrder.getValue();
        if (selected == null || selected.equals("Không sắp xếp")) {
            priceOrder = "";
        } else if (selected.equals("Giá thấp đến cao")) {
            priceOrder = "ASC";
        } else if (selected.equals("Giá cao đến thấp")) {
            priceOrder = "DESC";
        }
        applyFilters();
    }

    /**
     * Apply filter logic - đồng bộ hóa với CategoryController pattern
     * Nếu đang ở trang 0 thì gọi loadPageData trực tiếp
     * Nếu không thì gọi setCurrentPage(0) để pagination tự trigger loadPageData
     */
    public void applyFilters() {
        if (paginationController.getCurrentPage() == 0) {
            loadPageData(0, true);
        } else {
            paginationController.setCurrentPage(0);
        }
    }

    /**
     * Reset filter & reload data
     */
    public void resetFilters() {
        isResetting = true; // Khóa handler

        txtProductNameSearch.clear();
        cbCategoryFilter.getSelectionModel().selectFirst();
        cbPriceSortOrder.getSelectionModel().selectFirst();

        keyword = "";
        selectedCategoryId = -1;
        priceOrder = "";

        // Load dữ liệu trang 0 ngay lập tức
        loadPageData(0, true);
        paginationController.setCurrentPage(0);

        // Chỉ mở khóa sau khi các sự kiện UI đã ổn định
        javafx.application.Platform.runLater(() -> {
            isResetting = false;
        });
    }

    /**
     * Recalculate totalPrice based on importPrice, quantity, and profitPercent
     * Formula: totalPrice = importPrice * quantity * (1 + profitPercent/100)
     */
    private void recalculateTotalPrice(TempDetailImportDTO item) {
        if (item == null)
            return;

        BigDecimal basePrice = item.getImportPrice().multiply(new BigDecimal(item.getQuantity()));
        BigDecimal profitAmount = basePrice.multiply(item.getProfitPercent())
                .divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = basePrice.add(profitAmount);

        item.setTotalPrice(totalPrice);
        updateTotalImportPrice();
    }

    /**
     * Handle remove selected item from detail import table
     * Xóa sản phẩm được chọn khỏi bảng chi tiết
     */
    private void handleRemoveSelectedItem() {
        TempDetailImportDTO selectedItem = tbvDetailImportProduct.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            Stage currentStage = (Stage) btnImportListProductRemove.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Vui lòng chọn sản phẩm để xóa");
            return;
        }

        detailImportList.remove(selectedItem);
        tbvDetailImportProduct.refresh();
        updateTotalImportPrice();

        Stage currentStage = (Stage) btnImportListProductRemove.getScene().getWindow();
        NotificationUtils.showToast(currentStage, "Xóa: " + selectedItem.getName());
    }

    /**
     * Update lbTotalImportPrice label with sum of all totalPrice in
     * detailImportList
     * Cập nhật tổng tiền nhập từ tất cả các chi tiết trong bảng
     */
    private void updateTotalImportPrice() {
        BigDecimal total = BigDecimal.ZERO;

        for (TempDetailImportDTO item : detailImportList) {
            if (item.getTotalPrice() != null) {
                total = total.add(item.getTotalPrice());
            }
        }

        lbTotalImportPrice.setText(validationUtils.formatCurrency(total) + " VNĐ");
    }

    /**
     * Handle clear all data - delete all detail items and reset supplier
     * Xóa toàn bộ dữ liệu chi tiết nhập và reset nhà cung cấp
     */
    private void handleClearAllData() {
        detailImportList.clear();
        selectedSupplier = null;
        tbvDetailImportProduct.setItems(FXCollections.observableArrayList(detailImportList));
        updateTotalImportPrice();
        lbTotalImportPrice.setText(validationUtils.formatCurrency(BigDecimal.ZERO) + " VNĐ");
        txtSupplierName.setText("Chọn nhà cung cấp...");
        scrollPane.setHvalue(0);
        Stage currentStage = (Stage) btnImportListProductClear.getScene().getWindow();
        NotificationUtils.showToast(currentStage, "Đã xóa toàn bộ dữ liệu nhập");
    }

    /**
     * Get the selected supplier
     * 
     * @return SupplierDTO hoặc null nếu chưa chọn
     */
    /**
     * Get selected supplier (restricted DTO with only id, name, phone, address)
     */
    public SupplierForImportDTO getSelectedSupplier() {
        return selectedSupplier;
    }

    /**
     * Set the selected supplier
     * 
     * @param supplier SupplierDTO được chọn
     */
    /**
     * Set selected supplier (restricted DTO with only id, name, phone, address)
     */
    public void setSelectedSupplier(SupplierForImportDTO supplier) {
        this.selectedSupplier = supplier;
    }

    /**
     * Get the detail import list for saving
     * 
     * @return ObservableList<TempDetailImportDTO>
     */
    public ObservableList<TempDetailImportDTO> getDetailImportList() {
        return detailImportList;
    }

    /**
     * Open supplier selection modal (lazy loading - data only loads on search)
     * Mở modal chọn nhà cung cấp (lazy loading - chỉ tải dữ liệu khi tìm kiếm)
     */
    private void openSupplierModalForSelection() {
        SupForImportModalController modalController = new ModalBuilder<SupForImportModalController>(
                "/GUI/SupForImportModal.fxml", SupForImportModalController.class)
                .setTitle("Chọn nhà cung cấp")
                .open();

        if (modalController != null && modalController.getSelectedSupplier() != null) {
            setSelectedSupplier(modalController.getSelectedSupplier());
            Stage currentStage = (Stage) btnGetSupInfo.getScene().getWindow();
            txtSupplierName.setText("ID: " + selectedSupplier.getId() + " - " + selectedSupplier.getName());
            NotificationUtils.showToast(currentStage, "Nhà cung cấp: " + selectedSupplier.getName());
        }
    }

    private void confirmImport() {
        if (!validateImportData()) {
            return;
        }

        ConfirmImportModalController modalController = new ModalBuilder<ConfirmImportModalController>(
                "/GUI/ConfirmImportModal.fxml", ConfirmImportModalController.class)
                .setTitle("Xác nhận nhập hàng")
                .configure(e -> e.setConfirmData(selectedSupplier, new ArrayList<>(detailImportList)))
                .open();

        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) btnSubmitImport.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            handleClearAllData();
            applyFilters();
            updateImportId();
        }
    }

    /**
     * Kiểm tra tính hợp lệ của dữ liệu trước khi xác nhận nhập hàng
     * 
     * @return true nếu dữ liệu hợp lệ, false nếu có lỗi
     */
    private boolean validateImportData() {
        // 1. Kiểm tra Nhà cung cấp
        if (selectedSupplier == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn nhà cung cấp trước khi nhập hàng!", "Thiếu thông tin");
            return false;
        }

        // 2. Kiểm tra danh sách sản phẩm nhập
        if (detailImportList.isEmpty()) {
            NotificationUtils.showErrorAlert("Danh sách sản phẩm nhập đang trống!", "Thiếu thông tin");
            return false;
        }

        // 3. Kiểm tra chi tiết từng sản phẩm
        for (TempDetailImportDTO item : detailImportList) {
            // Kiểm tra Số lượng (Phải > 0)
            if (item.getQuantity() <= 0) {
                NotificationUtils.showErrorAlert(
                        "Sản phẩm '" + item.getName() + "' có số lượng không hợp lệ (phải > 0)!", "Lỗi dữ liệu");
                return false;
            }

            // Kiểm tra Giá nhập (Phải > 0)
            if (item.getImportPrice() == null || item.getImportPrice().compareTo(BigDecimal.ZERO) <= 0) {
                NotificationUtils.showErrorAlert("Sản phẩm '" + item.getName() + "' chưa có giá nhập hợp lệ!",
                        "Lỗi dữ liệu");
                return false;
            }

            // Kiểm tra Lợi nhuận (Thường là >= 0, vì có thể bán hòa vốn nhưng không nên âm)
            if (item.getProfitPercent() == null || item.getProfitPercent().compareTo(BigDecimal.ZERO) < 0) {
                NotificationUtils.showErrorAlert(
                        "Sản phẩm '" + item.getName() + "' có tỷ lệ lợi nhuận không hợp lệ (>= 0)!", "Lỗi dữ liệu");
                return false;
            }
        }

        return true;
    }

    /**
     * Handle importing from Excel file
     * Open file chooser, read Excel, validate data, populate detailImportList
     * Validation:
     * - Check duplicate productId (nhiều hơn 1 dòng cùng productId)
     * - Check productId có khớp với điều kiện nhập (phải có trong danh sách sản
     * phẩm hợp lệ)
     * - Báo lỗi chi tiết dòng nào
     */
    private void handleImportFromExcel() {
        try {
            // Open file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn file Excel để nhập hàng");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("File Excel (*.xlsx)", "*.xlsx"));

            Stage stage = (Stage) btnImportFromExcel.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile == null) {
                return; // User cancelled
            }

            // Read Excel data
            ExcelService excelService = ExcelService.getInstance();
            List<TempDetailImportDTO> importedData = excelService.readImportDataFromExcel(selectedFile);

            if (importedData == null || importedData.isEmpty()) {
                NotificationUtils.showErrorAlert("File Excel không chứa dữ liệu hợp lệ!", "Lỗi");
                return;
            }

            // Validate imported data using ImportBUS
            BUSResult validationResult = importBUS.validateImportData(importedData, currentDisplayedProducts);

            if (!validationResult.isSuccess()) {
                // Show validation errors
                NotificationUtils.showErrorAlert(validationResult.getMessage(), "Lỗi dữ liệu nhập");
                return;
            }

            // Recalculate totalPrice for all imported items based on profit percent
            for (TempDetailImportDTO item : importedData) {
                recalculateTotalPrice(item);
            }

            // Ask user if they want to append or replace current data
            boolean append = UiUtils.gI().showConfirmAlert(
                    "Bạn có muốn thêm dữ liệu vào danh sách hiện tại không?\n" +
                            "Chọn 'OK' để thêm vào, 'Cancel' để thay thế toàn bộ.",
                    "Xác nhận nhập dữ liệu");

            if (!append) {
                // Clear current list
                detailImportList.clear();
            }

            // Add imported data to list
            detailImportList.addAll(importedData);
            tbvDetailImportProduct.setItems(FXCollections.observableArrayList(detailImportList));

            updateTotalImportPrice();

            Stage currentStage = (Stage) btnImportFromExcel.getScene().getWindow();
            NotificationUtils.showToast(currentStage,
                    "Nhập thành công " + importedData.size() + " sản phẩm từ file Excel!");

        } catch (IOException e) {
            NotificationUtils.showErrorAlert("Lỗi khi đọc file Excel:\n" + e.getMessage(), "Lỗi");
            System.err.println("Error reading Excel: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            NotificationUtils.showErrorAlert("Lỗi xảy ra:\n" + e.getMessage(), "Lỗi");
            System.err.println("Error importing from Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle download import template
     * Generate and download Excel template file with sample data
     */
    private void handleDownloadTemplate() {
        TaskUtil.executePublic(loadingOverlay,
                () -> TemplateGeneratorService.getInstance().generateImportTemplate(currentDisplayedProducts),
                result -> {
                    Stage currentStage = (Stage) btnDownloadTemplate.getScene().getWindow();
                    if (result.isSuccess()) {
                        NotificationUtils.showToast(currentStage, result.getMessage());
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), "Lỗi");
                    }
                });
    }

    @Override
    public void hideButtonWithoutPermission() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hideButtonWithoutPermission'");
    }
}
