package GUI;

import BUS.DetailImportBUS;
import BUS.EmployeeBUS;
import BUS.ImportBUS;
import BUS.SupplierBUS;
import DTO.BUSResult;
import DTO.DetailImportDTO;
import DTO.ImportDTO;
import DTO.SupplierForImportDTO;
import DTO.TempDetailImportDTO;
import ENUM.BUSOperationResult;
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

/**
 * Controller cho ConfirmImportModal.fxml
 * Modal xác nhận phiếu nhập hàng trước khi lưu vào database
 * Hiển thị thông tin nhà cung cấp, nhân viên, ngày tạo và danh sách sản phẩm
 * nhập
 */
public class ConfirmImportModalController implements IModalController {

    // ==================== UI COMPONENTS ====================
    // Info Labels
    @FXML
    private Label lblSupplierName;
    @FXML
    private Label lblEmployeeName;
    @FXML
    private Label lblCreateDate;

    // Total Price Label
    @FXML
    private Label lblFinalTotal;

    // Table & Columns
    @FXML
    private TableView<TempDetailImportDTO> tvConfirmDetail;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tbl_col_productId;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tbl_col_productName;
    @FXML
    private TableColumn<TempDetailImportDTO, Integer> tbl_col_quantity;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tbl_col_profitPercent;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tbl_col_importPrice;
    @FXML
    private TableColumn<TempDetailImportDTO, String> tbl_col_totalPrice;

    // Buttons
    @FXML
    private Button btnBack;
    @FXML
    private Button btnFinalSubmit;

    // Loading overlay
    @FXML
    private StackPane loadingOverlay;

    // ==================== STATE ====================
    private ImportBUS importBUS;
    private DetailImportBUS detailImportBUS;
    private SupplierBUS supplierBUS;
    private EmployeeBUS employeeBUS;
    private SessionManagerService session;
    private ValidationUtils validationUtils;

    @Getter
    private boolean isSaved = false;

    // Data to be created
    @Getter
    private ImportDTO importDTO;
    @Getter
    private ArrayList<DetailImportDTO> detailImportList;
    @Getter
    private String resultMessage = "";
    // Temp data from import controller
    private SupplierForImportDTO selectedSupplier;
    private ArrayList<TempDetailImportDTO> tempDetailImportList;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        // Initialize BUS instances
        importBUS = ImportBUS.getInstance();
        detailImportBUS = DetailImportBUS.getInstance();
        supplierBUS = SupplierBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
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
        // STT column - show index
        tbl_col_productId.setCellValueFactory(new PropertyValueFactory<>("productId"));

        // Product name column
        tbl_col_productName.setCellValueFactory(new PropertyValueFactory<>("name"));
        UiUtils.gI().addTooltipToColumn(tbl_col_productName, 20);

        // Quantity column
        tbl_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Profit percent column - formatted as percentage
        tbl_col_profitPercent.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                validationUtils.formatPercent(cellData.getValue().getProfitPercent())));

        // Import price column - formatted as currency
        tbl_col_importPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getImportPrice())));

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
     * Dữ liệu từ ImportProductController
     *
     * @param supplier       selected supplier
     * @param tempDetailList temporary detail import list
     */
    public void setConfirmData(SupplierForImportDTO supplier, ArrayList<TempDetailImportDTO> tempDetailList) {
        this.selectedSupplier = supplier;
        this.tempDetailImportList = tempDetailList;

        // Display supplier information
        if (selectedSupplier != null) {
            lblSupplierName.setText(selectedSupplier.getName());
        }

        lblEmployeeName.setText(session.getLoggedName());

        // Display current date and time
        lblCreateDate.setText(ValidationUtils.getInstance().formatDateTime(LocalDateTime.now()));

        // Display detail import list
        displayDetailImportList();

        // Calculate and display total price
        updateTotalPrice();
    }

    /**
     * Display detail import list in table
     * Hiển thị danh sách chi tiết phiếu nhập trong bảng
     */
    private void displayDetailImportList() {
        if (tempDetailImportList == null || tempDetailImportList.isEmpty()) {
            tvConfirmDetail.setItems(FXCollections.observableArrayList());
            return;
        }

        tvConfirmDetail.setItems(FXCollections.observableArrayList(tempDetailImportList));
    }

    /**
     * Calculate and update total price
     * Tính toán và cập nhật tổng tiền
     */
    private void updateTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;

        if (tempDetailImportList != null) {
            for (TempDetailImportDTO temp : tempDetailImportList) {
                if (temp.getTotalPrice() != null) {
                    total = total.add(temp.getTotalPrice());
                }
            }
        }

        lblFinalTotal.setText(validationUtils.formatCurrency(total) + " VNĐ");
    }

    /**
     * Handle back button - close modal without saving
     * Quay lại màn hình chỉnh sửa
     */
    private void handleBack() {
        if (btnBack.getScene() != null && btnBack.getScene().getWindow() != null) {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Handle final submit - create import and detail import records
     * Xác nhận nhập hàng - tạo phiếu nhập vào database
     */
    private void handleFinalSubmit() {
        // Validation
        if (selectedSupplier == null || tempDetailImportList == null
                || tempDetailImportList.isEmpty()) {
            NotificationUtils.showErrorAlert("Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.", AppMessages.DIALOG_TITLE);
            return;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (TempDetailImportDTO temp : tempDetailImportList) {
            if (temp.getTotalPrice() != null) {
                totalPrice = totalPrice.add(temp.getTotalPrice());
            }
        }

        // 2. Create ImportDTO
        importDTO = new ImportDTO();
        importDTO.setEmployeeId(session.employeeLoginId());
        importDTO.setSupplierId(selectedSupplier.getId());
        importDTO.setTotalPrice(totalPrice);

        // 3. Convert TempDetailImportDTO to DetailImportDTO
        detailImportList = new ArrayList<>();
        for (TempDetailImportDTO temp : tempDetailImportList) {
            DetailImportDTO detail = new DetailImportDTO();
            detail.setProductId(temp.getProductId());
            detail.setQuantity(temp.getQuantity());
            detail.setPrice(temp.getImportPrice());
            detail.setTotalPrice(temp.getTotalPrice());
            detail.setProfitPercent(temp.getProfitPercent());
            detailImportList.add(detail);
        }

        // 4. Insert using transaction in ImportBUS with TaskUtil
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.IMPORT_INSERT,
                () -> importBUS.insertFullImport(importDTO, detailImportList),
                result -> {
                    isSaved = true;
                    resultMessage = result.getMessage();
                    NotificationUtils.showInfoAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    handleBack();
                });
    }

    @Override
    public void setTypeModal(int mode) {
        // Not used for this controller
        throw new UnsupportedOperationException("Unimplemented method 'setTypeModal'");
    }
}
