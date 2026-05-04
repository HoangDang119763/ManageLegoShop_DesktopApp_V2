package GUI;

import BUS.PayrollHistoryBUS;
import DTO.PayrollHistoryDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import SERVICE.SessionManagerService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileOutputStream;

public class PayrollTabController {

    @FXML
    private Button btnRefresh, btnPrevious, btnNext;
    @FXML
    private Button btnExportMonth, btnExportYear;
    @FXML
    private ComboBox<Integer> cbMonth;
    @FXML
    private ComboBox<Integer> cbYear;

    @FXML private Label lblPeriod;
    @FXML private Label lblBaseSalary;
    @FXML private Label lblActualWorkDays;
    @FXML private Label lblOvertimeAmount;
    @FXML private Label lblTotalAllowance;
    @FXML private Label lblRewardAmount;
    @FXML private Label lblViolationAmount;
    @FXML private Label lblTotalInsurance;
    @FXML private Label lblTaxableIncome;
    @FXML private Label lblTaxAmount;
    @FXML private Label lblNetSalary;

    private PayrollHistoryBUS payrollHistoryBUS;
    private SessionManagerService sessionManager;

    private int currentEmployeeId;
    private ArrayList<PayrollHistoryDTO> allPayrolls;
    private ArrayList<PayrollHistoryDTO> filteredPayrolls;
    private int currentDisplayIndex = 0;

    @FXML
    public void initialize() {
        payrollHistoryBUS = PayrollHistoryBUS.getInstance();
        sessionManager = SessionManagerService.getInstance();

        allPayrolls = new ArrayList<>();
        filteredPayrolls = new ArrayList<>();

        setupMonthYearCombo();
        setupListeners();

        loadPayrolls();
    }

    public void loadEmployeePayroll(int employeeId) {
        this.currentEmployeeId = employeeId;
        loadPayrolls();
    }

    private void setupMonthYearCombo() {
        ArrayList<Integer> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) months.add(i);

        cbMonth.setItems(FXCollections.observableArrayList(months));
        cbMonth.setValue(LocalDate.now().getMonthValue());

        ArrayList<Integer> years = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = 0; i < 5; i++) years.add(currentYear - i);

        cbYear.setItems(FXCollections.observableArrayList(years));
        cbYear.setValue(currentYear);
    }

    private void setupListeners() {
        btnRefresh.setOnAction(e -> loadPayrolls());
        btnPrevious.setOnAction(e -> showPreviousPayroll());
        btnNext.setOnAction(e -> showNextPayroll());
        btnExportMonth.setOnAction(e -> exportPayrollMonth());
        btnExportYear.setOnAction(e -> exportPayrollYear());

        cbMonth.valueProperty().addListener((obs, o, n) -> filterPayrolls());
        cbYear.valueProperty().addListener((obs, o, n) -> filterPayrolls());
    }

    private void loadPayrolls() {
        allPayrolls.clear();
        allPayrolls.addAll(payrollHistoryBUS.getByEmployeeId(currentEmployeeId));
        filterPayrolls();
    }

    private void filterPayrolls() {
        Integer m = cbMonth.getValue();
        Integer y = cbYear.getValue();

        filteredPayrolls = allPayrolls.stream()
                .filter(p -> p.getSalaryPeriod() != null
                        && p.getSalaryPeriod().getMonthValue() == m
                        && p.getSalaryPeriod().getYear() == y)
                .collect(Collectors.toCollection(ArrayList::new));

        currentDisplayIndex = 0;
        displayCurrentPayroll();
    }

    private void displayCurrentPayroll() {
        if (filteredPayrolls.isEmpty()) {
            clearAllLabels();
            return;
        }

        PayrollHistoryDTO payroll = filteredPayrolls.get(currentDisplayIndex);
        updatePayrollDisplay(payroll);

        btnPrevious.setDisable(currentDisplayIndex == 0);
        btnNext.setDisable(currentDisplayIndex == filteredPayrolls.size() - 1);
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

    private void updatePayrollDisplay(PayrollHistoryDTO p) {
        lblPeriod.setText(p.getSalaryPeriod() != null ? p.getSalaryPeriod().toString() : "--");
        lblBaseSalary.setText(formatCurrency(p.getBaseSalary()));
        lblActualWorkDays.setText(String.valueOf(p.getActualWorkDays()));
        lblOvertimeAmount.setText(formatCurrency(p.getOvertimeAmount()));
        lblTotalAllowance.setText(formatCurrency(p.getTotalAllowance()));
        lblRewardAmount.setText(formatCurrency(p.getRewardAmount()));
        lblViolationAmount.setText(formatCurrency(p.getViolationAmount()));
        lblTotalInsurance.setText(formatCurrency(p.getTotalInsurance()));
        lblTaxableIncome.setText(formatCurrency(p.getTaxableIncome()));
        lblTaxAmount.setText(formatCurrency(p.getTaxAmount()));
        lblNetSalary.setText(formatCurrency(p.getNetSalary()));
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

    // ✅ FIX: dùng BigDecimal chuẩn
    private String formatCurrency(BigDecimal value) {
        if (value == null) return "--";
        return String.format("%,.0f đ", value);
    }

    private void exportPayrollMonth() {
        Integer month = cbMonth.getValue();
        Integer year = cbYear.getValue();

        if (month == null || year == null) {
            showAlert("Cảnh báo", "Vui lòng chọn tháng và năm", Alert.AlertType.WARNING);
            return;
        }

        ArrayList<PayrollHistoryDTO> payrollsToExport =
                payrollHistoryBUS.getByEmployeeAndMonth(currentEmployeeId, month, year);

        if (payrollsToExport.isEmpty()) {
            showAlert("Thông báo", "Không có dữ liệu", Alert.AlertType.INFORMATION);
            return;
        }

        exportToExcel(payrollsToExport, "BangLuong_" + month + "_" + year);
    }

    private void exportPayrollYear() {
        Integer year = cbYear.getValue();

        if (year == null) {
            showAlert("Cảnh báo", "Vui lòng chọn năm", Alert.AlertType.WARNING);
            return;
        }

        ArrayList<PayrollHistoryDTO> payrollsToExport =
                payrollHistoryBUS.getByEmployeeAndYear(currentEmployeeId, year);

        if (payrollsToExport.isEmpty()) {
            showAlert("Thông báo", "Không có dữ liệu", Alert.AlertType.INFORMATION);
            return;
        }

        exportToExcel(payrollsToExport, "BangLuong_" + year);
    }

    private void exportToExcel(ArrayList<PayrollHistoryDTO> list, String fileNamePrefix) {
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                    new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet =
                    workbook.createSheet("Payroll");

            String[] headers = {
                    "Kỳ", "Lương cơ bản", "Ngày công", "Tăng ca",
                    "Trợ cấp", "Khen thưởng", "Vi phạm",
                    "Bảo hiểm", "Thu nhập chịu thuế", "Thuế", "Thực nhận"
            };

            // header
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            // data
            int rowNum = 1;
            for (PayrollHistoryDTO p : list) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(
                        p.getSalaryPeriod() != null ? p.getSalaryPeriod().toString() : "");

                row.createCell(1).setCellValue(getDouble(p.getBaseSalary()));
                row.createCell(2).setCellValue(getDouble(p.getActualWorkDays()));
                row.createCell(3).setCellValue(getDouble(p.getOvertimeAmount()));
                row.createCell(4).setCellValue(getDouble(p.getTotalAllowance()));
                row.createCell(5).setCellValue(getDouble(p.getRewardAmount()));
                row.createCell(6).setCellValue(getDouble(p.getViolationAmount()));
                row.createCell(7).setCellValue(getDouble(p.getTotalInsurance()));
                row.createCell(8).setCellValue(getDouble(p.getTaxableIncome()));
                row.createCell(9).setCellValue(getDouble(p.getTaxAmount()));
                row.createCell(10).setCellValue(getDouble(p.getNetSalary()));
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName(fileNamePrefix + ".xlsx");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel", "*.xlsx"));

            File file = fileChooser.showSaveDialog(btnExportMonth.getScene().getWindow());

            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                    showAlert("Thành công", "Đã xuất file", Alert.AlertType.INFORMATION);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private double getDouble(BigDecimal val) {
        return val != null ? val.doubleValue() : 0;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}