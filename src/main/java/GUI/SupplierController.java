package GUI;

import BUS.StatusBUS;
import BUS.SupplierBUS;
import DTO.BUSResult;
import DTO.SupplierDisplayDTO;
import DTO.SupplierDTO;
import DTO.StatusDTO;
import DTO.PagedResponse;
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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;

public class SupplierController implements IController {
    @FXML
    private TableView<SupplierDisplayDTO> tblSupplier;
    @FXML
    private TableColumn<SupplierDisplayDTO, Integer> tlb_col_id;
    @FXML
    private TableColumn<SupplierDisplayDTO, String> tlb_col_name;
    @FXML
    private TableColumn<SupplierDisplayDTO, String> tlb_col_phone;
    @FXML
    private TableColumn<SupplierDisplayDTO, String> tlb_col_email;
    @FXML
    private TableColumn<SupplierDisplayDTO, String> tlb_col_address;
    @FXML
    private TableColumn<SupplierDisplayDTO, String> tlb_col_status;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<StatusDTO> cbStatusFilter;
    @FXML
    private PaginationController paginationController;
    @FXML
    private AnchorPane mainContent;
    @FXML
    private StackPane loadingOverlay;

    private final int PAGE_SIZE = 14;
    private String keyword = "";
    private StatusDTO statusFilter = null;
    private SupplierDisplayDTO selectedSupplier;

    // BUS instances - initialized once
    private SupplierBUS supplierBUS;
    private StatusBUS statusBUS;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        supplierBUS = SupplierBUS.getInstance();
        statusBUS = StatusBUS.getInstance();

        tblSupplier.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblSupplier.getSelectionModel().clearSelection());

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
        ArrayList<StatusDTO> statusList = statusBUS.getAllByType(StatusType.SUPPLIER);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusList);
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tlb_col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        tlb_col_address.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Không cần gọi BUS - statusDescription đã có từ JOIN
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatusDescription() != null ? cellData.getValue().getStatusDescription() : ""));

        UiUtils.gI().addTooltipToColumn(tlb_col_name, 20);
        UiUtils.gI().addTooltipToColumn(tlb_col_email, 20);
        UiUtils.gI().addTooltipToColumn(tlb_col_address, 30);
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

        // Sử dụng method DISPLAY version - JOIN status, không cần gọi BUS lẻ
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.SUPPLIER_LIST_VIEW,
                () -> supplierBUS.filterSuppliersPagedForManageDisplay(keyword, statusId, pageIndex, PAGE_SIZE),
                result -> {
                    // Lấy dữ liệu SupplierDisplayDTO đã được JOIN
                    PagedResponse<SupplierDisplayDTO> res = result.getPagedData();

                    tblSupplier.setItems(FXCollections.observableArrayList(res.getItems()));

                    // Cập nhật tổng số trang dựa trên COUNT(*) từ DB
                    int totalPages = (int) Math.ceil((double) res.getTotalItems() / PAGE_SIZE);
                    paginationController.setPageCount(totalPages > 0 ? totalPages : 1);

                    tblSupplier.getSelectionModel().clearSelection();
                });
    }

    @Override
    public void setupListeners() {
        cbStatusFilter.setOnAction(event -> handleStatusFilterChange());
        UiUtils.gI().applySearchDebounce(txtSearch, 500, () -> handleKeywordChange());
        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert(AppMessages.GENERAL_REFRESH_SUCCESS, AppMessages.DIALOG_TITLE);
        });

        addBtn.setOnAction(e -> handleAdd());
        editBtn.setOnAction(e -> handleEdit());
        deleteBtn.setOnAction(e -> handleDelete());
        tblSupplier.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                handleDetail();
        });
    }

    // =====================
    // 3️⃣ CRUD HANDLERS (Add/Edit/Detail/Delete)
    // =====================
    private void handleAdd() {
        // TODO: Implement supplier add functionality
        NotificationUtils.showInfoAlert("Coming soon", AppMessages.DIALOG_TITLE);
    }

    private void handleEdit() {
        // TODO: Implement supplier edit functionality
        NotificationUtils.showInfoAlert("Coming soon", AppMessages.DIALOG_TITLE);
    }

    private void handleDetail() {
        // TODO: Implement supplier detail view
        NotificationUtils.showInfoAlert("Coming soon", AppMessages.DIALOG_TITLE);
    }

    private void handleDelete() {
        // TODO: Implement supplier delete functionality
        NotificationUtils.showInfoAlert("Coming soon", AppMessages.DIALOG_TITLE);
    }

    // =====================
    // 4️⃣ FILTER HANDLERS
    // =====================

    private void handleStatusFilterChange() {
        statusFilter = cbStatusFilter.getValue();
        applyFilters();
    }

    private void handleKeywordChange() {
        keyword = txtSearch.getText().trim();
        applyFilters();
    }

    @Override
    public void applyFilters() {
        if (paginationController.getCurrentPage() == 0) {
            loadPageData(0); // Nếu hiện tại đang ở trang 0 rồi thì phải gọi thủ công
        } else {
            paginationController.setCurrentPage(0);
        }
    }

    @Override
    public void resetFilters() {
        cbStatusFilter.getSelectionModel().selectFirst();
        txtSearch.clear();
        keyword = "";
        statusFilter = null;
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();
        boolean canView = session.hasPermission(PermissionKey.SUPPLIER_LIST_VIEW);

        // Block entire view if no permission
        if (!canView) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }

        boolean canAdd = session.hasPermission(PermissionKey.SUPPLIER_INSERT);
        boolean canEdit = session.hasPermission(PermissionKey.SUPPLIER_UPDATE);
        boolean canDelete = session.hasPermission(PermissionKey.SUPPLIER_DELETE);

        if (!canAdd)
            UiUtils.gI().setVisibleItem(addBtn);
        if (!canEdit)
            UiUtils.gI().setVisibleItem(editBtn);
        if (!canDelete)
            UiUtils.gI().setVisibleItem(deleteBtn);
    }

    // =====================
    // 5️⃣ UTILITY METHODS
    // =====================
    private boolean isNotSelectedSupplier() {
        selectedSupplier = tblSupplier.getSelectionModel().getSelectedItem();
        return selectedSupplier == null;
    }
}