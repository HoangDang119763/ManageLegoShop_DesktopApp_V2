
package GUI;

import java.math.BigDecimal;

import BUS.DetailImportBUS;
import BUS.ImportBUS;
import BUS.StatusBUS;
import DTO.DetailImportDTO;
import DTO.ImportDisplayDTO;
import DTO.PagedResponse;
import ENUM.PermissionKey;
import INTERFACE.IController;
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

public class ImportController implements IController {
    @FXML
    private TableView<ImportDisplayDTO> tblImport;
    @FXML
    private TableColumn<ImportDisplayDTO, Integer> tlb_col_id;
    @FXML
    private TableColumn<ImportDisplayDTO, String> tlb_col_createDate;
    @FXML
    private TableColumn<ImportDisplayDTO, Integer> tlb_col_employeeId;
    @FXML
    private TableColumn<ImportDisplayDTO, Integer> tlb_col_supplierId;
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
    private Button refreshBtn;
    @FXML
    private Button advanceSearchBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private PaginationController paginationController;
    @FXML
    private StackPane loadingOverlay;

    private String keyword = "";
    private ImportDisplayDTO selectedImport;
    private StatusBUS statusBUS;
    private ImportBUS importBUS;
    private DetailImportBUS detailImportBUS;
    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        statusBUS = StatusBUS.getInstance();
        importBUS = ImportBUS.getInstance();
        detailImportBUS = DetailImportBUS.getInstance();

        tblImport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblImport.getSelectionModel().clearSelection());
        Platform.runLater(() -> tblDetailImport.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        setupListeners();

        loadTable();
        setupPagination();
    }

    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_createDate.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatDateTimeWithHour(cellData.getValue().getCreateDate())));
        tlb_col_employeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        tlb_col_supplierId.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        tlb_col_totalPrice.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));
        tlb_col_status.setCellValueFactory(new PropertyValueFactory<>("statusDescription"));
        UiUtils.gI().addTooltipToColumn(tlb_col_createDate, 10);
        tblImport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Tải trang đầu tiên
        loadPageData(0);
    }

    public void loadSubTable(int importId) {
        if (importId <= 0)
            return;
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        this.id.setText(String.valueOf(selectedImport.getId()));
        this.createDate.setText(validationUtils.formatDateTime(selectedImport.getCreateDate()));
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
        tblDetailImport.setItems(FXCollections
                .observableArrayList(detailImportBUS.getAllDetailImportByImportId(importId)));

        tblDetailImport.getSelectionModel().clearSelection();
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
        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert("Làm mới thành công.", "Thông báo");
        });
        addImportBtn.setOnAction(event -> handleAddImportBtn());
    }

    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex);
        });
    }

    private void loadPageData(int pageIndex) {
        String keyword = txtSearch.getText().trim();
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.IMPORT_LIST_VIEW,
                () -> importBUS.filterImportsPagedForManage(keyword, pageIndex, PAGE_SIZE),
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
        // Will be implemented similar to EmployeeController
        // Shows modal for adding new import
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
        keyword = txtSearch.getText().trim();
        applyFilters();
    }

    @Override
    public void applyFilters() {
        clearSubTable();
        if (paginationController.getCurrentPage() == 0) {
            loadPageData(0); // Trường hợp đang ở trang 0 rồi thì phải gọi thủ công
        } else {
            paginationController.setCurrentPage(0);
        }
    }

    @Override
    public void resetFilters() {
        txtSearch.clear();
        clearSubTable();
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {

    }

    private boolean isSelectedImport() {
        selectedImport = tblImport.getSelectionModel().getSelectedItem();
        return selectedImport != null;
    }
}