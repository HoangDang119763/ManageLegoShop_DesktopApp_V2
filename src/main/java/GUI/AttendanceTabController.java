package GUI;

import BUS.TimeSheetBUS;
import BUS.EmployeeBUS;
import BUS.DepartmentBUS;
import DTO.*;
import ENUM.BUSOperationResult;
import UTILS.NotificationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class AttendanceTabController {
    private static final int PAGE_SIZE = 15;
    
    @FXML private TableView<TimeSheetDTO> tblAttendance;
    @FXML private TableColumn<TimeSheetDTO, Integer> colId;
    @FXML private TableColumn<TimeSheetDTO, String> colName, colStatus;
    @FXML private TableColumn<TimeSheetDTO, LocalDate> colDate;
    @FXML private TableColumn<TimeSheetDTO, LocalDateTime> colCheckIn, colCheckOut;
    @FXML private TableColumn<TimeSheetDTO, BigDecimal> colWorkHours, colOTHours;

    @FXML private ComboBox<YearMonth> cbMonth;
    @FXML private ComboBox<DepartmentDTO> cbDepartment;
    @FXML private ComboBox<Integer> cbYear;
    @FXML private TextField txtSearchEmployee;
    @FXML private DatePicker dpSpecificDate;

    @FXML private Button btnRefresh, btnImport, btnDownloadTemplate, btnPrevious, btnNext;
    @FXML private Label lblStatPresent, lblStatLate, lblStatAbsent, lblPageInfo;

    private TimeSheetBUS timeSheetBUS;
    private EmployeeBUS employeeBUS;
    private ArrayList<TimeSheetDTO> masterData = new ArrayList<>();
    private ArrayList<TimeSheetDTO> filteredData = new ArrayList<>();
    private int currentPageIndex = 0;

    @FXML
    public void initialize() {
        timeSheetBUS = TimeSheetBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();

        setupTable();
        setupFilters();
        setupListeners();
        loadAttendance();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colWorkHours.setCellValueFactory(new PropertyValueFactory<>("workHours"));
        colOTHours.setCellValueFactory(new PropertyValueFactory<>("otHours"));
        
        colDate.setCellValueFactory(cellData -> {
            LocalDateTime dt = cellData.getValue().getCheckIn();
            return new javafx.beans.property.SimpleObjectProperty<>(dt != null ? dt.toLocalDate() : null);
        });

        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));

        colCheckIn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null)
                        ? null
                        : item.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        });

        colCheckOut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null)
                        ? null
                        : item.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        });

        // Cột Trạng thái màu sắc
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    TimeSheetDTO data = getTableRow().getItem();
                    double hours = data.getWorkHours() != null ? data.getWorkHours().doubleValue() : 0;
                    if (hours >= 8) { setText("ĐỦ GIỜ"); setStyle("-fx-text-fill: green; -fx-font-weight: bold;"); }
                    else if (hours > 0) { setText("THIẾU GIỜ"); setStyle("-fx-text-fill: orange; -fx-font-weight: bold;"); }
                    else { setText("VẮNG"); setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); }
                }
            }
        });
    }

    private void setupFilters() {
        ArrayList<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 0; i < 12; i++) months.add(current.minusMonths(i));
        cbMonth.setItems(FXCollections.observableArrayList(months));
        cbMonth.setValue(current);

        ArrayList<DepartmentDTO> depts = DepartmentBUS.getInstance().getAll();
        cbDepartment.setItems(FXCollections.observableArrayList(depts));
    }

    private void setupListeners() {
        btnRefresh.setOnAction(e -> loadAttendance());
        btnImport.setOnAction(e -> handleImport());
        btnDownloadTemplate.setOnAction(e -> handleExport());
        
        btnPrevious.setOnAction(e -> { if (currentPageIndex > 0) displayPage(--currentPageIndex); });
        btnNext.setOnAction(e -> { if (currentPageIndex < getTotalPages() - 1) displayPage(++currentPageIndex); });

        cbMonth.setOnAction(e -> applyFilters());
        if(cbDepartment != null) cbDepartment.setOnAction(e -> applyFilters());
        if(dpSpecificDate != null) dpSpecificDate.setOnAction(e -> applyFilters());
        txtSearchEmployee.textProperty().addListener((obs, old, newVal) -> applyFilters());
    }


    private void loadAttendance() {
        masterData = timeSheetBUS.getAll();
        applyFilters();
    }

    private void applyFilters() {
        YearMonth selMonth = cbMonth.getValue();
        DepartmentDTO selDept = cbDepartment.getValue();
        LocalDate selDate = dpSpecificDate.getValue();
        String keyword = txtSearchEmployee.getText().toLowerCase().trim();

        filteredData = masterData.stream().filter(item -> {
            boolean matchMonth = (item.getCheckIn() != null && YearMonth.from(item.getCheckIn()).equals(selMonth));
            boolean matchDate = (selDate == null || (item.getCheckIn() != null && item.getCheckIn().toLocalDate().equals(selDate)));
            
            // Filter by Dept and Keyword (Name/ID)
            boolean matchDept = true;
            if (selDept != null) {
                EmployeeDTO emp = employeeBUS.getById(item.getEmployeeId());
                matchDept = (emp != null && emp.getDepartmentId() == selDept.getId());
            }
            
            String name = item.getEmployeeName() != null ? item.getEmployeeName().toLowerCase() : "";
            boolean matchKey = keyword.isEmpty() || name.contains(keyword) || String.valueOf(item.getEmployeeId()).contains(keyword);

            return matchMonth && matchDate && matchDept && matchKey;
        }).collect(Collectors.toCollection(ArrayList::new));

        currentPageIndex = 0;
        displayPage(0);
        updateDashboardStats();
    }

    private void displayPage(int pageIndex) {
        int total = filteredData.size();
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, total);
        
        tblAttendance.setItems(FXCollections.observableArrayList(new ArrayList<>(filteredData.subList(start, end))));
        
        int totalPages = getTotalPages();
        if (lblPageInfo != null) {
            lblPageInfo.setText(String.format("Trang %d / %d (Tổng: %d bản ghi)", pageIndex + 1, totalPages, total));
        }
        btnPrevious.setDisable(pageIndex == 0);
        btnNext.setDisable(pageIndex >= totalPages - 1);
    }

    private int getTotalPages() { return (int) Math.ceil((double) filteredData.size() / PAGE_SIZE); }

    private void updateDashboardStats() {
        long present = filteredData.stream().filter(t -> t.getWorkHours() != null && t.getWorkHours().doubleValue() > 0).count();
        long late = filteredData.stream().filter(t -> t.getWorkHours() != null && t.getWorkHours().doubleValue() < 8 && t.getWorkHours().doubleValue() > 0).count();
        
        if (lblStatPresent != null) lblStatPresent.setText(String.valueOf(present));
        if (lblStatLate != null) lblStatLate.setText(String.valueOf(late));
        if (lblStatAbsent != null) lblStatAbsent.setText("0");
    }

    // --- LOGIC ĐỌC EXCEL NÂNG CAO ---

    private static LocalDateTime getDateTimeFromCell(
        org.apache.poi.ss.usermodel.Cell cell,
        DateTimeFormatter fmt) {
        if (cell == null || cell.getCellType() == CellType.BLANK)
            return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }

            DataFormatter df = new DataFormatter();
            String val = df.formatCellValue(cell).trim();

            if (val.isEmpty()) return null;

            return LocalDateTime.parse(val, fmt);

        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRowEmpty(Row row) {

        if (row == null) return true;

        int first = row.getFirstCellNum();
        int last = row.getLastCellNum();

        if (first < 0) return true;

        for (int c = first; c < last; c++) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private void handleImport() {

        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file Excel chấm công");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        File file = fc.showOpenDialog(btnImport.getScene().getWindow());
        if (file == null) return;

        javafx.concurrent.Task<Integer> task = new javafx.concurrent.Task<>() {

            @Override
            protected Integer call() throws Exception {

                ArrayList<TimeSheetDTO> list = new ArrayList<>();
                DateTimeFormatter strFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                DataFormatter dataFormatter = new DataFormatter();

                try (FileInputStream fis = new FileInputStream(file);
                    Workbook wb = new XSSFWorkbook(fis)) {

                    Sheet sheet = wb.getSheetAt(0);

                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {

                        Row row = sheet.getRow(i);
                        if (isRowEmpty(row)) continue;

                        try {
                            TimeSheetDTO dto = new TimeSheetDTO();

                            // ===== 1. Employee ID =====
                            String idStr = dataFormatter
                                    .formatCellValue(row.getCell(0))
                                    .trim()
                                    .replaceAll("[^0-9]", "");

                            if (idStr.isEmpty()) continue;

                            int empId = Integer.parseInt(idStr);

                            // Check tồn tại nhân viên
                            if (employeeBUS.getById(empId) == null) {
                                System.err.println("NV không tồn tại: " + empId);
                                continue;
                            }

                            dto.setEmployeeId(empId);

                            // ===== 2. CheckIn / CheckOut =====
                            LocalDateTime checkIn =
                                    getDateTimeFromCell(row.getCell(1), strFmt);

                            LocalDateTime checkOut =
                                    getDateTimeFromCell(row.getCell(2), strFmt);

                            if (checkIn == null) continue;

                            dto.setCheckIn(checkIn);
                            dto.setCheckOut(checkOut);

                            // ===== 3. Tính WorkHours =====
                            if (checkOut != null && checkOut.isAfter(checkIn)) {

                                long minutes =
                                        java.time.Duration
                                                .between(checkIn, checkOut)
                                                .toMinutes();

                                double hours = minutes / 60.0;

                                dto.setWorkHours(BigDecimal.valueOf(hours));

                                if (hours > 8)
                                    dto.setOtHours(
                                            BigDecimal.valueOf(hours - 8)
                                    );
                                else
                                    dto.setOtHours(BigDecimal.ZERO);
                            }

                            list.add(dto);

                        } catch (Exception ex) {
                            System.err.println(
                                    "Lỗi dòng " + (i + 1) + ": " + ex.getMessage()
                            );
                        }
                    }

                    if (!list.isEmpty()) {
                        timeSheetBUS.bulkInsert(list);
                    }

                    return list.size();
                }
            }
        };

        task.setOnSucceeded(e -> {
            NotificationUtils.showInfoAlert(
                    "Thành công!",
                    "Đã import " + task.getValue() + " bản ghi."
            );
            Platform.runLater(this::loadAttendance);
        });

        task.setOnFailed(e -> {
            NotificationUtils.showErrorAlert(
                    "Lỗi Import",
                    "Kiểm tra định dạng: Mã NV | Giờ vào | Giờ ra"
            );
            task.getException().printStackTrace();
        });

        new Thread(task).start();
    }

    @FXML
    private void handleExport() {
        // 1. Kiểm tra nếu bảng trống
        ObservableList<TimeSheetDTO> dataToExport = tblAttendance.getItems();
        if (dataToExport == null || dataToExport.isEmpty()) {
            NotificationUtils.showErrorAlert("Lỗi", "Không có dữ liệu để xuất file!");
            return;
        }

        // 2. Cấu hình vị trí lưu file
        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu báo cáo chấm công");
        fc.setInitialFileName("Bao_Cao_Cham_Cong_" + LocalDate.now() + ".xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showSaveDialog(null);

        if (file == null) return;

        // 3. Thực hiện ghi file Excel
        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            Sheet s = wb.createSheet("Dữ liệu chấm công");
            
            // Tạo Header row
            String[] headers = {"Mã NV", "Họ Tên", "Ngày", "Giờ vào", "Giờ ra", "Giờ làm", "Giờ OT"};
            Row headerRow = s.createRow(0);
            
            // Định dạng Header (In đậm, màu nền)
            CellStyle headerStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Ghi dữ liệu từ TableView
            int rowIdx = 1;
            DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
            
            for (TimeSheetDTO dto : dataToExport) {
                Row r = s.createRow(rowIdx++);
                r.createCell(0).setCellValue(dto.getEmployeeId());
                r.createCell(1).setCellValue(dto.getEmployeeName() != null ? dto.getEmployeeName() : "N/A");
                r.createCell(2).setCellValue(dto.getCheckIn() != null ? dto.getCheckIn().toLocalDate().toString() : "N/A");
                
                // Format giờ
                r.createCell(3).setCellValue(dto.getCheckIn() != null ? dto.getCheckIn().format(timeFmt) : "");
                r.createCell(4).setCellValue(dto.getCheckOut() != null ? dto.getCheckOut().format(timeFmt) : "");
                
                // Số giờ công
                r.createCell(5).setCellValue(dto.getWorkHours() != null ? dto.getWorkHours().doubleValue() : 0.0);
                r.createCell(6).setCellValue(dto.getOtHours() != null ? dto.getOtHours().doubleValue() : 0.0);
            }

            // Tự động căn chỉnh độ rộng cột
            for (int i = 0; i < headers.length; i++) {
                s.autoSizeColumn(i);
            }

            wb.write(fos);
            NotificationUtils.showInfoAlert("Thành công", "Đã xuất dữ liệu ra file Excel!");

        } catch (Exception e) {
            e.printStackTrace();
            NotificationUtils.showErrorAlert("Lỗi", "Không thể ghi file: " + e.getMessage());
        }
    }
}