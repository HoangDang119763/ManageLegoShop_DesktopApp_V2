package GUI;

import java.util.ArrayList;

import BUS.CategoryBUS;
import BUS.StatusBUS;
import DTO.CategoryDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
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

    // Filter state
    private String searchBy = "Mã thể loại";
    private String keyword = "";
    private StatusDTO statusFilter = null;
    private CategoryDTO selectedCategory;

    @FXML
    public void initialize() {
        CategoryBUS categoryBUS = CategoryBUS.getInstance();
        if (categoryBUS.isLocalEmpty())
            categoryBUS.loadLocal();
        tblCategory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        // Tránh deprecated
        Platform.runLater(() -> tblCategory.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        setupListeners();

        loadTable();
        applyFilters();
    }

    @Override
    public void loadTable() {
        StatusBUS statusBUS = StatusBUS.getInstance();
        tlb_col_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tlb_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tlb_col_status.setCellValueFactory(cellData -> new SimpleStringProperty(statusBUS
                .getByIdLocal(cellData.getValue().getStatusId()).getDescription()));
    }

    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã thể loại", "Tên thể loại");
        // Load Status ComboBox
        StatusBUS statusBUS = StatusBUS.getInstance();
        ArrayList<StatusDTO> statusList = statusBUS.getAllByTypeLocal(StatusType.PRODUCT);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả cả trạng thái");
        cbStatusFilter.getItems().add(allStatus); // "Tất cả" option
        cbStatusFilter.getItems().addAll(statusList);
        // default selection
        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst(); // "Tất cả"
    }

    @Override
    public void setupListeners() {
        // Search and Filters Controls
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

    private void handleStatusFilterChange() {
        statusFilter = cbStatusFilter.getValue();
        applyFilters();
    }

    private void handleSearchByChange() {
        searchBy = cbSearchBy.getValue();
        applyFilters();
    }

    private void handleKeywordChange() {
        keyword = txtSearch.getText().trim();
        applyFilters();
    }

    @Override
    public void applyFilters() {
        int statusId = statusFilter == null ? -1 : statusFilter.getId();
        tblCategory.setItems(FXCollections.observableArrayList(
                CategoryBUS.getInstance().filterCategories(searchBy, keyword, statusId)));
        tblCategory.getSelectionModel().clearSelection();
    }

    @Override
    public void resetFilters() {
        cbSearchBy.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst(); // "Tất cả"
        txtSearch.clear();

        searchBy = "Mã thể loại";
        keyword = "";
        statusFilter = null;
        applyFilters();

    }

    @Override
    public void hideButtonWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();
        boolean canAdd = session.hasPermission(PermissionKey.CATEGORY_INSERT);
        boolean canEdit = session.hasPermission(PermissionKey.CATEGORY_UPDATE);
        boolean canDelete = session.hasPermission(PermissionKey.CATEGORY_DELETE);

        if (!canAdd)
            functionBtns.getChildren().remove(addBtn);
        if (!canEdit)
            functionBtns.getChildren().remove(editBtn);
        if (!canDelete)
            functionBtns.getChildren().remove(deleteBtn);
    }

    private void handleAddBtn() {
        CategoryModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/CategoryModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm thể loại");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert(AppMessages.GENERAL_REFRESH_SUCCESS, AppMessages.DIALOG_TITLE);
            applyFilters();
        }
    }

    private void handleDeleteBtn() {
        // Get selected category
        selectedCategory = tblCategory.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        if (selectedCategory.getId() == 1) {
            NotificationUtils.showErrorAlert(AppMessages.CATEGORY_CANNOT_DELETE_DEFAULT, AppMessages.DIALOG_TITLE);
            return;
        }

        CategoryBUS categoryBUS = CategoryBUS.getInstance();
        SessionManagerService session = SessionManagerService.getInstance();
        int deleteResult = categoryBUS.delete(
                selectedCategory.getId(),
                session.employeeRoleId(),
                session.employeeLoginId());

        switch (deleteResult) {
            case 1 -> {
                NotificationUtils.showInfoAlert(AppMessages.GENERAL_REFRESH_SUCCESS, AppMessages.DIALOG_TITLE);
                applyFilters();
            }
            case 2 -> NotificationUtils.showErrorAlert(AppMessages.CATEGORY_DELETE_ERROR, AppMessages.DIALOG_TITLE);
            case 3 ->
                NotificationUtils.showErrorAlert(AppMessages.CATEGORY_CANNOT_DELETE_DEFAULT, AppMessages.DIALOG_TITLE);
            case 4 ->
                NotificationUtils.showErrorAlert(AppMessages.CATEGORY_DELETE_NO_PERMISSION, AppMessages.DIALOG_TITLE);
            case 5 -> NotificationUtils.showErrorAlert(AppMessages.CATEGORY_NOT_FOUND, AppMessages.DIALOG_TITLE);
            case 6 -> NotificationUtils.showErrorAlert(AppMessages.CATEGORY_DELETE_DB_ERROR, AppMessages.DIALOG_TITLE);
            default -> NotificationUtils.showErrorAlert(AppMessages.GENERAL_ERROR, AppMessages.DIALOG_TITLE);
        }
    }

    private void handleEditBtn() {
        // Get selected category
        selectedCategory = tblCategory.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            NotificationUtils.showErrorAlert(AppMessages.PRODUCT_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }

        if (selectedCategory.getId() == 1) {
            NotificationUtils.showErrorAlert(AppMessages.CATEGORY_CANNOT_UPDATE_DEFAULT, AppMessages.DIALOG_TITLE);
            return;
        }

        CategoryModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/CategoryModal.fxml",
                controller -> {
                    controller.setTypeModal(1);
                    controller.setCategory(selectedCategory);
                },
                "Sửa thể loại");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert(AppMessages.GENERAL_REFRESH_SUCCESS, AppMessages.DIALOG_TITLE);
            applyFilters();
        }
    }
}
