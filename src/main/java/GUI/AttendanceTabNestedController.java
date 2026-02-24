package GUI;

import BUS.TimeSheetBUS;
import BUS.EmployeeBUS;
import DTO.TimeSheetDTO;
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public class AttendanceTabNestedController {
    private static final Logger log = LoggerFactory.getLogger(AttendanceTabNestedController.class);

    @FXML
    private VBox containerAttendance;

    @FXML
    private ComboBox<String> cbMonth; // Month selector (MM/yyyy)

    @FXML
    private Button btnAdd;

    @FXML
    private TableView<TimeSheetDTO> tblAttendance;

    @FXML
    private TableColumn<TimeSheetDTO, String> colDate;
    @FXML
    private TableColumn<TimeSheetDTO, String> colCheckIn;
    @FXML
    private TableColumn<TimeSheetDTO, String> colCheckOut;
    @FXML
    private TableColumn<TimeSheetDTO, String> colWorkHours;
    @FXML
    private TableColumn<TimeSheetDTO, String> colOtHours; // Overtime hours

    @FXML
    private Label lblTotalHours; // Total work hours
    @FXML
    private Label lblWorkDays; // Total work days
    @FXML
    private Label lblOtHours; // Total OT hours

    // BUS instances
    private TimeSheetBUS timeSheetBUS;
    private EmployeeBUS employeeBUS;
    private ValidationUtils validationUtils;

    private int currentEmployeeId = -1;
    private ObservableList<TimeSheetDTO> attendanceList;

    /**
     * Initialize Attendance nested controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing AttendanceTabNestedController");
        timeSheetBUS = TimeSheetBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        validationUtils = ValidationUtils.getInstance();
        attendanceList = FXCollections.observableArrayList();

        setupButtons();
        setupMonthSelector();
        setupTable();
    }

    /**
     * Setup button actions
     */
    private void setupButtons() {
        btnAdd.setOnAction(event -> addNewAttendance());
    }

    /**
     * Setup month selector combobox
     */
    private void setupMonthSelector() {
        ObservableList<String> months = FXCollections.observableArrayList();

        // Generate last 12 months
        for (int i = 11; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            months.add(String.format("%02d/%d", ym.getMonthValue(), ym.getYear()));
        }

        cbMonth.setItems(months);
        cbMonth.setValue(String.format("%02d/%d", YearMonth.now().getMonthValue(), YearMonth.now().getYear()));

        cbMonth.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (currentEmployeeId > 0) {
                loadEmployeeAttendance(currentEmployeeId);
            }
        });
    }

    /**
     * Setup table columns
     */
    private void setupTable() {
        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCheckIn() != null ? cellData.getValue().getCheckIn().toLocalDate().toString()
                        : ""));
        colCheckIn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCheckIn() != null ? cellData.getValue().getCheckIn().toString() : ""));
        colCheckOut.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCheckOut() != null ? cellData.getValue().getCheckOut().toString() : ""));
        colWorkHours.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getWorkHours() != null ? String.format("%.2f", cellData.getValue().getWorkHours())
                        : "0"));
        colOtHours.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getOtHours() != null ? String.format("%.2f", cellData.getValue().getOtHours())
                        : "0"));

        tblAttendance.setItems(attendanceList);
    }

    /**
     * Load attendance records for specified employee and selected month
     */
    public void loadEmployeeAttendance(int employeeId) {
        currentEmployeeId = employeeId;
        String monthStr = cbMonth.getValue();

        log.info("Loading attendance for employee: {} in month: {}", employeeId, monthStr);

        new Thread(() -> {
            try {
                // Get all timesheets for employee, then filter by month
                List<TimeSheetDTO> allTimesheets = timeSheetBUS.getByEmployeeId(employeeId);

                // Parse month string (MM/yyyy)
                String[] parts = monthStr.split("/");
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);

                // Filter by month and year
                List<TimeSheetDTO> timesheets = allTimesheets.stream()
                        .filter(ts -> ts.getCheckIn() != null &&
                                ts.getCheckIn().getMonthValue() == month &&
                                ts.getCheckIn().getYear() == year)
                        .toList();

                Platform.runLater(() -> {
                    if (timesheets != null && !timesheets.isEmpty()) {
                        attendanceList.setAll(timesheets);
                        calculateStatistics();
                    } else {
                        attendanceList.clear();
                        clearStatistics();
                    }
                });
            } catch (Exception e) {
                log.error("Error loading attendance records", e);
                Platform.runLater(() -> {
                    NotificationUtils.showErrorAlert("Lỗi tải chấm công",
                            "Chi tiết: " + e.getMessage());
                    clearStatistics();
                });
            }
        }).start();
    }

    /**
     * Calculate statistics: total hours, total days, total OT hours
     */
    private void calculateStatistics() {
        BigDecimal totalHours = BigDecimal.ZERO;
        BigDecimal totalOtHours = BigDecimal.ZERO;
        int totalDays = attendanceList.size();

        for (TimeSheetDTO ts : attendanceList) {
            if (ts.getWorkHours() != null) {
                totalHours = totalHours.add(ts.getWorkHours());
            }
            if (ts.getOtHours() != null) {
                totalOtHours = totalOtHours.add(ts.getOtHours());
            }
        }

        lblTotalHours.setText(String.format("%.2f giờ", totalHours));
        lblWorkDays.setText(String.valueOf(totalDays) + " ngày");
        lblOtHours.setText(String.format("%.2f giờ", totalOtHours));
    }

    /**
     * Clear statistics labels
     */
    private void clearStatistics() {
        lblTotalHours.setText("0.00 giờ");
        lblWorkDays.setText("0 ngày");
        lblOtHours.setText("0.00 giờ");
    }

    /**
     * Load attendance records for specified month
     */
    private void loadEmployeeAttendanceByMonth(String monthStr) {
        if (currentEmployeeId < 0) {
            return;
        }

        log.info("Loading attendance for employee: {} in month: {}", currentEmployeeId, monthStr);

        new Thread(() -> {
            try {
                // Get all timesheets for employee, then filter by month
                List<TimeSheetDTO> allTimesheets = timeSheetBUS.getByEmployeeId(currentEmployeeId);

                // Parse month string (MM/yyyy)
                String[] parts = monthStr.split("/");
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);

                // Filter by month and year
                List<TimeSheetDTO> timesheets = allTimesheets.stream()
                        .filter(ts -> ts.getCheckIn() != null &&
                                ts.getCheckIn().getMonthValue() == month &&
                                ts.getCheckIn().getYear() == year)
                        .toList();

                Platform.runLater(() -> {
                    if (timesheets != null && !timesheets.isEmpty()) {
                        attendanceList.setAll(timesheets);
                        calculateStatistics();
                    } else {
                        attendanceList.clear();
                        clearStatistics();
                    }
                });
            } catch (Exception e) {
                log.error("Error loading attendance records", e);
                Platform.runLater(() -> {
                    NotificationUtils.showErrorAlert("Lỗi tải chấm công",
                            "Chi tiết: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Add new attendance record
     */
    private void addNewAttendance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/AttendanceModal.fxml"));
            VBox modalRoot = loader.load();
            AttendanceModalController modalController = loader.getController();
            modalController.setEmployeeId(currentEmployeeId);
            modalController.setParentController(this);

            Stage modalStage = new Stage();
            modalStage.setTitle("Thêm Chấm Công");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();
        } catch (IOException e) {
            log.error("Error opening attendance modal", e);
            NotificationUtils.showErrorAlert("Lỗi", "Không thể mở form thêm chấm công");
        }
    }
}
