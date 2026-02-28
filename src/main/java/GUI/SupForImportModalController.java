package GUI;

import java.util.ArrayList;

import BUS.SupplierBUS;
import DTO.SupplierForImportDTO;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

/**
 * Controller cho SupForImportModal.fxml
 * Hiển thị danh sách nhà cung cấp để chọn khi nhập hàng
 * Hỗ trợ tìm kiếm theo tên hoặc SĐT (không phân trang)
 */
public class SupForImportModalController implements IModalController {

    // ==================== UI COMPONENTS ====================
    @FXML
    private AnchorPane acSupplierContainer;

    // Search & Filter
    @FXML
    private TextField txtSearchSupplier;
    @FXML
    private Button btnRefreshFilter;
    @FXML
    private Button addBtn, btnSubmitImport;

    // Table & Columns
    @FXML
    private TableView<SupplierForImportDTO> tblSupplier;
    @FXML
    private TableColumn<SupplierForImportDTO, String> tlb_col_name;
    @FXML
    private TableColumn<SupplierForImportDTO, String> tlb_col_phone;
    @FXML
    private TableColumn<SupplierForImportDTO, String> tlb_col_address;
    @FXML
    private StackPane loadingOverlay;
    // Buttons
    @FXML
    private Button btnExitGetSupplier;
    @FXML
    private Button btnSubmitSupplier;

    // ==================== STATE ====================
    private SupplierBUS supplierBUS;
    @Getter
    private SupplierForImportDTO selectedSupplier = null;
    private SessionManagerService session;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        supplierBUS = SupplierBUS.getInstance();
        session = SessionManagerService.getInstance();
        // Setup table columns
        setupTableColumns();

        // Setup listeners
        setupListeners();

        // DO NOT load initial data - use lazy loading (only load when user searches)
        // Ngăn tải dữ liệu ngay khi mở modal - chỉ tải khi người dùng tìm kiếm
    }

    public void hideButtonWithoutPermission() {
        boolean canAdd = session.hasPermission(PermissionKey.SUPPLIER_INSERT);
        if (!canAdd)
            UiUtils.gI().setVisibleItem(addBtn);
    }

    /**
     * Setup table column value factories và tooltips
     */
    private void setupTableColumns() {
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tlb_col_address.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Add tooltips for long text
        UiUtils.gI().addTooltipToColumn(tlb_col_name, 25);
        UiUtils.gI().addTooltipToColumn(tlb_col_phone, 15);
        UiUtils.gI().addTooltipToColumn(tlb_col_address, 20);
    }

    /**
     * Setup button & search listeners
     */
    private void setupListeners() {
        // Search input - real-time filtering without pagination
        txtSearchSupplier.setOnAction(e -> handleSearch());

        // Refresh filter button - clear search and reset table
        btnRefreshFilter.setOnAction(e -> handleRefreshFilter());

        // Exit button
        btnExitGetSupplier.setOnAction(e -> handleClose());

        // Submit button - confirm selected supplier
        btnSubmitSupplier.setOnAction(e -> handleSubmitSupplier());

        // Add button - open supplier creation modal
        addBtn.setOnAction(e -> handleAddSupplier());
    }

    /**
     * Search suppliers by name or phone (no pagination)
     * Tìm kiếm nhà cung cấp theo tên hoặc SĐT (không phân trang)
     */
    private void handleSearch() {
        String keyword = txtSearchSupplier.getText().trim();

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.IMPORT_INSERT,
                () -> supplierBUS.filterSuppliersByKeywordForImport(keyword),
                result -> {
                    if (result.isSuccess()) {
                        ArrayList<SupplierForImportDTO> res = result.getData();
                        tblSupplier.setItems(FXCollections.observableArrayList(res));
                        tblSupplier.getSelectionModel().clearSelection();
                    }

                });

    }

    /**
     * Refresh filter - clear search field and reset table
     * Làm mới bộ lọc - xóa ô tìm kiếm và đặt lại bảng
     */
    private void handleRefreshFilter() {
        txtSearchSupplier.clear();
        tblSupplier.setItems(FXCollections.observableArrayList());
        tblSupplier.getSelectionModel().clearSelection();
        Stage currentStage = (Stage) btnRefreshFilter.getScene().getWindow();
        NotificationUtils.showToast(currentStage, AppMessages.GENERAL_REFRESH_SUCCESS);
    }

    /**
     * Handle submit - get selected supplier and close
     * Lấy nhà cung cấp được chọn và đóng modal
     */
    private void handleSubmitSupplier() {
        selectedSupplier = tblSupplier.getSelectionModel().getSelectedItem();

        if (selectedSupplier == null) {
            NotificationUtils.showErrorAlert(AppMessages.SUPPLIER_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        // Close modal with selected supplier
        handleClose();
    }

    /**
     * Handle add new supplier
     * Mở modal tạo nhà cung cấp mới
     */
    private void handleAddSupplier() {
        SupplierModalController modalController = new ModalBuilder<SupplierModalController>(
                "/GUI/SupplierModal.fxml", SupplierModalController.class)
                .setTitle("Thêm nhà cung cấp")
                .modeAdd()
                .open();

        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) addBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            handleRefreshFilter();
        }
    }

    /**
     * Close the modal
     * Đóng modal
     */
    private void handleClose() {
        if (btnExitGetSupplier.getScene() != null && btnExitGetSupplier.getScene().getWindow() != null) {
            Stage stage = (Stage) btnExitGetSupplier.getScene().getWindow();
            stage.close();
        }
    }

    @Override
    public void setTypeModal(int mode) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setTypeModal'");
    }
}
