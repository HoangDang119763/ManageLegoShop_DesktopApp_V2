package GUI;

import java.math.BigDecimal;
import java.util.ArrayList;

import BUS.DetailDiscountBUS;
import BUS.DiscountBUS;
import DTO.DetailDiscountDTO;
import DTO.DiscountDTO;
import DTO.DiscountForInvoiceDTO;
import DTO.SupplierForImportDTO;
import ENUM.DiscountType;
import ENUM.PermissionKey;
import INTERFACE.IModalController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

/**
 * Controller cho DiscountForSellingModal.fxml
 * Hiển thị danh sách mã khuyến mãi để chọn khi bán hàng
 * Hỗ trợ tìm kiếm theo mã hoặc tên (không phân trang)
 */
public class DiscountForSellingModalController implements IModalController {

    // ==================== UI COMPONENTS ====================
    // Search & Filter
    @FXML
    private TextField txtSearchDiscount;
    @FXML
    private Button btnRefreshFilter;
    @FXML
    private Button btnSearchDiscount, btnSubmitDiscount;

    // Table & Columns
    @FXML
    private TableView<DiscountForInvoiceDTO> tbvDiscount;
    @FXML
    private TableColumn<DiscountForInvoiceDTO, String> tbcCode;
    @FXML
    private TableColumn<DiscountForInvoiceDTO, String> tbcDiscountName;
    @FXML
    private TableColumn<DiscountForInvoiceDTO, String> tbcDiscountType;
    @FXML
    private StackPane loadingOverlay;

    // Buttons
    @FXML
    private Button btnExitGetDiscount;

    // ==================== STATE ====================
    private DiscountBUS discountBUS;
    @Getter
    private DiscountForInvoiceDTO selectedDiscount = null;
    @Getter
    private boolean isSelectSuccess = false;
    @Setter
    private BigDecimal price;
    @Getter
    private SessionManagerService session;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        discountBUS = DiscountBUS.getInstance();
        session = SessionManagerService.getInstance();

        // Setup table columns
        setupTableColumns();

        // Setup listeners
        setupListeners();

        // DO NOT load initial data - use lazy loading (only load when user searches)
        // Ngăn tải dữ liệu ngay khi mở modal - chỉ tải khi người dùng tìm kiếm
    }

    @Override
    public void setTypeModal(int mode) {
        // For selling modal, mode is not used (always search mode)
        // Dành cho thống nhất interface, mode không được sử dụng ở đây
    }

    /**
     * Setup table column value factories và tooltips
     */
    private void setupTableColumns() {
        tbcCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        tbcDiscountName.setCellValueFactory(new PropertyValueFactory<>("name"));
        tbcDiscountType.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        DiscountType.fromCode(cellData.getValue().getType()).getDisplayName()));

        // Add tooltips for long text
        UiUtils.gI().addTooltipToColumn(tbcDiscountName, 25);
    }

    /**
     * Setup button & search listeners
     */
    private void setupListeners() {
        // Search input - real-time filtering without pagination
        txtSearchDiscount.setOnAction(e -> handleSearch());

        // Search/Filter button
        btnSearchDiscount.setOnAction(e -> handleSearch());

        // Refresh filter button - clear search and reset table
        btnRefreshFilter.setOnAction(e -> handleRefreshFilter());

        // Exit button
        btnExitGetDiscount.setOnAction(e -> handleClose());

        // Submit button - confirm selected discount
        btnSubmitDiscount.setOnAction(e -> handleSubmitDiscount());
    }

    /**
     * Search discounts by code or name (no pagination)
     * Tìm kiếm khuyến mãi theo mã hoặc tên (không phân trang)
     */
    private void handleSearch() {
        String keyword = txtSearchDiscount.getText().trim();

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.INVOICE_INSERT,
                () -> discountBUS.filterDiscountsByKeywordForInvoice(keyword),
                result -> {
                    if (result.isSuccess()) {
                        ArrayList<DiscountForInvoiceDTO> res = result.getData();
                        tbvDiscount.setItems(FXCollections.observableArrayList(res));
                        tbvDiscount.getSelectionModel().clearSelection();
                    }

                });
    }

    /**
     * Refresh filter - clear search field and reset table
     * Làm mới bộ lọc - xóa ô tìm kiếm và đặt lại bảng
     */
    private void handleRefreshFilter() {
        txtSearchDiscount.clear();
        tbvDiscount.setItems(FXCollections.observableArrayList());
        tbvDiscount.getSelectionModel().clearSelection();
        Stage currentStage = (Stage) btnRefreshFilter.getScene().getWindow();
        NotificationUtils.showToast(currentStage, AppMessages.GENERAL_REFRESH_SUCCESS);
    }

    /**
     * Handle submit - get selected discount and validate
     * Lấy khuyến mãi được chọn và kiểm tra điều kiện
     */
    private void handleSubmitDiscount() {
        selectedDiscount = tbvDiscount.getSelectionModel().getSelectedItem();

        if (selectedDiscount == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn mã khuyến mãi", AppMessages.DIALOG_TITLE);
            return;
        }

        if (!isValid(selectedDiscount.getDetailDiscountList())) {
            NotificationUtils.showErrorAlert("Vui lòng mua thêm tối thiểu " +
                    ValidationUtils.getInstance().formatCurrency(
                            selectedDiscount.getDetailDiscountList().getFirst().getTotalPriceInvoice().subtract(price))
                    +
                    " Đ để dùng voucher này!", "Thông báo");
            return;
        }
        isSelectSuccess = true;

        // Close modal with selected discount
        handleClose();
    }

    /**
     * Close the modal
     * Đóng modal
     */
    private void handleClose() {
        if (btnExitGetDiscount.getScene() != null && btnExitGetDiscount.getScene().getWindow() != null) {
            Stage stage = (Stage) btnExitGetDiscount.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Validate if current price meets minimum discount requirement
     * Kiểm tra xem giá hiện tại có đạt mức tối thiểu của khuyến mãi hay không
     */
    private boolean isValid(ArrayList<DetailDiscountDTO> details) {
        if (details == null || details.isEmpty()) {
            return true; // No details = no minimum requirement
        }

        for (DetailDiscountDTO detail : details) {
            // Check if price >= minimum price threshold for this detail
            if (detail.getTotalPriceInvoice().compareTo(price) <= 0) {
                return true; // Price meets at least one threshold
            }
        }
        return false; // Price doesn't meet any threshold
    }
}
