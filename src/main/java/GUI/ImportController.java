
package GUI;

import BUS.DetailImportBUS;
import BUS.ImportBUS;
import BUS.StatusBUS;
import DTO.DetailImportDTO;
import DTO.ImportDTO;
import INTERFACE.IController;
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

public class ImportController implements IController {
    @FXML
    private TableView<ImportDTO> tblImport;
    @FXML
    private TableColumn<ImportDTO, Integer> tlb_col_id;
    @FXML
    private TableColumn<ImportDTO, String> tlb_col_createDate;
    @FXML
    private TableColumn<ImportDTO, Integer> tlb_col_employeeId;
    @FXML
    private TableColumn<ImportDTO, Integer> tlb_col_supplierId;
    @FXML
    private TableColumn<ImportDTO, String> tlb_col_totalPrice;
    @FXML
    private TableColumn<ImportDTO, String> tlb_col_status;
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
    private String keyword = "";
    private ImportDTO selectedImport;
    private StatusBUS statusBUS;
    private ImportBUS importBUS;
    private DetailImportBUS detailImportBUS;

    @FXML
    public void initialize() {
        statusBUS = StatusBUS.getInstance();
        importBUS = ImportBUS.getInstance();
        detailImportBUS = DetailImportBUS.getInstance();

        if (importBUS.isLocalEmpty())
            importBUS.loadLocal();
        if (detailImportBUS.isLocalEmpty())
            detailImportBUS.loadLocal();
        if (statusBUS.isLocalEmpty())
            statusBUS.loadLocal();

        tblImport.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblImport.getSelectionModel().clearSelection());
        Platform.runLater(() -> tblDetailImport.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        setupListeners();

        loadTable();
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
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(statusBUS
                .getByIdLocal(cellData.getValue().getStatusId()).getDescription()));
        UiUtils.gI().addTooltipToColumn(tlb_col_createDate, 10);
        tblImport.setItems(FXCollections.observableArrayList(ImportBUS.getInstance().getAllLocal()));
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
        this.status.setText(statusBUS.getByIdLocal(selectedImport.getStatusId()).getDescription());
        tlb_col_productId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        tlb_col_quantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tlb_col_price.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getPrice())));
        tlb_col_totalPriceP.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getTotalPrice())));
        tblDetailImport.setItems(FXCollections
                .observableArrayList(detailImportBUS.getAllDetailImportByImportIdLocal(importId)));
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
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> handleKeywordChange());
        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert("Làm mới thành công.", "Thông báo");
        });
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
        ImportBUS importBUS = ImportBUS.getInstance();
        clearSubTable();
        if (keyword.isEmpty()) {
            // Nߦ+u keyword r�+�ng, lߦ�y tߦ�t cߦ� h+�a -��n
            tblImport.setItems(FXCollections.observableArrayList(importBUS.getAllLocal()));
        } else {
            try {
                int id = Integer.parseInt(keyword);
                tblImport.setItems(FXCollections.observableArrayList(
                        importBUS.getByIdLocal(id)));
            } catch (NumberFormatException e) {
                // X�+� l++ tr���+�ng h�+�p kh+�ng phߦ�i s�+�
                tblImport.setItems(FXCollections.observableArrayList());
            }
        }
        tblImport.getSelectionModel().clearSelection();
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