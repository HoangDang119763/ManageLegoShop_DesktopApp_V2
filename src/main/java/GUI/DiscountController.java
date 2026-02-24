
package GUI;

import BUS.DetailDiscountBUS;
import BUS.DiscountBUS;
import DTO.DetailDiscountDTO;
import DTO.DiscountDTO;
import DTO.PagedResponse;
import ENUM.DiscountType;
import ENUM.PermissionKey;
import INTERFACE.IController;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

public class DiscountController implements IController {
    @FXML
    private TableView<DiscountDTO> tblDiscount;
    @FXML
    private TableColumn<DiscountDTO, Integer> tlb_col_code;
    @FXML
    private TableColumn<DiscountDTO, String> tlb_col_name;
    @FXML
    private TableColumn<DiscountDTO, String> tlb_col_type;
    @FXML
    private TableColumn<DiscountDTO, String> tlb_col_startDate;
    @FXML
    private TableColumn<DiscountDTO, String> tlb_col_endDate;
    @FXML
    private TableView<DetailDiscountDTO> tblDetailDiscount;
    @FXML
    private TableColumn<DetailDiscountDTO, String> tlb_col_totalPriceInvoice;
    @FXML
    private TableColumn<DetailDiscountDTO, String> tlb_col_discountAmount;
    @FXML
    private TextField code, name, type, startDate, endDate;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn, advanceSearchBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private PaginationController paginationController;
    @FXML
    private StackPane loadingOverlay;
    @FXML
    private AnchorPane mainContent;

    private String keyword = "";
    private DiscountDTO selectedDiscount;
    private SessionManagerService session;
    private DiscountBUS discountBUS;
    private static final int PAGE_SIZE = 10;
    private boolean isResetting = false;

    @FXML
    public void initialize() {
        tblDiscount.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblDetailDiscount.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblDiscount.getSelectionModel().clearSelection());
        Platform.runLater(() -> tblDetailDiscount.getSelectionModel().clearSelection());
        session = SessionManagerService.getInstance();
        discountBUS = DiscountBUS.getInstance();
        hideButtonWithoutPermission();
        // loadComboBox();
        setupListeners();

        loadTable();
        setupPagination();
        applyFilters();
    }

    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_code.setCellValueFactory(new PropertyValueFactory<>("code"));
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_type.setCellValueFactory(
                cellData -> formatCell(DiscountType.fromCode(cellData.getValue().getType()).getDisplayName()));
        tlb_col_startDate.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatDateTime(cellData.getValue().getStartDate())));
        tlb_col_endDate.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatDateTime(cellData.getValue().getEndDate())));
        UiUtils.gI().addTooltipToColumn(tlb_col_name, 10);
        tblDiscount.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public void loadSubTable(String discountCode) {
        if (discountCode == null || discountCode.isEmpty())
            return;
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        this.code.setText(selectedDiscount.getCode());
        this.name.setText(selectedDiscount.getName());
        this.type.setText(DiscountType.fromCode(selectedDiscount.getType()).getDisplayName());
        this.startDate.setText(validationUtils.formatDateTime(selectedDiscount.getStartDate()));
        this.endDate.setText(validationUtils.formatDateTime(selectedDiscount.getEndDate()));

        // Setup detail table columns
        tlb_col_totalPriceInvoice.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getTotalPriceInvoice())));
        tlb_col_discountAmount.setCellValueFactory(cellData -> {
            BigDecimal discountValue = cellData.getValue().getDiscountAmount();
            if (!isNotSelectedDiscount() && selectedDiscount.getType() == DiscountType.PERCENTAGE.getCode()) {
                // Type PERCENTAGE: Giảm theo phần trăm (%)
                return formatCell(validationUtils.formatCurrency(discountValue) + " %");
            } else {
                // Type FIXED_AMOUNT: Giảm trực tiếp bằng số tiền
                return formatCell(validationUtils.formatCurrency(discountValue));
            }
        });
        UiUtils.gI().addTooltipToColumn(tlb_col_discountAmount, 10);
        TaskUtil.executeSecure(null, PermissionKey.DISCOUNT_LIST_VIEW,
                () -> DetailDiscountBUS.getInstance().getAllDetailDiscountByDiscountId(discountCode),
                result -> {
                    ArrayList<DetailDiscountDTO> detailDiscounts = result.getData();
                    if (!detailDiscounts.isEmpty()) {
                        tblDetailDiscount.setItems(FXCollections.observableArrayList(detailDiscounts));
                        Stage currentStage = (Stage) tblDetailDiscount.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                    }
                });
    }

    private SimpleStringProperty formatCell(String value) {
        return new SimpleStringProperty(value);
    }

    @Override
    public void setupListeners() {
        tblDiscount.setOnMouseClicked(event -> {
            selectedDiscount = tblDiscount.getSelectionModel().getSelectedItem(); // Cߦ�p nhߦ�t selectedDiscount
            if (!isNotSelectedDiscount()) {
                loadSubTable(selectedDiscount.getCode());
            } else {
                tblDetailDiscount.getItems().clear();
            }
        });
        UiUtils.gI().applySearchDebounce(txtSearch, 500, () -> handleKeywordChange());
        refreshBtn.setOnAction(event -> {
            resetFilters();
            Stage currentStage = (Stage) refreshBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, AppMessages.GENERAL_REFRESH_SUCCESS);
        });
        advanceSearchBtn.setOnAction(e -> handleAdvanceSearch());
        addBtn.setOnAction(event -> handleAddBtn());
        deleteBtn.setOnAction(e -> handleDeleteBtn());
        editBtn.setOnAction(e -> handleEditBtn());
    }

    private void clearSubTable() {
        this.code.setText("");
        this.name.setText("");
        this.type.setText("");
        this.startDate.setText("");
        this.endDate.setText("");
        tblDetailDiscount.setItems(FXCollections.observableArrayList());
        tblDetailDiscount.getSelectionModel().clearSelection();
    }

    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex, true);
        });
    }

    private void loadPageData(int pageIndex, boolean showOverlay) {
        String keyword = txtSearch.getText().trim();
        StackPane overlay = showOverlay ? loadingOverlay : null;
        TaskUtil.executeSecure(overlay, PermissionKey.DISCOUNT_LIST_VIEW,
                () -> DiscountBUS.getInstance().filterDiscountsPagedForManage(keyword, pageIndex, PAGE_SIZE),
                result -> {
                    // Lấy dữ liệu DiscountDTO
                    PagedResponse<DiscountDTO> res = result.getPagedData();

                    if (res != null) {
                        tblDiscount.setItems(FXCollections.observableArrayList(res.getItems()));
                        int totalItems = res.getTotalItems();
                        int pageCount = (int) Math.ceil((double) totalItems / PAGE_SIZE);
                        paginationController.setPageCount(pageCount > 0 ? pageCount : 1);
                    }
                    tblDiscount.getSelectionModel().clearSelection();
                });
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
        keyword = "";
        clearSubTable();

        applyFilters();

        javafx.application.Platform.runLater(() -> isResetting = false);
    }

    private void handleAdvanceSearch() {
        // DiscountAdvanceSearchModalController modalController =
        // UiUtils.gI().openStageWithController(
        // "/GUI/DiscountAdvanceSearchModal.fxml",
        // null,
        // "Tiềm kiếm nâng cao");
        // if (modalController != null && modalController.isSaved()) {
        // tblDiscount.setItems(FXCollections.observableArrayList(modalController.getFilteredDiscounts()));
        // tblDiscount.getSelectionModel().clearSelection();
        // clearSubTable();
        // }
    }

    private void handleAddBtn() {
        DiscountModalController modalController = new ModalBuilder<DiscountModalController>(
                "/GUI/DiscountModal.fxml", DiscountModalController.class)
                .setTitle("Thêm khuyến mãi")
                .modeAdd()
                .open();
        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) addBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            loadPageData(paginationController.getCurrentPage(), false);
            clearSubTable();
        }
    }

    private void handleEditBtn() {
        if (isNotSelectedDiscount()) {
            NotificationUtils.showErrorAlert(AppMessages.DISCOUNT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        if (selectedDiscount.getEndDate().toLocalDate().isBefore(LocalDate.now())) {
            NotificationUtils.showErrorAlert("Khuyến mãi đã hết hạn, không thể sửa.",
                    "Thông báo");
            return;
        }
        DiscountModalController modalController = new ModalBuilder<DiscountModalController>(
                "/GUI/DiscountModal.fxml", DiscountModalController.class)
                .setTitle("Sửa khuyến mãi")
                .modeEdit()
                .configure(c -> c.setDiscount(selectedDiscount.getCode()))
                .open();
        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) editBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            loadPageData(paginationController.getCurrentPage(), false);
            clearSubTable();
        }
    }

    private void handleDeleteBtn() {
        if (isNotSelectedDiscount()) {
            NotificationUtils.showErrorAlert(AppMessages.DISCOUNT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        if (!UiUtils.gI().showConfirmAlert(AppMessages.DISCOUNT_DELETE_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM))
            return;

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.DISCOUNT_DELETE,
                () -> discountBUS.deleteFullDiscount(selectedDiscount.getCode()),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) deleteBtn.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());
                        loadPageData(paginationController.getCurrentPage(), false);
                        clearSubTable();
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    @Override
    public void hideButtonWithoutPermission() {
        boolean canView = session.hasPermission(PermissionKey.DISCOUNT_LIST_VIEW);

        if (!canView) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }

        boolean canAdd = SessionManagerService.getInstance().hasPermission(PermissionKey.DISCOUNT_INSERT);
        boolean canEdit = SessionManagerService.getInstance().hasPermission(PermissionKey.DISCOUNT_UPDATE);
        boolean canDelete = SessionManagerService.getInstance().hasPermission(PermissionKey.DISCOUNT_DELETE);

        if (!canAdd)
            UiUtils.gI().setReadOnlyItem(addBtn);
        if (!canEdit)
            UiUtils.gI().setReadOnlyItem(editBtn);
        if (!canDelete)
            UiUtils.gI().setReadOnlyItem(deleteBtn);
    }

    private boolean isNotSelectedDiscount() {
        selectedDiscount = tblDiscount.getSelectionModel().getSelectedItem();
        return selectedDiscount == null;
    }

}