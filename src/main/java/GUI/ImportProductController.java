package GUI;

import BUS.CategoryBUS;
import BUS.ProductBUS;
import DTO.CategoryDTO;
import DTO.PagedResponse;
import DTO.ProductDisplayForImportDTO;
import ENUM.Status;
import SERVICE.SessionManagerService;
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

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/**
 * Controller cho ImportProduct.fxml
 * Hiển thị danh sách sản phẩm có tồn kho > 0 với pagination
 * Hỗ trợ tìm kiếm theo tên + lọc theo thể loại
 */
public class ImportProductController {

    // ==================== FILTER & SEARCH CONTROLS ====================
    @FXML
    private TextField txtProductNameSearch;
    @FXML
    private ComboBox<CategoryDTO> cbCategoryFilter;
    @FXML
    private Button btnSearchProduct;
    @FXML
    private Button btnClearProduct;

    // ==================== DISPLAY CONTROLS ====================
    @FXML
    private GridPane gpShowProductWrapper;
    @FXML
    private StackPane loadingOverlay;

    // ==================== PAGINATION ====================
    @FXML
    private PaginationController paginationController;

    // ==================== BUTTONS ====================
    @FXML
    private Button btnExitImportingForm;

    // ==================== BUS INSTANCES ====================
    private ProductBUS productBUS;
    private CategoryBUS categoryBUS;
    private SessionManagerService session;
    private final int PAGE_SIZE = 12; // 3 cột x 3 hàng

    // ==================== FILTER STATE ====================
    private String keyword = "";
    private int selectedCategoryId = -1;
    private ValidationUtils validationUtils = ValidationUtils.getInstance();
    private boolean isResetting = false;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        productBUS = ProductBUS.getInstance();
        categoryBUS = CategoryBUS.getInstance();
        session = SessionManagerService.getInstance();

        // Setup UI
        loadCategoryFilter();
        loadTable();
        setupPagination();
        setupListeners();

        // Load initial data trang 0
        applyFilters();
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
     * Setup GridPane layout (3 cột)
     * Chỉ cấu hình cấu trúc, dữ liệu sẽ được đổ vào khi tải
     */
    private void loadTable() {
        gpShowProductWrapper.getChildren().clear();
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
    private void setupListeners() {
        cbCategoryFilter.setOnAction(event -> handleCategoryFilterChange());

        UiUtils.gI().applySearchDebounce(txtProductNameSearch, 500, () -> handleKeywordChange());

        btnSearchProduct.setOnAction(e -> applyFilters());

        btnClearProduct.setOnAction(e -> {
            resetFilters();
            Stage currentStage = (Stage) btnClearProduct.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Làm mới danh sách sản phẩm");
        });

        btnExitImportingForm.setOnAction(e -> {
            Stage stage = (Stage) btnExitImportingForm.getScene().getWindow();
            stage.close();
        });
    }

    /**
     * Load data cho trang hiện tại
     * Gom lại tham số filter, gọi BUS, cập nhật GridPane
     */
    private void loadPageData(int pageIndex, boolean showOverlay) {
        StackPane overlay = showOverlay ? loadingOverlay : null;

        // Chạy task ngầm
        TaskUtil.executePublic(overlay,
                () -> productBUS.filterProductsPagedForImport(keyword, selectedCategoryId, pageIndex, PAGE_SIZE),
                result -> {
                    PagedResponse<ProductDisplayForImportDTO> res = result.getPagedData();

                    // Cập nhật GridPane with product cards
                    displayProductCards(res.getItems());

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
        card.setStyle("-fx-background-color: white; -fx-border-color: #eee; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 5, 0, 0, 2);");
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
        Label nameLabel = new Label(product.getName());
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
     * Gửi thông tin sản phẩm vào form nhập hàng bên phải
     */
    private void handleProductCardClick(ProductDisplayForImportDTO product) {
        // TODO: Implement logic để thêm sản phẩm vào danh sách nhập
        System.out.println("Selected product: " + product.getName());
        Stage currentStage = (Stage) btnClearProduct.getScene().getWindow();
        NotificationUtils.showToast(currentStage, "Chọn: " + product.getName());
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
    private void resetFilters() {
        isResetting = true; // Khóa handler

        txtProductNameSearch.clear();
        cbCategoryFilter.getSelectionModel().selectFirst();

        keyword = "";
        selectedCategoryId = -1;

        // Load dữ liệu trang 0 ngay lập tức
        loadPageData(0, true);
        paginationController.setCurrentPage(0);

        // Chỉ mở khóa sau khi các sự kiện UI đã ổn định
        javafx.application.Platform.runLater(() -> {
            isResetting = false;
        });
    }
}
