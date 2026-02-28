package GUI;

import BUS.TimeSheetBUS;
import BUS.EmployeeBUS;
import DTO.TimeSheetDTO;
import DTO.EmployeeDTO;
import DTO.BUSResult;
import ENUM.BUSOperationResult;
import ENUM.PermissionKey;
import SERVICE.SecureExecutor;
import UTILS.NotificationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;

public class AttendanceTabController {
    private static final int PAGE_SIZE = 10;
    
    @FXML
    private TableView<TimeSheetDTO> tblAttendance;
    @FXML
    private TableColumn<TimeSheetDTO, Integer> colId;
    @FXML
    private TableColumn<TimeSheetDTO, LocalDateTime> colCheckIn;
    @FXML
    private TableColumn<TimeSheetDTO, LocalDateTime> colCheckOut;
    @FXML
    private TableColumn<TimeSheetDTO, BigDecimal> colWorkHours;
    @FXML
    private TableColumn<TimeSheetDTO, BigDecimal> colOTHours;

    @FXML
    private ComboBox<YearMonth> cbMonth;
    @FXML
    private Button btnRefresh, btnImport, btnDownloadTemplate;
    @FXML
    private Button btnPrevious, btnNext;

    @FXML
    private Label lblEmployeeName;
    @FXML
    private Label lblTotalHours;
    @FXML
    private Label lblOTHours;
    @FXML
    private Label lblWorkDays;
    @FXML
    private Label lblPageInfo;

    private TimeSheetBUS timeSheetBUS;
    private EmployeeBUS employeeBUS;
    private int currentEmployeeId;
    private ArrayList<TimeSheetDTO> allTimesheets;
    private ArrayList<TimeSheetDTO> filteredTimesheets;
    private int currentPageIndex = 0;

    @FXML
    public void initialize() {
        timeSheetBUS = TimeSheetBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        allTimesheets = new ArrayList<>();
        filteredTimesheets = new ArrayList<>();

        setupTable();
        setupMonthCombo();
        setupListeners();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        colWorkHours.setCellValueFactory(new PropertyValueFactory<>("workHours"));
        colOTHours.setCellValueFactory(new PropertyValueFactory<>("otHours"));
    }

    private void setupMonthCombo() {
        ArrayList<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            months.add(current.minusMonths(i));
        }
        cbMonth.setItems(FXCollections.observableArrayList(months));
        cbMonth.setValue(YearMonth.now());
        cbMonth.valueProperty().addListener((obs, oldVal, newVal) -> loadAttendance());
    }

    private void setupListeners() {
        btnRefresh.setOnAction(e -> loadAttendance());
        btnImport.setOnAction(e -> handleImport());
        btnDownloadTemplate.setOnAction(e -> handleDownloadTemplate());
        btnPrevious.setOnAction(e -> showPreviousPage());
        btnNext.setOnAction(e -> showNextPage());
    }

    public void loadEmployeeAttendance(int employeeId) {
        this.currentEmployeeId = employeeId;
        EmployeeDTO emp = employeeBUS.getById(employeeId);
        if (emp != null) {
            lblEmployeeName.setText(emp.getFirstName() + " " + emp.getLastName());
        }
        loadAttendance();
    }
    private void loadAttendance() {
        allTimesheets.clear();
        allTimesheets.addAll(timeSheetBUS.getByEmployeeId(currentEmployeeId));

        // Filter by selected month
        filteredTimesheets.clear();
        YearMonth selectedMonth = cbMonth.getValue();

        for (TimeSheetDTO sheet : allTimesheets) {
            if (sheet.getCheckIn() != null) {
                YearMonth sheetMonth = YearMonth.from(sheet.getCheckIn().toLocalDate());
                if (sheetMonth.equals(selectedMonth)) {
                    filteredTimesheets.add(sheet);
                }
            }
        }

        currentPageIndex = 0;
        displayPage(0);
        updateStatistics();
    }

    private void showPreviousPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            displayPage(currentPageIndex);
        }
    }

    private void showNextPage() {
        int totalPages = (int) Math.ceil((double) filteredTimesheets.size() / PAGE_SIZE);
        if (filteredTimesheets.isEmpty()) totalPages = 1;
        if (currentPageIndex < totalPages - 1) {
            currentPageIndex++;
            displayPage(currentPageIndex);
        }
    }

    private void displayPage(int pageIndex) {
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredTimesheets.size());
        
        ArrayList<TimeSheetDTO> pageData = new ArrayList<>(filteredTimesheets.subList(start, end));
        tblAttendance.setItems(FXCollections.observableArrayList(pageData));
        
        // Update page info label
        int totalPages = filteredTimesheets.isEmpty() ? 1 : (int) Math.ceil((double) filteredTimesheets.size() / PAGE_SIZE);
        lblPageInfo.setText(String.format("Trang %d / %d (Tổng: %d ngày)", 
            pageIndex + 1, totalPages, filteredTimesheets.size()));
        
        // Update button states
        btnPrevious.setDisable(pageIndex == 0);
        btnNext.setDisable(pageIndex >= totalPages - 1);
    }
    
    private void updateStatistics() {
        // Calculate statistics for all filtered data
        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal totalOTHours = BigDecimal.ZERO;

        for (TimeSheetDTO sheet : filteredTimesheets) {
            if (sheet.getWorkHours() != null) {
                totalHours = totalHours.add(sheet.getWorkHours());
            }
            if (sheet.getOtHours() != null) {
                totalOTHours = totalOTHours.add(sheet.getOtHours());
            }
        }

        lblTotalHours.setText(String.format("%.1f giờ", totalHours));
        lblOTHours.setText(String.format("%.1f giờ OT", totalOTHours));
        lblWorkDays.setText(String.format("%d ngày", filteredTimesheets.size()));
    }

    private void handleImport() {
        BUSResult res = SecureExecutor.executeSafeBusResult(
            PermissionKey.EMPLOYEE_ATTENDANCE_MANAGE,
            () -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Chọn file Excel chấm công");
                fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
                );

                File selectedFile = fileChooser.showOpenDialog(btnImport.getScene().getWindow());
                if (selectedFile == null) {
                    return new BUSResult(BUSOperationResult.CANCELLED, "Hủy import");
                }

                // TODO: Implement actual Excel import logic
                // For now, just show a placeholder
                NotificationUtils.showInfoAlert("Đang import dữ liệu từ: " + selectedFile.getName(), "Import");
                
                return new BUSResult(BUSOperationResult.SUCCESS, "Import thành công");
            }
        );

        if (res.getCode() == BUSOperationResult.SUCCESS) {
            NotificationUtils.showInfoAlert(res.getMessage(), "Thành công");
            loadAttendance();
        } else if (res.getCode() != BUSOperationResult.CANCELLED) {
            NotificationUtils.showErrorAlert(res.getMessage(), "Lỗi");
        }
    }

    private void handleDownloadTemplate() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu file template chấm công");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            fileChooser.setInitialFileName("cham-cong-template.xlsx");

            File selectedFile = fileChooser.showSaveDialog(btnDownloadTemplate.getScene().getWindow());
            if (selectedFile != null) {
                generateExcelTemplate(selectedFile);
                NotificationUtils.showInfoAlert("File template đã được tải xuống tại: " + selectedFile.getAbsolutePath(), "Thành công");
            }
        } catch (Exception e) {
            NotificationUtils.showErrorAlert("Lỗi tải template: " + e.getMessage(), "Lỗi");
        }
    }

    private void generateExcelTemplate(File file) throws Exception {
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Chấm công");

        // Set sheet encoding and font
        sheet.getPrintSetup().setLandscape(false);

        // Create header row
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        String[] headers = {"Mã Nhân Viên", "Ngày Giờ Vào", "Ngày Giờ Ra"};
        
        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setCharSet(org.apache.poi.ss.usermodel.Font.DEFAULT_CHARSET);
        headerStyle.setFont(boldFont);

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(new org.apache.poi.xssf.usermodel.XSSFRichTextString(headers[i]));
            cell.setCellStyle(headerStyle);
        }

        // Create sample data rows
        for (int i = 1; i <= 5; i++) {
            org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(i);
            dataRow.createCell(0).setCellValue(new org.apache.poi.xssf.usermodel.XSSFRichTextString("NV000" + i));
            dataRow.createCell(1).setCellValue(new org.apache.poi.xssf.usermodel.XSSFRichTextString("2026-02-0" + i + " 08:00"));
            dataRow.createCell(2).setCellValue(new org.apache.poi.xssf.usermodel.XSSFRichTextString("2026-02-0" + i + " 17:00"));
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
        }

        // Write to file
        java.nio.file.Files.createDirectories(file.getParentFile().toPath());
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
    }
}
