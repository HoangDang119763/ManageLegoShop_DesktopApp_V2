package GUI;

import BUS.AllowanceBUS;
import BUS.EmployeeBUS;
import DTO.AllowanceDTO;
import DTO.EmployeeDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import UTILS.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class AllowanceTabController {
    @FXML
    private TableView<AllowanceDTO> tblAllowance;
    @FXML
    private TableColumn<AllowanceDTO, Integer> colId;
    @FXML
    private TableColumn<AllowanceDTO, LocalDate> colPeriod;
    @FXML
    private TableColumn<AllowanceDTO, BigDecimal> colAttendanceBonus;
    @FXML
    private TableColumn<AllowanceDTO, BigDecimal> colLeaveDays;
    @FXML
    private TableColumn<AllowanceDTO, BigDecimal> colTransport;
    @FXML
    private TableColumn<AllowanceDTO, BigDecimal> colAccommodation;

    @FXML
    private ComboBox<YearMonth> cbPeriod;
    @FXML
    private Button btnAdd, btnEdit, btnDelete, btnRefresh;

    @FXML
    private TextField txtAttendanceBonus;
    @FXML
    private TextField txtLeaveDays;
    @FXML
    private TextField txtTransport;
    @FXML
    private TextField txtAccommodation;
    @FXML
    private Label lblEmployeeName;

    private AllowanceBUS allowanceBUS;
    private EmployeeBUS employeeBUS;
    private int currentEmployeeId;

    @FXML
    public void initialize() {
        allowanceBUS = AllowanceBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();

        setupTable();
        setupPeriodCombo();
        setupListeners();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPeriod.setCellValueFactory(new PropertyValueFactory<>("salaryPeriod"));
        colAttendanceBonus.setCellValueFactory(new PropertyValueFactory<>("attendanceBonus"));
        colLeaveDays.setCellValueFactory(new PropertyValueFactory<>("annualLeaveDays"));
        colTransport.setCellValueFactory(new PropertyValueFactory<>("transportationSupport"));
        colAccommodation.setCellValueFactory(new PropertyValueFactory<>("accommodationSupport"));
    }

    private void setupPeriodCombo() {
        ArrayList<YearMonth> periods = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            periods.add(current.minusMonths(i));
        }
        cbPeriod.setItems(FXCollections.observableArrayList(periods));
        cbPeriod.valueProperty().addListener((obs, oldVal, newVal) -> loadAllowances());
    }

    private void setupListeners() {
        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadAllowances());
        tblAllowance.setOnMouseClicked(e -> loadSelectedAllowance());
    }

    public void loadEmployeeAllowances(int employeeId) {
        this.currentEmployeeId = employeeId;
        EmployeeDTO emp = employeeBUS.getById(employeeId);
        if (emp != null) {
            lblEmployeeName.setText(emp.getFirstName() + " " + emp.getLastName());
        }
        loadAllowances();
    }

    private void loadAllowances() {
        ArrayList<AllowanceDTO> allowances = allowanceBUS.getByEmployeeId(currentEmployeeId);
        tblAllowance.setItems(FXCollections.observableArrayList(allowances));
    }

    private void loadSelectedAllowance() {
        AllowanceDTO selected = tblAllowance.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtAttendanceBonus
                    .setText(selected.getAttendanceBonus() != null ? selected.getAttendanceBonus().toString() : "0");
            txtLeaveDays
                    .setText(selected.getAnnualLeaveDays() != null ? selected.getAnnualLeaveDays().toString() : "0");
            txtTransport.setText(
                    selected.getTransportationSupport() != null ? selected.getTransportationSupport().toString() : "0");
            txtAccommodation.setText(
                    selected.getAccommodationSupport() != null ? selected.getAccommodationSupport().toString() : "0");
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) {
            NotificationUtils.showErrorAlert("Vui lòng kiểm tra lại dữ liệu", "Lỗi nhập liệu");
            return;
        }

        AllowanceDTO allowance = new AllowanceDTO();
        allowance.setEmployeeId(currentEmployeeId);
        allowance.setSalaryPeriod(cbPeriod.getValue().atDay(1));
        allowance.setAttendanceBonus(
                new BigDecimal(txtAttendanceBonus.getText().isEmpty() ? "0" : txtAttendanceBonus.getText()));
        allowance.setAnnualLeaveDays(new BigDecimal(txtLeaveDays.getText().isEmpty() ? "0" : txtLeaveDays.getText()));
        allowance.setTransportationSupport(
                new BigDecimal(txtTransport.getText().isEmpty() ? "0" : txtTransport.getText()));
        allowance.setAccommodationSupport(
                new BigDecimal(txtAccommodation.getText().isEmpty() ? "0" : txtAccommodation.getText()));

        if (allowanceBUS.insert(allowance, 1, 1)) {
            NotificationUtils.showInfoAlert("Thêm trợ cấp thành công", "Thành công");
            clearInputs();
            loadAllowances();
        } else {
            NotificationUtils.showErrorAlert("Không thể thêm trợ cấp", "Lỗi");
        }
    }

    @FXML
    private void handleEdit() {
        AllowanceDTO selected = tblAllowance.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn trợ cấp để sửa", "Cảnh báo");
            return;
        }

        selected.setAttendanceBonus(
                new BigDecimal(txtAttendanceBonus.getText().isEmpty() ? "0" : txtAttendanceBonus.getText()));
        selected.setAnnualLeaveDays(new BigDecimal(txtLeaveDays.getText().isEmpty() ? "0" : txtLeaveDays.getText()));
        selected.setTransportationSupport(
                new BigDecimal(txtTransport.getText().isEmpty() ? "0" : txtTransport.getText()));
        selected.setAccommodationSupport(
                new BigDecimal(txtAccommodation.getText().isEmpty() ? "0" : txtAccommodation.getText()));

        if (allowanceBUS.update(selected, 1, 1)) {
            NotificationUtils.showInfoAlert("Cập nhật trợ cấp thành công", "Thành công");
            clearInputs();
            loadAllowances();
        } else {
            NotificationUtils.showErrorAlert("Không thể cập nhật trợ cấp", "Lỗi");
        }
    }

    @FXML
    private void handleDelete() {
        AllowanceDTO selected = tblAllowance.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn trợ cấp để xóa", "Cảnh báo");
            return;
        }

        boolean confirmed = UiUtils.gI().showConfirmAlert("Xóa trợ cấp này?", "Xác nhận xóa");
        if (confirmed) {
            if (allowanceBUS.delete(selected.getId(), 1, 1)) {
                NotificationUtils.showInfoAlert("Xóa trợ cấp thành công", "Thành công");
                clearInputs();
                loadAllowances();
            }
        }
    }

    private boolean validateInputs() {

        if (!txtAttendanceBonus.getText().isEmpty()) {
            try {
                BigDecimal val = new BigDecimal(txtAttendanceBonus.getText());
                if (val.signum() < 0)
                    return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        if (!txtLeaveDays.getText().isEmpty()) {
            try {
                BigDecimal val = new BigDecimal(txtLeaveDays.getText());
                if (val.signum() < 0)
                    return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    private void clearInputs() {
        txtAttendanceBonus.clear();
        txtLeaveDays.clear();
        txtTransport.clear();
        txtAccommodation.clear();
        tblAllowance.getSelectionModel().clearSelection();
    }
}
