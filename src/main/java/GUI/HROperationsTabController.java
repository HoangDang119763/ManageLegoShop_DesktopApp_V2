package GUI;

import BUS.EmployeeBUS;
import DTO.EmployeeDTO;
import UTILS.NotificationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

/**
 * Controller for HR Operations Tab
 */
@Slf4j
public class HROperationsTabController {

    @FXML
    public VBox containerHROperations;

    @FXML
    public ComboBox<EmployeeDTO> cbEmployeeSelector;

    @FXML
    public TextField tfEmployeeSearch;

    @FXML
    public TabPane tabPaneHROperations;

    // Nested controllers
    private LeaveRequestTabNestedController leaveRequestTabNestedController;
    private DisciplineTabNestedController disciplineTabNestedController;
    private AttendanceTabNestedController attendanceTabNestedController;

    private EmployeeBUS employeeBUS;
    private ObservableList<EmployeeDTO> allEmployees;
    private EmployeeDTO selectedEmployee;

    @FXML
    public void initialize() {
        log.info("Initializing HROperationsTabController");
        employeeBUS = EmployeeBUS.getInstance();
        allEmployees = FXCollections.observableArrayList();

        // Load nested FXML files and get controllers
        loadNestedTabs();

        setupEmployeeComboBox();
        setupSearchField();
        loadAllEmployees();
    }

    private void loadNestedTabs() {
        try {
            // Load Leave Request Tab
            FXMLLoader leaveLoader = new FXMLLoader(getClass().getResource("/GUI/LeaveRequestTabNested.fxml"));
            VBox leaveContent = leaveLoader.load();
            leaveRequestTabNestedController = leaveLoader.getController();
            Tab leaveTab = new Tab("Đơn Nghỉ Phép", leaveContent);
            leaveTab.setClosable(false);
            tabPaneHROperations.getTabs().add(leaveTab);

            // Load Discipline Tab
            FXMLLoader disciplineLoader = new FXMLLoader(getClass().getResource("/GUI/DisciplineTabNested.fxml"));
            VBox disciplineContent = disciplineLoader.load();
            disciplineTabNestedController = disciplineLoader.getController();
            Tab disciplineTab = new Tab("Kỷ Luật & Khen Thưởng", disciplineContent);
            disciplineTab.setClosable(false);
            tabPaneHROperations.getTabs().add(disciplineTab);

            // Load Attendance Tab
            FXMLLoader attendanceLoader = new FXMLLoader(getClass().getResource("/GUI/AttendanceTabNested.fxml"));
            VBox attendanceContent = attendanceLoader.load();
            attendanceTabNestedController = attendanceLoader.getController();
            Tab attendanceTab = new Tab("Chấm Công", attendanceContent);
            attendanceTab.setClosable(false);
            tabPaneHROperations.getTabs().add(attendanceTab);

            log.info("Successfully loaded all nested HR tabs");
        } catch (IOException e) {
            log.error("Error loading nested tabs", e);
            NotificationUtils.showErrorAlert("Lỗi", "Không thể tải các tab HR: " + e.getMessage());
        }
    }

    private void setupEmployeeComboBox() {
        cbEmployeeSelector.setCellFactory(param -> new ListCell<EmployeeDTO>() {
            @Override
            protected void updateItem(EmployeeDTO item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });

        cbEmployeeSelector.setOnAction(event -> {
            selectedEmployee = cbEmployeeSelector.getValue();
            if (selectedEmployee != null) {
                loadSubTabsForSelectedEmployee(selectedEmployee.getId());
            }
        });
    }

    private void setupSearchField() {
        tfEmployeeSearch.setOnKeyReleased(event -> {
            String searchText = tfEmployeeSearch.getText().toLowerCase().trim();
            if (searchText.isEmpty()) {
                cbEmployeeSelector.setItems(allEmployees);
            } else {
                ObservableList<EmployeeDTO> filtered = FXCollections.observableArrayList();
                for (EmployeeDTO emp : allEmployees) {
                    String firstName = emp.getFirstName() != null ? emp.getFirstName().toLowerCase() : "";
                    String lastName = emp.getLastName() != null ? emp.getLastName().toLowerCase() : "";
                    if (firstName.contains(searchText) || lastName.contains(searchText) || 
                        String.valueOf(emp.getId()).contains(searchText)) {
                        filtered.add(emp);
                    }
                }
                cbEmployeeSelector.setItems(filtered);
            }
        });
    }

    private void loadAllEmployees() {
        new Thread(() -> {
            try {
                List<EmployeeDTO> employees = employeeBUS.getAll();
                Platform.runLater(() -> {
                    if (employees != null && !employees.isEmpty()) {
                        allEmployees.setAll(employees);
                        cbEmployeeSelector.setItems(allEmployees);
                    } else {
                        NotificationUtils.showInfoAlert("Thông báo", "Không có nhân viên");
                    }
                });
            } catch (Exception e) {
                log.error("Error loading employees", e);
                Platform.runLater(() -> {
                    NotificationUtils.showErrorAlert("Lỗi", e.getMessage());
                });
            }
        }).start();
    }

    private void loadSubTabsForSelectedEmployee(int employeeId) {
        // Load Leave Request data
        if (leaveRequestTabNestedController != null) {
            leaveRequestTabNestedController.loadEmployeeLeaves(employeeId);
        }

        // Load Discipline data
        if (disciplineTabNestedController != null) {
            disciplineTabNestedController.loadEmployeeDiscipline(employeeId);
        }

        // Load Attendance data
        if (attendanceTabNestedController != null) {
            attendanceTabNestedController.loadEmployeeAttendance(employeeId);
        }
    }

    public int getSelectedEmployeeId() {
        return selectedEmployee != null ? selectedEmployee.getId() : -1;
    }
}

