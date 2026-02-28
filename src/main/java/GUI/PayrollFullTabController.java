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
import javafx.scene.layout.StackPane;
import UTILS.NotificationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PayrollFullTabController {
    private static final int PAGE_SIZE = 10;

    @FXML
    private TableView<PayrollHistoryDTO> tblPayroll;
    @FXML
    private TableColumn<PayrollHistoryDTO, Integer> colEmployeeId;
    @FXML
    private TableColumn<PayrollHistoryDTO, String> colEmployeeName;
    @FXML
    private TableColumn<PayrollHistoryDTO, LocalDate> colPeriod;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colBaseSalary;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colActualWorkDays;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colOvertimeAmount;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colTotalAllowance;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colRewardAmount;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colViolationAmount;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colTotalInsurance;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colTaxableIncome;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colTaxAmount;
    @FXML
    private TableColumn<PayrollHistoryDTO, BigDecimal> colNetSalary;

    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<Integer> cbMonth;
    @FXML
    private ComboBox<Integer> cbYear;
    @FXML
    private Button btnCalculateAll, btnRefresh;
    @FXML
    private Pagination pagination;
    @FXML
    private Label lblPageInfo;

    private PayrollHistoryBUS payrollHistoryBUS;
    private PayRollBUS payrollBUS;
    private EmployeeBUS employeeBUS;
    private ArrayList<PayrollHistoryDTO> allPayrolls;
    private ArrayList<PayrollHistoryDTO> filteredPayrolls;
    private int currentPage = 0;

    @FXML
    public void initialize() {
        payrollHistoryBUS = PayrollHistoryBUS.getInstance();
        payrollBUS = new PayRollBUS();
        employeeBUS = EmployeeBUS.getInstance();
        allPayrolls = new ArrayList<>();
        filteredPayrolls = new ArrayList<>();

        setupTable();
        setupMonthYearCombo();
        setupPagination();
        setupListeners();
        loadPayrolls();
    }

    private void setupPagination() {
        if (pagination != null) {
            pagination.setPageFactory(pageIndex -> {
                currentPage = pageIndex;
                displayPage(pageIndex);
                return new StackPane();
            });
        }
    }

    private void displayPage(int pageIndex) {
        int totalPages = (int) Math.ceil(filteredPayrolls.size() / (double) PAGE_SIZE);
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredPayrolls.size());
        
        ArrayList<PayrollHistoryDTO> pageData = new ArrayList<>();
        if (start < filteredPayrolls.size()) {
            pageData.addAll(filteredPayrolls.subList(start, end));
        }
        tblPayroll.setItems(FXCollections.observableArrayList(pageData));
        
        // Update page info label
        if (lblPageInfo != null) {
            lblPageInfo.setText(String.format("Trang %d / %d (Tổng: %d nhân viên)", 
                pageIndex + 1, Math.max(1, totalPages), filteredPayrolls.size()));
        }
    }

    private void setupTable() {
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        
        // Custom cell factory for employee name
        colEmployeeName.setCellValueFactory(cellData -> {
            int empId = cellData.getValue().getEmployeeId();
            EmployeeDTO emp = employeeBUS.getById(empId);
            return new javafx.beans.property.SimpleObjectProperty<>(
                emp != null ? emp.getFirstName() + " " + emp.getLastName() : "N/A");
        });
        
        colPeriod.setCellValueFactory(new PropertyValueFactory<>("salaryPeriod"));
        colBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
        colActualWorkDays.setCellValueFactory(new PropertyValueFactory<>("actualWorkDays"));
        colOvertimeAmount.setCellValueFactory(new PropertyValueFactory<>("overtimeAmount"));
        colTotalAllowance.setCellValueFactory(new PropertyValueFactory<>("totalAllowance"));
        colRewardAmount.setCellValueFactory(new PropertyValueFactory<>("rewardAmount"));
        colViolationAmount.setCellValueFactory(new PropertyValueFactory<>("violationAmount"));
        colTotalInsurance.setCellValueFactory(new PropertyValueFactory<>("totalInsurance"));
        colTaxableIncome.setCellValueFactory(new PropertyValueFactory<>("taxableIncome"));
        colTaxAmount.setCellValueFactory(new PropertyValueFactory<>("taxAmount"));
        colNetSalary.setCellValueFactory(new PropertyValueFactory<>("netSalary"));
    }

    private void setupMonthYearCombo() {
        // Setup Month ComboBox (1-12)
        ArrayList<Integer> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(i);
        }
        cbMonth.setItems(FXCollections.observableArrayList(months));
        cbMonth.setValue(java.time.LocalDate.now().getMonthValue());

        // Setup Year ComboBox (5 năm gần nhất)
        ArrayList<Integer> years = new ArrayList<>();
        int currentYear = java.time.LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) {
            years.add(currentYear - i);
        }
        cbYear.setItems(FXCollections.observableArrayList(years));
        cbYear.setValue(currentYear);
    }

    private void setupListeners() {
        btnCalculateAll.setOnAction(e -> handleCalculateAll());
        btnRefresh.setOnAction(e -> loadPayrolls());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterPayrolls());
        cbMonth.valueProperty().addListener((obs, oldVal, newVal) -> filterPayrolls());
        cbYear.valueProperty().addListener((obs, oldVal, newVal) -> filterPayrolls());
    }

    private void loadPayrolls() {
        allPayrolls.clear();
        allPayrolls.addAll(payrollHistoryBUS.getAll());
        filterPayrolls();
    }

    private void filterPayrolls() {
        String searchText = txtSearch.getText().toLowerCase();
        Integer selectedMonth = cbMonth.getValue();
        Integer selectedYear = cbYear.getValue();

        filteredPayrolls = allPayrolls.stream()
            .filter(p -> {
                // Search by employee name or ID
                EmployeeDTO emp = employeeBUS.getById(p.getEmployeeId());
                String empName = emp != null ? (emp.getFirstName() + " " + emp.getLastName()).toLowerCase() : "";
                boolean matchesSearch = empName.contains(searchText) || String.valueOf(p.getEmployeeId()).contains(searchText);
                
                // Filter by month and year
                boolean matchesPeriod = true;
                if (selectedMonth != null && selectedYear != null && p.getSalaryPeriod() != null) {
                    matchesPeriod = p.getSalaryPeriod().getMonthValue() == selectedMonth 
                                 && p.getSalaryPeriod().getYear() == selectedYear;
                }
                
                return matchesSearch && matchesPeriod;
            })
            .collect(Collectors.toCollection(ArrayList::new));

        // Reset to first page and update pagination
        currentPage = 0;
        if (pagination != null) {
            int totalPages = Math.max(1, (int) Math.ceil(filteredPayrolls.size() / (double) PAGE_SIZE));
            pagination.setPageCount(totalPages);
            pagination.setCurrentPageIndex(0);
        } else {
            displayPage(0);
        }
    }

    @FXML
    private void handleCalculateAll() {
        Integer month = cbMonth.getValue();
        Integer year = cbYear.getValue();
        if (month == null || year == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn tháng và năm", "Cảnh báo");
            return;
        }

        // Get all employees
        ArrayList<EmployeeDTO> employees = employeeBUS.getAll();
        if (employees.isEmpty()) {
            NotificationUtils.showErrorAlert("Không có nhân viên nào", "Thông báo");
            return;
        }

        // Calculate salary for all employees in the selected month
        int successCount = 0;
        String periodDate = String.format("%04d-%02d-01", year, month);
        
        for (EmployeeDTO emp : employees) {
            try {
                if (payrollBUS.calculateMonthlySalary(emp.getId(), periodDate)) {
                    successCount++;
                }
            } catch (Exception e) {
                // Continue with next employee if calculation fails
                System.err.println("Error calculating salary for employee " + emp.getId() + ": " + e.getMessage());
            }
        }

        NotificationUtils.showInfoAlert(
            "Tính lương thành công cho " + successCount + "/" + employees.size() + " nhân viên",
            "Hoàn thành"
        );
        loadPayrolls();
    }
}
