package GUI;

import java.util.ArrayList;

import BUS.CustomerBUS;
import BUS.StatusBUS;
import DTO.BUSResult;
import DTO.CustomerDisplayDTO;
import DTO.PagedResponse;
import DTO.StatusDTO;
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class CustomerController implements IController {
    @FXML
    private TableView<CustomerDisplayDTO> tblCustomer;
    @FXML
    private TableColumn<CustomerDisplayDTO, Integer> tlb_col_customerId;
    @FXML
    private TableColumn<CustomerDisplayDTO, String> tlb_col_fullName;
    @FXML
    private TableColumn<CustomerDisplayDTO, String> tlb_col_phone;
    @FXML
    private TableColumn<CustomerDisplayDTO, String> tlb_col_address;
    @FXML
    private TableColumn<CustomerDisplayDTO, String> tlb_col_dob;
    @FXML
    private TableColumn<CustomerDisplayDTO, String> tlb_col_status;
    @FXML
    private TableColumn<CustomerDisplayDTO, String> tlb_col_updatedAt;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn, exportExcel;
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
    private CustomerDisplayDTO selectedCustomer;

    // BUS instances - initialized once
    private CustomerBUS customerBUS;
    private StatusBUS statusBUS;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        customerBUS = CustomerBUS.getInstance();
        statusBUS = StatusBUS.getInstance();

        tblCustomer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblCustomer.getSelectionModel().clearSelection());

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
        ArrayList<StatusDTO> statusList = statusBUS.getAllByType(StatusType.CUSTOMER);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusList);
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_customerId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        tlb_col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tlb_col_address.setCellValueFactory(new PropertyValueFactory<>("address"));

        // Không cần gọi BUS - dateOfBirth đã có từ DTO
        tlb_col_dob.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatDateTime(cellData.getValue().getDateOfBirth())));

        // Không cần gọi BUS - statusDescription đã có từ JOIN
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStatusDescription() != null ? cellData.getValue().getStatusDescription() : ""));

        tlb_col_updatedAt.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatDateTimeWithHour(cellData.getValue().getUpdatedAt())));

        UiUtils.gI().addTooltipToColumn(tlb_col_fullName, 20);
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
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.CUSTOMER_LIST_VIEW,
                () -> customerBUS.filterCustomersPagedForManageDisplay(keyword, statusId, pageIndex, PAGE_SIZE),
                result -> {
                    // Lấy dữ liệu CustomerDisplayDTO đã được JOIN
                    PagedResponse<CustomerDisplayDTO> res = result.getPagedData();

                    tblCustomer.setItems(FXCollections.observableArrayList(res.getItems()));

                    // Cập nhật tổng số trang dựa trên COUNT(*) từ DB
                    int totalPages = (int) Math.ceil((double) res.getTotalItems() / PAGE_SIZE);
                    paginationController.setPageCount(totalPages > 0 ? totalPages : 1);

                    tblCustomer.getSelectionModel().clearSelection();
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
        tblCustomer.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                handleDetail();
        });
        exportExcel.setOnAction(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            handleExportExcel(stage);
        });
    }

    // =====================
    // 3️⃣ CRUD HANDLERS (Add/Edit/Detail/Delete)
    // =====================
    private void handleAdd() {
        CustomerModalController modalController = new ModalBuilder<CustomerModalController>(
                "/GUI/CustomerModal.fxml", CustomerModalController.class)
                .setTitle("Thêm khách hàng")
                .modeAdd()
                .open();
        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) addBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            resetFilters();
        }
    }

    private void handleEdit() {
        if (isNotSelectedCustomer()) {
            NotificationUtils.showErrorAlert(AppMessages.CUSTOMER_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        CustomerModalController modalController = new ModalBuilder<CustomerModalController>(
                "/GUI/CustomerModal.fxml", CustomerModalController.class)
                .setTitle("Sửa khách hàng")
                .modeEdit()
                .configure(c -> c.setCustomer(selectedCustomer.getId()))
                .open();
        // if (modalController != null && modalController.isSaved()) {
        // Stage currentStage = (Stage) editBtn.getScene().getWindow();
        // NotificationUtils.showToast(currentStage,
        // modalController.getResultMessage());
        // resetFilters();
        // }
    }

    private void handleDetail() {
        if (isNotSelectedCustomer()) {
            NotificationUtils.showErrorAlert(AppMessages.CUSTOMER_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        new ModalBuilder<CustomerModalController>("/GUI/CustomerModal.fxml", CustomerModalController.class)
                .setTitle("Xem chi tiết khách hàng")
                .modeDetail()
                .configure(c -> c.setCustomer(selectedCustomer.getId()))
                .open();
    }

    private void handleDelete() {
        if (isNotSelectedCustomer()) {
            NotificationUtils.showErrorAlert(AppMessages.CUSTOMER_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert(AppMessages.CUSTOMER_DELETE_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        BUSResult updateResult = SecureExecutor.executeSafeBusResult(PermissionKey.CUSTOMER_DELETE,
                () -> customerBUS.delete(selectedCustomer.getId()));

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
    private void handleKeywordChange() {
        keyword = txtSearch.getText().trim();
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
        txtSearch.clear();
        keyword = "";
        statusFilter = null;
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();
        boolean canView = session.hasPermission(PermissionKey.CUSTOMER_LIST_VIEW);

        // Block entire view if no permission
        if (!canView) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }

        boolean canAdd = session.hasPermission(PermissionKey.CUSTOMER_INSERT);
        boolean canEdit = session.hasPermission(PermissionKey.CUSTOMER_UPDATE);
        boolean canDelete = session.hasPermission(PermissionKey.CUSTOMER_DELETE);

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
    private boolean isNotSelectedCustomer() {
        selectedCustomer = tblCustomer.getSelectionModel().getSelectedItem();
        return selectedCustomer == null;
    }

    private void handleExportExcel(Stage stage) {
        // try {
        // ExcelService.getInstance().ExportSheet("customers", stage);
        // } catch (IOException e) {
        // NotificationUtils.showErrorAlert("Xuất Excel thất bại",
        // AppMessages.DIALOG_TITLE);
        // e.printStackTrace();
        // }
    }

}
