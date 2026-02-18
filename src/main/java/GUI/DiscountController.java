
package GUI;

import BUS.DetailDiscountBUS;
import BUS.DiscountBUS;
import DTO.DetailDiscountDTO;
import DTO.DiscountDTO;
import DTO.PagedResponse;
import ENUM.PermissionKey;
import INTERFACE.IController;
import SERVICE.SessionManagerService;
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

import java.math.BigDecimal;
import java.time.LocalDate;

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

    private String keyword = "";
    private DiscountDTO selectedDiscount;
    private static final int PAGE_SIZE = 10;

    @FXML
    public void initialize() {
        tblDiscount.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tblDetailDiscount.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblDiscount.getSelectionModel().clearSelection());
        Platform.runLater(() -> tblDetailDiscount.getSelectionModel().clearSelection());

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
                cellData -> formatCell(cellData.getValue().getType() == 0 ? "Phần trăm" : "Giảm cứng"));
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
        this.type.setText(selectedDiscount.getType() == 0 ? "Phần trăm" : "Giảm cứng");
        this.startDate.setText(validationUtils.formatDateTime(selectedDiscount.getStartDate()));
        this.endDate.setText(validationUtils.formatDateTime(selectedDiscount.getEndDate()));

        // Setup detail table columns
        tlb_col_totalPriceInvoice.setCellValueFactory(
                cellData -> formatCell(validationUtils.formatCurrency(cellData.getValue().getTotalPriceInvoice())));
        tlb_col_discountAmount.setCellValueFactory(cellData -> {
            BigDecimal discountValue = cellData.getValue().getDiscountAmount();
            if (!isNotSelectedDiscount() && selectedDiscount.getType() == 0) {
                // Type 0: Giảm theo phần trăm (%)
                return formatCell(validationUtils.formatCurrency(discountValue) + " %");
            } else {
                // Type 1: Giảm trực tiếp bằng số tiền
                return formatCell(validationUtils.formatCurrency(discountValue));
            }
        });
        UiUtils.gI().addTooltipToColumn(tlb_col_discountAmount, 10);
        tblDetailDiscount.setItems(FXCollections
                .observableArrayList(DetailDiscountBUS.getInstance().getAllDetailDiscountByDiscountId(discountCode)));

        tblDetailDiscount.getSelectionModel().clearSelection();
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
            NotificationUtils.showInfoAlert("Làm mới thành công.", "Thông báo");
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
        TaskUtil.executeSecure(overlay, PermissionKey.PROMOTION_LIST_VIEW,
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
        keyword = txtSearch.getText().trim();
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
        txtSearch.clear();
        clearSubTable();
        applyFilters();
    }

    private void handleAdvanceSearch() {
        DiscountAdvanceSearchModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/DiscountAdvanceSearchModal.fxml",
                null,
                "Tiềm kiếm nâng cao");
        if (modalController != null && modalController.isSaved()) {
            tblDiscount.setItems(FXCollections.observableArrayList(modalController.getFilteredDiscounts()));
            tblDiscount.getSelectionModel().clearSelection();
            clearSubTable();
        }
    }

    private void handleAddBtn() {
        DiscountModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/DiscountModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm khuyến mãi");
        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) addBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            resetFilters();
        }
    }

    private void handleEditBtn() {
        if (isNotSelectedDiscount()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn khuyến mãi.", "Thông báo");
            return;
        }

        if (selectedDiscount.getEndDate().toLocalDate().isBefore(LocalDate.now())) {
            NotificationUtils.showErrorAlert("Khuyến mãi đã hết hạn, không thể sửa.", "Thông báo");
            return;
        }
        DiscountModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/DiscountModal.fxml",
                controller -> {
                    controller.setDiscount(selectedDiscount);
                    controller.setTypeModal(1);
                },
                "Sửa khuyến mãi");
        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) editBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            resetFilters();
        }
    }

    private void handleDeleteBtn() {
        if (isNotSelectedDiscount()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn khuyến mãi.", "Thông báo");
            return;
        }
        // if
        // (!AvailableUtils.getInstance().isNotUsedDiscount(selectedDiscount.getCode()))
        // {
        // NotificationUtils.showErrorAlert("Khuyến mãi đã được sử dụng, không thể
        // xóa.", "Thông báo");
        // return;
        // }
        if (!UiUtils.gI().showConfirmAlert("Bạn chắc muốn xóa khuyến mãi này?", "Thông báo xác nhận"))
            return;

        // int deleteResult =
        // DiscountService.getInstance().deleteDiscountWithDetailDiscounts(selectedDiscount.getCode(),
        // SessionManagerService.getInstance().employeeRoleId(),
        // SessionManagerService.getInstance().employeeLoginId());

        // switch (deleteResult) {
        // case 1 -> {
        // NotificationUtils.showInfoAlert("Xóa khuyến mãi thành công.", "Thông báo");
        // resetFilters();
        // }
        // case 2 ->
        // NotificationUtils.showErrorAlert("Có lỗi khi xóa khuyến mãi. Vui lòng thử
        // lại.", "Thông báo");
        // case 3 ->
        // NotificationUtils.showErrorAlert("Bạn không có quyền \"Xóa khuyến mãi\" để
        // thực hiện thao tác này.",
        // "Thông báo");
        // case 4 ->
        // NotificationUtils.showErrorAlert("Khuyến mãi đã được sử dụng, không thể
        // xóa.", "Thông báo");
        // case 5 ->
        // NotificationUtils.showErrorAlert("Xóa khuyến mãi thất bại. Vui lòng thử lại
        // sau.", "Thông báo");
        // default ->
        // NotificationUtils.showErrorAlert("Lỗi không xác định, vui lòng thử lại sau.",
        // "Thông báo");
        // }
    }

    @Override
    public void hideButtonWithoutPermission() {
        boolean canAdd = SessionManagerService.getInstance().hasPermission(PermissionKey.PROMOTION_INSERT);
        boolean canEdit = SessionManagerService.getInstance().hasPermission(PermissionKey.PROMOTION_UPDATE);
        boolean canDelete = SessionManagerService.getInstance().hasPermission(PermissionKey.PROMOTION_DELETE);

        if (!canAdd)
            functionBtns.getChildren().remove(addBtn);
        if (!canEdit)
            functionBtns.getChildren().remove(editBtn);
        if (!canDelete)
            functionBtns.getChildren().remove(deleteBtn);
    }

    private boolean isNotSelectedDiscount() {
        selectedDiscount = tblDiscount.getSelectionModel().getSelectedItem();
        return selectedDiscount == null;
    }

}