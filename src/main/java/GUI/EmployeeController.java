package GUI;

import BUS.EmployeeBUS;
import BUS.RoleBUS;
import BUS.SalaryBUS;
import BUS.StatusBUS;
import BUS.TaxBUS;
import DTO.EmployeeDTO;
import DTO.EmployeeDetailDTO;
import DTO.RoleDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import PROVIDER.EmployeeViewProvider;
import PROVIDER.EmployeeDetailFilterer;
import SERVICE.ExcelService;
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

import java.io.IOException;
import java.util.ArrayList;

public class EmployeeController implements IController {
    @FXML
    private TableView<EmployeeDetailDTO> tblEmployee;
    @FXML
    private TableColumn<EmployeeDetailDTO, Integer> tlb_col_employeeId;
    @FXML
    private TableColumn<EmployeeDetailDTO, String> tlb_col_fullName;
    @FXML
    private TableColumn<EmployeeDetailDTO, String> tlb_col_gender;
    @FXML
    private TableColumn<EmployeeDetailDTO, String> tlb_col_email;
    @FXML
    private TableColumn<EmployeeDetailDTO, String> tlb_col_baseSalary;
    @FXML
    private TableColumn<EmployeeDetailDTO, String> tlb_col_salaryCoefficient;
    @FXML
    private TableColumn<EmployeeDetailDTO, String> tlb_col_username;
    @FXML
    private TableColumn<EmployeeDetailDTO, String> tlb_col_status;
    @FXML
    private HBox functionBtns;
    @FXML
    private Button addBtn, editBtn, deleteBtn, refreshBtn, exportExcel;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbSearchBy;
    @FXML
    private ComboBox<RoleDTO> cbRoleFilter;
    @FXML
    private ComboBox<StatusDTO> cbStatusFilter;

    private String searchBy = "Mã nhân viên";
    private String keyword = "";
    private RoleDTO roleFilter = null;
    private StatusDTO statusFilter = null;
    private ArrayList<EmployeeDetailDTO> cachedTableData = new ArrayList<>();
    private EmployeeDetailDTO selectedEmployeeTable;

    // BUS instances
    private EmployeeBUS employeeBUS;
    private RoleBUS roleBUS;
    private StatusBUS statusBUS;
    private SalaryBUS salaryBUS;
    private TaxBUS taxBUS;

    @FXML
    public void initialize() {
        // Gán BUS instance một lần
        employeeBUS = EmployeeBUS.getInstance();
        roleBUS = RoleBUS.getInstance();
        statusBUS = StatusBUS.getInstance();
        salaryBUS = SalaryBUS.getInstance();
        taxBUS = TaxBUS.getInstance();

        if (employeeBUS.isLocalEmpty())
            employeeBUS.loadLocal();
        if (roleBUS.isLocalEmpty())
            roleBUS.loadLocal();
        if (statusBUS.isLocalEmpty())
            statusBUS.loadLocal();
        if (salaryBUS.isLocalEmpty())
            salaryBUS.loadLocal();
        if (taxBUS.isLocalEmpty())
            taxBUS.loadLocal();

        tblEmployee.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblEmployee.getSelectionModel().clearSelection());

        loadComboBox();
        setupListeners();
        hideButtonWithoutPermission();
        loadTable();
        applyFilters();
    }

    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_employeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        tlb_col_fullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        tlb_col_baseSalary.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getBaseSalary())));
        tlb_col_salaryCoefficient.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getSalaryCoefficient())));
        tlb_col_username.setCellValueFactory(new PropertyValueFactory<>("username"));
        tlb_col_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        tlb_col_gender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        tlb_col_status.setCellValueFactory(new PropertyValueFactory<>("statusDescription"));
    }

    private void loadComboBox() {
        cbSearchBy.getItems().addAll("Mã nhân viên", "Họ tên", "SDT", "Email");

        // Load Status ComboBox
        ArrayList<StatusDTO> statusList = statusBUS.getAllByTypeLocal(StatusType.EMPLOYEE);
        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbStatusFilter.getItems().add(allStatus); // "Tất cả" option
        cbStatusFilter.getItems().addAll(statusList);

        // Load Role ComboBox
        ArrayList<RoleDTO> roleList = roleBUS.getAllLocal();
        RoleDTO allRole = new RoleDTO(-1, "Tất cả chức vụ");
        cbRoleFilter.getItems().add(allRole); // "Tất cả" option
        cbRoleFilter.getItems().addAll(roleList);

        cbSearchBy.getSelectionModel().selectFirst();
        cbRoleFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @Override
    public void setupListeners() {
        cbSearchBy.setOnAction(event -> handleSearchByChange());
        cbRoleFilter.setOnAction(event -> handleRoleFilterChange());
        cbStatusFilter.setOnAction(event -> handleStatusFilterChange());
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> handleKeywordChange());

        tblEmployee.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleDetailView();
            }
        });

        refreshBtn.setOnAction(event -> {
            resetFilters();
            NotificationUtils.showInfoAlert("Làm mới thành công", AppMessages.DIALOG_TITLE);
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

    private void handleSearchByChange() {
        searchBy = cbSearchBy.getValue();
        applyFilters();
    }

    private void handleKeywordChange() {
        keyword = txtSearch.getText().trim();
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
        EmployeeViewProvider provider = EmployeeViewProvider.getInstance();
        EmployeeDetailFilterer filterer = EmployeeDetailFilterer.getInstance();

        String roleFilterName = roleFilter != null ? roleFilter.getName() : "";
        String statusFilterDesc = statusFilter != null ? statusFilter.getDescription() : "";

        // Step 1: Transform employee data using provider
        ArrayList<EmployeeDetailDTO> tableData = provider.toTableDTOs(employeeBUS.getAllLocal());

        // Step 2: Apply filters using dedicated filterer
        cachedTableData = filterer.applyAllFilters(
                tableData,
                searchBy,
                keyword,
                roleFilterName,
                statusFilterDesc);

        tblEmployee.setItems(FXCollections.observableArrayList(cachedTableData));
        tblEmployee.getSelectionModel().clearSelection();
    }

    @Override
    public void resetFilters() {
        cbSearchBy.getSelectionModel().selectFirst();
        cbRoleFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
        txtSearch.clear();

        searchBy = "Mã nhân viên";
        keyword = "";
        roleFilter = null;
        statusFilter = null;
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        SessionManagerService session = SessionManagerService.getInstance();
        boolean canAdd = session.hasPermission(PermissionKey.EMPLOYEE_INSERT);
        // boolean canEdit = session.hasPermission(PermissionKey.EMPLOYEE_UPDATE);
        boolean canDelete = session.hasPermission(PermissionKey.EMPLOYEE_DELETE);

        if (!canAdd)
            functionBtns.getChildren().remove(addBtn);
        // if (!canEdit)
        // functionBtns.getChildren().remove(editBtn);
        if (!canDelete)
            functionBtns.getChildren().remove(deleteBtn);
    }

    public void handleAddBtn() {
        EmployeeModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/EmployeeModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm nhân viên");
        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert("Thêm nhân viên thành công", AppMessages.DIALOG_TITLE);
            resetFilters();
        }
    }

    public void handleDeleteBtn() {
//        if (isNotSelectedEmployee()) {
//            NotificationUtils.showErrorAlert("Vui lòng chọn nhân viên", AppMessages.DIALOG_TITLE);
//            return;
//        }
//
//        // Lấy thông tin đầy đủ của nhân viên
//        EmployeeDTO employee = employeeBUS.getByIdLocal(selectedEmployeeTable.getEmployeeId());
//
//        if (employee == null) {
//            NotificationUtils.showErrorAlert("Không tìm thấy thông tin nhân viên", AppMessages.DIALOG_TITLE);
//            return;
//        }
//
//        SessionManagerService session = SessionManagerService.getInstance();
//        if (employee.getId() == session.employeeLoginId()) {
//            NotificationUtils.showErrorAlert("Bạn không thể xóa thông tin của chính mình", AppMessages.DIALOG_TITLE);
//            return;
//        }
//
//        if (employee.getId() == 1) {
//            NotificationUtils.showErrorAlert("Không thể xóa nhân viên gốc", AppMessages.DIALOG_TITLE);
//            return;
//        }
//
//        int deleteResult = employeeBUS.delete(employee.getId(), session.employeeRoleId(), session.employeeLoginId());
//
//        switch (deleteResult) {
//            case 1 -> {
//                NotificationUtils.showInfoAlert("Xóa nhân viên thành công", AppMessages.DIALOG_TITLE);
//                resetFilters();
//            }
//            case 2 -> NotificationUtils.showErrorAlert("Có lỗi khi xóa nhân viên", AppMessages.DIALOG_TITLE);
//            case 3 ->
//                NotificationUtils.showErrorAlert("Không thể xóa thông tin của chính mình", AppMessages.DIALOG_TITLE);
//            case 4 -> NotificationUtils.showErrorAlert("Bạn không có quyền xóa nhân viên", AppMessages.DIALOG_TITLE);
//            case 5 ->
//                NotificationUtils.showErrorAlert("Bạn không thể xóa nhân viên ngang quyền", AppMessages.DIALOG_TITLE);
//            case 6 -> NotificationUtils.showErrorAlert("Xóa nhân viên thất bại", AppMessages.DIALOG_TITLE);
//            case 7 ->
//                NotificationUtils.showErrorAlert("Nhân viên không hợp lệ hoặc đã bị xóa", AppMessages.DIALOG_TITLE);
//            case 8 -> NotificationUtils.showErrorAlert("Không thể xóa nhân viên gốc", AppMessages.DIALOG_TITLE);
//            default -> NotificationUtils.showErrorAlert("Lỗi không xác định", AppMessages.DIALOG_TITLE);
//        }
    }

    public void handleEditBtn() {
        if (isNotSelectedEmployee()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn nhân viên", AppMessages.DIALOG_TITLE);
            return;
        }

        EmployeeDTO employee = employeeBUS.getByIdLocal(selectedEmployeeTable.getEmployeeId());

        if (employee == null) {
            NotificationUtils.showErrorAlert("Không tìm thấy thông tin nhân viên", AppMessages.DIALOG_TITLE);
            return;
        }

        SessionManagerService session = SessionManagerService.getInstance();
        if (employee.getId() == 1 && session.employeeLoginId() != 1) {
            NotificationUtils.showErrorAlert("Không thể sửa nhân viên gốc", AppMessages.DIALOG_TITLE);
            return;
        }

        EmployeeModalController modalController = UiUtils.gI().openStageWithController(
                "/GUI/EmployeeModal.fxml",
                controller -> {
                    controller.setTypeModal(1);
                    controller.setEmployee(employee);
                },
                "Sửa nhân viên");

        if (modalController != null && modalController.isSaved()) {
            NotificationUtils.showInfoAlert("Sửa nhân viên thành công", AppMessages.DIALOG_TITLE);
            applyFilters();
        }
    }

    private void handleDetailView() {
        // selectedEmployeeTable = tblEmployee.getSelectionModel().getSelectedItem();
        // if (selectedEmployeeTable != null) {
        // EmployeeBUS employeeBUS = EmployeeBUS.getInstance();
        // EmployeeDTO employee =
        // employeeBUS.getByIdLocal(selectedEmployeeTable.getEmployeeId());

        // if (employee != null) {
        // UiUtils.gI().openStageWithController(
        // "/GUI/EmployeeModal.fxml",
        // controller -> {
        // controller.setTypeModal(2);
        // controller.setEmployee(employee);
        // },
        // "Xem thông tin nhân viên");
        // }
        // }
    }

    private void handleExportExcel() throws IOException {
        try {
            ExcelService.getInstance().exportToFileExcel("employee");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNotSelectedEmployee() {
        selectedEmployeeTable = tblEmployee.getSelectionModel().getSelectedItem();
        return selectedEmployeeTable == null;
    }
}