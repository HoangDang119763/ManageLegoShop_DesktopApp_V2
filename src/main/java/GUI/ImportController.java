
package GUI;

import java.math.BigDecimal;
import java.util.ArrayList;

import BUS.DetailImportBUS;
import BUS.ImportBUS;
import BUS.StatusBUS;
import DTO.DetailImportDTO;
import DTO.ImportDisplayDTO;
import DTO.PagedResponse;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ImportController implements IController {
    @FXML
    private TableView<ImportDisplayDTO> tblImport;
    @FXML
    private TableColumn<ImportDisplayDTO, Integer> tlb_col_id;
    @FXML
    private TableColumn<ImportDisplayDTO, String> tlb_col_createDate;
    @FXML
    private TableColumn<ImportDisplayDTO, String> tlb_col_employeeName;
    @FXML
    private TableColumn<ImportDisplayDTO, String> tlb_col_supplierName;
    @FXML
    private TableColumn<ImportDisplayDTO, String> tlb_col_totalPrice;
    @FXML
    private TableColumn<ImportDisplayDTO, String> tlb_col_status;
    @FXML
    private TableView<DetailImportDTO> tblDetailImport;
    @FXML
    private TableColumn<DetailImportDTO, String> tlb_col_productId;
    @FXML
    private TableColumn<DetailImportDTO, String> tlb_col_quantity;
    @FXML
    private TableColumn<DetailImportDTO, String> tlb_col_price;
    @FXML
    private TableColumn<DetailImportDTO, String> tlb_col_totalPriceP;
    @FXML
    private TableColumn<DetailImportDTO, String> tlb_col_push_status;
    @FXML
    private TableColumn<DetailImportDTO, String> tlb_col_profit_percent;
    @FXML
    private TextField id;

    @FXML
    private TextField createDate;

    @FXML
    private TextField employeeId;

    @FXML
    private TextField supplierId;

    @FXML
    private TextField totalPrice;

    @FXML
    private TextField status;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button addImportBtn;
    @FXML
    private Button exportPdf;
    @FXML
    private Button approveImportBtn;
    @FXML
    private Button deleteImportBtn;
    @FXML
    private Button refreshBtn;
    @FXML
    private Button advanceSearchBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<StatusDTO> cbStatusFilter;
    @FXML
    private PaginationController paginationController;
    @FXML
    private StackPane loadingOverlay;

    private String keyword = "";
    private ImportDisplayDTO selectedImport;
    private StatusBUS statusBUS;
    private ImportBUS importBUS;
    private DetailImportBUS detailImportBUS;
    private StatusDTO statusFilter = null;
    private static final int PAGE_SIZE = 10;
    private boolean isResetting = false;
    private SessionManagerService sessionManagerService = SessionManagerService.getInstance();

    @FXML
    public void initialize() {
        statusBUS = StatusBUS.getInstance();
        importBUS = ImportBUS.getInstance();
        detailImportBUS = DetailImportBUS.getInstance();

        tblImport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblImport.getSelectionModel().clearSelection());
        Platform.runLater(() -> tblDetailImport.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        setupListeners();

        loadTable();
        setupPagination();
        applyFilters();
    }

    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_createDate.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatDateTimeWithHour(cellData.getValue().getCreatedAt())));
        tlb_col_employeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        tlb_col_supplierName.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        tlb_col_totalPrice.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));
        tlb_col_status.setCellValueFactory(new PropertyValueFactory<>("statusDescription"));
        UiUtils.gI().addTooltipToColumn(tlb_col_createDate, 10);
        tblImport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    private void loadComboBox() {
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusBUS.getAllByType(StatusType.IMPORT));
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    public void loadSubTable(int importId) {
        if (importId <= 0)
            return;
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        this.id.setText(String.valueOf(selectedImport.getId()));
        this.createDate.setText(validationUtils.formatDateTime(selectedImport.getCreatedAt()));
        this.employeeId.setText(String.valueOf(selectedImport.getEmployeeId()));
        this.supplierId.setText(String.valueOf(selectedImport.getSupplierId()));
        this.totalPrice.setText(validationUtils.formatCurrency(selectedImport.getTotalPrice()));
        // Dùng statusDescription từ DisplayDTO thay vì gọi BUS
        this.status.setText(selectedImport.getStatusDescription());
        tlb_col_productId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        tlb_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tlb_col_price.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getPrice())));
        tlb_col_totalPriceP.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));
        tlb_col_push_status.setCellValueFactory(cellData -> formatCell(
                cellData.getValue().isPushed() ? "Đã đẩy" : "Chưa đẩy"));
        tlb_col_profit_percent.setCellValueFactory(cellData -> formatCell(
                validationUtils.formatPercent(
                        cellData.getValue().getProfitPercent() != null ? cellData.getValue().getProfitPercent()
                                : BigDecimal.ZERO)));
        TaskUtil.executeSecure(null, PermissionKey.DISCOUNT_LIST_VIEW,
                () -> DetailImportBUS.getInstance().getAllDetailImportByImportId(importId),
                result -> {
                    ArrayList<DetailImportDTO> detailImports = result.getData();
                    if (!detailImports.isEmpty()) {
                        tblDetailImport.setItems(FXCollections.observableArrayList(detailImports));
                        Stage currentStage = (Stage) tblDetailImport.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                    }
                });
    }

    private SimpleStringProperty formatCell(String value) {
        return new SimpleStringProperty(value);
    }

    @Override
    public void setupListeners() {
        tblImport.setOnMouseClicked(event -> {
            selectedImport = tblImport.getSelectionModel().getSelectedItem();
            if (isSelectedImport()) {
                loadSubTable(selectedImport.getId());
            } else {
                tblDetailImport.getItems().clear();
            }
        });
        UiUtils.gI().applySearchDebounce(txtSearch, 500, () -> handleKeywordChange());
        cbStatusFilter.setOnAction(event -> handleStatusFilterChange());
        refreshBtn.setOnAction(event -> {
            resetFilters();
            Stage currentStage = (Stage) refreshBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Làm mới thành công");
        });
        approveImportBtn.setOnAction(event -> handleApproveImport());
        deleteImportBtn.setOnAction(event -> handleDeleteImport());
        addImportBtn.setOnAction(event -> handleAddImportBtn());
    }

    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex, true);
        });
    }

    private void loadPageData(int pageIndex, boolean showOverlay) {
        String keyword = txtSearch.getText().trim();
        int statusId = (cbStatusFilter.getValue() == null) ? -1 : cbStatusFilter.getValue().getId();
        StackPane overlay = showOverlay ? loadingOverlay : null;
        TaskUtil.executeSecure(overlay, PermissionKey.IMPORT_LIST_VIEW,
                () -> importBUS.filterImportsPagedForManage(keyword, statusId, pageIndex, PAGE_SIZE),
                result -> {
                    // Lấy dữ liệu ImportDisplayDTO đã được JOIN
                    PagedResponse<ImportDisplayDTO> res = result.getPagedData();

                    if (res != null) {
                        tblImport.setItems(FXCollections.observableArrayList(res.getItems()));
                        int totalItems = res.getTotalItems();
                        int pageCount = (int) Math.ceil((double) totalItems / PAGE_SIZE);
                        paginationController.setPageCount(pageCount > 0 ? pageCount : 1);
                    }
                    tblImport.getSelectionModel().clearSelection();
                });
    }

    private void handleAddImportBtn() {
        // Lấy stage hiện tại (MainUI) từ button
        Stage currentStage = (Stage) addImportBtn.getScene().getWindow();
        currentStage.close();

        // Mở ImportProduct mới
        UiUtils.gI().openStage("/GUI/ImportProduct.fxml", "Nhập hàng");
    }

    private void clearSubTable() {
        this.id.setText("");
        this.createDate.setText("");
        this.employeeId.setText("");
        this.supplierId.setText("");
        this.totalPrice.setText("");
        tblDetailImport.setItems(FXCollections.observableArrayList());
        tblDetailImport.getSelectionModel().clearSelection();
    }

    private void handleKeywordChange() {
        if (isResetting)
            return;

        String newKeyword = txtSearch.getText().trim();
        if (newKeyword.equals(keyword))
            return;

        keyword = newKeyword;
        applyFilters();
    }

    private void handleStatusFilterChange() {
        statusFilter = cbStatusFilter.getValue();
        applyFilters();
    }

    @Override
    public void applyFilters() {
        clearSubTable();
        if (paginationController.getCurrentPage() == 0) {
            loadPageData(0, true); // Trường hợp đang ở trang 0 rồi thì phải gọi thủ công
        } else {
            paginationController.setCurrentPage(0);
        }
    }

    @Override
    public void resetFilters() {
        isResetting = true;

        txtSearch.clear();
        cbStatusFilter.getSelectionModel().selectFirst();
        keyword = "";
        statusFilter = null;
        clearSubTable();

        applyFilters();

        javafx.application.Platform.runLater(() -> isResetting = false);
    }

    @Override
    public void hideButtonWithoutPermission() {
        boolean canApprove = sessionManagerService.hasPermission(PermissionKey.IMPORT_APPROVE);
        if (!canApprove) {
            UiUtils.gI().setVisibleItem(approveImportBtn);
            UiUtils.gI().setVisibleItem(deleteImportBtn);
        }
    }

    private boolean isSelectedImport() {
        selectedImport = tblImport.getSelectionModel().getSelectedItem();
        return selectedImport != null;
    }

    private void handleApproveImport() {
        if (!isSelectedImport()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn một phiếu nhập để duyệt!", AppMessages.DIALOG_TITLE);
            return;
        }
        if (!UiUtils.gI().showConfirmAlert("Bạn có chắc chắn muốn duyệt phiếu nhập này không?\n" +
                "(Hành động này sẽ cập nhật tồn kho và giá sản phẩm)", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.IMPORT_APPROVE,
                () -> importBUS.approveImport(selectedImport.getId()),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) approveImportBtn.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                        applyFilters();
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    private void handleDeleteImport() {
        if (!isSelectedImport()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn một phiếu nhập để xóa!", AppMessages.DIALOG_TITLE);
            return;
        }
        if (!UiUtils.gI().showConfirmAlert("Bạn có chắc chắn muốn xóa phiếu nhập này không?\n" +
                "(Chỉ có thể xóa phiếu nhập ở trạng thái DRAFT)", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.IMPORT_APPROVE,
                () -> importBUS.deleteImport(selectedImport.getId()),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) deleteImportBtn.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                        applyFilters();
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }
}