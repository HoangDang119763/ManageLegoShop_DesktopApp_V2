package GUI;

import BUS.HolidayBUS;
import DTO.PagedResponse;
import DTO.HolidayDTO;
import ENUM.BUSOperationResult;
import ENUM.PermissionKey;
import INTERFACE.IController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.ModalBuilder;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class HolidayController implements IController {
    @FXML
    private TableView<HolidayDTO> tblHoliday;
    @FXML
    private TableColumn<HolidayDTO, String> tlb_col_id;
    @FXML
    private TableColumn<HolidayDTO, String> tlb_col_name;
    @FXML
    private TableColumn<HolidayDTO, String> tlb_col_date;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbSearchBy;
    @FXML
    private AnchorPane mainContent;
    @FXML
    private PaginationController paginationController;
    @FXML
    private StackPane loadingOverlay;

    private String searchBy = "Mã ngày lễ";
    private String keyword = "";
    private HolidayDTO selectedHoliday;
    private boolean isResetting = false;
    private HolidayBUS holidayBUS;
    private SessionManagerService session;
    private final int PAGE_SIZE = 15;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        holidayBUS = HolidayBUS.getInstance();
        session = SessionManagerService.getInstance();

        tblHoliday.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblHoliday.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        loadTable();
        setupPagination();
        setupListeners();
        applyFilters();
    }

    // =====================
    // 2️⃣ UI SETUP
    // =====================
    @Override
    public void loadTable() {
        tlb_col_id.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getId())));
        tlb_col_name.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getName() != null ? cellData.getValue().getName() : ""));
        tlb_col_date.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDate() != null ? cellData.getValue().getDate().format(dateFormatter) : ""));
    }

    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex, true);
        });
    }

    private void loadPageData(int pageIndex, boolean showOverlay) {
        // 1. Thu thập tham số lọc từ UI
        String keyword = txtSearch.getText().trim();
        StackPane overlay = showOverlay ? loadingOverlay : null;

        // 2. Chạy tác vụ ngầm với TaskUtil
        TaskUtil.executePublic(overlay,
                () -> holidayBUS.filterHolidaysPagedForManageDisplay(keyword, pageIndex, PAGE_SIZE),
                result -> {
                    // 3. Lấy PagedResponse
                    PagedResponse<HolidayDTO> res = result.getPagedData();

                    // 4. Cập nhật dữ liệu vào bảng
                    tblHoliday.setItems(FXCollections.observableArrayList(res.getItems()));

                    // 5. Đồng bộ hóa bộ phân trang
                    int totalPages = (int) Math.ceil((double) res.getTotalItems() / PAGE_SIZE);
                    paginationController.setPageCount(totalPages > 0 ? totalPages : 1);

                    // 6. Dọn dẹp trạng thái chọn dòng
                    tblHoliday.getSelectionModel().clearSelection();
                });
    }

    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã ngày lễ", "Tên ngày lễ");
        cbSearchBy.getSelectionModel().selectFirst();
    }

    @Override
    public void setupListeners() {
        cbSearchBy.setOnAction(event -> handleSearchByChange());
        UiUtils.gI().applySearchDebounce(txtSearch, 500, () -> handleKeywordChange());

        refreshBtn.setOnAction(event -> {
            resetFilters();
            Stage currentStage = (Stage) refreshBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, AppMessages.GENERAL_REFRESH_SUCCESS);
        });

        addBtn.setOnAction(event -> handleAddBtn());
        editBtn.setOnAction(event -> handleEditBtn());
        deleteBtn.setOnAction(event -> handleDeleteBtn());

        tblHoliday.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            selectedHoliday = newSel;
        });
    }

    // =====================
    // 3️⃣ CRUD HANDLERS
    // =====================
    private void handleAddBtn() {
        HolidayModalController modalController = new ModalBuilder<HolidayModalController>(
                "/GUI/HolidayModal.fxml", HolidayModalController.class)
                .setTitle("Thêm ngày lễ")
                .modeAdd()
                .open();

        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) addBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Thêm ngày lễ thành công");
            loadPageData(paginationController.getCurrentPage(), false);
        }
    }

    private void handleEditBtn() {
        if (isNotSelectedHoliday()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn ngày lễ", AppMessages.DIALOG_TITLE);
            return;
        }

        HolidayModalController modalController = new ModalBuilder<HolidayModalController>(
                "/GUI/HolidayModal.fxml", HolidayModalController.class)
                .setTitle("Sửa ngày lễ")
                .modeEdit()
                .configure(c -> c.setHoliday(selectedHoliday.getId()))
                .open();

        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) editBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Cập nhật ngày lễ thành công");
            loadPageData(paginationController.getCurrentPage(), false);
        }
    }

    private void handleDeleteBtn() {
        if (isNotSelectedHoliday()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn ngày lễ", AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert("Bạn chắc muốn xóa ngày lễ này?", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        TaskUtil.executePublic(loadingOverlay,
                () -> HolidayBUS.getInstance().delete(selectedHoliday.getId()),
                result -> {
                    Stage currentStage = (Stage) deleteBtn.getScene().getWindow();
                    NotificationUtils.showToast(currentStage, result.getMessage());
                    loadPageData(0, false);
                });
    }

    // =====================
    // 4️⃣ FILTER & PERMISSION
    // =====================
    private void handleKeywordChange() {
        if (isResetting)
            return;

        String newKeyword = txtSearch.getText().trim();
        if (newKeyword.equals(keyword))
            return;

        keyword = newKeyword;
        applyFilters();
    }

    private void handleSearchByChange() {
        searchBy = cbSearchBy.getValue();
        applyFilters();
    }

    @Override
    public void applyFilters() {
        if (paginationController.getCurrentPage() == 0) {
            loadPageData(0, true);
        } else {
            paginationController.setCurrentPage(0);
        }
    }

    @Override
    public void resetFilters() {
        isResetting = true;

        cbSearchBy.getSelectionModel().selectFirst();
        txtSearch.clear();
        searchBy = "Mã ngày lễ";
        keyword = "";

        applyFilters();

        javafx.application.Platform.runLater(() -> isResetting = false);
    }

    @Override
    public void hideButtonWithoutPermission() {
        // boolean canView = session.hasPermission(PermissionKey.HOLIDAY_LIST_VIEW);

        // if (!canView) {
        // mainContent.setVisible(false);
        // mainContent.setManaged(false);
        // NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED,
        // AppMessages.DIALOG_TITLE);
        // return;
        // }

        // boolean canAdd = session.hasPermission(PermissionKey.HOLIDAY_INSERT);
        // boolean canEdit = session.hasPermission(PermissionKey.HOLIDAY_UPDATE);
        // boolean canDelete = session.hasPermission(PermissionKey.HOLIDAY_DELETE);

        // if (!canAdd)
        // UiUtils.gI().setVisibleItem(addBtn);
        // if (!canEdit)
        // UiUtils.gI().setVisibleItem(editBtn);
        // if (!canDelete)
        // UiUtils.gI().setVisibleItem(deleteBtn);
    }

    private boolean isNotSelectedHoliday() {
        selectedHoliday = tblHoliday.getSelectionModel().getSelectedItem();
        return selectedHoliday == null;
    }
}
