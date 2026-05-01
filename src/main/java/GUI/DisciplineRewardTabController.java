package GUI;

import BUS.FineBUS;
import BUS.EmployeeBUS;
import DTO.FineDTO;
import DTO.EmployeeDTO;
import DTO.BUSResult;
import ENUM.BUSOperationResult;
import ENUM.Status.FineLevel;
import ENUM.Status.FineType; // Import Enum mới của bạn
import ENUM.PermissionKey;
import SERVICE.SecureExecutor;
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DisciplineRewardTabController {
    private static final int PAGE_SIZE = 10;
    
    @FXML private TableView<FineDTO> tblDisciplineReward;
    @FXML private TableColumn<FineDTO, Integer> colId;
    @FXML private TableColumn<FineDTO, String> colEmployeeName;
    @FXML private TableColumn<FineDTO, String> colType; // Hiển thị loại khen thưởng/kỷ luật
    @FXML private TableColumn<FineDTO, String> colReason;
    @FXML private TableColumn<FineDTO, String> colLevel;
    @FXML private TableColumn<FineDTO, BigDecimal> colAmount;
    @FXML private TableColumn<FineDTO, LocalDateTime> colDate;
    @FXML private TableColumn<FineDTO, Void> colAction;

    @FXML private TextField txtSearch;
    @FXML private ComboBox<Object> cbType; // Để Object để chứa cả String "TẤT CẢ" và FineType
    @FXML private Button btnAdd, btnRefresh;
    @FXML private Button btnPrevious, btnNext;
    @FXML private Label lblPageInfo;

    private FineBUS fineBUS;
    private EmployeeBUS employeeBUS;
    private ArrayList<FineDTO> allData;
    private ArrayList<FineDTO> filteredData;
    private int currentPageIndex = 0;

    @FXML
    public void initialize() {
        fineBUS = FineBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        allData = new ArrayList<>();
        filteredData = new ArrayList<>();

        setupTable();
        setupTypeFilter();
        setupListeners();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("fineLevel"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Hiển thị Loại có màu sắc dựa trên Enum
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    FineType type = FineType.fromString(item);
                    setText(type.getLabel());
                    setStyle("-fx-text-fill: " + type.getColor() + "; -fx-font-weight: bold;");
                }
            }
        });

        colEmployeeName.setCellValueFactory(cellData -> {
            int empId = cellData.getValue().getEmployeeId();
            EmployeeDTO emp = employeeBUS.getById(empId);
            return new javafx.beans.property.SimpleObjectProperty<>(
                emp != null ? emp.getFirstName() + " " + emp.getLastName() : "N/A");
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Sửa");
            private final Button deleteBtn = new Button("Xóa");
            private final HBox pane = new HBox(5);
            {
                editBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px;");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 5px;");
                pane.setAlignment(Pos.CENTER);
                pane.getChildren().addAll(editBtn, deleteBtn);
                editBtn.setOnAction(event -> handleEdit(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(event -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void setupTypeFilter() {
        ArrayList<Object> items = new ArrayList<>();
        items.add("TẤT CẢ");
        items.add(FineType.REWARD);
        items.add(FineType.DISCIPLINE);
        cbType.setItems(FXCollections.observableArrayList(items));
        cbType.setValue("TẤT CẢ");
    }

    private void setupListeners() {
        btnAdd.setOnAction(e -> handleAdd());
        btnRefresh.setOnAction(e -> loadData());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        cbType.valueProperty().addListener((obs, oldVal, newVal) -> filterData());
        btnPrevious.setOnAction(e -> showPreviousPage());
        btnNext.setOnAction(e -> showNextPage());
    }

    private void loadData() {
        SecureExecutor.executeSafeBusResult(PermissionKey.EMPLOYEE_FINE_REWARD_VIEW, () -> {
            allData.clear();
            allData.addAll(fineBUS.getAll());
            System.out.println("Loaded " + allData.size() + " records from database.");
            return new BUSResult(BUSOperationResult.SUCCESS, "Loaded");
        });
        filterData();
    }

    private void filterData() {
        String searchText = txtSearch.getText().toLowerCase();
        Object typeFilter = cbType.getValue();

        filteredData = allData.stream()
            .filter(f -> {
                EmployeeDTO emp = employeeBUS.getById(f.getEmployeeId());
                String empName = emp != null ? (emp.getFirstName() + " " + emp.getLastName()).toLowerCase() : "";
                return f.getReason().toLowerCase().contains(searchText) || empName.contains(searchText);
            })
            .filter(f -> {
                if (typeFilter.equals("TẤT CẢ")) return true;
                return f.getType().equals(((FineType)typeFilter).name());
            })
            .collect(Collectors.toCollection(ArrayList::new));

        currentPageIndex = 0;
        displayPage(0);
    }

    private void displayPage(int pageIndex) {
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredData.size());
        tblDisciplineReward.setItems(FXCollections.observableArrayList(new ArrayList<>(filteredData.subList(start, end))));
        
        int totalPages = filteredData.isEmpty() ? 1 : (int) Math.ceil((double) filteredData.size() / PAGE_SIZE);
        lblPageInfo.setText(String.format("Trang %d / %d (Tổng: %d)", pageIndex + 1, totalPages, filteredData.size()));
        btnPrevious.setDisable(pageIndex == 0);
        btnNext.setDisable(pageIndex >= totalPages - 1);
    }

    private void handleAdd() {
        Dialog<FineDTO> dialog = new Dialog<>();
        dialog.setTitle("Thêm Khen thưởng / Kỷ luật");
        GridPane grid = createDialogGrid(null);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) return getFineFromGrid(grid, null);
            return null;
        });

        dialog.showAndWait().ifPresent(fine -> {
            SessionManagerService session = SessionManagerService.getInstance();
            BUSResult res = SecureExecutor.executeSafeBusResult(PermissionKey.EMPLOYEE_FINE_REWARD_MANAGE,
                () -> fineBUS.insert(fine, session.employeeRoleId(), session.employeeLoginId()) ? 
                new BUSResult(BUSOperationResult.SUCCESS, "OK") : new BUSResult(BUSOperationResult.DB_ERROR, "Error"));

            if (res.getCode() == BUSOperationResult.SUCCESS) {
                NotificationUtils.showInfoAlert("Thành công", "Thông báo");
                loadData();
            }
            else {
                NotificationUtils.showErrorAlert("Lỗi", res.toString());
            }
        });
    }

    private void handleEdit(FineDTO fine) {
        Dialog<FineDTO> dialog = new Dialog<>();
        dialog.setTitle("Sửa Khen thưởng / Kỷ luật");
        GridPane grid = createDialogGrid(fine);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) return getFineFromGrid(grid, fine);
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            SessionManagerService session = SessionManagerService.getInstance();
            SecureExecutor.executeSafeBusResult(PermissionKey.EMPLOYEE_FINE_REWARD_MANAGE,
                () -> fineBUS.update(updated, session.employeeRoleId(), session.employeeLoginId()) ?
                new BUSResult(BUSOperationResult.SUCCESS, "OK") : new BUSResult(BUSOperationResult.DB_ERROR, "Error"));
            loadData();
        });
    }

    private GridPane createDialogGrid(FineDTO fine) {
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));

        ComboBox<EmployeeDTO> cbEmployee = new ComboBox<>();
        cbEmployee.setItems(FXCollections.observableArrayList(employeeBUS.getAll()));
        if (fine != null) {
            cbEmployee.setValue(employeeBUS.getById(fine.getEmployeeId()));
            cbEmployee.setDisable(true);
        }

        ComboBox<FineType> cbTypeDlg = new ComboBox<>();
        cbTypeDlg.setItems(FXCollections.observableArrayList(FineType.values()));
        if (fine != null) cbTypeDlg.setValue(FineType.fromString(fine.getType()));

        ComboBox<String> cbLevel = new ComboBox<>();
        cbLevel.setItems(FXCollections.observableArrayList(FineLevel.values()).stream().map(FineLevel::name).collect(Collectors.toCollection(FXCollections::observableArrayList)));
        if (fine != null) cbLevel.setValue(fine.getFineLevel());

        TextArea txtReason = new TextArea(fine != null ? fine.getReason() : "");
        TextField txtAmount = new TextField(fine != null ? fine.getAmount().abs().toPlainString() : "");

        grid.add(new Label("Nhân viên:"), 0, 0); grid.add(cbEmployee, 1, 0);
        grid.add(new Label("Loại:"), 0, 1); grid.add(cbTypeDlg, 1, 1);
        grid.add(new Label("Mức độ:"), 0, 2); grid.add(cbLevel, 1, 2);
        grid.add(new Label("Lý do:"), 0, 3); grid.add(txtReason, 1, 3);
        grid.add(new Label("Số tiền:"), 0, 4); grid.add(txtAmount, 1, 4);
        
        return grid;
    }

    private FineDTO getFineFromGrid(GridPane grid, FineDTO existing) {
        try {
            ComboBox<EmployeeDTO> cbEmp = (ComboBox) grid.getChildren().get(1);
            ComboBox<FineType> cbT = (ComboBox) grid.getChildren().get(3);
            ComboBox<String> cbL = (ComboBox) grid.getChildren().get(5);
            TextArea taR = (TextArea) grid.getChildren().get(7);
            TextField tfA = (TextField) grid.getChildren().get(9);

            BigDecimal amount = new BigDecimal(tfA.getText());
            if (cbT.getValue() == FineType.DISCIPLINE) amount = amount.negate();

            FineDTO f = (existing == null) ? new FineDTO() : existing;
            f.setEmployeeId(cbEmp.getValue().getId());
            f.setType(cbT.getValue().name());
            f.setFineLevel(cbL.getValue());
            f.setReason(taR.getText());
            f.setAmount(amount);
            if (existing == null) {
                f.setFinePay(BigDecimal.ZERO);
                f.setCreatedAt(LocalDateTime.now());
            }
            return f;
        } catch (Exception e) {
            NotificationUtils.showErrorAlert("Dữ liệu nhập không hợp lệ", "Lỗi");
            return null;
        }
    }

    private void handleDelete(FineDTO fine) {
        if (UiUtils.gI().showConfirmAlert("Bạn có chắc chắn muốn xóa bản ghi này?", "Xác nhận xóa")) {
            SessionManagerService session = SessionManagerService.getInstance();
            SecureExecutor.executeSafeBusResult(PermissionKey.EMPLOYEE_FINE_REWARD_MANAGE,
                () -> fineBUS.delete(fine.getId(), session.employeeRoleId(), session.employeeLoginId()) ?
                new BUSResult(BUSOperationResult.SUCCESS, "Xóa thành công") : new BUSResult(BUSOperationResult.DB_ERROR, "Lỗi"));
            loadData();
        }
    }
    
    private void showPreviousPage() { if (currentPageIndex > 0) displayPage(--currentPageIndex); }
    private void showNextPage() { if (currentPageIndex < (int) Math.ceil((double) filteredData.size() / PAGE_SIZE) - 1) displayPage(++currentPageIndex); }

    public void loadEmployeeDisciplines(int employeeId) {
        // Implementation for loading employee disciplines
        filteredData = fineBUS.getByEmployeeId(employeeId);
        currentPageIndex = 0;
        displayPage(0);
    }
}