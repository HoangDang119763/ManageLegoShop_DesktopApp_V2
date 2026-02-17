package GUI;

import BUS.*;
import DTO.DetailInvoiceDTO;
import DTO.InvoiceDTO;
import ENUM.PermissionKey;
import INTERFACE.IController;
import SERVICE.PrintService;
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

public class InvoiceController implements IController {
    @FXML
    private TableView<InvoiceDTO> tblInvoice;
    @FXML
    private TableColumn<InvoiceDTO, Integer> tlb_col_id;
    @FXML
    private TableColumn<InvoiceDTO, String> tlb_col_createDate;
    @FXML
    private TableColumn<InvoiceDTO, Integer> tlb_col_employeeId;
    @FXML
    private TableColumn<InvoiceDTO, Integer> tlb_col_customerId;
    @FXML
    private TableColumn<InvoiceDTO, String> tlb_col_totalPrice;
    @FXML
    private TableColumn<InvoiceDTO, String> tlb_col_status;
    @FXML
    private TableView<DetailInvoiceDTO> tblDetailInvoice;
    @FXML
    private TableColumn<DetailInvoiceDTO, String> tlb_col_productId;
    @FXML
    private TableColumn<DetailInvoiceDTO, String> tlb_col_quantity;
    @FXML
    private TableColumn<DetailInvoiceDTO, String> tlb_col_price;
    @FXML
    private TableColumn<DetailInvoiceDTO, String> tlb_col_totalPriceP;
    @FXML
    private TextField id;

    @FXML
    private TextField createDate;

    @FXML
    private TextField employeeId;

    @FXML
    private TextField customerId;

    @FXML
    private TextField discountCode;

    @FXML
    private TextField discountAmount;

    @FXML
    private TextField totalPrice;

    @FXML
    private TextField status;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button exportPdf;
    @FXML
    private Button advanceSearchBtn, refreshBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private PaginationController paginationController;
    @FXML
    private StackPane loadingOverlay;

    private String keyword = "";
    private InvoiceDTO selectedInvoice;
    private InvoiceBUS invoiceBUS;
    private DetailInvoiceBUS detailInvoiceBUS;
    private StatusBUS statusBUS;
    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        invoiceBUS = InvoiceBUS.getInstance();
        detailInvoiceBUS = DetailInvoiceBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        // [STATELESS] No pre-load needed - InvoiceBUS, DetailInvoiceBUS, StatusBUS load
        // on-demand

        tblInvoice.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblInvoice.getSelectionModel().clearSelection());
        Platform.runLater(() -> tblDetailInvoice.getSelectionModel().clearSelection());

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
        tlb_col_customerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));

        tlb_col_totalPrice.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(statusBUS
                .getById(cellData.getValue().getStatusId()).getDescription()));
        UiUtils.gI().addTooltipToColumn(tlb_col_createDate, 10);
        UiUtils.gI().addTooltipToColumn(tlb_col_totalPrice, 10);
        // tblInvoice.setItems(FXCollections.observableArrayList(invoiceBUS.getAllLocal()));
    }

    public void loadSubTable(int invoiceId) {
        if (invoiceId <= 0)
            return;
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        this.id.setText(String.valueOf(selectedInvoice.getId()));
        this.createDate.setText(validationUtils.formatDateTime(selectedInvoice.getCreateDate()));
        this.employeeId.setText(String.valueOf(selectedInvoice.getEmployeeId()));
        this.customerId.setText(String.valueOf(selectedInvoice.getCustomerId()));
        this.discountCode
                .setText(selectedInvoice.getDiscountCode() != null ? selectedInvoice.getDiscountCode() : "");
        this.discountAmount.setText(validationUtils.formatCurrency(selectedInvoice.getDiscountAmount()));
        this.totalPrice.setText(validationUtils.formatCurrency(selectedInvoice.getTotalPrice()));
        this.status.setText(statusBUS.getById(selectedInvoice.getStatusId()).getDescription());
        tlb_col_productId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        tlb_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tlb_col_price.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getPrice())));
        tlb_col_totalPriceP.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));

        UiUtils.gI().addTooltipToColumn(tlb_col_price, 10);
        UiUtils.gI().addTooltipToColumn(tlb_col_totalPriceP, 10);
        // tblDetailInvoice.setItems(FXCollections
        // .observableArrayList(detailInvoiceBUS.getAllDetailInvoiceByInvoiceIdLocal(invoiceId)));
        tblDetailInvoice.getSelectionModel().clearSelection();
    }

    private SimpleStringProperty formatCell(String value) {
        return new SimpleStringProperty(value);
    }

    @Override
    public void setupListeners() {
        tblInvoice.setOnMouseClicked(event -> {
            selectedInvoice = tblInvoice.getSelectionModel().getSelectedItem();
            if (isSelectedInvoice()) {
                loadSubTable(selectedInvoice.getId());
            } else {
                tblDetailInvoice.getItems().clear();
            }
        });
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> handleKeywordChange());
        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert("Làm mới thành công", "Thông báo");
        });
        exportPdf.setOnAction(e -> handleExportPDF());
        advanceSearchBtn.setOnAction(e -> handleAdvanceSearch());
    }

    private void clearSubTable() {
        this.id.setText("");
        this.createDate.setText("");
        this.employeeId.setText("");
        this.customerId.setText("");
        this.discountCode.setText("");
        this.discountAmount.setText("");
        this.totalPrice.setText("");
        tblDetailInvoice.setItems(FXCollections.observableArrayList());
        tblDetailInvoice.getSelectionModel().clearSelection();
    }

    private void handleKeywordChange() {
        keyword = txtSearch.getText().trim();
        applyFilters();
    }

    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex);
        });
    }

    private void loadPageData(int pageIndex) {
        // TaskUtil.executeSecure(loadingOverlay, PermissionKey.INVOICE_LIST_VIEW,
        // () -> invoiceBUS.getAll(),
        // result -> {
        // tblInvoice.setItems(FXCollections.observableArrayList(result));
        // paginationController.setPageCount(1);
        // tblInvoice.getSelectionModel().clearSelection();
        // });
    }

    @Override
    public void applyFilters() {
        InvoiceBUS invoiceBUS = InvoiceBUS.getInstance();
        clearSubTable();
        if (keyword.isEmpty()) {
            // Nߦ+u keyword r�+�ng, lߦ�y tߦ�t cߦ� h+�a -��n
            tblInvoice.setItems(FXCollections.observableArrayList(invoiceBUS.getAll()));
        } else {
            try {
                int id = Integer.parseInt(keyword);
                tblInvoice.setItems(FXCollections.observableArrayList(
                        invoiceBUS.getById(id)));
            } catch (NumberFormatException e) {
                // X�+� l++ tr���+�ng h�+�p kh+�ng phߦ�i s�+�
                tblInvoice.setItems(FXCollections.observableArrayList());
            }
        }
        tblInvoice.getSelectionModel().clearSelection();
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

    private void handleAdvanceSearch() {
        InvoiceAdvanceSearchModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/InvoiceAdvanceSearchModal.fxml",
                null,
                "Tìm kiếm nâng cao");
        if (modalController != null && modalController.isSaved()) {
            tblInvoice.setItems(FXCollections.observableArrayList(modalController.getFilteredInvoices()));
            tblInvoice.getSelectionModel().clearSelection();
            clearSubTable();
        }
    }

    private void handleExportPDF() {
        if (!isSelectedInvoice()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn hóa đơn.", "Thông báo");
            return;
        }
        PrintService.getInstance().printInvoiceForm(selectedInvoice.getId());
    }

    private boolean isSelectedInvoice() {
        selectedInvoice = tblInvoice.getSelectionModel().getSelectedItem();
        return selectedInvoice != null;
    }
}