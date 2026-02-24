package GUI;

import java.util.ArrayList;

import BUS.CustomerBUS;
import DTO.CustomerForInvoiceDTO;
import ENUM.PermissionKey;
import INTERFACE.IModalController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.ModalBuilder;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
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

/**
 * Controller cho CusForSellingModal.fxml
 * Hiển thị danh sách khách hàng để chọn khi bán hàng
 * Hỗ trợ tìm kiếm theo tên hoặc SĐT (không phân trang)
 */
public class CusForSellingModalController implements IModalController {

    // ==================== UI COMPONENTS ====================
    // Search & Filter
    @FXML
    private TextField txtSearchCustomer;
    @FXML
    private Button btnRefreshFilter;
    @FXML
    private Button addBtn, btnSubmitCustomer;

    // Table & Columns
    @FXML
    private TableView<CustomerForInvoiceDTO> tblCustomer;
    @FXML
    private TableColumn<CustomerForInvoiceDTO, String> tlb_col_fullName;
    @FXML
    private TableColumn<CustomerForInvoiceDTO, String> tlb_col_phone;
    @FXML
    private TableColumn<CustomerForInvoiceDTO, String> tlb_col_address;
    @FXML
    private StackPane loadingOverlay;

    // Buttons
    @FXML
    private Button btnExitGetCustomer;

    // ==================== STATE ====================
    private CustomerBUS customerBUS;
    @Getter
    private CustomerForInvoiceDTO selectedCustomer = null;
    private SessionManagerService session;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        customerBUS = CustomerBUS.getInstance();
        session = SessionManagerService.getInstance();

        // Setup table columns
        setupTableColumns();

        // Setup listeners
        setupListeners();

        // Hide buttons without permission
        hideButtonWithoutPermission();

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
        // Combined firstName and lastName for fullName display
        tlb_col_fullName.setCellValueFactory(cellData -> {
            CustomerForInvoiceDTO customer = cellData.getValue();
            String fullName = (customer.getFirstName() != null ? customer.getFirstName() : "") + " " +
                    (customer.getLastName() != null ? customer.getLastName() : "");
            return new javafx.beans.property.SimpleStringProperty(fullName.trim());
        });

        tlb_col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tlb_col_address.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Add tooltips for long text
        UiUtils.gI().addTooltipToColumn(tlb_col_fullName, 25);
        UiUtils.gI().addTooltipToColumn(tlb_col_phone, 15);
        UiUtils.gI().addTooltipToColumn(tlb_col_address, 20);
    }

    /**
     * Setup button & search listeners
     */
    private void setupListeners() {
        // Search input - real-time filtering without pagination
        txtSearchCustomer.setOnAction(e -> handleSearch());

        // Refresh filter button - clear search and reset table
        btnRefreshFilter.setOnAction(e -> handleRefreshFilter());

        // Exit button
        btnExitGetCustomer.setOnAction(e -> handleClose());

        // Submit button - confirm selected customer
        btnSubmitCustomer.setOnAction(e -> handleSubmitCustomer());

        // Add button - open customer creation modal
        addBtn.setOnAction(e -> handleAddCustomer());
    }

    /**
     * Search customers by name or phone (no pagination)
     * Tìm kiếm khách hàng theo tên hoặc SĐT (không phân trang)
     */
    private void handleSearch() {
        String keyword = txtSearchCustomer.getText().trim();

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.IMPORT_INSERT,
                () -> CustomerBUS.getInstance().filterCustomersByKeywordForInvoice(keyword),
                result -> {
                    if (result.isSuccess()) {
                        ArrayList<CustomerForInvoiceDTO> res = result.getData();
                        tblCustomer.setItems(FXCollections.observableArrayList(res));
                        tblCustomer.getSelectionModel().clearSelection();
                    }

                });
    }

    /**
     * Refresh filter - clear search field and reset table
     * Làm mới bộ lọc - xóa ô tìm kiếm và đặt lại bảng
     */
    private void handleRefreshFilter() {
        txtSearchCustomer.clear();
        tblCustomer.setItems(FXCollections.observableArrayList());
        tblCustomer.getSelectionModel().clearSelection();
        Stage currentStage = (Stage) btnRefreshFilter.getScene().getWindow();
        NotificationUtils.showToast(currentStage, AppMessages.GENERAL_REFRESH_SUCCESS);
    }

    /**
     * Handle submit - get selected customer and close
     * Lấy khách hàng được chọn và đóng modal
     */
    private void handleSubmitCustomer() {
        selectedCustomer = tblCustomer.getSelectionModel().getSelectedItem();

        if (selectedCustomer == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn khách hàng", AppMessages.DIALOG_TITLE);
            return;
        }

        // Close modal with selected customer
        handleClose();
    }

    /**
     * Handle add new customer
     * Mở modal tạo khách hàng mới
     */
    private void handleAddCustomer() {
        CustomerModalController modalController = new ModalBuilder<CustomerModalController>(
                "/GUI/CustomerModal.fxml", CustomerModalController.class)
                .setTitle("Thêm khách hàng")
                .modeAdd()
                .open();

        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) addBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            // Refresh table after adding new customer
            handleRefreshFilter();
        }
    }

    /**
     * Close the modal
     * Đóng modal
     */
    private void handleClose() {
        if (btnExitGetCustomer.getScene() != null && btnExitGetCustomer.getScene().getWindow() != null) {
            Stage stage = (Stage) btnExitGetCustomer.getScene().getWindow();
            stage.close();
        }
    }

    public void hideButtonWithoutPermission() {
        boolean canAdd = session.hasPermission(PermissionKey.CUSTOMER_INSERT);
        if (!canAdd)
            UiUtils.gI().setVisibleItem(addBtn);
    }
}
