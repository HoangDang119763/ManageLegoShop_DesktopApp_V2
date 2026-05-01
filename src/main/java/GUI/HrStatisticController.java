package GUI;

import BUS.HrStatisticBUS;
import DTO.HrStatisticDTO;
import DTO.HrStatisticDTO.AttendanceRow;
import DTO.HrStatisticDTO.DepartmentDistributionItem;
import DTO.HrStatisticDTO.DailyWorkPoint;
import DTO.HrStatisticDTO.FineRewardRow;
import DTO.HrStatisticDTO.HeadcountChangeRow;
import DTO.HrStatisticDTO.HeadcountPoint;
import DTO.HrStatisticDTO.LeaveByTypeItem;
import DTO.HrStatisticDTO.LeaveRow;
import DTO.HrStatisticDTO.LeaveStatusItem;
import DTO.HrStatisticDTO.RewardFineSummary;
import DTO.HrStatisticDTO.SalaryRow;
import DTO.HrStatisticDTO.StatusDistributionItem;
import ENUM.PermissionKey;
import INTERFACE.IController;
import SERVICE.ExcelService;
import SERVICE.SessionManagerService;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.ValidationUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class HrStatisticController implements IController {

    // ── Bộ lọc chung ──────────────────────────────────────
    @FXML private ComboBox<Integer> cbMonth;
    @FXML private ComboBox<Integer> cbYear;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;
    @FXML private Button btnExport;

    // ── Tab 1: Tổng quan nhân sự ──────────────────────────
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblNewEmployees;
    @FXML private Label lblTotalPaidSalary;
    @FXML private BarChart<String, Number> headcountChart;
    @FXML private PieChart statusPieChart;
    @FXML private PieChart departmentPieChart;

    // ── Tab 1 – Bảng thay đổi nhân sự ────────────────────
    @FXML private TableView<HeadcountChangeRow> tblHeadcountChanges;
    @FXML private TableColumn<HeadcountChangeRow, String> colHcName;
    @FXML private TableColumn<HeadcountChangeRow, String> colHcFromDept;
    @FXML private TableColumn<HeadcountChangeRow, String> colHcToDept;
    @FXML private TableColumn<HeadcountChangeRow, String> colHcFromPos;
    @FXML private TableColumn<HeadcountChangeRow, String> colHcToPos;
    @FXML private TableColumn<HeadcountChangeRow, String> colHcDate;
    @FXML private TableColumn<HeadcountChangeRow, String> colHcStatus;

    // ── Tab 2: Chấm công ──────────────────────────────────
    @FXML private Label lblTotalSessions;
    @FXML private Label lblAttendanceEmployees;
    @FXML private Label lblTotalWorkHours;
    @FXML private Label lblTotalOtHours;
    @FXML private BarChart<String, Number> attendanceChart;
    @FXML private TableView<AttendanceRow> tblAttendance;
    @FXML private TableColumn<AttendanceRow, String> colAttName;
    @FXML private TableColumn<AttendanceRow, String> colAttDept;
    @FXML private TableColumn<AttendanceRow, String> colAttSessions;
    @FXML private TableColumn<AttendanceRow, String> colAttWork;
    @FXML private TableColumn<AttendanceRow, String> colAttOt;

    // ── Tab 3: Nghỉ phép ──────────────────────────────────
    @FXML private Label lblTotalLeaveRequests;
    @FXML private Label lblTotalLeaveDays;
    @FXML private BarChart<String, Number> leaveTypeChart;
    @FXML private PieChart leaveStatusChart;
    @FXML private TableView<LeaveRow> tblLeave;
    @FXML private TableColumn<LeaveRow, String> colLeaveName;
    @FXML private TableColumn<LeaveRow, String> colLeaveType;
    @FXML private TableColumn<LeaveRow, String> colLeaveStart;
    @FXML private TableColumn<LeaveRow, String> colLeaveEnd;
    @FXML private TableColumn<LeaveRow, String> colLeaveDays;
    @FXML private TableColumn<LeaveRow, String> colLeaveStatus;

    // ── Tab 4: Khen thưởng – Kỷ luật ─────────────────────
    @FXML private Label lblTotalAllowance;
    @FXML private Label lblEmployeesWithAllowance;
    @FXML private Label lblTotalFine;
    @FXML private Label lblEmployeesWithFine;
    @FXML private BarChart<String, Number> allowanceFineChart;
    @FXML private ComboBox<String> cbFilterDept;
    @FXML private ComboBox<String> cbFilterFineLevel;
    @FXML private TableView<FineRewardRow> tblFineReward;
    @FXML private TableColumn<FineRewardRow, String> colEmpCode;
    @FXML private TableColumn<FineRewardRow, String> colFullName;
    @FXML private TableColumn<FineRewardRow, String> colDept;
    @FXML private TableColumn<FineRewardRow, String> colPosition;
    @FXML private TableColumn<FineRewardRow, String> colFineLevel;
    @FXML private TableColumn<FineRewardRow, String> colAmount;
    @FXML private TableColumn<FineRewardRow, String> colDate;

    // ── Tab 5: Thống kê lương ─────────────────────────────
    @FXML private Label lblSalaryEmployees;
    @FXML private Label lblTotalNet;
    @FXML private Label lblAvgNet;
    @FXML private Label lblTotalInsurance;
    @FXML private BarChart<String, Number> salaryTopChart;
    @FXML private TableView<SalaryRow> tblSalary;
    @FXML private TableColumn<SalaryRow, String> colSalaryName;
    @FXML private TableColumn<SalaryRow, String> colSalaryDept;
    @FXML private TableColumn<SalaryRow, String> colSalaryBase;
    @FXML private TableColumn<SalaryRow, String> colSalaryNet;
    @FXML private TableColumn<SalaryRow, String> colSalaryInsurance;
    @FXML private TableColumn<SalaryRow, String> colSalaryWork;

    // ── Tab 6: Báo cáo nhân sự ───────────────────────────
    @FXML private Label lblReportEmployees;
    @FXML private Label lblReportNewEmployees;
    @FXML private Label lblReportTotalSalary;
    @FXML private Label lblReportAttSessions;
    @FXML private Label lblReportLeaveRequests;

    // ─────────────────────────────────────────────────────

    private final HrStatisticBUS hrStatisticBUS = HrStatisticBUS.getInstance();
    private List<FineRewardRow> allRows = new ArrayList<>();
    private HrStatisticDTO lastLoadedStatistic = null;

    @FXML
    public void initialize() {
        initMonthYearCombo();
        loadTable();
        hideButtonWithoutPermission();
        setupListeners();
        loadCurrentMonth();
    }

    private void initMonthYearCombo() {
        cbMonth.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(1, 12).boxed().toList()));
        LocalDate now = LocalDate.now();
        cbMonth.setValue(now.getMonthValue());

        int currentYear = now.getYear();
        cbYear.setItems(FXCollections.observableArrayList(
                IntStream.rangeClosed(currentYear - 5, currentYear).boxed().toList()));
        cbYear.setValue(currentYear);
    }

    @Override
    public void loadTable() {
        // Tab 1: Bảng thay đổi nhân sự
        tblHeadcountChanges.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colHcName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colHcFromDept.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFromDept()));
        colHcToDept.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getToDept()));
        colHcFromPos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFromPos()));
        colHcToPos.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getToPos()));
        colHcDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEffectiveDate()));
        colHcStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        // Tab 2: Chấm công
        tblAttendance.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colAttName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colAttDept.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeptName()));
        colAttSessions.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getSessionCount())));
        colAttWork.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTotalWork().setScale(1, RoundingMode.HALF_UP) + " h"));
        colAttOt.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getTotalOt().setScale(1, RoundingMode.HALF_UP) + " h"));

        // Tab 3: Nghỉ phép
        tblLeave.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colLeaveName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colLeaveType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLeaveType()));
        colLeaveStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStartDate()));
        colLeaveEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEndDate()));
        colLeaveDays.setCellValueFactory(c -> new SimpleStringProperty(
                String.valueOf(c.getValue().getDays())));
        colLeaveStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));

        // Tab 4: Khen thưởng – Kỷ luật
        tblFineReward.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colEmpCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmployeeCode()));
        colFullName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colDept.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartmentName()));
        colPosition.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPositionName()));
        colFineLevel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFineLevel()));
        colAmount.setCellValueFactory(c -> new SimpleStringProperty(
                fmt(c.getValue().getAmount())));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt()));

        // Tab 5: Thống kê lương
        tblSalary.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colSalaryName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colSalaryDept.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeptName()));
        colSalaryBase.setCellValueFactory(c -> new SimpleStringProperty(
                fmt(c.getValue().getBaseSalary())));
        colSalaryNet.setCellValueFactory(c -> new SimpleStringProperty(
                fmt(c.getValue().getNetSalary())));
        colSalaryInsurance.setCellValueFactory(c -> new SimpleStringProperty(
                fmt(c.getValue().getTotalInsurance())));
        colSalaryWork.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getActualWorkDays().setScale(1, RoundingMode.HALF_UP) + " ngày"));
    }

    private void loadCurrentMonth() {
        LocalDate now = LocalDate.now();
        loadStatistic(now.getMonthValue(), now.getYear());
    }

    @Override
    public void setupListeners() {
        btnSearch.setOnAction(e -> handleSearch());
        btnRefresh.setOnAction(e -> {
            initMonthYearCombo();
            clearAll();
            loadCurrentMonth();
        });
        btnExport.setOnAction(e ->
                handleExportReport());
        cbFilterDept.setOnAction(e -> applyTableFilter());
        cbFilterFineLevel.setOnAction(e -> applyTableFilter());
    }

    private void handleSearch() {
        Integer month = cbMonth.getValue();
        Integer year = cbYear.getValue();
        if (month == null || year == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn tháng và năm.", "Lỗi");
            return;
        }
        loadStatistic(month, year);
    }

    private void loadStatistic(int month, int year) {
        TaskUtil.executeAsync(
                () -> hrStatisticBUS.getHrStatistic(month, year),
                dto -> {
                    lastLoadedStatistic = dto;
                    renderTab1(dto);
                    renderTab2(dto);
                    renderTab3(dto);
                    renderTab4(dto);
                    renderTab5(dto);
                    renderTab6(dto);
                    populateFineTable(dto.getFineRewardRows());
                }
        );
    }

    // ── Tab 1 ──────────────────────────────────────────────

    private void renderTab1(HrStatisticDTO dto) {
        lblTotalEmployees.setText(String.valueOf(dto.getTotalEmployees()));
        lblNewEmployees.setText(String.valueOf(dto.getNewEmployees()));
        lblTotalPaidSalary.setText(fmt(dto.getTotalPaidSalary()));

        headcountChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Số nhân sự");
        for (HeadcountPoint p : dto.getHeadcountOverTime()) {
            series.getData().add(new XYChart.Data<>(p.getLabel(), p.getHeadcount()));
        }
        headcountChart.getData().add(series);

        statusPieChart.getData().clear();
        for (StatusDistributionItem item : dto.getStatusDistribution()) {
            statusPieChart.getData().add(new PieChart.Data(item.getStatusName(), item.getCount()));
        }

        departmentPieChart.getData().clear();
        for (DepartmentDistributionItem item : dto.getDepartmentDistribution()) {
            departmentPieChart.getData().add(
                    new PieChart.Data(item.getDepartmentName(), item.getCount()));
        }

        tblHeadcountChanges.setItems(FXCollections.observableArrayList(dto.getHeadcountChanges()));
    }

    // ── Tab 2 ──────────────────────────────────────────────

    private void renderTab2(HrStatisticDTO dto) {
        HrStatisticDTO.AttendanceStat s = dto.getAttendanceStat();
        lblTotalSessions.setText(String.valueOf(s.getTotalSessions()));
        lblAttendanceEmployees.setText(String.valueOf(s.getEmployeeCount()));
        lblTotalWorkHours.setText(s.getTotalWorkHours().setScale(1, RoundingMode.HALF_UP) + " h");
        lblTotalOtHours.setText(s.getTotalOtHours().setScale(1, RoundingMode.HALF_UP) + " h");

        attendanceChart.getData().clear();
        XYChart.Series<String, Number> seriesWork = new XYChart.Series<>();
        seriesWork.setName("Giờ làm");
        XYChart.Series<String, Number> seriesOt = new XYChart.Series<>();
        seriesOt.setName("Giờ OT");
        for (DailyWorkPoint p : dto.getDailyWorkPoints()) {
            seriesWork.getData().add(new XYChart.Data<>(p.getDayLabel(),
                    p.getTotalWork().setScale(1, RoundingMode.HALF_UP).doubleValue()));
            seriesOt.getData().add(new XYChart.Data<>(p.getDayLabel(),
                    BigDecimal.ZERO.doubleValue()));
        }
        attendanceChart.getData().add(seriesWork);
        attendanceChart.getData().add(seriesOt);

        tblAttendance.setItems(FXCollections.observableArrayList(dto.getAttendanceRows()));
    }

    // ── Tab 3 ──────────────────────────────────────────────

    private void renderTab3(HrStatisticDTO dto) {
        HrStatisticDTO.LeaveStat s = dto.getLeaveStat();
        lblTotalLeaveRequests.setText(String.valueOf(s.getTotalRequests()));
        lblTotalLeaveDays.setText(String.valueOf(s.getTotalDays()));

        leaveTypeChart.getData().clear();
        XYChart.Series<String, Number> seriesType = new XYChart.Series<>();
        seriesType.setName("Số đơn");
        for (LeaveByTypeItem item : dto.getLeaveByType()) {
            seriesType.getData().add(new XYChart.Data<>(item.getLeaveType(), item.getCount()));
        }
        leaveTypeChart.getData().add(seriesType);

        leaveStatusChart.getData().clear();
        for (LeaveStatusItem item : dto.getLeaveByStatus()) {
            leaveStatusChart.getData().add(
                    new PieChart.Data(item.getStatusName(), item.getCount()));
        }

        tblLeave.setItems(FXCollections.observableArrayList(dto.getLeaveRows()));
    }

    // ── Tab 4 ──────────────────────────────────────────────

    private void renderTab4(HrStatisticDTO dto) {
        RewardFineSummary s = dto.getRewardFineSummary();
        lblTotalAllowance.setText(fmt(s.getTotalAllowance()));
        lblEmployeesWithAllowance.setText(String.valueOf(s.getEmployeesWithAllowance()));
        lblTotalFine.setText(fmt(s.getTotalFine()));
        lblEmployeesWithFine.setText(String.valueOf(s.getEmployeesWithFine()));

        allowanceFineChart.getData().clear();
        XYChart.Series<String, Number> seriesEmp = new XYChart.Series<>();
        seriesEmp.setName("Số nhân viên");
        seriesEmp.getData().add(new XYChart.Data<>("Phụ cấp", s.getEmployeesWithAllowance()));
        seriesEmp.getData().add(new XYChart.Data<>("Vi phạm", s.getEmployeesWithFine()));

        XYChart.Series<String, Number> seriesMoney = new XYChart.Series<>();
        seriesMoney.setName("Số tiền (nghìn đ)");
        seriesMoney.getData().add(new XYChart.Data<>("Phụ cấp",
                s.getTotalAllowance().divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP).doubleValue()));
        seriesMoney.getData().add(new XYChart.Data<>("Vi phạm",
                s.getTotalFine().divide(BigDecimal.valueOf(1000), RoundingMode.HALF_UP).doubleValue()));

        allowanceFineChart.getData().add(seriesEmp);
        allowanceFineChart.getData().add(seriesMoney);
    }

    private void populateFineTable(List<FineRewardRow> rows) {
        allRows = rows != null ? rows : new ArrayList<>();

        List<String> depts = allRows.stream()
                .map(FineRewardRow::getDepartmentName)
                .distinct().sorted().collect(Collectors.toList());
        depts.add(0, "Tất cả phòng ban");
        cbFilterDept.setItems(FXCollections.observableArrayList(depts));
        cbFilterDept.setValue("Tất cả phòng ban");

        List<String> levels = allRows.stream()
                .map(FineRewardRow::getFineLevel)
                .distinct().sorted().collect(Collectors.toList());
        levels.add(0, "Tất cả loại");
        cbFilterFineLevel.setItems(FXCollections.observableArrayList(levels));
        cbFilterFineLevel.setValue("Tất cả loại");

        tblFineReward.setItems(FXCollections.observableArrayList(allRows));
    }

    private void applyTableFilter() {
        String dept = cbFilterDept.getValue();
        String level = cbFilterFineLevel.getValue();
        ObservableList<FineRewardRow> filtered = allRows.stream()
                .filter(r -> dept == null || dept.equals("Tất cả phòng ban")
                        || dept.equals(r.getDepartmentName()))
                .filter(r -> level == null || level.equals("Tất cả loại")
                        || level.equals(r.getFineLevel()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        tblFineReward.setItems(filtered);
    }

    // ── Tab 5 ──────────────────────────────────────────────

    private void renderTab5(HrStatisticDTO dto) {
        HrStatisticDTO.SalaryStat s = dto.getSalaryStat();
        lblSalaryEmployees.setText(String.valueOf(s.getEmployeeCount()));
        lblTotalNet.setText(fmt(s.getTotalNet()));
        lblAvgNet.setText(fmt(s.getAvgNet()));
        lblTotalInsurance.setText(fmt(s.getTotalInsurance()));

        salaryTopChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Lương NET (tr.đ)");
        dto.getSalaryRows().stream()
                .limit(10)
                .forEach(row -> series.getData().add(new XYChart.Data<>(
                        row.getFullName(),
                        row.getNetSalary()
                                .divide(BigDecimal.valueOf(1_000_000), 2, RoundingMode.HALF_UP)
                                .doubleValue())));
        salaryTopChart.getData().add(series);

        tblSalary.setItems(FXCollections.observableArrayList(dto.getSalaryRows()));
    }

    // ── Tab 6 ──────────────────────────────────────────────

    private void renderTab6(HrStatisticDTO dto) {
        lblReportEmployees.setText(String.valueOf(dto.getTotalEmployees()));
        lblReportNewEmployees.setText(String.valueOf(dto.getNewEmployees()));
        lblReportTotalSalary.setText(fmt(dto.getTotalPaidSalary()));
        lblReportAttSessions.setText(String.valueOf(dto.getAttendanceStat().getTotalSessions()));
        lblReportLeaveRequests.setText(String.valueOf(dto.getLeaveStat().getTotalRequests()));
    }

    // ── Helpers ────────────────────────────────────────────

    private String fmt(BigDecimal value) {
        return ValidationUtils.getInstance().formatCurrency(value);
    }

    private void clearAll() {
        lastLoadedStatistic = null;
        headcountChart.getData().clear();
        statusPieChart.getData().clear();
        departmentPieChart.getData().clear();
        attendanceChart.getData().clear();
        leaveTypeChart.getData().clear();
        leaveStatusChart.getData().clear();
        allowanceFineChart.getData().clear();
        salaryTopChart.getData().clear();

        tblHeadcountChanges.setItems(FXCollections.emptyObservableList());
        tblAttendance.setItems(FXCollections.emptyObservableList());
        tblLeave.setItems(FXCollections.emptyObservableList());
        tblFineReward.setItems(FXCollections.emptyObservableList());
        tblSalary.setItems(FXCollections.emptyObservableList());

        lblTotalEmployees.setText("—");
        lblNewEmployees.setText("—");
        lblTotalPaidSalary.setText("—");
        lblTotalSessions.setText("—");
        lblAttendanceEmployees.setText("—");
        lblTotalWorkHours.setText("—");
        lblTotalOtHours.setText("—");
        lblTotalLeaveRequests.setText("—");
        lblTotalLeaveDays.setText("—");
        lblTotalAllowance.setText("—");
        lblEmployeesWithAllowance.setText("—");
        lblTotalFine.setText("—");
        lblEmployeesWithFine.setText("—");
        lblSalaryEmployees.setText("—");
        lblTotalNet.setText("—");
        lblAvgNet.setText("—");
        lblTotalInsurance.setText("—");
        lblReportEmployees.setText("—");
        lblReportNewEmployees.setText("—");
        lblReportTotalSalary.setText("—");
        lblReportAttSessions.setText("—");
        lblReportLeaveRequests.setText("—");
    }

    private void handleExportReport() {
        Integer month = cbMonth.getValue();
        Integer year = cbYear.getValue();
        if (month == null || year == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn tháng và năm trước khi xuất.", "Lỗi");
            return;
        }
        if (lastLoadedStatistic == null) {
            NotificationUtils.showErrorAlert("Chưa có dữ liệu để xuất. Vui lòng bấm Tìm kiếm trước.", "Thông báo");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn nơi lưu báo cáo thống kê nhân sự");
            fileChooser.setInitialFileName("ThongKe_NhanSu_" + month + "_" + year + ".xlsx");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
            File selectedFile = fileChooser.showSaveDialog(btnExport.getScene().getWindow());
            if (selectedFile == null) {
                return;
            }

            ExcelService.getInstance().exportHrStatisticWorkbook(lastLoadedStatistic, month, year, selectedFile);
            NotificationUtils.showInfoAlert("Xuất báo cáo nhân sự thành công.", "Thông báo");
        } catch (Exception ex) {
            NotificationUtils.showErrorAlert("Xuất báo cáo thất bại: " + ex.getMessage(), "Lỗi");
        }
    }

    @Override
    public void applyFilters() { handleSearch(); }

    @Override
    public void resetFilters() {
        initMonthYearCombo();
        clearAll();
        loadCurrentMonth();
    }

    @Override
    public void hideButtonWithoutPermission() {
        boolean canView = SessionManagerService.getInstance()
                .hasPermission(PermissionKey.HR_STATISTIC_VIEW);
        if (!canView) {
            btnSearch.setDisable(true);
            btnExport.setDisable(true);
            NotificationUtils.showErrorAlert(
                    "Bạn không có quyền xem thống kê nhân sự.", "Lỗi phân quyền");
        }
    }
}
