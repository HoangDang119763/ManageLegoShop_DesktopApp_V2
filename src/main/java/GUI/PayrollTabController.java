package GUI;

import BUS.PayrollHistoryBUS;
import BUS.PayRollBUS;
import BUS.EmployeeBUS;
import DTO.PayrollHistoryDTO;
import DTO.EmployeeDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import UTILS.NotificationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class PayrollTabController {
    @FXML
    private TableView<PayrollHistoryDTO> tblPayroll;
    @FXML
    private TableColumn<PayrollHistoryDTO, LocalDate> colPeriod;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colTemporarySalary;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colOvertime;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colAllowance;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colBonus;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colDeduction;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colFine;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colNetSalary;

    @FXML
    private ComboBox<YearMonth> cbPeriod;
    @FXML
    private Button btnCalculate, btnRefresh;

    @FXML
    private Label lblEmployeeName;
    @FXML
    private Label lblBaseSalary;
    @FXML
    private Label lblTemporarySalary;
    @FXML
    private Label lblNetSalary;

    private PayrollHistoryBUS payrollHistoryBUS;
    private PayRollBUS payrollBUS;
    private EmployeeBUS employeeBUS;
    private int currentEmployeeId;

    @FXML
    public void initialize() {
        payrollHistoryBUS = PayrollHistoryBUS.getInstance();
        payrollBUS = new PayRollBUS();
        employeeBUS = EmployeeBUS.getInstance();

        setupTable();
        setupPeriodCombo();
        setupListeners();
    }

    private void setupTable() {
        colPeriod.setCellValueFactory(new PropertyValueFactory<>("salaryPeriod"));
        colTemporarySalary.setCellValueFactory(new PropertyValueFactory<>("temporarySalary"));
        colOvertime.setCellValueFactory(new PropertyValueFactory<>("overtimeAmount"));
        colAllowance.setCellValueFactory(new PropertyValueFactory<>("totalAllowance"));
        colBonus.setCellValueFactory(new PropertyValueFactory<>("totalBonus"));
        colDeduction.setCellValueFactory(new PropertyValueFactory<>("totalDeduction"));
        colFine.setCellValueFactory(new PropertyValueFactory<>("totalFine"));
        colNetSalary.setCellValueFactory(new PropertyValueFactory<>("netSalary"));
    }

    private void setupPeriodCombo() {
        ArrayList<YearMonth> periods = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 23; i >= 0; i--) {
            periods.add(current.minusMonths(i));
        }
        cbPeriod.setItems(FXCollections.observableArrayList(periods));
        cbPeriod.setValue(YearMonth.now());
        cbPeriod.valueProperty().addListener((obs, oldVal, newVal) -> loadPayrolls());
    }

    private void setupListeners() {
        btnCalculate.setOnAction(e -> handleCalculate());
        btnRefresh.setOnAction(e -> loadPayrolls());
        tblPayroll.setOnMouseClicked(e -> loadSelectedPayroll());
    }

    public void loadEmployeePayroll(int employeeId) {
        this.currentEmployeeId = employeeId;
        EmployeeDTO emp = employeeBUS.getById(employeeId);
        if (emp != null) {
            lblEmployeeName.setText(emp.getFirstName() + " " + emp.getLastName());
        }
        loadPayrolls();
    }

    private void loadPayrolls() {
        ArrayList<PayrollHistoryDTO> payrolls = payrollHistoryBUS.getByEmployeeId(currentEmployeeId);
        tblPayroll.setItems(FXCollections.observableArrayList(payrolls));
    }

    private void loadSelectedPayroll() {
        PayrollHistoryDTO selected = tblPayroll.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lblTemporarySalary.setText(String.format("%,.0f đ", selected.getTemporarySalary()));
            lblNetSalary.setText(String.format("%,.0f đ", selected.getNetSalary()));
        }
    }

    @FXML
    private void handleCalculate() {
        YearMonth selected = cbPeriod.getValue();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn kỳ tính lương", "Cảnh báo");
            return;
        }

        String periodDate = selected.atDay(1).toString() + "";
        if (payrollBUS.calculateMonthlySalary(currentEmployeeId, periodDate)) {
            NotificationUtils.showInfoAlert("Tính lương tháng thành công", "Thành công");
            loadPayrolls();
        } else {
            NotificationUtils.showErrorAlert("Không thể tính lương tháng", "Lỗi");
        }
    }
}
