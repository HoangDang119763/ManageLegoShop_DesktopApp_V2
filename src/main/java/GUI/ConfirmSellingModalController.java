package GUI;

import BUS.InvoiceBUS;
import DTO.CustomerForInvoiceDTO;
import DTO.DetailInvoiceDTO;
import DTO.InvoiceDTO;
import DTO.TempDetailInvoiceDTO;
import ENUM.PermissionKey;
import INTERFACE.IModalController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller cho ConfirmSellingModal.fxml
 * Modal xác nhận phiếu bán hàng trước khi lưu vào database
 * Hiển thị thông tin khách hàng, nhân viên, ngày tạo, khuyến mãi và danh sách
 * sản phẩm bán
 */
public class ConfirmSellingModalController implements IModalController {

    // ==================== UI COMPONENTS ====================
    // Info Labels
    @FXML
    private Label lblCustomerName;
    @FXML
    private Label lblEmployeeName;
    @FXML
    private Label lblCreateDate;
    @FXML
    private Label lblDiscountCode;

    // Price Labels
    @FXML
    private Label lblSubtotal;
    @FXML
    private Label lblDiscountAmount;
    @FXML
    private Label lblFinalTotal;

    // Table & Columns
    @FXML
    private TableView<TempDetailInvoiceDTO> tvConfirmDetail;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tbl_col_productId;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tbl_col_productName;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, Integer> tbl_col_quantity;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tbl_col_sellingPrice;
    @FXML
    private TableColumn<TempDetailInvoiceDTO, String> tbl_col_totalPrice;

    // Buttons
    @FXML
    private Button btnBack;
    @FXML
    private Button btnFinalSubmit;

    // Loading overlay
    @FXML
    private StackPane loadingOverlay;

    // ==================== STATE ====================
    private InvoiceBUS invoiceBUS;
    private SessionManagerService session;
    private ValidationUtils validationUtils;

    @Getter
    private boolean isSaved = false;

    // Data to be created
    @Getter
    private InvoiceDTO invoiceDTO;
    @Getter
    private List<DetailInvoiceDTO> detailInvoiceList;
    @Getter
    private String resultMessage = "";

    // Temp data from selling controller
    private CustomerForInvoiceDTO selectedCustomer;
    private String discountCode;
    private BigDecimal discountAmount;
    private ArrayList<TempDetailInvoiceDTO> tempDetailInvoiceList;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        // Initialize BUS instances
        invoiceBUS = InvoiceBUS.getInstance();
        session = SessionManagerService.getInstance();
        validationUtils = ValidationUtils.getInstance();

        // Setup table columns
        setupTableColumns();

        // Setup listeners
        setupListeners();
    }

    /**
     * Setup table column value factories
     */
    private void setupTableColumns() {
        // Product ID column
        tbl_col_productId.setCellValueFactory(new PropertyValueFactory<>("productId"));

        // Product name column
        tbl_col_productName.setCellValueFactory(new PropertyValueFactory<>("name"));
        UiUtils.gI().addTooltipToColumn(tbl_col_productName, 20);

        // Quantity column
        tbl_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Selling price column - formatted as currency
        tbl_col_sellingPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getSellingPrice())));

        // Total price column - formatted as currency
        tbl_col_totalPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        btnBack.setOnAction(e -> handleBack());
        btnFinalSubmit.setOnAction(e -> handleFinalSubmit());
    }

    /**
     * Set data for confirmation modal
     * Dữ liệu từ SellingProductController
     *
     * @param customer       selected customer
     * @param tempDetailList temporary detail invoice list
     * @param discountCode   discount code (nullable)
     * @param discountAmount discount amount (nullable)
     */
    public void setConfirmData(CustomerForInvoiceDTO customer, ArrayList<TempDetailInvoiceDTO> tempDetailList,
            String discountCode, BigDecimal discountAmount) {
        this.selectedCustomer = customer;
        this.tempDetailInvoiceList = tempDetailList;
        this.discountCode = discountCode;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;

        // Display customer information
        if (selectedCustomer != null) {
            lblCustomerName.setText("ID: " + selectedCustomer.getId() + " - " + selectedCustomer.getFullName());
        }

        lblEmployeeName.setText(session.getLoggedName());

        // Display current date and time
        lblCreateDate.setText(ValidationUtils.getInstance().formatDateTime(LocalDateTime.now()));

        // Display discount code if any
        if (discountCode != null && !discountCode.isEmpty()) {
            lblDiscountCode.setText("CODE: " + discountCode);
        } else {
            lblDiscountCode.setText("Không có khuyến mãi");
        }

        // Display detail invoice list
        displayDetailInvoiceList();

        // Calculate and display total price
        updateTotalPrices();
    }

    /**
     * Display detail invoice list in table
     * Hiển thị danh sách chi tiết phiếu bán trong bảng
     */
    private void displayDetailInvoiceList() {
        if (tempDetailInvoiceList == null || tempDetailInvoiceList.isEmpty()) {
            tvConfirmDetail.setItems(FXCollections.observableArrayList());
            return;
        }

        tvConfirmDetail.setItems(FXCollections.observableArrayList(tempDetailInvoiceList));
    }

    /**
     * Calculate and update total prices (subtotal, discount, final)
     * Tính toán và cập nhật tổng tiền (cộng, giảm, cuối)
     */
    private void updateTotalPrices() {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (tempDetailInvoiceList != null) {
            for (TempDetailInvoiceDTO temp : tempDetailInvoiceList) {
                if (temp.getTotalPrice() != null) {
                    subtotal = subtotal.add(temp.getTotalPrice());
                }
            }
        }

        BigDecimal finalTotal = subtotal.subtract(discountAmount);

        lblSubtotal.setText(validationUtils.formatCurrency(subtotal) + " VNĐ");
        lblDiscountAmount.setText(validationUtils.formatCurrency(discountAmount) + " VNĐ");
        lblFinalTotal.setText(validationUtils.formatCurrency(finalTotal) + " VNĐ");
    }

    /**
     * Handle back button - close modal without saving
     * Quay lại màn hình bán hàng
     */
    private void handleBack() {
        if (btnBack.getScene() != null && btnBack.getScene().getWindow() != null) {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Handle final submit - create invoice and detail invoice records
     * Xác nhận bán hàng - tạo phiếu bán vào database
     */
    private void handleFinalSubmit() {
        // Validation
        if (selectedCustomer == null || tempDetailInvoiceList == null
                || tempDetailInvoiceList.isEmpty()) {
            NotificationUtils.showErrorAlert("Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.", AppMessages.DIALOG_TITLE);
            return;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (TempDetailInvoiceDTO temp : tempDetailInvoiceList) {
            if (temp.getTotalPrice() != null) {
                totalPrice = totalPrice.add(temp.getTotalPrice());
            }
        }

        // Create InvoiceDTO
        invoiceDTO = new InvoiceDTO();
        invoiceDTO.setEmployeeId(session.employeeLoginId());
        invoiceDTO.setCustomerId(selectedCustomer.getId());
        invoiceDTO.setTotalPrice(totalPrice);
        invoiceDTO.setDiscountAmount(discountAmount);
        invoiceDTO.setCreatedAt(LocalDateTime.now());

        if (discountCode != null && !discountCode.isEmpty()) {
            invoiceDTO.setDiscountCode(discountCode);
        }

        // Convert TempDetailInvoiceDTO to DetailInvoiceDTO
        detailInvoiceList = new ArrayList<>();
        for (TempDetailInvoiceDTO temp : tempDetailInvoiceList) {
            DetailInvoiceDTO detail = new DetailInvoiceDTO();
            detail.setProductId(temp.getProductId());
            detail.setQuantity(temp.getQuantity());
            detail.setPrice(temp.getSellingPrice());
            // Cost price is set in InvoiceBUS during insert (snapshot at sale time), so we
            // don't set it here
            detail.setTotalPrice(temp.getTotalPrice());
            detailInvoiceList.add(detail);
        }

        // Insert using transaction in InvoiceBUS with TaskUtil
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.INVOICE_INSERT,
                () -> invoiceBUS.insertFullInvoice(invoiceDTO, detailInvoiceList),
                result -> {
                    isSaved = true;
                    resultMessage = result.getMessage();
//                    NotificationUtils.showInfoAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    handleBack();
                });
    }

    @Override
    public void setTypeModal(int mode) {
        // Not used for this controller
        throw new UnsupportedOperationException("Unimplemented method 'setTypeModal'");
    }
}
