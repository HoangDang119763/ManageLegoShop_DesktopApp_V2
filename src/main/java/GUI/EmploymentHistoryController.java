package GUI;

import BUS.DepartmentBUS;
import BUS.EmployeeBUS;
import BUS.EmploymentHistoryBUS;
import BUS.PositionBUS;
import BUS.StatusBUS;
import DTO.DepartmentDTO;
import DTO.EmployeeDTO;
import DTO.EmploymentHistoryDisplayDTO;
import DTO.PagedResponse;
import DTO.PositionDTO;
import DTO.StatusDTO;
import ENUM.PermissionKey;
import ENUM.StatusType;
import INTERFACE.IController;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class EmploymentHistoryController implements IController {

    // =====================
    // TABLE & COLUMNS
    // =====================
    @FXML
    private TableView<EmploymentHistoryDisplayDTO> tblHistory;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, Integer> colId;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, Integer> colEmployeeId;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colEmployeeName;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colDeptName;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colPosName;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, String> colEffectiveDate;
    @FXML
    private TableColumn<EmploymentHistoryDisplayDTO, Integer> colApproverId;
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
    private ComboBox<EmployeeDTO> cbFilterEmp;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =====================
    // 1️⃣ LIFECYCLE & INITIALIZATION
    // =====================
    @FXML
    public void initialize() {
        employmentHistoryBUS = EmploymentHistoryBUS.getInstance();
        session = SessionManagerService.getInstance();

        tblHistory.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        Platform.runLater(() -> tblHistory.getSelectionModel().clearSelection());

        hideButtonWithoutPermission();
        loadComboBox();
        loadTable();
        setupPagination();
        setupListeners();
        applyFilters();

        // Set creator to current logged user
        txtCreator.setText(session.getLoggedName());
    }

    // =====================
    // 2️⃣ UI SETUP
    // =====================
    @Override
    public void loadTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colDeptName.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        colPosName.setCellValueFactory(new PropertyValueFactory<>("positionName"));

        // Format date columns
        colEffectiveDate.setCellValueFactory(new PropertyValueFactory<>("effectiveDate"));
        colEffectiveDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    EmploymentHistoryDisplayDTO dto = getTableRow().getItem();
                    setText(dto.getEffectiveDate() != null ? dto.getEffectiveDate().format(DATE_FORMATTER) : "");
                }
            }
        });

        colApproverId.setCellValueFactory(new PropertyValueFactory<>("approverId"));
        colApproverName.setCellValueFactory(new PropertyValueFactory<>("approverName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusDescription"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));

        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colCreatedAt.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    EmploymentHistoryDisplayDTO dto = getTableRow().getItem();
                    setText(dto.getCreatedAt() != null ? dto.getCreatedAt().format(DATETIME_FORMATTER) : "");
                }
            }
        });
    }

    private void setupPagination() {
        paginationController.init(0, PAGE_SIZE, pageIndex -> {
            loadPageData(pageIndex, true);
        });
    }

    private void loadPageData(int pageIndex, boolean showOverlay) {
        // 1. Thu thập tham số lọc từ UI
        String keyword = txtSearch.getText().trim();
        Integer empId = (cbFilterEmp.getValue() == null || cbFilterEmp.getValue().getId() == -1) ? null
                : cbFilterEmp.getValue().getId();
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
                        keyword, empId, deptId, posId, statusId, pageIndex, PAGE_SIZE),
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
        cbFilterEmp.getItems().add(allEmployees);
        cbFilterEmp.getItems().addAll(EmployeeBUS.getInstance().getAll());
        cbFilterEmp.setConverter(new javafx.util.StringConverter<EmployeeDTO>() {
            @Override
            public String toString(EmployeeDTO emp) {
                return emp == null ? ""
                        : emp.getId() == -1 ? "Tất cả nhân viên"
                                : emp.getId() + " - " + emp.getFirstName() + " " + emp.getLastName();
            }

            @Override
            public EmployeeDTO fromString(String string) {
                return null;
            }
        });
        cbFilterEmp.getSelectionModel().selectFirst();

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
        cbFilterStatus.getSelectionModel().selectFirst();

        // Load form comboboxes
        cbEmployee.getItems().addAll(EmployeeBUS.getInstance().getAll());
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

        cbDepartment.getItems().addAll(DepartmentBUS.getInstance().getAll());
        cbDepartment.setConverter(new javafx.util.StringConverter<DepartmentDTO>() {
            @Override
            public String toString(DepartmentDTO dept) {
                return dept == null ? "" : dept.getName();
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
                return pos == null ? "" : pos.getName();
            }

            @Override
            public PositionDTO fromString(String string) {
                return null;
            }
        });
    }

    @Override
    public void setupListeners() {
        // Filter listeners
        cbFilterEmp.setOnAction(event -> handleFilterChange());
        cbFilterDept.setOnAction(event -> handleFilterChange());
        cbFilterPos.setOnAction(event -> handleFilterChange());
        cbFilterStatus.setOnAction(event -> handleFilterChange());
        UiUtils.gI().applySearchDebounce(txtSearch, 500, this::handleKeywordChange);

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

        // Table selection listener
        tblHistory.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        loadSelectedToForm(newSelection);
                    }
                });
    }

    // =====================
    // 3️⃣ CRUD HANDLERS
    // =====================
    private void handleNewBtn() {
        clearForm();
        txtCreator.setText(session.getLoggedName());
    }

    private void handleSaveBtn() {
        // TODO: Implement save logic (insert or update employment history)
        // This would require additional methods in BUS and DAL
        NotificationUtils.showInfoAlert("Chức năng đang phát triển", "Thông báo");
    }

    private void handleApproveBtn() {
        if (isNotSelectedHistory()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn quyết định cần duyệt", AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert("Xác nhận duyệt quyết định này?", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        // TODO: Implement approve logic (update status to approved)
        NotificationUtils.showInfoAlert("Chức năng đang phát triển", "Thông báo");
    }

    private void handleRejectBtn() {
        if (isNotSelectedHistory()) {
            NotificationUtils.showErrorAlert("Vui lòng chọn quyết định cần hủy", AppMessages.DIALOG_TITLE);
            return;
        }

        if (!UiUtils.gI().showConfirmAlert("Xác nhận hủy quyết định này?", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }

        // TODO: Implement reject logic (update status to rejected)
        NotificationUtils.showInfoAlert("Chức năng đang phát triển", "Thông báo");
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

        cbFilterEmp.getSelectionModel().selectFirst();
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

    private void loadSelectedToForm(EmploymentHistoryDisplayDTO dto) {
        if (dto == null)
            return;

        txtId.setText(String.valueOf(dto.getId()));

        // Set employee
        cbEmployee.getItems().stream()
                .filter(emp -> emp.getId() == dto.getEmployeeId())
                .findFirst()
                .ifPresent(cbEmployee::setValue);

        // Set department
        if (dto.getDepartmentId() != null) {
            cbDepartment.getItems().stream()
                    .filter(dept -> dept.getId() == dto.getDepartmentId())
                    .findFirst()
                    .ifPresent(cbDepartment::setValue);
        }

        // Set position
        if (dto.getPositionId() != null) {
            cbPosition.getItems().stream()
                    .filter(pos -> pos.getId() == dto.getPositionId())
                    .findFirst()
                    .ifPresent(cbPosition::setValue);
        }

        dpEffectiveDate.setValue(dto.getEffectiveDate());
        txtReason.setText(dto.getReason());

        if (dto.getApproverName() != null) {
            txtCreator.setText(dto.getApproverName());
        }
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
