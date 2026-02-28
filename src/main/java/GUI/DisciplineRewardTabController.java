package GUI;

import BUS.FineBUS;
import BUS.EmployeeBUS;
import DTO.FineDTO;
import DTO.EmployeeDTO;
import DTO.BUSResult;
import ENUM.BUSOperationResult;
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
    
    @FXML
    private TableView<FineDTO> tblDisciplineReward;
    @FXML
    private TableColumn<FineDTO, Integer> colId;
    @FXML
    private TableColumn<FineDTO, String> colEmployeeName;
    @FXML
    private TableColumn<FineDTO, String> colType;
    @FXML
    private TableColumn<FineDTO, String> colReason;
    @FXML
    private TableColumn<FineDTO, String> colLevel;
    @FXML
    private TableColumn<FineDTO, BigDecimal> colAmount;
    @FXML
    private TableColumn<FineDTO, LocalDateTime> colDate;
    @FXML
    private TableColumn<FineDTO, Void> colAction;

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private Button btnAdd, btnRefresh;
    @FXML
    private Button btnPrevious, btnNext;
    @FXML
    private Label lblPageInfo;

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
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        // Custom cell factory for employee name (join with employee table)
        colEmployeeName.setCellValueFactory(cellData -> {
            int empId = cellData.getValue().getEmployeeId();
            EmployeeDTO emp = employeeBUS.getById(empId);
            return new javafx.beans.property.SimpleObjectProperty<>(
                emp != null ? emp.getFirstName() + " " + emp.getLastName() : "N/A");
        });

        // Action column (Edit, Delete)
        colAction.setCellFactory(param -> new TableCell<FineDTO, Void>() {
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
        cbType.setItems(FXCollections.observableArrayList("TẤT CẢ", "REWARD", "DISCIPLINE"));
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
        SecureExecutor.executeSafeBusResult(
            PermissionKey.EMPLOYEE_FINE_REWARD_VIEW,
            () -> {
                allData.clear();
                allData.addAll(fineBUS.getAll());
                return new BUSResult(BUSOperationResult.SUCCESS, "Loaded");
            }
        );

        filterData();
    }

    private void filterData() {
        String searchText = txtSearch.getText().toLowerCase();
        String type = cbType.getValue();

        filteredData.clear();
        filteredData.addAll(allData.stream()
            .filter(f -> {
                EmployeeDTO emp = employeeBUS.getById(f.getEmployeeId());
                String empName = emp != null ? (emp.getFirstName() + " " + emp.getLastName()).toLowerCase() : "";
                return f.getReason().toLowerCase().contains(searchText) || empName.contains(searchText);
            })
            .filter(f -> type.equals("TẤT CẢ") || f.getType().equals(type))
            .collect(Collectors.toCollection(ArrayList::new)));

        currentPageIndex = 0;
        displayPage(0);
    }

    private void showPreviousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            displayPage(currentPageIndex);
        }
    }

    private void showNextPage() {
        int totalPages = (int) Math.ceil((double) filteredData.size() / PAGE_SIZE);
        if (filteredData.isEmpty()) totalPages = 1;
        if (currentPageIndex < totalPages - 1) {
            currentPageIndex++;
            displayPage(currentPageIndex);
        }
    }

    private void displayPage(int pageIndex) {
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredData.size());
        
        ArrayList<FineDTO> pageData = new ArrayList<>(filteredData.subList(start, end));
        tblDisciplineReward.setItems(FXCollections.observableArrayList(pageData));
        
        // Update page info label
        int totalPages = filteredData.isEmpty() ? 1 : (int) Math.ceil((double) filteredData.size() / PAGE_SIZE);
        lblPageInfo.setText(String.format("Trang %d / %d (Tổng: %d bản ghi)", 
            pageIndex + 1, totalPages, filteredData.size()));
        
        // Update button states
        btnPrevious.setDisable(pageIndex == 0);
        btnNext.setDisable(pageIndex >= totalPages - 1);
    }

    private void handleAdd() {
        Dialog<FineDTO> dialog = new Dialog<>();
        dialog.setTitle("Thêm Khen thưởng / Kỷ luật");
        dialog.setHeaderText("Nhập thông tin");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));

        ComboBox<EmployeeDTO> cbEmployee = new ComboBox<>();
        cbEmployee.setItems(FXCollections.observableArrayList(employeeBUS.getAll()));
        cbEmployee.setCellFactory(param -> new ListCell<EmployeeDTO>() {
            @Override
            protected void updateItem(EmployeeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getFirstName() + " " + item.getLastName());
            }
        });

        ComboBox<String> cbType = new ComboBox<>();
        cbType.setItems(FXCollections.observableArrayList("REWARD", "DISCIPLINE"));

        ComboBox<String> cbLevel = new ComboBox<>();
        cbLevel.setItems(FXCollections.observableArrayList("LEVEL_1", "LEVEL_2", "LEVEL_3", "HIGH", "MEDIUM", "LOW"));

        TextArea txtReason = new TextArea();
        txtReason.setPrefRowCount(3);

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Nhập số tiền");

        grid.add(new Label("Nhân viên:"), 0, 0);
        grid.add(cbEmployee, 1, 0);
        grid.add(new Label("Loại:"), 0, 1);
        grid.add(cbType, 1, 1);
        grid.add(new Label("Mức độ:"), 0, 2);
        grid.add(cbLevel, 1, 2);
        grid.add(new Label("Lý do:"), 0, 3);
        grid.add(txtReason, 1, 3);
        grid.add(new Label("Số tiền:"), 0, 4);
        grid.add(txtAmount, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (cbEmployee.getValue() == null || cbType.getValue() == null || txtAmount.getText().isEmpty()) {
                    NotificationUtils.showErrorAlert("Vui lòng điền đầy đủ thông tin", "Lỗi");
                    return null;
                }

                try {
                    BigDecimal amount = new BigDecimal(txtAmount.getText());
                    // Khen thưởng là dương, kỷ luật là âm
                    if (cbType.getValue().equals("DISCIPLINE")) {
                        amount = amount.negate();
                    }

                    FineDTO fine = new FineDTO();
                    fine.setEmployeeId(cbEmployee.getValue().getId());
                    fine.setType(cbType.getValue());
                    fine.setFineLevel(cbLevel.getValue());
                    fine.setReason(txtReason.getText());
                    fine.setAmount(amount);
                    fine.setFinePay(BigDecimal.ZERO);
                    fine.setCreatedAt(LocalDateTime.now());

                    return fine;
                } catch (NumberFormatException e) {
                    NotificationUtils.showErrorAlert("Số tiền không hợp lệ", "Lỗi");
                }
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            FineDTO fine = result.get();
            SessionManagerService sessionManager = SessionManagerService.getInstance();
            BUSResult res = SecureExecutor.executeSafeBusResult(
                PermissionKey.EMPLOYEE_FINE_REWARD_MANAGE,
                () -> fineBUS.insert(fine, sessionManager.employeeRoleId(), sessionManager.employeeLoginId()) ? 
                    new BUSResult(BUSOperationResult.SUCCESS, "Thêm thành công") :
                    new BUSResult(BUSOperationResult.DB_ERROR, "Thêm thất bại")
            );

            if (res.getCode() == BUSOperationResult.SUCCESS) {
                NotificationUtils.showInfoAlert("Thêm thành công", "Thành công");
                loadData();
            } else {
                NotificationUtils.showErrorAlert(res.getMessage(), "Lỗi");
            }
        }
    }

    private void handleEdit(FineDTO fine) {
        Dialog<FineDTO> dialog = new Dialog<>();
        dialog.setTitle("Sửa Khen thưởng / Kỷ luật");
        dialog.setHeaderText("Cập nhật thông tin");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(10));

        ComboBox<String> cbType = new ComboBox<>();
        cbType.setItems(FXCollections.observableArrayList("REWARD", "DISCIPLINE"));
        cbType.setValue(fine.getType());

        ComboBox<String> cbLevel = new ComboBox<>();
        cbLevel.setItems(FXCollections.observableArrayList("LEVEL_1", "LEVEL_2", "LEVEL_3", "HIGH", "MEDIUM", "LOW"));
        cbLevel.setValue(fine.getFineLevel());

        TextArea txtReason = new TextArea();
        txtReason.setText(fine.getReason());

        TextField txtAmount = new TextField();
        txtAmount.setText(fine.getAmount().toPlainString());

        EmployeeDTO emp = employeeBUS.getById(fine.getEmployeeId());
        Label lblEmployee = new Label(emp != null ? emp.getFirstName() + " " + emp.getLastName() : "");

        grid.add(new Label("Nhân viên:"), 0, 0);
        grid.add(lblEmployee, 1, 0);
        grid.add(new Label("Loại:"), 0, 1);
        grid.add(cbType, 1, 1);
        grid.add(new Label("Mức độ:"), 0, 2);
        grid.add(cbLevel, 1, 2);
        grid.add(new Label("Lý do:"), 0, 3);
        grid.add(txtReason, 1, 3);
        grid.add(new Label("Số tiền:"), 0, 4);
        grid.add(txtAmount, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                if (cbType.getValue() == null || txtAmount.getText().isEmpty()) {
                    NotificationUtils.showErrorAlert("Vui lòng điền đầy đủ thông tin", "Lỗi");
                    return null;
                }

                try {
                    BigDecimal amount = new BigDecimal(txtAmount.getText());
                    if (cbType.getValue().equals("DISCIPLINE")) {
                        amount = amount.negate();
                    }

                    fine.setType(cbType.getValue());
                    fine.setFineLevel(cbLevel.getValue());
                    fine.setReason(txtReason.getText());
                    fine.setAmount(amount);

                    return fine;
                } catch (NumberFormatException e) {
                    NotificationUtils.showErrorAlert("Số tiền không hợp lệ", "Lỗi");
                }
            }
            return null;
        });

        var result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            FineDTO updated = result.get();
            SessionManagerService sessionManager = SessionManagerService.getInstance();
            BUSResult res = SecureExecutor.executeSafeBusResult(
                PermissionKey.EMPLOYEE_FINE_REWARD_MANAGE,
                () -> fineBUS.update(updated, sessionManager.employeeRoleId(), sessionManager.employeeLoginId()) ? 
                    new BUSResult(BUSOperationResult.SUCCESS, "Cập nhật thành công") :
                    new BUSResult(BUSOperationResult.DB_ERROR, "Cập nhật thất bại")
            );

            if (res.getCode() == BUSOperationResult.SUCCESS) {
                NotificationUtils.showInfoAlert("Cập nhật thành công", "Thành công");
                loadData();
            } else {
                NotificationUtils.showErrorAlert(res.getMessage(), "Lỗi");
            }
        }
    }

    private void handleDelete(FineDTO fine) {
        if (UiUtils.gI().showConfirmAlert("Bạn có chắc chắn muốn xóa?", "Xác nhận")) {
            SessionManagerService sessionManager = SessionManagerService.getInstance();
            BUSResult res = SecureExecutor.executeSafeBusResult(
                PermissionKey.EMPLOYEE_FINE_REWARD_MANAGE,
                () -> fineBUS.delete(fine.getId(), sessionManager.employeeRoleId(), sessionManager.employeeLoginId()) ? 
                    new BUSResult(BUSOperationResult.SUCCESS, "Xóa thành công") :
                    new BUSResult(BUSOperationResult.DB_ERROR, "Xóa thất bại")
            );

            if (res.getCode() == BUSOperationResult.SUCCESS) {
                NotificationUtils.showInfoAlert("Xóa thành công", "Thành công");
                loadData();
            } else {
                NotificationUtils.showErrorAlert(res.getMessage(), "Lỗi");
            }
        }
    }
}
