package GUI;

import BUS.TimeSheetBUS;
import BUS.EmployeeBUS;
import DTO.TimeSheetDTO;
import DTO.EmployeeDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;

public class AttendanceTabController {
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
    private Button btnRefresh;

    @FXML
    private Label lblEmployeeName;
    @FXML
    private Label lblTotalHours;
    @FXML
    private Label lblOTHours;
    @FXML
    private Label lblWorkDays;

    private TimeSheetBUS timeSheetBUS;
    private EmployeeBUS employeeBUS;
    private int currentEmployeeId;

    @FXML
    public void initialize() {
        timeSheetBUS = TimeSheetBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();

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
        ArrayList<TimeSheetDTO> timesheets = timeSheetBUS.getByEmployeeId(currentEmployeeId);

        // Filter by selected month
        YearMonth selectedMonth = cbMonth.getValue();
        ArrayList<TimeSheetDTO> filtered = new ArrayList<>();

        for (TimeSheetDTO sheet : timesheets) {
            if (sheet.getCheckIn() != null) {
                YearMonth sheetMonth = YearMonth.from(sheet.getCheckIn().toLocalDate());
                if (sheetMonth.equals(selectedMonth)) {
                    filtered.add(sheet);
                }
            }
        }

        tblAttendance.setItems(FXCollections.observableArrayList(filtered));

        // Calculate statistics
        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal totalOTHours = BigDecimal.ZERO;

        for (TimeSheetDTO sheet : filtered) {
            if (sheet.getWorkHours() != null) {
                totalHours = totalHours.add(sheet.getWorkHours());
            }
            if (sheet.getOtHours() != null) {
                totalOTHours = totalOTHours.add(sheet.getOtHours());
            }
        }

        lblTotalHours.setText(String.format("%.1f giờ", totalHours));
        lblOTHours.setText(String.format("%.1f giờ OT", totalOTHours));
        lblWorkDays.setText(String.format("%d ngày", filtered.size()));
    }
}
