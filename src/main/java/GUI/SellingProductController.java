package GUI;

import BUS.*;
import DTO.*;
import SERVICE.PrintService;
import SERVICE.SessionManagerService;
import UTILS.ModalBuilder;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SellingProductController {
    // ==================== BUS INSTANCES ====================
    private ProductBUS productBUS;
    private CategoryBUS categoryBUS;
    private SessionManagerService session;
    private InvoiceBUS invoiceBUS;
    private final int PAGE_SIZE = 12; // 3 columns x 4 rows

    // ==================== FILTER STATE ====================
    private String keyword = "";
    private int selectedCategoryId = -1;
    private String priceOrder = ""; // "" = no sort, "ASC" = low to high, "DESC" = high to low
    private ValidationUtils validationUtils = ValidationUtils.getInstance();
    private boolean isResetting = false;

    // ==================== DETAIL DATA ====================
    private ObservableList<TempDetailInvoiceDTO> detailInvoiceList = FXCollections.observableArrayList();

    // ==================== DISCOUNT DATA ====================
    @Setter
    private CustomerForInvoiceDTO selectedCustomer = null;
    @Setter
    private DiscountForInvoiceDTO selectedDiscount = null;
    private ArrayList<DetailDiscountDTO> selectedDetailDiscountList = new java.util.ArrayList<>();
    private DetailDiscountDTO selectedDetailDiscount = null;
    private BigDecimal totalPriceInvoice = BigDecimal.ZERO;
    private BigDecimal discountPrice = BigDecimal.ZERO;
    private BigDecimal finalTotalPriceInvoice = BigDecimal.ZERO;

    // ==================== FXML COMPONENTS ====================
    @FXML
    private TextField txtInvoiceId;
    @FXML
    private TextField txtEmployeeId, txtCodeDiscount;
    @FXML
    private TextField txtEmployeeFullName;
    @FXML
    private TextField txtCustomerName;
    @FXML
    private GridPane gpShowProductWrapper;
    @FXML
    private TextField txtProductNameSearch;
    @FXML
    private TextField lbTotalInvoicePrice, lbDiscountPrice;
    @FXML
    private TextField lbFinalTotalInvoicePrice;
    @FXML
    private ComboBox<CategoryDTO> cbCategoryFilter;
    @FXML
    private ComboBox<String> cbPriceSortOrder;
    @FXML
    private TableView<TempDetailInvoiceDTO> tbvDetailInvoiceProduct;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tlb_col_index;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tlb_col_productName;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tlb_col_quantity;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tlb_col_price;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tlb_col_totalPrice;
    @FXML
    private Button btnExitSellingForm;
    @FXML
    private Button btnGetCusInfo;
    @FXML
    private Button btnGetDiscount;
    @FXML
    private Button btnInvoiceListProductRemove;
    @FXML
    private Button btnInvoiceListProductClear;
    @FXML
    private Button btnSubmitInvoice;
    @FXML
    private Button btnClearProduct;
    @FXML
    private StackPane loadingOverlay;
    @FXML
    private PaginationController paginationController;
    @FXML
    private ScrollPane scrollPane;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        productBUS = ProductBUS.getInstance();
        categoryBUS = CategoryBUS.getInstance();
        session = SessionManagerService.getInstance();
        invoiceBUS = InvoiceBUS.getInstance();

        updateInvoiceId();
        txtEmployeeFullName.setText(session.getLoggedName());
        txtEmployeeFullName.setText(EmployeeBUS.getInstance().getById(session.employeeLoginId()).getFullName());

        // Setup UI
        loadCategoryFilter();
        loadPriceSortFilter();
        loadTable();
        setupDetailTable();
        setupPagination();
        setupListeners();

        // Load initial data page 0
        applyFilters();
    }

    private void updateInvoiceId() {
        int nextId = invoiceBUS.nextId();
        txtInvoiceId.setText(String.valueOf(nextId));
    }

    /**
     * Load category combo box for filter
     */
    private void loadCategoryFilter() {
        CategoryDTO allCategories = new CategoryDTO();
        allCategories.setId(-1);
        allCategories.setName("Tất cả thể loại");

        cbCategoryFilter.getItems().add(allCategories);
        cbCategoryFilter.getItems().addAll(categoryBUS.getAll());
        cbCategoryFilter.getSelectionModel().selectFirst();
    }

    /**
     * Load price sort combo box for filter
     */
    private void loadPriceSortFilter() {
        cbPriceSortOrder.getItems().addAll(
                "Không sắp xếp",
                "Giá thấp đến cao",
                "Giá cao đến thấp");
        cbPriceSortOrder.getSelectionModel().selectFirst();
    }

    /**
     * Setup GridPane layout (3 columns)
     */
    public void loadTable() {
        gpShowProductWrapper.getChildren().clear();
    }

    /**
     * Setup TableView to display invoice detail product list
     */
    private void setupDetailTable() {
        tlb_col_index.setCellValueFactory(new PropertyValueFactory<>("productId"));
        tlb_col_productName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_quantity.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getQuantity())));

        tlb_col_quantity.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object != null ? object : "";
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        }));

        tlb_col_quantity.setOnEditCommit(event -> {
            TempDetailInvoiceDTO item = event.getRowValue();
            String newValue = event.getNewValue();

            try {
                int quantity = Integer.parseInt(newValue.trim());
                if (quantity < 1) {
                    NotificationUtils.showErrorAlert("Số lượng phải >= 1", "Thông báo");
                    tbvDetailInvoiceProduct.refresh();
                    return;
                }
                if (quantity > item.getStockQuantity()) {
                    NotificationUtils.showErrorAlert(
                            "Số lượng không vượt quá " + item.getStockQuantity() + " (kho hiện có)", "Thông báo");
                    tbvDetailInvoiceProduct.refresh();
                    return;
                }
                item.setQuantity(quantity);
                recalculateTotalPrice(item);
                tbvDetailInvoiceProduct.refresh();
            } catch (NumberFormatException e) {
                NotificationUtils.showErrorAlert("Vui lòng nhập số hợp lệ", "Thông báo");
                tbvDetailInvoiceProduct.refresh();
            }
        });
        tlb_col_price.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        validationUtils.formatCurrency(cellData.getValue().getSellingPrice())));
        tlb_col_totalPrice.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));

        tbvDetailInvoiceProduct.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tbvDetailInvoiceProduct.setEditable(true);
        tbvDetailInvoiceProduct.setItems(detailInvoiceList);
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

        btnExitSellingForm.setOnAction(e -> {
            UiUtils.gI().openStage(
                    "/GUI/NavigatePermission.fxml",
                    "Danh sách chức năng");
            if (btnExitSellingForm.getScene() != null && btnExitSellingForm.getScene().getWindow() != null) {
                Stage stage = (Stage) btnExitSellingForm.getScene().getWindow();
                stage.close();
            }
        });

        btnGetCusInfo.setOnAction(e -> onMouseClickedShowCustomerContainer());
        btnInvoiceListProductRemove.setOnAction(e -> handleRemoveSelectedItem());
        btnInvoiceListProductClear.setOnAction(e -> handleClearAllData());
        btnGetDiscount.setOnAction(e -> onMouseClickedShowDiscountContainer());
        btnSubmitInvoice.setOnAction(e -> handleInvoice());
    }

    /**
     * Load data for current page
     */
    private void loadPageData(int pageIndex, boolean showOverlay) {
        StackPane overlay = showOverlay ? loadingOverlay : null;

        TaskUtil.executePublic(overlay,
                () -> productBUS.filterProductsPagedForSelling(keyword, selectedCategoryId, priceOrder, pageIndex,
                        PAGE_SIZE),
                result -> {
                    PagedResponse<ProductDisplayForSellingDTO> res = result.getPagedData();
                    displayProductCards(res.getItems());
                    int totalPages = (int) Math.ceil((double) res.getTotalItems() / PAGE_SIZE);
                    paginationController.setPageCount(totalPages > 0 ? totalPages : 1);
                });
    }

    /**
     * Display product cards in GridPane
     */
    private void displayProductCards(List<ProductDisplayForSellingDTO> products) {
        gpShowProductWrapper.getChildren().clear();

        if (products.isEmpty()) {
            Label emptyLabel = new Label("Không tìm thấy sản phẩm");
            emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-font-size: 14;");
            gpShowProductWrapper.add(emptyLabel, 0, 0);
            return;
        }

        int row = 0, col = 0;
        for (ProductDisplayForSellingDTO product : products) {
            HBox productCard = createProductCard(product);
            gpShowProductWrapper.add(productCard, col, row);
            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }

    /**
     * Create HBox card for a product
     */
    private HBox createProductCard(ProductDisplayForSellingDTO product) {
        HBox card = new HBox();
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #30bac4; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 5, 0, 0, 2);");

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #1f7d84; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(31,125,132,0.2), 10, 0, 0, 0);"));

        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #30bac4; " +
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 5, 0, 0, 2);"));

        card.setSpacing(10);
        card.setPadding(new Insets(8));
        card.setMinHeight(120);

        // Image container
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-background-color: #f1f2f6; -fx-background-radius: 5;");
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setMinWidth(85);
        imageContainer.setPrefWidth(85);
        imageContainer.setMinHeight(85);

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            try {
                File imageFile = new File(product.getImageUrl());
                if (imageFile.exists()) {
                    ImageView imageView = new ImageView(new Image(imageFile.toURI().toString()));
                    imageView.setFitHeight(85);
                    imageView.setFitWidth(85);
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
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label nameLabel = new Label(product.getId() + "-" + product.getName());
        nameLabel.setMaxWidth(130);
        nameLabel.setWrapText(false);
        nameLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 12;");
        nameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        UiUtils.gI().addTooltipToLabel(nameLabel, 20);

        Label statusLabel = new Label(product.getStatusDescription() != null ? product.getStatusDescription() : "N/A");
        statusLabel.setStyle("-fx-padding: 2 6; -fx-font-size: 10; -fx-font-weight: bold; -fx-text-fill: white; " +
                "-fx-background-radius: 3;");
        String statusColor = getStatusColor(product.getStatusName());
        statusLabel.setStyle(statusLabel.getStyle() + " -fx-background-color: " + statusColor + ";");

        Label stockLabel = new Label("Kho: " + product.getStockQuantity());
        stockLabel.setStyle("-fx-text-fill: #16a085; -fx-font-weight: bold; -fx-font-size: 11;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox priceBox = new HBox();
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        String priceText = validationUtils.formatCurrency(product.getSellingPrice());
        Label priceLabel = new Label(priceText);
        priceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 14;");
        UiUtils.gI().addTooltipToLabel(priceLabel, 8);
        priceBox.getChildren().add(priceLabel);

        contentBox.getChildren().addAll(nameLabel, statusLabel, stockLabel, spacer, priceBox);
        card.getChildren().addAll(imageContainer, contentBox);

        card.setOnMouseClicked(e -> handleProductCardClick(product));
        card.setStyle(card.getStyle() + "; -fx-cursor: hand;");

        return card;
    }

    /**
     * Determine status color
     */
    private String getStatusColor(String statusName) {
        if (statusName == null)
            return "#95a5a6";
        switch (statusName.toUpperCase()) {
            case "ACTIVE":
                return "#27ae60";
            case "SUSPENDED":
                return "#f39c12";
            default:
                return "#95a5a6";
        }
    }

    /**
     * Handle product card click - add to invoice
     */
    private void handleProductCardClick(ProductDisplayForSellingDTO product) {
        for (TempDetailInvoiceDTO item : detailInvoiceList) {
            if (item.getProductId().equals(product.getId())) {
                if (item.getQuantity() >= item.getStockQuantity()) {
                    Stage currentStage = (Stage) btnClearProduct.getScene().getWindow();
                    NotificationUtils.showToast(currentStage, "Hết hàng: " + product.getName());
                    return;
                }
                item.setQuantity(item.getQuantity() + 1);
                recalculateTotalPrice(item);
                tbvDetailInvoiceProduct.refresh();
                Stage currentStage = (Stage) btnClearProduct.getScene().getWindow();
                NotificationUtils.showToast(currentStage, "Tăng SL: " + product.getName());
                return;
            }
        }

        TempDetailInvoiceDTO newDetail = new TempDetailInvoiceDTO(
                product.getId(),
                product.getName(),
                1,
                product.getStockQuantity(),
                product.getSellingPrice(),
                product.getSellingPrice());
        detailInvoiceList.add(newDetail);
        tbvDetailInvoiceProduct.refresh();
        updateInvoiceTotals();

        Stage currentStage = (Stage) btnClearProduct.getScene().getWindow();
        NotificationUtils.showToast(currentStage, "Thêm: " + product.getName());
    }

    /**
     * Handle keyword change
     */
    private void handleKeywordChange() {
        if (isResetting)
            return;
        String newKeyword = txtProductNameSearch.getText().trim();
        if (newKeyword.equals(keyword))
            return;
        keyword = newKeyword;
        applyFilters();
    }

    /**
     * Handle category filter change
     */
    private void handleCategoryFilterChange() {
        selectedCategoryId = (cbCategoryFilter.getValue() == null) ? -1 : cbCategoryFilter.getValue().getId();
        applyFilters();
    }

    /**
     * Handle price sort change
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
     * Apply filters
     */
    public void applyFilters() {
        if (paginationController.getCurrentPage() == 0) {
            loadPageData(0, true);
        } else {
            paginationController.setCurrentPage(0);
        }
    }

    /**
     * Reset filters & reload data
     */
    public void resetFilters() {
        isResetting = true;
        txtProductNameSearch.clear();
        cbCategoryFilter.getSelectionModel().selectFirst();
        cbPriceSortOrder.getSelectionModel().selectFirst();
        keyword = "";
        selectedCategoryId = -1;
        priceOrder = "";
        loadPageData(0, true);
        paginationController.setCurrentPage(0);

        Platform.runLater(() -> isResetting = false);
    }

    /**
     * Recalculate totalPrice
     */
    private void recalculateTotalPrice(TempDetailInvoiceDTO item) {
        if (item == null)
            return;
        BigDecimal totalPrice = item.getSellingPrice().multiply(new BigDecimal(item.getQuantity()));
        item.setTotalPrice(totalPrice);
        updateInvoiceTotals();
    }

    /**
     * Handle remove selected item
     */
    private void handleRemoveSelectedItem() {
        TempDetailInvoiceDTO selectedItem = tbvDetailInvoiceProduct.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Stage currentStage = (Stage) btnInvoiceListProductRemove.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Vui lòng chọn sản phẩm để xóa");
            return;
        }
        detailInvoiceList.remove(selectedItem);
        tbvDetailInvoiceProduct.refresh();
        updateInvoiceTotals();
        Stage currentStage = (Stage) btnInvoiceListProductRemove.getScene().getWindow();
        NotificationUtils.showToast(currentStage, "Xóa: " + selectedItem.getName());
    }

    /**
     * Handle clear all data
     */
    private void handleClearAllData() {
        detailInvoiceList.clear();
        selectedCustomer = null;
        selectedDiscount = null;
        selectedDetailDiscountList.clear();
        discountPrice = BigDecimal.ZERO;
        tbvDetailInvoiceProduct.refresh();
        txtCustomerName.setText("Chọn khách...");
        txtCodeDiscount.setText("Chọn khuyến mãi...");
        updateInvoiceTotals();
        scrollPane.setHvalue(0);
        Stage currentStage = (Stage) btnInvoiceListProductClear.getScene().getWindow();
        NotificationUtils.showToast(currentStage, "Đã xóa toàn bộ dữ liệu");
    }

    /**
     * Update invoice totals with discount calculation
     */
    private void updateInvoiceTotals() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (TempDetailInvoiceDTO item : detailInvoiceList) {
            if (item.getTotalPrice() != null) {
                subtotal = subtotal.add(item.getTotalPrice());
            }
        }
        totalPriceInvoice = subtotal;
        if (selectedDiscount != null && selectedDetailDiscountList != null) {
            setDiscountPrice();
        } else {
            discountPrice = BigDecimal.ZERO;
        }
        finalTotalPriceInvoice = totalPriceInvoice.subtract(discountPrice);
        lbTotalInvoicePrice.setText(validationUtils.formatCurrency(totalPriceInvoice) + " VNĐ");
        lbDiscountPrice.setText(validationUtils.formatCurrency(discountPrice) + " VNĐ");
        lbFinalTotalInvoicePrice.setText(validationUtils.formatCurrency(finalTotalPriceInvoice) + " VNĐ");
    }

    /**
     * Open customer selection modal
     */
    private void onMouseClickedShowCustomerContainer() {
        CusForSellingModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/CusForSellingModal.fxml", null, "Danh sách khách hàng");
        if (modalController != null && modalController.getSelectedCustomer() != null) {
            setSelectedCustomer(modalController.getSelectedCustomer());
            Stage currentStage = (Stage) btnGetCusInfo.getScene().getWindow();
            txtCustomerName.setText("ID: " + selectedCustomer.getId() + " - " + selectedCustomer.getFullName());
            NotificationUtils.showToast(currentStage, "Khách hàng: " + selectedCustomer.getFullName());
        }
    }

    /**
     * Open discount selection modal
     */
    private void onMouseClickedShowDiscountContainer() {
        DiscountForSellingModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/DiscountForSellingModal.fxml",
                discountForSellingModalController -> discountForSellingModalController.setPrice(totalPriceInvoice),
                "Danh sách khuyến mãi");
        if (modalController != null && modalController.getSelectedDiscount() != null
                && modalController.isSelectSuccess()) {
            setSelectedDiscount(modalController.getSelectedDiscount());
            selectedDetailDiscountList = selectedDiscount.getDetailDiscountList();
            setDiscountPrice();
            updateInvoiceTotals();
            Stage currentStage = (Stage) btnGetDiscount.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Khuyến mãi: " + selectedDiscount.getCode());
        }
    }

    /**
     * Calculate discount price based on selected tier
     */
    private void setDiscountPrice() {
        int length = selectedDetailDiscountList.size();
        for (int i = length - 1; i >= 0; i--) {
            if (totalPriceInvoice.compareTo(selectedDetailDiscountList.get(i).getTotalPriceInvoice()) >= 0) {
                if (selectedDiscount.getType() == 0) {
                    discountPrice = totalPriceInvoice.multiply(
                            selectedDetailDiscountList.get(i).getDiscountAmount()
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                } else {
                    discountPrice = selectedDetailDiscountList.get(i).getDiscountAmount();
                }
                selectedDetailDiscount = new DetailDiscountDTO(selectedDetailDiscountList.get(i));
                txtCodeDiscount.setText("CODE: " + selectedDiscount.getCode() + " - " + selectedDiscount.getName()
                        + " - Giảm: " + selectedDetailDiscount.getDiscountAmount()
                        + (selectedDiscount.getType() == 0 ? "%" : " VNĐ"));
                return;
            }
        }
        discountPrice = BigDecimal.ZERO;

    }

    /**
     * Handle invoice submission
     */
    private void handleInvoice() {
        if (detailInvoiceList.isEmpty()) {
            NotificationUtils.showErrorAlert("Vui lòng thêm ít nhất một sản phẩm.", "Thông báo");
            return;
        }
        if (selectedCustomer == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn khách hàng để bán hàng.", "Thông báo");
            return;
        }
        if (selectedDiscount != null && selectedDetailDiscount != null) {
            if (totalPriceInvoice.compareTo(selectedDetailDiscount.getTotalPriceInvoice()) < 0) {
                NotificationUtils.showErrorAlert(
                        "Giá trị đơn hàng không còn đủ để áp dụng khuyến mãi này.", "Thông báo");
                selectedDiscount = null;
                selectedDetailDiscount = null;
                txtCodeDiscount.setText("");
                discountPrice = BigDecimal.ZERO;
                updateInvoiceTotals();
                return;
            }
        }

        ConfirmSellingModalController modalController = new ModalBuilder<ConfirmSellingModalController>(
                "/GUI/ConfirmSellingModal.fxml", ConfirmSellingModalController.class)
                .setTitle("Xác nhận bán hàng")
                .configure(e -> e.setConfirmData(selectedCustomer, new ArrayList<>(detailInvoiceList),
                        selectedDiscount != null ? selectedDiscount.getCode() : null, discountPrice))
                .open();

        if (modalController != null && modalController.isSaved()) {
            handleClearAllData();
            applyFilters();
            updateInvoiceId();
            InvoiceDTO selectedInvoice = modalController.getInvoiceDTO();
            TaskUtil.executePublic(loadingOverlay,
                    () -> PrintService.getInstance().printInvoiceForm(selectedInvoice.getId()),
                    result -> {
                        Stage currentStage = (Stage) btnSubmitInvoice.getScene().getWindow();
                        if (result.isSuccess()) {
                            NotificationUtils.showToast(currentStage, result.getMessage());
                        } else {
                            NotificationUtils.showErrorAlert(result.getMessage(), "Lỗi");
                        }
                    });
        }

    }

    /**
     * Make nodes read-only
     */
    private void makeReadOnly(Node node) {
        node.setDisable(false);
        node.setMouseTransparent(true);
        node.setFocusTraversable(false);
        if (node instanceof TextInputControl textInput) {
            textInput.setEditable(false);
        }
        if (node instanceof ComboBox<?> comboBox) {
            comboBox.setEditable(false);
            Platform.runLater(() -> {
                Node arrow = comboBox.lookup(".arrow-button");
                if (arrow != null) {
                    arrow.setStyle("-fx-background-color: #999999; -fx-opacity: 0.75;");
                }
            });
        }
        if (node instanceof Button button) {
            button.setDisable(true);
            button.setStyle("-fx-background-color: #999999; -fx-opacity: 0.75;");
        }
    }

    /**
     * Make nodes editable
     */
    private void makeEditable(Node node) {
        node.setDisable(false);
        node.setMouseTransparent(false);
        node.setFocusTraversable(true);
        if (node instanceof Button) {
            node.setStyle("");
        }
    }
}
