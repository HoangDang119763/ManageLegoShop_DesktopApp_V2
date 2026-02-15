package GUI;

import BUS.StatusBUS;
import BUS.SupplierBUS;
import DTO.BUSResult;
import DTO.StatusDTO;
import DTO.SupplierDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.SecureExecutor;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.ModalBuilder;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.util.ArrayList;

public class SupplierController implements IController {
    @FXML
    private TableView<SupplierDTO> tblSupplier;
    @FXML
    private TableColumn<SupplierDTO, Integer> tlb_col_id;
    @FXML
    private TableColumn<SupplierDTO, String> tlb_col_name;
    @FXML
    private TableColumn<SupplierDTO, String> tlb_col_phone;
    @FXML
    private TableColumn<SupplierDTO, String> tlb_col_email;
    @FXML
    private TableColumn<SupplierDTO, String> tlb_col_address;
    @FXML
    private TableColumn<SupplierDTO, String> tlb_col_status;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<StatusDTO> cbStatusFilter;
    @FXML
    private ComboBox<String> cbSearchBy;

    // BUS instances
    private SupplierBUS supplierBUS;
    private StatusBUS statusBUS;

    // Filter states
    private String searchBy = "Mã nhà cung cấp";
    private String keyword = "";
    private StatusDTO statusFilter = null;
    private SupplierDTO selectedSupplier;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        supplierBUS = SupplierBUS.getInstance();
        // [STATELESS] No pre-load needed - SupplierBUS loads on-demand

        statusBUS = StatusBUS.getInstance();
        // [STATELESS] Data loads on-demand via HikariCP

        tblSupplier.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblSupplier.getSelectionModel().clearSelection());

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
        cbSearchBy.getItems().addAll("Mã nhà cung cấp", "Tên nhà cung cấp");
        cbSearchBy.getSelectionModel().selectFirst();

        ArrayList<StatusDTO> statusList = statusBUS.getAllByType(StatusType.SUPPLIER);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusList);
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @Override
    public void loadTable() {
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tlb_col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        tlb_col_address.setCellValueFactory(new PropertyValueFactory<>("address"));

        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(
                statusBUS.getById(cellData.getValue().getStatusId()).getDescription()));

        UiUtils.gI().addTooltipToColumn(tlb_col_name, 20);
        UiUtils.gI().addTooltipToColumn(tlb_col_email, 20);
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

        addBtn.setOnAction(event -> handleAdd());
        editBtn.setOnAction(event -> handleEdit());
        deleteBtn.setOnAction(event -> handleDelete());

        // Hỗ trợ xem chi tiết khi double click
        tblSupplier.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2)
                handleDetail();
        });
    }

    // =====================
    // 3️⃣ CRUD HANDLERS (Add/Edit/Detail/Delete)
    // =====================
    private void handleAdd() {
        SupplierModalController modalController = new ModalBuilder<SupplierModalController>("/GUI/SupplierModal.fxml",
                SupplierModalController.class)
                .setTitle("Thêm nhà cung cấp")
                .modeAdd()
                .open();
        if (modalController != null && modalController.isSaved()) {
            resetFilters();
        }
    }

    private void handleEdit() {
        if (isNotSelectedSupplier()) {
            NotificationUtils.showErrorAlert(AppMessages.SUPPLIER_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        SupplierModalController modalController = new ModalBuilder<SupplierModalController>("/GUI/SupplierModal.fxml",
                SupplierModalController.class)
                .setTitle("Sửa nhà cung cấp")
                .modeEdit()
                .configure(c -> c.setSupplier(selectedSupplier))
                .open();
        if (modalController != null && modalController.isSaved()) {
            applyFilters();
        }
        tblSupplier.refresh();
    }

    private void handleDetail() {
        if (isNotSelectedSupplier())
            return;
        new ModalBuilder<SupplierModalController>("/GUI/SupplierModal.fxml", SupplierModalController.class)
                .setTitle("Chi tiết nhà cung cấp")
                .modeDetail()
                .configure(c -> c.setSupplier(selectedSupplier))
                .open();
    }

    private void handleDelete() {
        if (isNotSelectedSupplier()) {
            NotificationUtils.showErrorAlert(AppMessages.SUPPLIER_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert(AppMessages.SUPPLIER_DELETE_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        // BUSResult result =
        // SecureExecutor.runSafeBUSResult(PermissionKey.SUPPLIER_DELETE,
        // () -> supplierBUS.delete(selectedSupplier.getId(),
        // SessionManagerService.getInstance().employeeRoleId(),
        // SessionManagerService.getInstance().employeeLoginId()));

        // if (result.isSuccess()) {
        // NotificationUtils.showInfoAlert(result.getMessage(),
        // AppMessages.DIALOG_TITLE);
        // resetFilters();
        // } else {
        // NotificationUtils.showErrorAlert(result.getMessage(),
        // AppMessages.DIALOG_TITLE);
        // }
    }

    // =====================
    // 4️⃣ FILTER HANDLERS
    // =====================
    private void handleSearchByChange() {
        searchBy = cbSearchBy.getValue();
        applyFilters();
    }

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
        int statusId = (statusFilter == null) ? -1 : statusFilter.getId();
        tblSupplier.setItems(FXCollections.observableArrayList(
                supplierBUS.filterSuppliers(searchBy, keyword, statusId)));
        tblSupplier.getSelectionModel().clearSelection();
    }

    @Override
    public void resetFilters() {
        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
        txtSearch.clear();

        searchBy = "Mã nhà cung cấp";
        keyword = "";
        statusFilter = null;
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();

        // Kiểm tra quyền xem danh sách (Nếu cần thiết chặn view)
        // boolean canView = session.hasPermission(PermissionKey.SUPPLIER_LIST_VIEW);

        if (!session.hasPermission(PermissionKey.SUPPLIER_INSERT))
            UiUtils.gI().setVisibleItem(addBtn);
        if (!session.hasPermission(PermissionKey.SUPPLIER_UPDATE))
            UiUtils.gI().setVisibleItem(editBtn);
        if (!session.hasPermission(PermissionKey.SUPPLIER_DELETE))
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