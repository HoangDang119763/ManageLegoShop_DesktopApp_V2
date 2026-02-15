package GUI;

import BUS.CustomerBUS;
import BUS.StatusBUS;
import DTO.BUSResult;
import DTO.CustomerDTO;
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
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class CustomerController implements IController {
    @FXML
    private TableView<CustomerDTO> tblCustomer;
    @FXML
    private TableColumn<CustomerDTO, Integer> tlb_col_customerId;
    @FXML
    private TableColumn<CustomerDTO, String> tlb_col_fullName;
    @FXML
    private TableColumn<CustomerDTO, String> tlb_col_phone;
    @FXML
    private TableColumn<CustomerDTO, String> tlb_col_address;
    @FXML
    private TableColumn<CustomerDTO, String> tlb_col_dob;
    @FXML
    private TableColumn<CustomerDTO, String> tlb_col_status;
    @FXML
    private TableColumn<CustomerDTO, String> tlb_col_updatedAt;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn, exportExcel;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbSearchBy;
    @FXML
    private ComboBox<StatusDTO> cbStatusFilter;
    @FXML
    private AnchorPane mainContent;

    private String searchBy = "Mã khách hàng";
    private String keyword = "";
    private StatusDTO statusFilter = null;
    private CustomerDTO selectedCustomer;
    private CustomerBUS customerBUS;
    private SessionManagerService session;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        customerBUS = CustomerBUS.getInstance();
        session = SessionManagerService.getInstance();
        tblCustomer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblCustomer.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        loadTable();
        setupListeners();
        applyFilters();
    }

    // =====================
    // 2️⃣ UI SETUP (LOAD & CONFIG)
    // =====================
    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã khách hàng", "Họ đệm", "Tên", "Số điện thoại");
        StatusBUS statusBUS = StatusBUS.getInstance();
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusBUS.getAllByType(StatusType.CUSTOMER));
        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_customerId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_fullName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFullName()));
        tlb_col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tlb_col_address.setCellValueFactory(new PropertyValueFactory<>("address"));
        tlb_col_dob.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatDateTime(cellData.getValue().getDateOfBirth())));
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(
                StatusBUS.getInstance().getById(cellData.getValue().getStatusId()).getDescription()));
        tlb_col_updatedAt.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatDateTimeWithHour(cellData.getValue().getUpdatedAt())));
        UiUtils.gI().addTooltipToColumn(tlb_col_fullName, 20);
        UiUtils.gI().addTooltipToColumn(tlb_col_address, 30);
    }

    @Override
    public void setupListeners() {
        cbSearchBy.setOnAction(event -> handleSearchByChange());
        cbStatusFilter.setOnAction(event -> handleStatusFilterChange());
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> handleKeywordChange());
        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert(AppMessages.GENERAL_REFRESH_SUCCESS, AppMessages.DIALOG_TITLE);
        });
        addBtn.setOnAction(event -> handleAddBtn());
        editBtn.setOnAction(event -> handleEditBtn());
        deleteBtn.setOnAction(event -> handleDeleteBtn());
        exportExcel.setOnAction(event -> handleExportExcel());
    }

    // =====================
    // 3️⃣ CRUD HANDLERS (Add/Edit/Delete)
    // =====================
    private void handleAddBtn() {
        CustomerModalController modalController = new ModalBuilder<CustomerModalController>(
                "/GUI/CustomerModal.fxml", CustomerModalController.class)
                .setTitle("Thêm khách hàng")
                .modeAdd()
                .open();
        if (modalController != null && modalController.isSaved()) {
            resetFilters();
        }
    }

    private void handleEditBtn() {
        if (isNotSelectedCustomer()) {
            NotificationUtils.showErrorAlert(AppMessages.CUSTOMER_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        CustomerModalController modalController = new ModalBuilder<CustomerModalController>(
                "/GUI/CustomerModal.fxml", CustomerModalController.class)
                .setTitle("Sửa khách hàng")
                .modeEdit()
                .configure(c -> {
                    c.setCustomer(selectedCustomer);
                })
                .open();
        if (modalController != null && modalController.isSaved()) {
            resetFilters();
        }

    }

    private void handleDeleteBtn() {
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
            NotificationUtils.showInfoAlert(updateResult.getMessage(), AppMessages.DIALOG_TITLE);
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

    private void handleSearchByChange() {
        searchBy = cbSearchBy.getValue();
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
        int statusId = statusFilter == null ? -1 : statusFilter.getId();
        tblCustomer.setItems(FXCollections.observableArrayList(
                CustomerBUS.getInstance().filterCustomers(keyword, statusId, 0, 100)));
        tblCustomer.getSelectionModel().clearSelection();
    }

    @Override
    public void resetFilters() {
        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
        txtSearch.clear();
        searchBy = "Mã KH";
        keyword = "";
        statusFilter = null;
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        boolean canView = session.hasPermission(PermissionKey.CUSTOMER_LIST_VIEW);

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
            UiUtils.gI().setReadOnlyItem(addBtn);
        if (!canEdit)
            UiUtils.gI().setReadOnlyItem(editBtn);
        if (!canDelete)
            UiUtils.gI().setReadOnlyItem(deleteBtn);
    }

    // =====================
    // 6️⃣ UTILITY METHODS
    // =====================
    private void handleExportExcel() {
        // try {
        // ExcelService.getInstance().ExportSheet("customers");
        // } catch (IOException e) {
        // NotificationUtils.showErrorAlert("Xuất Excel thất bại", "Lỗi");
        // }
    }

    private boolean isNotSelectedCustomer() {
        selectedCustomer = tblCustomer.getSelectionModel().getSelectedItem();
        return selectedCustomer == null;
    }

}
