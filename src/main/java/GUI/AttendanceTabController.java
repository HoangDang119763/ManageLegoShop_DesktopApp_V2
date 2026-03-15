package GUI;

import BUS.TimeSheetBUS;
import BUS.EmployeeBUS;
import BUS.DepartmentBUS;
import DTO.*;
import UTILS.NotificationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AttendanceTabController {
    private static final int PAGE_SIZE = 15;
    
    @FXML private TableView<TimeSheetDTO> tblAttendance;
    @FXML private TableColumn<TimeSheetDTO, Integer> colId;
    @FXML private TableColumn<TimeSheetDTO, String> colName, colStatus;
    @FXML private TableColumn<TimeSheetDTO, LocalDateTime> colCheckIn, colCheckOut;
    @FXML private TableColumn<TimeSheetDTO, BigDecimal> colWorkHours, colOTHours;

    @FXML private ComboBox<Integer> cbDay, cbMonth, cbYear;
    @FXML private ComboBox<DepartmentDTO> cbDepartment;
    @FXML private TextField txtSearchEmployee;

    @FXML private Button btnRefresh, btnImport, btnDownloadTemplate, btnExport, btnPrevious, btnNext;
    @FXML private Label lblStatPresent, lblTotalHours, lblOTHours, lblWorkDays, lblPageInfo, lblEmployeeName, lblStatLate, lblStatAbsent;

    private TimeSheetBUS timeSheetBUS;
    private EmployeeBUS employeeBUS;
    private ArrayList<TimeSheetDTO> masterData = new ArrayList<>();
    private ArrayList<TimeSheetDTO> filteredData = new ArrayList<>();
    private int currentPageIndex = 0;
    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        timeSheetBUS = TimeSheetBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        setupTable();
        setupDateTimeFilters();
        setupDepartmentFilter();
        setupListeners();
        loadAttendance();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        colWorkHours.setCellValueFactory(new PropertyValueFactory<>("workHours"));
        colOTHours.setCellValueFactory(new PropertyValueFactory<>("otHours"));
        

        colCheckIn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        colCheckOut.setCellValueFactory(new PropertyValueFactory<>("checkOut"));

        colCheckIn.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.format(timeFmt));
            }
        });
        colCheckOut.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.format(timeFmt));
            }
        });
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setText(null); setStyle(""); }
                else {
                    TimeSheetDTO data = getTableRow().getItem();
                    double hours = data.getWorkHours() != null ? data.getWorkHours().doubleValue() : 0;
                    if (hours >= 8) { setText("ĐỦ GIỜ"); setStyle("-fx-text-fill: green; -fx-font-weight: bold;"); }
                    else if (hours > 0) { setText("THIẾU GIỜ"); setStyle("-fx-text-fill: orange; -fx-font-weight: bold;"); }
                    else { setText("VẮNG"); setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); }
                }
            }
        });
    }

    private void setupDateTimeFilters() {
        LocalDate today = LocalDate.now();
        if (cbYear != null) {
            cbYear.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(2020, today.getYear() + 1).boxed().sorted((a,b)->b-a).collect(Collectors.toList())));
            cbYear.setValue(today.getYear());
        }
        if (cbMonth != null) {
            cbMonth.setItems(FXCollections.observableArrayList(IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList())));
            cbMonth.setValue(today.getMonthValue());
        }
        updateDayComboBox(today.getYear(), today.getMonthValue());
        cbDay.setValue(null); // Mặc định để trống để xem cả tháng
    }

    private void updateDayComboBox(int year, int month) {
        int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
        ObservableList<Integer> days = FXCollections.observableArrayList();
        days.add(null); // Lựa chọn trống
        days.addAll(IntStream.rangeClosed(1, daysInMonth).boxed().collect(Collectors.toList()));
        cbDay.setItems(days);
    }

    private void setupDepartmentFilter() {
        cbDepartment.setItems(FXCollections.observableArrayList(DepartmentBUS.getInstance().getAll()));
    }

    private void setupListeners() {
        btnRefresh.setOnAction(e -> loadAttendance());
        btnImport.setOnAction(e -> handleImport());
        btnDownloadTemplate.setOnAction(e -> handleExport());
        if (btnExport != null) btnExport.setOnAction(e -> handleExport());
        cbDay.setOnAction(e -> applyFilters());
        cbMonth.setOnAction(e -> { updateDayComboBox(cbYear.getValue(), cbMonth.getValue()); applyFilters(); });
        cbYear.setOnAction(e -> { updateDayComboBox(cbYear.getValue(), cbMonth.getValue()); applyFilters(); });
        cbDepartment.setOnAction(e -> applyFilters());
        txtSearchEmployee.textProperty().addListener((obs, old, newVal) -> applyFilters());
        btnPrevious.setOnAction(e -> { if (currentPageIndex > 0) displayPage(--currentPageIndex); });
        btnNext.setOnAction(e -> { if (currentPageIndex < getTotalPages() - 1) displayPage(++currentPageIndex); });
    }

    private void loadAttendance() {
        masterData = timeSheetBUS.getAll();
        applyFilters();
    }

    private void applyFilters() {
        Integer d = cbDay.getValue();
        Integer m = cbMonth.getValue();
        Integer y = cbYear.getValue();
        DepartmentDTO dept = cbDepartment.getValue();
        String key = txtSearchEmployee.getText().toLowerCase().trim();

        filteredData = masterData.stream().filter(item -> {
            if (item.getCheckIn() == null) return false;
            LocalDate ld = item.getCheckIn().toLocalDate();
            // Nếu d == null thì chỉ so khớp tháng và năm
            boolean matchDate = (d == null) ? (ld.getMonthValue() == m && ld.getYear() == y) 
                                            : (ld.getDayOfMonth() == d && ld.getMonthValue() == m && ld.getYear() == y);
            boolean matchDept = (dept == null || employeeBUS.getById(item.getEmployeeId()).getDepartmentId() == dept.getId());
            String name = item.getEmployeeName() != null ? item.getEmployeeName().toLowerCase() : "";
            boolean matchKey = key.isEmpty() || name.contains(key) || String.valueOf(item.getEmployeeId()).contains(key);
            return matchDate && matchDept && matchKey;
        }).collect(Collectors.toCollection(ArrayList::new));

        currentPageIndex = 0;
        displayPage(0);
        updateStatistics();
    }

    private void updateStatistics() {
        BigDecimal totalH = BigDecimal.ZERO;
        BigDecimal totalOT = BigDecimal.ZERO;
        long present = 0, late = 0;

        for (TimeSheetDTO t : filteredData) {
            double h = t.getWorkHours() != null ? t.getWorkHours().doubleValue() : 0;
            if (h > 0) present++;
            if (h > 0 && h < 8) late++;
            if (t.getWorkHours() != null) totalH = totalH.add(t.getWorkHours());
            if (t.getOtHours() != null) totalOT = totalOT.add(t.getOtHours());
        }

        if (lblStatPresent != null) lblStatPresent.setText(String.valueOf(present));
        if (lblStatLate != null) lblStatLate.setText(String.valueOf(late));
        if (lblTotalHours != null) lblTotalHours.setText(String.format("%.1f giờ", totalH));
        if (lblOTHours != null) lblOTHours.setText(String.format("%.1f giờ OT", totalOT));
        if (lblWorkDays != null) lblWorkDays.setText(String.format("%d dòng", filteredData.size()));
    }

    private void displayPage(int pageIndex) {
        int start = pageIndex * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, filteredData.size());
        tblAttendance.setItems(FXCollections.observableArrayList(new ArrayList<>(filteredData.subList(start, end))));
        if (lblPageInfo != null) lblPageInfo.setText(String.format("Trang %d / %d", pageIndex + 1, getTotalPages()));
        btnPrevious.setDisable(pageIndex == 0);
        btnNext.setDisable(pageIndex >= getTotalPages() - 1);
    }

    private int getTotalPages() { return Math.max(1, (int) Math.ceil((double) filteredData.size() / PAGE_SIZE)); }

    private static LocalDateTime getDateTimeFromCell(org.apache.poi.ss.usermodel.Cell cell, DateTimeFormatter fmt) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue();
            String val = new DataFormatter().formatCellValue(cell).trim();
            return val.isEmpty() ? null : LocalDateTime.parse(val, fmt);
        } catch (Exception e) { return null; }
    }

    private boolean isRowEmpty(Row row) {
        if (row == null || row.getFirstCellNum() < 0) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    private void handleImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file Excel");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showOpenDialog(btnImport.getScene().getWindow());
        if (file == null) return;
        javafx.concurrent.Task<Integer> task = new javafx.concurrent.Task<>() {
            @Override protected Integer call() throws Exception {
                ArrayList<TimeSheetDTO> list = new ArrayList<>();
                DateTimeFormatter strFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                DataFormatter df = new DataFormatter();
                try (FileInputStream fis = new FileInputStream(file); Workbook wb = new XSSFWorkbook(fis)) {
                    Sheet sheet = wb.getSheetAt(0);
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (isRowEmpty(row)) continue;
                        try {
                            TimeSheetDTO dto = new TimeSheetDTO();
                            String idStr = df.formatCellValue(row.getCell(0)).trim().replaceAll("[^0-9]", "");
                            if (idStr.isEmpty()) continue;
                            dto.setEmployeeId(Integer.parseInt(idStr));
                            LocalDateTime in = getDateTimeFromCell(row.getCell(1), strFmt);
                            LocalDateTime out = getDateTimeFromCell(row.getCell(2), strFmt);
                            if (in == null) continue;
                            dto.setCheckIn(in); dto.setCheckOut(out);
                            list.add(dto);
                        } catch (Exception ignored) {}
                    }
                    if (!list.isEmpty()) timeSheetBUS.bulkInsert(list);
                    return list.size();
                }
            }
        };
        task.setOnSucceeded(e -> { NotificationUtils.showInfoAlert("Thành công!", "Đã import " + task.getValue() + " bản ghi."); loadAttendance(); });
        new Thread(task).start();
    }

    private void handleExport() {
        ObservableList<TimeSheetDTO> data = tblAttendance.getItems();
        if (data == null || data.isEmpty()) { NotificationUtils.showErrorAlert("Lỗi", "Không có dữ liệu!"); return; }
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("Bao_Cao_Cham_Cong.xlsx");
        File file = fc.showSaveDialog(null);
        if (file == null) return;
        try (Workbook wb = new XSSFWorkbook(); FileOutputStream fos = new FileOutputStream(file)) {
            Sheet s = wb.createSheet("Attendance");
            Row header = s.createRow(0);
            String[] heads = {"Mã NV", "Họ Tên", "Vào", "Ra", "Công", "OT"};
            for(int i=0; i<heads.length; i++) header.createCell(i).setCellValue(heads[i]);
            int idx = 1;
            for (TimeSheetDTO d : data) {
                Row r = s.createRow(idx++);
                r.createCell(0).setCellValue(d.getEmployeeId());
                r.createCell(1).setCellValue(d.getEmployeeName());
                r.createCell(2).setCellValue(d.getCheckIn().toString());
                r.createCell(3).setCellValue(d.getCheckOut() != null ? d.getCheckOut().toString() : "");
                r.createCell(4).setCellValue(d.getWorkHours().doubleValue());
                r.createCell(5).setCellValue(d.getOtHours().doubleValue());
            }
            wb.write(fos);
            NotificationUtils.showInfoAlert("Thành công", "Đã xuất file.");
        } catch (Exception e) { e.printStackTrace(); }
    }
}