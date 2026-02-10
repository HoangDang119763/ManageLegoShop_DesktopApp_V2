package GUI;

import BUS.CustomerBUS;
import BUS.StatusBUS;
import DTO.CustomerDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.ExcelService;
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;

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

    private String searchBy = "Mã KH";
    private String keyword = "";
    private StatusDTO statusFilter = null;
    private CustomerDTO selectedCustomer;

    @FXML
    public void initialize() {
        if (CustomerBUS.getInstance().isLocalEmpty())
            CustomerBUS.getInstance().loadLocal();
        tblCustomer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblCustomer.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        setupListeners();
        loadTable();
        applyFilters();
    }

    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã KH", "Họ và tên", "Số điện thoại");
        StatusBUS statusBUS = StatusBUS.getInstance();
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusBUS.getAllByTypeLocal(StatusType.CUSTOMER));
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
                StatusBUS.getInstance().getByIdLocal(cellData.getValue().getStatusId()).getDescription()));
        tlb_col_updatedAt.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatDateTimeWithHour(cellData.getValue().getUpdatedAt())));
        UiUtils.gI().addTooltipToColumn(tlb_col_fullName, 10);
        UiUtils.gI().addTooltipToColumn(tlb_col_address, 10);
    }

    @Override
    public void setupListeners() {
        cbSearchBy.setOnAction(event -> handleSearchByChange());
        cbStatusFilter.setOnAction(event -> handleStatusFilterChange());
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> handleKeywordChange());
        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert("Làm mới thành công", "Thông báo");
        });
        addBtn.setOnAction(event -> handleAddBtn());
        editBtn.setOnAction(event -> handleEditBtn());
        deleteBtn.setOnAction(event -> handleDeleteBtn());
        exportExcel.setOnAction(event -> handleExportExcel());
    }

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
        int statusId = statusFilter == null ? -1 : statusFilter.getId();
        tblCustomer.setItems(FXCollections.observableArrayList(
                CustomerBUS.getInstance().filterCustomers(searchBy, keyword, statusId)));
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
        boolean canAdd = SessionManagerService.getInstance().hasPermission(PermissionKey.CUSTOMER_INSERT);
        boolean canEdit = SessionManagerService.getInstance().hasPermission(PermissionKey.CUSTOMER_UPDATE);
        boolean canDelete = SessionManagerService.getInstance().hasPermission(PermissionKey.CUSTOMER_DELETE);
        if (!canAdd)
            functionBtns.getChildren().remove(addBtn);
        if (!canEdit)
            functionBtns.getChildren().remove(editBtn);
        if (!canDelete)
            functionBtns.getChildren().remove(deleteBtn);
    }

    private void handleAddBtn() {
        CustomerModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/CustomerModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm khách hàng");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert("Thêm khách hàng thành công", "Thông báo");
            applyFilters();
        }
    }

    private void handleEditBtn() {
        selectedCustomer = tblCustomer.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn khách hàng cần sửa", "Lỗi");
            return;
        }
        CustomerModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/CustomerModal.fxml",
                controller -> {
                    controller.setTypeModal(1);
                    controller.setCustomer(selectedCustomer);
                },
                "Sửa khách hàng");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert("Sửa khách hàng thành công", "Thông báo");
            applyFilters();
        }
    }

    private void handleDeleteBtn() {
        selectedCustomer = tblCustomer.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn khách hàng cần xóa", "Lỗi");
            return;
        }
        if (selectedCustomer.getId() == 1) {
            NotificationUtils.showErrorAlert("Bạn không thể xóa khách hàng vãng lai.", "Thông báo");
            return;
        }
        int deleteResult = CustomerBUS.getInstance().delete(
                selectedCustomer.getId(),
                SessionManagerService.getInstance().employeeRoleId(),
                SessionManagerService.getInstance().employeeLoginId());
        switch (deleteResult) {
            case 1 -> {
                NotificationUtils.showInfoAlert("Xóa khách hàng thành công", "Thông báo");
                resetFilters();
            }
            case 2 -> NotificationUtils.showErrorAlert("Có lỗi khi xóa khách hàng. Vui lòng thử lại.", "Thông báo");
            case 3 -> NotificationUtils.showErrorAlert("Bạn không thể xóa khách hàng vãng lai.", "Thông báo");
            case 4 -> NotificationUtils.showErrorAlert("Bạn không có quyền xóa khách hàng", "Thông báo");
            case 5 -> NotificationUtils.showErrorAlert("Khách hàng không tồn tại hoặc đã bị xóa", "Thông báo");
            case 6 -> NotificationUtils.showErrorAlert("Xoá khách hàng thất bại. Vui lòng thử lại.", "Thông báo");
            default -> NotificationUtils.showErrorAlert("Lỗi không xác định", "Thông báo");
        }
    }

    private void handleExportExcel() {
        // try {
        // ExcelService.getInstance().ExportSheet("customers");
        // } catch (IOException e) {
        // NotificationUtils.showErrorAlert("Xuất Excel thất bại", "Lỗi");
        // }
    }
}
