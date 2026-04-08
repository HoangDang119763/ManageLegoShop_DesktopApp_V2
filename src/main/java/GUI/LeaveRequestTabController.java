package GUI;

import BUS.LeaveRequestBUS;
import BUS.LeaveTypeBUS;
import DTO.LeaveRequestDTO;
import DTO.LeaveTypeDTO;
import ENUM.BUSOperationResult;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class LeaveRequestTabController {

    @FXML private TableView<LeaveRequestDTO> tblLeaveRequest;
    @FXML private TableColumn<LeaveRequestDTO, Integer> colId;
    @FXML private TableColumn<LeaveRequestDTO, String> colEmployeeName;
    @FXML private TableColumn<LeaveRequestDTO, LocalDate> colStartDate;
    @FXML private TableColumn<LeaveRequestDTO, LocalDate> colEndDate;
    @FXML private TableColumn<LeaveRequestDTO, String> colLeaveType;
    @FXML private TableColumn<LeaveRequestDTO, String> colStatus;
    @FXML private TableColumn<LeaveRequestDTO, String> colReason;
    @FXML private TableColumn<LeaveRequestDTO, Void> colAction;
    @FXML private TableColumn<LeaveRequestDTO, Long> colDays;

    @FXML private Button btnAdd, btnRefresh;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbStatus;

    private final LeaveRequestBUS leaveRequestBUS = LeaveRequestBUS.getInstance();
    private final LeaveTypeBUS leaveTypeBUS = LeaveTypeBUS.getInstance();
    private final SessionManagerService sessionManager = SessionManagerService.getInstance();

    private int currentEmployeeId;
    private int currentEmployeeRoleId;

    private ObservableList<LeaveRequestDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        currentEmployeeId = sessionManager.employeeLoginId();
        currentEmployeeRoleId = sessionManager.employeeRoleId();

        setupTable();
        setupFilter();
        setupEvents();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEmployeeName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colLeaveType.setCellValueFactory(new PropertyValueFactory<>("leaveTypeName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusName"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("content"));

        colDays.setCellValueFactory(cell -> {
            LeaveRequestDTO dto = cell.getValue();
            if (dto.getStartDate() != null && dto.getEndDate() != null) {
                long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
                return new javafx.beans.property.SimpleLongProperty(days).asObject();
            }
            return new javafx.beans.property.SimpleLongProperty(0).asObject();
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button approveBtn = new Button("Duyệt");
            private final Button rejectBtn = new Button("Từ chối");
            private final HBox box = new HBox(5, approveBtn, rejectBtn);

            {
                approveBtn.getStyleClass().add("btn-approve");
                rejectBtn.getStyleClass().add("btn-reject");

                approveBtn.setOnAction(e -> {
                    LeaveRequestDTO dto = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(dto.getId(), true);
                });

                rejectBtn.setOnAction(e -> {
                    LeaveRequestDTO dto = getTableView().getItems().get(getIndex());
                    handleUpdateStatus(dto.getId(), false);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || currentEmployeeRoleId != 1) {
                    setGraphic(null);
                    return;
                }

                LeaveRequestDTO dto = getTableView().getItems().get(getIndex());

                if (dto != null && dto.getStatusId() == 20) {
                    setGraphic(box);
                } else {
                    setGraphic(null);
                }
            }
        });
    }

    private void setupFilter() {
        cbStatus.setItems(FXCollections.observableArrayList(
                "Tất cả", "Chờ duyệt", "Đã duyệt", "Từ chối"
        ));
        cbStatus.getSelectionModel().selectFirst();
    }

    private void setupEvents() {
        btnAdd.setOnAction(e -> showDialog(null));
        btnRefresh.setOnAction(e -> loadData());

        cbStatus.setOnAction(e -> applyFilter());

        txtSearch.textProperty().addListener((obs, o, n) -> applyFilter());
    }

    private void loadData() {
        ArrayList<LeaveRequestDTO> list;

        if (currentEmployeeRoleId == 1) {
            list = leaveRequestBUS.getAll();
        } else {
            list = leaveRequestBUS.getByEmployeeId(currentEmployeeId);
        }

        masterData.setAll(list);
        applyFilter();
    }

    private void applyFilter() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase();
        String status = cbStatus.getValue();

        FilteredList<LeaveRequestDTO> filtered = masterData.filtered(item -> {

            boolean matchStatus = true;
            if (!"Tất cả".equals(status)) {
                matchStatus =
                        ("Chờ duyệt".equals(status) && item.getStatusId() == 20) ||
                        ("Đã duyệt".equals(status) && item.getStatusId() == 21) ||
                        ("Từ chối".equals(status) && item.getStatusId() == 22);
            }

            boolean matchSearch = ((String) item.getEmployeeName()).toLowerCase().contains(keyword)
                    || item.getContent().toLowerCase().contains(keyword);

            return matchStatus && matchSearch;
        });

        SortedList<LeaveRequestDTO> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblLeaveRequest.comparatorProperty());
        tblLeaveRequest.setItems(sorted);
    }

    private void handleUpdateStatus(int id, boolean approve) {
        BUSOperationResult result = approve
                ? leaveRequestBUS.approve(id)
                : leaveRequestBUS.reject(id);

        handleResult(result);
    }

    private void handleResult(BUSOperationResult result) {
        if (result.isSuccess()) {
            NotificationUtils.showInfoAlert("Thành công", "OK");
            loadData();
        } else {
            NotificationUtils.showErrorAlert("Lỗi xử lý", "Error");
        }
    }

    private void showDialog(LeaveRequestDTO editing) {
        Dialog<LeaveRequestDTO> dialog = new Dialog<>();
        dialog.setTitle("Đơn nghỉ");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        ComboBox<LeaveTypeDTO> cbType =
                new ComboBox<>(FXCollections.observableArrayList(leaveTypeBUS.getAll()));

        cbType.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(LeaveTypeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        cbType.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(LeaveTypeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        DatePicker dpStart = new DatePicker(LocalDate.now());
        DatePicker dpEnd = new DatePicker(LocalDate.now());
        TextArea ta = new TextArea();

        grid.add(new Label("Loại"), 0, 0);
        grid.add(cbType, 1, 0);
        grid.add(new Label("Từ"), 0, 1);
        grid.add(dpStart, 1, 1);
        grid.add(new Label("Đến"), 0, 2);
        grid.add(dpEnd, 1, 2);
        grid.add(new Label("Lý do"), 0, 3);
        grid.add(ta, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {

                if (dpEnd.getValue().isBefore(dpStart.getValue())) {
                    NotificationUtils.showErrorAlert("Sai ngày", "Error");
                    return null;
                }

                LeaveRequestDTO dto = new LeaveRequestDTO();
                dto.setEmployeeId(currentEmployeeId);
                dto.setLeaveTypeId(cbType.getValue().getId());
                dto.setStartDate(dpStart.getValue());
                dto.setEndDate(dpEnd.getValue());
                dto.setContent(ta.getText());
                return dto;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dto -> {
            BUSOperationResult result = leaveRequestBUS.insert(dto);
            handleResult(result);
        });
    }
}