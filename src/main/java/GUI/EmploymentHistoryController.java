package GUI;

import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.EmploymentHistoryBUS;
import BUS.PositionBUS;
import BUS.StatusBUS;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.EmploymentHistoryDTO;
import DTO.EmploymentHistoryDisplayDTO;
import DTO.PagedResponse;
import DTO.PositionDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.Status;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class EmploymentHistoryController implements IController {

    // =====================
    // TABLE & COLUMNS
    // =====================
    @FXML
    private TableView<EmploymentHistoryDisplayDTO> tblHistory;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, Integer> colId;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colEmployeeName;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colDeptName;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colPosName;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colEffectiveDate;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colApproverName;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colStatus;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colReason;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colCreatedAt;

    // =====================
    // SEARCH & FILTERS
    // =====================
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<DepartmentDTO> cbFilterDept;
    @FXML
    private ComboBox<PositionDTO> cbFilterPos;
    @FXML
    private ComboBox<StatusDTO> cbFilterStatus;
    @FXML
    private Button btnClearFilter;

    // =====================
    // FORM FIELDS
    // =====================
    @FXML
    private ComboBox<EmployeeDTO> cbEmployee;
    @FXML
    private ComboBox<DepartmentDTO> cbDepartment;
    @FXML
    private ComboBox<PositionDTO> cbPosition;
    @FXML
    private DatePicker dpEffectiveDate;
    @FXML
    private TextField txtCreator;
    @FXML
    private TextField txtId;
    @FXML
    private TextArea txtReason;

    // =====================
    // BUTTONS
    // =====================
    @FXML
    private Button btnNew;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnApprove;
    @FXML
    private Button btnReject;
    @FXML
    private Button btnSync;

    // =====================
    // OTHER UI
    // =====================
    @FXML
    private AnchorPane mainContent;
    @FXML
    private PaginationController paginationController;
    @FXML
    private StackPane loadingOverlay;

    // =====================
    // STATE VARIABLES
    // =====================
    private String keyword = "";
    private Integer filterEmployeeId = null;
    private Integer filterDepartmentId = null;
    private Integer filterPositionId = null;
    private Integer filterStatusId = null;
    private boolean isResetting = false;

    private EmploymentHistoryDisplayDTO selectedHistory;
    private EmploymentHistoryBUS employmentHistoryBUS;
    private SessionManagerService session;
    private final int PAGE_SIZE = 15;

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        employmentHistoryBUS = EmploymentHistoryBUS.getInstance();
        session = SessionManagerService.getInstance();
        Platform.runLater(() -> tblHistory.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        loadTable();
        setupPagination();
        setupListeners();

        // Configure DatePicker once (only month/year selection from next month)
        configureDatePickerForMonthYearOnly();

        // Set default effective date to first day of next month
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1).withDayOfMonth(1);
        dpEffectiveDate.setValue(nextMonth);

        applyFilters();

        // Set next ID
        txtId.setText(String.valueOf(employmentHistoryBUS.nextId()));
        // Set creator name
        txtCreator.setText(session.getLoggedName());
    }

    // =====================
    // 2️⃣ UI SETUP
    // =====================
    @Override
    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployeeName.setCellValueFactory(cellData -> {
            EmploymentHistoryDisplayDTO dto = cellData.getValue();
            if (dto != null) {
                // Kết hợp ID và Tên theo format "ID - FullName"
                return new SimpleStringProperty(dto.getEmployeeId() + " - " + dto.getEmployeeName());
            }
            return new SimpleStringProperty("");
        });
        colDeptName.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colPosName.setCellValueFactory(new PropertyValueFactory<>("positionName"));

        // Format date columns
        colEffectiveDate.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatDateTime(cellData.getValue().getEffectiveDate())));

        colApproverName.setCellValueFactory(cellData -> {
            EmploymentHistoryDisplayDTO dto = cellData.getValue();
            if (dto != null) {
                // Kết hợp ID và Tên theo format "ID - FullName"
                return new SimpleStringProperty(dto.getApproverId() + " - " + dto.getApproverName());
            }
            return new SimpleStringProperty("");
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));

        colCreatedAt.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatDateTimeWithHour(cellData.getValue().getCreatedAt())));
        UiUtils.gI().addTooltipToColumn(colEmployeeName, 15);
        UiUtils.gI().addTooltipToColumn(colDeptName, 20);
        UiUtils.gI().addTooltipToColumn(colPosName, 20);
        UiUtils.gI().addTooltipToColumn(colStatus, 20);
        UiUtils.gI().addTooltipToColumn(colReason, 20);
        UiUtils.gI().addTooltipToColumn(colApproverName, 20);
    }

    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex, true);
        });
    }

    private void loadPageData(int pageIndex, boolean showOverlay) {
        // 1. Thu thập tham số lọc từ UI
        String keyword = txtSearch.getText().trim();
        Integer deptId = (cbFilterDept.getValue() == null || cbFilterDept.getValue().getId() == -1) ? null
                : cbFilterDept.getValue().getId();
        Integer posId = (cbFilterPos.getValue() == null || cbFilterPos.getValue().getId() == -1) ? null
                : cbFilterPos.getValue().getId();
        Integer statusId = (cbFilterStatus.getValue() == null || cbFilterStatus.getValue().getId() == -1) ? null
                : cbFilterStatus.getValue().getId();

        StackPane overlay = showOverlay ? loadingOverlay : null;

        // 2. Chạy tác vụ ngầm với TaskUtil
        TaskUtil.executeSecure(overlay, PermissionKey.EMPLOYMENT_HISTORY_LIST_VIEW,
                () -> employmentHistoryBUS.filterEmploymentHistoryPagedForManageDisplay(
                        keyword, null, deptId, posId, statusId, pageIndex, PAGE_SIZE),
                result -> {
                    // 3. Lấy PagedResponse
                    PagedResponse<EmploymentHistoryDisplayDTO> res = result.getPagedData();

                    // 4. Cập nhật dữ liệu vào bảng
                    tblHistory.setItems(FXCollections.observableArrayList(res.getItems()));

                    // 5. Đồng bộ hóa bộ phân trang
                    int totalPages = (int) Math.ceil((double) res.getTotalItems() / PAGE_SIZE);
                    paginationController.setPageCount(totalPages > 0 ? totalPages : 1);

                    // 6. Dọn dẹp trạng thái chọn dòng
                    tblHistory.getSelectionModel().clearSelection();
                });
    }

    private void loadComboBox() {
        // Load filter comboboxes
        EmployeeDTO allEmployees = new EmployeeDTO();
        allEmployees.setId(-1);
        allEmployees.setFirstName("Tất cả");
        allEmployees.setLastName("");

        DepartmentDTO allDepts = new DepartmentDTO();
        allDepts.setId(-1);
        allDepts.setName("Tất cả");
        cbFilterDept.getItems().add(allDepts);
        cbFilterDept.getItems().addAll(DepartmentBUS.getInstance().getAll());
        cbFilterDept.setConverter(new javafx.util.StringConverter<DepartmentDTO>() {
            @Override
            public String toString(DepartmentDTO dept) {
                return dept == null ? "" : dept.getId() == -1 ? "Tất cả phòng ban" : dept.getName();
            }

            @Override
            public DepartmentDTO fromString(String string) {
                return null;
            }
        });
        cbFilterDept.getSelectionModel().selectFirst();

        PositionDTO allPos = new PositionDTO();
        allPos.setId(-1);
        allPos.setName("Tất cả");
        cbFilterPos.getItems().add(allPos);
        cbFilterPos.getItems().addAll(PositionBUS.getInstance().getAll());
        cbFilterPos.setConverter(new javafx.util.StringConverter<PositionDTO>() {
            @Override
            public String toString(PositionDTO pos) {
                return pos == null ? "" : pos.getId() == -1 ? "Tất cả vị trí" : pos.getName();
            }

            @Override
            public PositionDTO fromString(String string) {
                return null;
            }
        });
        cbFilterPos.getSelectionModel().selectFirst();

        StatusDTO allStatus = new StatusDTO(-1, "Tất cả trạng thái");
        cbFilterStatus.getItems().add(allStatus);
        cbFilterStatus.getItems().addAll(StatusBUS.getInstance().getAllByType(StatusType.EMPLOYMENT_HISTORY));
        cbFilterStatus.setConverter(new javafx.util.StringConverter<StatusDTO>() {
            @Override
            public String toString(StatusDTO status) {
                return status == null ? ""
                        : status.getId() == -1 ? "Tất cả trạng thái"
                                : (status.getName() != null ? status.getName() + " - " + status.getDescription()
                                        : status.getDescription());
            }

            @Override
            public StatusDTO fromString(String string) {
                return null;
            }
        });
        cbFilterStatus.getSelectionModel().selectFirst();

        // Load form comboboxes (only active employees)
        ArrayList<EmployeeDTO> activeEmployees = EmployeeBUS.getInstance().getActiveEmployees();
        if (session.employeeLoginId() != 1)
            activeEmployees.removeIf(e -> e.getId() == 1); // Loại bỏ nhân viên hệ thống (ID=1) nếu k phải là admin
        cbEmployee.getItems().addAll(activeEmployees);
        cbEmployee.setConverter(new javafx.util.StringConverter<EmployeeDTO>() {
            @Override
            public String toString(EmployeeDTO emp) {
                return emp == null ? "" : emp.getId() + " - " + emp.getFirstName() + " " + emp.getLastName();
            }

            @Override
            public EmployeeDTO fromString(String string) {
                return null;
            }
        });

        // Load form comboboxes (only active departments)
        ArrayList<DepartmentDTO> activeDepts = DepartmentBUS.getInstance().getActiveDepartments();
        cbDepartment.getItems().addAll(activeDepts);
        cbDepartment.setConverter(new javafx.util.StringConverter<DepartmentDTO>() {
            @Override
            public String toString(DepartmentDTO dept) {
                if (dept == null)
                    return "";
                // Add "(Hiện tại)" if this is the current department of selected employee
                EmployeeDTO selectedEmp = cbEmployee.getValue();
                if (selectedEmp != null && dept.getId() == selectedEmp.getDepartmentId()) {
                    return dept.getName() + " (Hiện tại)";
                }
                return dept.getName();
            }

            @Override
            public DepartmentDTO fromString(String string) {
                return null;
            }
        });

        cbPosition.getItems().addAll(PositionBUS.getInstance().getAll());
        cbPosition.setConverter(new javafx.util.StringConverter<PositionDTO>() {
            @Override
            public String toString(PositionDTO pos) {
                if (pos == null)
                    return "";
                // Add "(Hiện tại)" if this is the current position of selected employee
                EmployeeDTO selectedEmp = cbEmployee.getValue();
                if (selectedEmp != null && pos.getId() == selectedEmp.getPositionId()) {
                    return pos.getName() + " (Hiện tại)";
                }
                return pos.getName();
            }

            @Override
            public PositionDTO fromString(String string) {
                return null;
            }
        });
    }

    @Override
    public void setupListeners() {
        cbFilterDept.setOnAction(event -> handleFilterChange());
        cbFilterPos.setOnAction(event -> handleFilterChange());
        cbFilterStatus.setOnAction(event -> handleFilterChange());
        UiUtils.gI().applySearchDebounce(txtSearch, 500, this::handleKeywordChange);

        // Update employee info label when employee is selected
        cbEmployee.setOnAction(event -> updateEmployeeInfo());

        btnClearFilter.setOnAction(event -> {
            resetFilters();
            Stage currentStage = (Stage) btnClearFilter.getScene().getWindow();
            NotificationUtils.showToast(currentStage, AppMessages.GENERAL_REFRESH_SUCCESS);
        });

        // Button listeners
        btnNew.setOnAction(event -> handleNewBtn());
        btnSave.setOnAction(event -> handleSaveBtn());
        btnApprove.setOnAction(event -> handleApproveBtn());
        btnReject.setOnAction(event -> handleRejectBtn());
        btnSync.setOnAction(event -> {
            TaskUtil.executePublic(loadingOverlay, () -> EmploymentHistoryBUS.getInstance().syncEmploymentChanges(),
                    result -> {
                        if (result.isSuccess()) {
                            Stage currentStage = (Stage) btnSync.getScene().getWindow();
                            NotificationUtils.showToast(currentStage, "Đồng bộ thành công!");
                            loadPageData(paginationController.getCurrentPage(), false);
                        } else {
                            Platform.runLater(() -> {
                                NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                            });
                        }
                    });
        });

    }

    // =====================
    // 3️⃣ CRUD HANDLERS
    // =====================
    private void handleNewBtn() {
        // Clear form and reset to default values
        clearForm();

        // Set next ID
        txtId.setText(String.valueOf(employmentHistoryBUS.nextId()));

        // Set creator name
        txtCreator.setText(session.getLoggedName());

        // Set default effective date to first day of next month
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1).withDayOfMonth(1);
        dpEffectiveDate.setValue(nextMonth);
    }

    /**
     * Configure DatePicker to only allow selecting month/year (day fixed at 1)
     * Starting from next month onwards
     */
    private void configureDatePickerForMonthYearOnly() {
        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1).withDayOfMonth(1);

        // 1. Set DayCellFactory to disable all days except day 1, and only from next
        // month
        // onwards
        dpEffectiveDate.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                // Disable if before next month
                if (date.isBefore(nextMonth)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #3AE2F4;");
                }
                // Disable if not the 1st day of month
                else if (date.getDayOfMonth() != 1) {
                    setDisable(true);
                    setStyle("-fx-background-color: #f0f0f0;");
                }
            }
        });

        // 2. Set StringConverter untuk handle input tay (typing)
        dpEffectiveDate.setConverter(new javafx.util.StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : formatter.format(date);
            }

            @Override
            public LocalDate fromString(String string) {
                if (string == null || string.trim().isEmpty()) {
                    return null;
                }

                try {
                    LocalDate parsed = LocalDate.parse(string, formatter);

                    // Force to day 1 nếu không phải ngày 1
                    if (parsed.getDayOfMonth() != 1) {
                        parsed = parsed.withDayOfMonth(1);
                    }

                    // Force to next month nếu trước tháng tiếp theo
                    if (parsed.isBefore(nextMonth)) {
                        parsed = nextMonth;
                    }

                    return parsed;
                } catch (Exception e) {
                    // Invalid format - return current value
                    return dpEffectiveDate.getValue();
                }
            }
        });

        // 3. Add listener to force day to 1 when user selects via calendar picker
        dpEffectiveDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                LocalDate corrected = newDate;

                // Force to day 1 if not
                if (corrected.getDayOfMonth() != 1) {
                    corrected = corrected.withDayOfMonth(1);
                }

                // Force to next month if before
                if (corrected.isBefore(nextMonth)) {
                    corrected = nextMonth;
                }

                // Update if changed
                if (!corrected.equals(newDate)) {
                    dpEffectiveDate.setValue(corrected);
                }
            }
        });
    }

    private void handleSaveBtn() {
        // 1. Kiểm tra nhân viên và ngày hiệu lực (Giữ nguyên)
        if (cbEmployee.getValue() == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn nhân viên", AppMessages.DIALOG_TITLE);
            return;
        }
        EmployeeDTO selectedEmployee = cbEmployee.getValue();

        if (dpEffectiveDate.getValue() == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn ngày hiệu lực", AppMessages.DIALOG_TITLE);
            return;
        }

        // 2. Lấy giá trị từ ComboBox (Nếu không chọn thì mặc định lấy giá trị hiện tại
        // của NV)
        int finalDeptId = (cbDepartment.getValue() != null)
                ? cbDepartment.getValue().getId()
                : selectedEmployee.getDepartmentId();

        int finalPosId = (cbPosition.getValue() != null)
                ? cbPosition.getValue().getId()
                : selectedEmployee.getPositionId();

        // 3. ĐIỀU KIỆN QUAN TRỌNG: So sánh với dữ liệu hiện tại của nhân viên
        boolean isDeptChanged = finalDeptId != selectedEmployee.getDepartmentId();
        boolean isPosChanged = finalPosId != selectedEmployee.getPositionId();

        if (!isDeptChanged && !isPosChanged) {
            NotificationUtils.showErrorAlert(
                    "Quyết định điều chuyển phải thay đổi ít nhất Phòng ban hoặc Vị trí so với hiện tại!",
                    AppMessages.DIALOG_TITLE);
            return;
        }

        // Validate reason (optional, but if provided must be valid Vietnamese text and
        // <= 255 chars)
        String reason = txtReason.getText() != null ? txtReason.getText().trim() : null;
        if (reason != null && !reason.isEmpty()) {
            ValidationUtils validationUtils = ValidationUtils.getInstance();
            if (!validationUtils.validateVietnameseText255(reason)) {
                NotificationUtils.showErrorAlert("Lý do điều chuyển không quá 255 ký tự",
                        AppMessages.DIALOG_TITLE);
                return;
            }
        }

        // Get pending status ID
        int pendingStatusId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.EMPLOYMENT_HISTORY, Status.EmploymentHistory.PENDING).getId();

        // Create EmploymentHistoryDTO
        EmploymentHistoryDTO newHistory = new EmploymentHistoryDTO();
        newHistory.setEmployeeId(selectedEmployee.getId());
        // If department not selected, use current department; otherwise use new
        // selection
        newHistory.setDepartmentId(finalDeptId);
        // If position not selected, use current position; otherwise use new selection
        newHistory.setPositionId(finalPosId);
        newHistory.setEffectiveDate(dpEffectiveDate.getValue());
        newHistory.setApproverId(session.employeeLoginId());
        newHistory.setStatusId(pendingStatusId);
        newHistory.setReason(reason); // reason is already validated and trimmed above

        // Save to database
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYMENT_HISTORY_INSERT,
                () -> employmentHistoryBUS.insert(newHistory),
                result -> {
                    if (result.isSuccess()) {
                        Stage currentStage = (Stage) btnSave.getScene().getWindow();
                        NotificationUtils.showToast(currentStage, result.getMessage());

                        clearForm();
                        loadPageData(paginationController.getCurrentPage(), false);
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    private void handleApproveBtn() {
        if (isNotSelectedHistory()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn quyết định cần duyệt", AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert("Xác nhận duyệt quyết định này?", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYMENT_HISTORY_APPROVE,
                () -> employmentHistoryBUS.approve(selectedHistory.getId()),
                result -> {
                    if (result.isSuccess()) {
                        NotificationUtils.showInfoAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                        clearForm();
                        loadPageData(0, false);
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    private void handleRejectBtn() {
        if (isNotSelectedHistory()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn quyết định cần hủy", AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert("Xác nhận hủy quyết định này?", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYMENT_HISTORY_DELETE,
                () -> employmentHistoryBUS.delete(selectedHistory.getId()),
                result -> {
                    if (result.isSuccess()) {
                        NotificationUtils.showInfoAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                        clearForm();
                        loadPageData(0, false);
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    }
                });
    }

    // =====================
    // 4️⃣ FILTER & FORM HANDLING
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

    private void handleFilterChange() {
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

        cbFilterDept.getSelectionModel().selectFirst();
        cbFilterPos.getSelectionModel().selectFirst();
        cbFilterStatus.getSelectionModel().selectFirst();
        txtSearch.clear();
        keyword = "";
        filterEmployeeId = null;
        filterDepartmentId = null;
        filterPositionId = null;
        filterStatusId = null;

        applyFilters();

        Platform.runLater(() -> isResetting = false);
    }

    /**
     * Refresh combobox display when employee is selected
     * StringConverter will automatically add "(Hiện tại)" indicator to current
     * dept/position
     */
    private void updateEmployeeInfo() {
        // Re-render combobox items to update the "(Hiện tại)" indicators
        // This is done automatically by JavaFX when StringConverter.toString() is
        // called
        // Just need to refresh the visual representation
        cbDepartment.getSelectionModel().clearSelection();
        cbPosition.getSelectionModel().clearSelection();
    }

    private void clearForm() {
        txtId.clear();
        cbEmployee.getSelectionModel().clearSelection();
        cbDepartment.getSelectionModel().clearSelection();
        cbPosition.getSelectionModel().clearSelection();
        dpEffectiveDate.setValue(null);
        txtReason.clear();
        tblHistory.getSelectionModel().clearSelection();
    }

    // =====================
    // 5️⃣ PERMISSION & VALIDATION
    // =====================
    @Override
    public void hideButtonWithoutPermission() {
        boolean canView = session.hasPermission(PermissionKey.EMPLOYMENT_HISTORY_LIST_VIEW);

        if (!canView) {
            mainContent.setVisible(false);
            mainContent.setManaged(false);
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }

        boolean canAdd = session.hasPermission(PermissionKey.EMPLOYMENT_HISTORY_INSERT);
        boolean canApprove = session.hasPermission(PermissionKey.EMPLOYMENT_HISTORY_APPROVE);
        boolean canDelete = session.hasPermission(PermissionKey.EMPLOYMENT_HISTORY_DELETE);

        if (!canAdd) {
            UiUtils.gI().setVisibleItem(btnNew);
            UiUtils.gI().setVisibleItem(btnSave);
        }
        if (!canApprove) {
            UiUtils.gI().setVisibleItem(btnApprove);
        }
        if (!canDelete) {
            UiUtils.gI().setVisibleItem(btnReject);
        }
    }

    private boolean isNotSelectedHistory() {
        selectedHistory = tblHistory.getSelectionModel().getSelectedItem();
        return selectedHistory == null;
    }
}
