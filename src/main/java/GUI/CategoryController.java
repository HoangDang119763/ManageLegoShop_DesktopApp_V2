package GUI;

import BUS.CategoryBUS;
import BUS.StatusBUS;
import DTO.BUSResult;
import DTO.CategoryDTO;
import DTO.StatusDTO;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class CategoryController implements IController {
    @FXML
    private TableView<CategoryDTO> tblCategory;
    @FXML
    private TableColumn<CategoryDTO, Integer> tlb_col_id;
    @FXML
    private TableColumn<CategoryDTO, String> tlb_col_name;
    @FXML
    private TableColumn<CategoryDTO, String> tlb_col_status;
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
    @FXML
    private AnchorPane mainContent;

    private String searchBy = "Mã thể loại";
    private String keyword = "";
    private StatusDTO statusFilter = null;
    private CategoryDTO selectedCategory;
    private CategoryBUS categoryBUS;
    private SessionManagerService session;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        categoryBUS = CategoryBUS.getInstance();
        session = SessionManagerService.getInstance();

        tblCategory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblCategory.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        loadTable();
        setupListeners();
        applyFilters();
    }

    // =====================
    // 2️⃣ UI SETUP
    // =====================
    @Override
    public void loadTable() {
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(
                StatusBUS.getInstance().getById(cellData.getValue().getStatusId()).getDescription()));
    }

    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã thể loại", "Tên thể loại");

        StatusBUS statusBUS = StatusBUS.getInstance();
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus);
        cbStatusFilter.getItems().addAll(statusBUS.getAllByType(StatusType.CATEGORY));

        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
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
    }

    // =====================
    // 3️⃣ CRUD HANDLERS
    // =====================
    private void handleAddBtn() {
        CategoryModalController modalController = new ModalBuilder<CategoryModalController>(
                "/GUI/CategoryModal.fxml", CategoryModalController.class)
                .setTitle("Thêm thể loại")
                .modeAdd()
                .open();

        if (modalController != null && modalController.isSaved()) {
            applyFilters();
        }
    }

    private void handleEditBtn() {
        if (isNotSelectedCategory()) {
            NotificationUtils.showErrorAlert(AppMessages.CATEGORY_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        CategoryModalController modalController = new ModalBuilder<CategoryModalController>(
                "/GUI/CategoryModal.fxml", CategoryModalController.class)
                .setTitle("Sửa thể loại")
                .modeEdit()
                .configure(c -> c.setCategory(selectedCategory))
                .open();

        if (modalController != null && modalController.isSaved()) {
            applyFilters();
        }
    }

    private void handleDeleteBtn() {
        if (isNotSelectedCategory()) {
            NotificationUtils.showErrorAlert(AppMessages.CATEGORY_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert(AppMessages.CATEGORY_DELETE_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        // Thực thi xóa qua SecureExecutor để đồng bộ logic với BUSResult
        BUSResult deleteResult = SecureExecutor.executeSafeBusResult(
                PermissionKey.CATEGORY_DELETE,
                () -> categoryBUS.delete(selectedCategory.getId()));

        if (deleteResult.isSuccess()) {
            NotificationUtils.showInfoAlert(deleteResult.getMessage(), AppMessages.DIALOG_TITLE);
            applyFilters();
        } else {
            NotificationUtils.showErrorAlert(deleteResult.getMessage(), AppMessages.DIALOG_TITLE);
        }
    }

    // =====================
    // 4️⃣ FILTER & PERMISSION
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

    @Override
    public void applyFilters() {
        int statusId = (statusFilter == null) ? -1 : statusFilter.getId();
        // tblCategory.setItems(FXCollections.observableArrayList(
        // categoryBUS.filterCategories(searchBy, keyword, statusId)));
        tblCategory.getSelectionModel().clearSelection();
    }

    @Override
    public void resetFilters() {
        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
        txtSearch.clear();
        searchBy = "Mã thể loại";
        keyword = "";
        statusFilter = null;
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        boolean canView = session.hasPermission(PermissionKey.CATEGORY_LIST_VIEW);

        if (!canView) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }

        boolean canAdd = session.hasPermission(PermissionKey.CATEGORY_INSERT);
        boolean canEdit = session.hasPermission(PermissionKey.CATEGORY_UPDATE);
        boolean canDelete = session.hasPermission(PermissionKey.CATEGORY_DELETE);

        if (!canAdd)
            UiUtils.gI().setReadOnlyItem(addBtn);
        if (!canEdit)
            UiUtils.gI().setReadOnlyItem(editBtn);
        if (!canDelete)
            UiUtils.gI().setReadOnlyItem(deleteBtn);
    }

    private boolean isNotSelectedCategory() {
        selectedCategory = tblCategory.getSelectionModel().getSelectedItem();
        return selectedCategory == null;
    }
}