package GUI;

import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.EmploymentHistoryBUS;
import BUS.RoleBUS;
import BUS.SalaryBUS;
import BUS.StatusBUS;
import BUS.TaxBUS;
import DTO.EmployeeDisplayDTO;
import DTO.PagedResponse;
import DTO.RoleDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.ExcelService;
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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class EmployeeController implements IController {
    @FXML
    private TableView<EmployeeDisplayDTO> tblEmployee;
    @FXML
    private TableColumn<EmployeeDisplayDTO, Integer> tlb_col_employeeId;
    @FXML
    private TableColumn<EmployeeDisplayDTO, String> tlb_col_fullName;
    @FXML
    private TableColumn<EmployeeDisplayDTO, String> tlb_col_gender;
    @FXML
    private TableColumn<EmployeeDisplayDTO, String> tlb_col_role;
    @FXML
    private TableColumn<EmployeeDisplayDTO, String> tlb_col_baseSalary;
    @FXML
    private TableColumn<EmployeeDisplayDTO, String> tlb_col_salaryCoefficient;
    @FXML
    private TableColumn<EmployeeDisplayDTO, String> tlb_col_username;
    @FXML
    private TableColumn<EmployeeDisplayDTO, String> tlb_col_status;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn, exportExcel;
    @FXML
    private AnchorPane mainContent;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<RoleDTO> cbRoleFilter;
    @FXML
    private ComboBox<StatusDTO> cbStatusFilter;
    @FXML
    private PaginationController paginationController;
    @FXML
    private StackPane loadingOverlay;

    private String keyword = "";
    private RoleDTO roleFilter = null;
    private StatusDTO statusFilter = null;
    private EmployeeDisplayDTO selectedEmployeeTable;
    private boolean isResetting = false;

    private static final int PAGE_SIZE = 14;

    // BUS instances
    private EmployeeBUS employeeBUS;
    private RoleBUS roleBUS;
    private StatusBUS statusBUS;
    private SalaryBUS salaryBUS;
    private TaxBUS taxBUS;
    private DepartmentBUS departmentBUS;
    private EmploymentHistoryBUS employmentHistoryBUS;

    @FXML
    public void initialize() {
        // Gán BUS instance một lần
        employeeBUS = EmployeeBUS.getInstance();
        roleBUS = RoleBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        salaryBUS = SalaryBUS.getInstance();
        taxBUS = TaxBUS.getInstance();
        departmentBUS = DepartmentBUS.getInstance();
        employmentHistoryBUS = EmploymentHistoryBUS.getInstance();

        tblEmployee.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblEmployee.getSelectionModel().clearSelection());

        loadComboBox();
        setupListeners();
        hideButtonWithoutPermission();
        loadTable();
        setupPagination();
        applyFilters();
    }

    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_employeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        tlb_col_fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        tlb_col_baseSalary.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getSalary())));
        tlb_col_salaryCoefficient.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getEfficientSalary())));
        tlb_col_username.setCellValueFactory(new PropertyValueFactory<>("username"));
        tlb_col_role.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        tlb_col_gender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        tlb_col_status.setCellValueFactory(new PropertyValueFactory<>("statusDescription"));
        UiUtils.gI().addTooltipToColumn(tlb_col_username, 10);
        UiUtils.gI().addTooltipToColumn(tlb_col_fullName, 15);
        UiUtils.gI().addTooltipToColumn(tlb_col_role, 13);
    }

    private void loadComboBox() {
        // Load Status ComboBox
        ArrayList<StatusDTO> statusList = statusBUS.getAllByType(StatusType.EMPLOYEE);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus); // "Tất cả" option
        cbStatusFilter.getItems().addAll(statusList);

        // Load Role ComboBox
        ArrayList<RoleDTO> roleList = roleBUS.getAll();
        RoleDTO allRole = new RoleDTO(-1, "Tất cả chức vụ");
        cbRoleFilter.getItems().add(allRole); // "Tất cả" option
        cbRoleFilter.getItems().addAll(roleList);
        cbRoleFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @Override
    public void setupListeners() {
        cbRoleFilter.setOnAction(event -> handleRoleFilterChange());
        cbStatusFilter.setOnAction(event -> handleStatusFilterChange());
        UiUtils.gI().applySearchDebounce(txtSearch, 500, () -> handleKeywordChange());

        refreshBtn.setOnAction(event -> {
            resetFilters();
            Stage currentStage = (Stage) refreshBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, "Làm mới thành công");
        });

        addBtn.setOnAction(event -> handleAddBtn());
        deleteBtn.setOnAction(e -> handleDeleteBtn());
        editBtn.setOnAction(e -> handleEditBtn());
        exportExcel.setOnAction(e -> {
            try {
                handleExportExcel();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
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

    private void handleRoleFilterChange() {
        roleFilter = cbRoleFilter.getValue();
        applyFilters();
    }

    private void handleStatusFilterChange() {
        statusFilter = cbStatusFilter.getValue();
        applyFilters();
    }

    @Override
    public void applyFilters() {
        if (paginationController != null && paginationController.getCurrentPage() == 0) {
            loadPageData(0, true);
        } else if (paginationController != null) {
            paginationController.setCurrentPage(0);
        }
    }

    @Override
    public void resetFilters() {
        isResetting = true;

        cbRoleFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
        txtSearch.clear();
        keyword = "";
        roleFilter = null;
        statusFilter = null;

        applyFilters();

        javafx.application.Platform.runLater(() -> isResetting = false);
    }

    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex, true);
        });
    }

    private void loadPageData(int pageIndex, boolean showOverlay) {
        int statusId = statusFilter == null ? -1 : statusFilter.getId();
        int roleId = roleFilter == null ? -1 : roleFilter.getId();

        StackPane overlay = showOverlay ? loadingOverlay : null;
        TaskUtil.executeSecure(overlay, PermissionKey.EMPLOYEE_LIST_VIEW,
                () -> employeeBUS.filterEmployeesPagedForManageDisplay(keyword, roleId, statusId, pageIndex,
                        PAGE_SIZE),
                result -> {
                    PagedResponse<EmployeeDisplayDTO> res = result.getPagedData();
                    tblEmployee.setItems(FXCollections.observableArrayList(res.getItems()));

                    // Cập nhật tổng số trang dựa trên COUNT(*) từ DB
                    int totalPages = (int) Math.ceil((double) res.getTotalItems() / PAGE_SIZE);
                    paginationController.setPageCount(totalPages > 0 ? totalPages : 1);

                    tblEmployee.getSelectionModel().clearSelection();
                });
    }

    @Override
    public void hideButtonWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();
        boolean canView = session.hasPermission(PermissionKey.PRODUCT_LIST_VIEW);

        if (!canView) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }
        boolean canAdd = session.hasPermission(PermissionKey.EMPLOYEE_INSERT);
        boolean canViewPersonal = session.hasPermission(PermissionKey.EMPLOYEE_PERSONAL_VIEW);
        boolean canUpdatePersonal = session.hasPermission(PermissionKey.EMPLOYEE_PERSONAL_UPDATE);
        boolean canViewJob = session.hasPermission(PermissionKey.EMPLOYEE_JOB_VIEW);
        boolean canUpdateJob = session.hasPermission(PermissionKey.EMPLOYEE_JOB_UPDATE);
        boolean canViewPayrollInfo = session.hasPermission(PermissionKey.EMPLOYEE_PAYROLLINFO_VIEW);
        boolean canUpdatePayrollInfo = session.hasPermission(PermissionKey.EMPLOYEE_PAYROLLINFO_UPDATE);
        boolean canviewAccount = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_VIEW);
        boolean canResetAccountPassword = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_RESET_PASSWORD);
        boolean canUpdateAccountStatus = session.hasPermission(PermissionKey.EMPLOYEE_ACCOUNT_UPDATE_STATUS);
        boolean canDelete = session.hasPermission(PermissionKey.EMPLOYEE_DELETE);
        boolean canViewDetail = canViewPersonal || canViewJob || canViewPayrollInfo || canviewAccount;
        boolean canEdit = canUpdatePersonal || canUpdateJob || canUpdatePayrollInfo || canUpdateAccountStatus
                || canResetAccountPassword;

        if (canViewDetail)
            tblEmployee.setRowFactory(tv -> {
                TableRow<EmployeeDisplayDTO> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        selectedEmployeeTable = row.getItem();
                        handleDetailView();
                    }
                });
                return row;
            });

        if (!canAdd)
            UiUtils.gI().setVisibleItem(addBtn);
        if (!canEdit)
            UiUtils.gI().setVisibleItem(editBtn);
        if (!canDelete)
            UiUtils.gI().setVisibleItem(deleteBtn);
    }

    public void handleAddBtn() {
        EmployeeAddModalController modalController = new ModalBuilder<EmployeeAddModalController>(
                "/GUI/EmployeeAddModal.fxml",
                EmployeeAddModalController.class)
                .open();
        if (modalController != null && modalController.isSaved()) {
            Stage currentStage = (Stage) addBtn.getScene().getWindow();
            NotificationUtils.showToast(currentStage, modalController.getResultMessage());
            loadPageData(paginationController.getCurrentPage(), false);
        }
    }

    public void handleDeleteBtn() {
        if (isNotSelectedEmployee()) {
            NotificationUtils.showErrorAlert(AppMessages.EMPLOYEE_NO_SELECTION,
                    AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert(AppMessages.EMPLOYEE_DELETE_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_DELETE,
                () -> EmployeeBUS.getInstance().delete(selectedEmployeeTable.getEmployeeId()),
                result -> {
                    Stage currentStage = (Stage) deleteBtn.getScene().getWindow();
                    NotificationUtils.showToast(currentStage, result.getMessage());
                    loadPageData(paginationController.getCurrentPage(), false);
                });

    }

    public void handleEditBtn() {
        if (isNotSelectedEmployee()) {
            NotificationUtils.showErrorAlert(AppMessages.EMPLOYEE_NO_SELECTION,
                    AppMessages.DIALOG_TITLE);
            return;
        }
        EmployeeModalController modalController = new ModalBuilder<EmployeeModalController>(
                "/GUI/EmployeeModal.fxml",
                EmployeeModalController.class)
                .modeEdit()
                .configure(c -> c.setData(selectedEmployeeTable.getEmployeeId()))
                .open();
        if (modalController != null) {
            loadPageData(paginationController.getCurrentPage(), false);
        }
    }

    private void handleDetailView() {
        new ModalBuilder<EmployeeModalController>("/GUI/EmployeeModal.fxml",
                EmployeeModalController.class)
                .setTitle("Xem thông tin nhân viên")
                .modeDetail()
                .configure(c -> c.setData(selectedEmployeeTable.getEmployeeId()))
                .open();

    }

    private void handleExportExcel() throws IOException {
        // try {
        // ExcelService.getInstance().exportToFileExcel("employee");
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    private boolean isNotSelectedEmployee() {
        selectedEmployeeTable = tblEmployee.getSelectionModel().getSelectedItem();
        return selectedEmployeeTable == null;
    }
}