package GUI;

import BUS.PayrollHistoryBUS;
import BUS.EmployeeBUS;
import DTO.PayrollHistoryDTO;
import DTO.EmployeeDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import SERVICE.SessionManagerService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PayrollTabController {

    @FXML
    private Button btnRefresh, btnPrevious, btnNext;
    @FXML
    private ComboBox<Integer> cbMonth;
    @FXML
    private ComboBox<Integer> cbYear;

    // Labels for displaying payroll data
    @FXML
    private Label lblPeriod;
    @FXML
    private Label lblBaseSalary;
    @FXML
    private Label lblActualWorkDays;
    @FXML
    private Label lblOvertimeAmount;
    @FXML
    private Label lblTotalAllowance;
    @FXML
    private Label lblRewardAmount;
    @FXML
    private Label lblViolationAmount;
    @FXML
    private Label lblTotalInsurance;
    @FXML
    private Label lblTaxableIncome;
    @FXML
    private Label lblTaxAmount;
    @FXML
    private Label lblNetSalary;

    private PayrollHistoryBUS payrollHistoryBUS;
    private EmployeeBUS employeeBUS;
    private SessionManagerService sessionManager;
    private int currentEmployeeId;
    private ArrayList<PayrollHistoryDTO> allPayrolls;
    private ArrayList<PayrollHistoryDTO> filteredPayrolls;
    private int currentDisplayIndex = 0;

    @FXML
    public void initialize() {
        payrollHistoryBUS = PayrollHistoryBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        sessionManager = SessionManagerService.getInstance();
        allPayrolls = new ArrayList<>();
        filteredPayrolls = new ArrayList<>();

        setupMonthYearCombo();
        setupListeners();
    }

    private void setupMonthYearCombo() {
        // Setup Month ComboBox (1-12)
        ArrayList<Integer> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(i);
        }
        cbMonth.setItems(FXCollections.observableArrayList(months));
        cbMonth.setValue(LocalDate.now().getMonthValue());

        // Setup Year ComboBox (5 năm gần nhất)
        ArrayList<Integer> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) {
            years.add(currentYear - i);
        }
        cbYear.setItems(FXCollections.observableArrayList(years));
        cbYear.setValue(currentYear);
    }

    private void setupListeners() {
        btnRefresh.setOnAction(e -> loadPayrolls());
        btnPrevious.setOnAction(e -> showPreviousPayroll());
        btnNext.setOnAction(e -> showNextPayroll());
        cbMonth.valueProperty().addListener((obs, oldVal, newVal) -> filterPayrolls());
        cbYear.valueProperty().addListener((obs, oldVal, newVal) -> filterPayrolls());
    }

    public void loadEmployeePayroll(int employeeId) {
        this.currentEmployeeId = employeeId;
        loadPayrolls();
    }

    private void loadPayrolls() {
        allPayrolls.clear();
        allPayrolls.addAll(payrollHistoryBUS.getByEmployeeId(currentEmployeeId));
        filterPayrolls();
    }

    private void filterPayrolls() {
        Integer selectedMonth = cbMonth.getValue();
        Integer selectedYear = cbYear.getValue();

        filteredPayrolls.clear();
        if (selectedMonth != null && selectedYear != null) {
            filteredPayrolls.addAll(allPayrolls.stream()
                .filter(p -> {
                    if (p.getSalaryPeriod() == null) {
                        return false;
                    }
                    return p.getSalaryPeriod().getMonthValue() == selectedMonth
                        && p.getSalaryPeriod().getYear() == selectedYear;
                })
                .collect(Collectors.toCollection(ArrayList::new)));
        }

        currentDisplayIndex = 0;
        displayCurrentPayroll();
    }

    private void showPreviousPayroll() {
        if (currentDisplayIndex > 0) {
            currentDisplayIndex--;
            displayCurrentPayroll();
        }
    }

    private void showNextPayroll() {
        if (currentDisplayIndex < filteredPayrolls.size() - 1) {
            currentDisplayIndex++;
            displayCurrentPayroll();
        }
    }

    private void displayCurrentPayroll() {
        if (filteredPayrolls.isEmpty()) {
            clearAllLabels();
            return;
        }

        PayrollHistoryDTO payroll = filteredPayrolls.get(currentDisplayIndex);
        updatePayrollDisplay(payroll);
        
        // Update button states
        btnPrevious.setDisable(currentDisplayIndex == 0);
        btnNext.setDisable(currentDisplayIndex == filteredPayrolls.size() - 1);
    }

    private void updatePayrollDisplay(PayrollHistoryDTO payroll) {
        if (payroll == null) {
            clearAllLabels();
            return;
        }

        lblPeriod.setText(payroll.getSalaryPeriod() != null ? 
            payroll.getSalaryPeriod().toString() : "--");
        lblBaseSalary.setText(formatCurrency(payroll.getBaseSalary()));
        lblActualWorkDays.setText(String.valueOf(payroll.getActualWorkDays()));
        lblOvertimeAmount.setText(formatCurrency(payroll.getOvertimeAmount()));
        lblTotalAllowance.setText(formatCurrency(payroll.getTotalAllowance()));
        lblRewardAmount.setText(formatCurrency(payroll.getRewardAmount()));
        lblViolationAmount.setText(formatCurrency(payroll.getViolationAmount()));
        lblTotalInsurance.setText(formatCurrency(payroll.getTotalInsurance()));
        lblTaxableIncome.setText(formatCurrency(payroll.getTaxableIncome()));
        lblTaxAmount.setText(formatCurrency(payroll.getTaxAmount()));
        lblNetSalary.setText(formatCurrency(payroll.getNetSalary()));
    }

    private void clearAllLabels() {
        lblPeriod.setText("--");
        lblBaseSalary.setText("--");
        lblActualWorkDays.setText("--");
        lblOvertimeAmount.setText("--");
        lblTotalAllowance.setText("--");
        lblRewardAmount.setText("--");
        lblViolationAmount.setText("--");
        lblTotalInsurance.setText("--");
        lblTaxableIncome.setText("--");
        lblTaxAmount.setText("--");
        lblNetSalary.setText("--");
    }

    private String formatCurrency(Object value) {
        if (value == null) {
            return "--";
        }
        try {
            double amount = Double.parseDouble(value.toString());
            return String.format("%,.0f đ", amount);
        } catch (NumberFormatException e) {
            return "--";
        }
    }
}


