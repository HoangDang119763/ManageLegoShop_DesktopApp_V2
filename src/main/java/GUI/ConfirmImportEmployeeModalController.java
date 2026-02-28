package GUI;

import BUS.EmployeeBUS;
import DTO.ImportEmployeeExcelDTO;
import ENUM.PermissionKey;
import INTERFACE.IModalController;
import SERVICE.ExcelImportService;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for ConfirmImportEmployeeModal.fxml
 * Preview and confirm employee import data from Excel
 * Hiển thị preview dữ liệu nhân viên từ Excel và xác nhận lưu
 */
public class ConfirmImportEmployeeModalController implements IModalController {

    // ==================== HEADER SECTION ====================
    @FXML
    private Label lblTitle;
    @FXML
    private Label lblTotalRows;
    @FXML
    private Label lblValidRows;
    @FXML
    private Label lblErrorRows;

    // ==================== TABLE SECTION ====================
    @FXML
    private TableView<ImportEmployeeExcelDTO> tvEmployeeConfirm;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, Integer> col_rowNumber;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_firstName;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_lastName;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_username;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_gender;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_dob;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, Integer> col_dept;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, Integer> col_pos;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, Integer> col_role;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_phone;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_email;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_healthIns;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_socialIns;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_unemploymentIns;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_meal;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_transport;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_accommodation;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, Integer> col_dependents;
    @FXML
    private TableColumn<ImportEmployeeExcelDTO, String> col_status;

    // ==================== ACTION BUTTONS ====================
    @FXML
    private Button btnBack;
    @FXML
    private Button btnFinalSubmit;

    // ==================== LOADING OVERLAY ====================
    @FXML
    private StackPane loadingOverlay;

    // ==================== STATE & DEPENDENCIES ====================
    private ExcelImportService importService;
    private EmployeeBUS employeeBUS;
    private SessionManagerService session;
    private ValidationUtils validationUtils;

    @Getter
    private boolean isSaved = false;

    @Getter
    private String resultMessage = "";

    // Data from import
    private List<ImportEmployeeExcelDTO> importDataList;
    private List<ImportEmployeeExcelDTO> validDataList;
    private List<ImportEmployeeExcelDTO> errorDataList;

    // ==================== LIFECYCLE ====================
    @FXML
    public void initialize() {
        // Initialize dependencies
        importService = ExcelImportService.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        session = SessionManagerService.getInstance();
        validationUtils = ValidationUtils.getInstance();

        // Setup table columns
        setupTableColumns();

        // Setup listeners
        setupListeners();

        // Initialize lists
        importDataList = new ArrayList<>();
        validDataList = new ArrayList<>();
        errorDataList = new ArrayList<>();
    }

    /**
     * Setup table column value factories
     */
    private void setupTableColumns() {
        // Row number
        col_rowNumber.setCellValueFactory(new PropertyValueFactory<>("rowNumber"));

        // First name
        col_firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        // Last name
        col_lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        // Username
        col_username.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Gender
        col_gender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        // Date of birth
        col_dob.setCellValueFactory(cellData -> {
            LocalDate dob = cellData.getValue().getDateOfBirth();
            String dobStr = dob != null ? dob.toString() : "";
            return new SimpleStringProperty(dobStr);
        });

        // Department ID
        col_dept.setCellValueFactory(new PropertyValueFactory<>("departmentId"));

        // Position ID
        col_pos.setCellValueFactory(new PropertyValueFactory<>("positionId"));

        // Role ID
        col_role.setCellValueFactory(new PropertyValueFactory<>("roleId"));

        // Phone
        col_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Email
        col_email.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Health insurance code
        col_healthIns.setCellValueFactory(new PropertyValueFactory<>("healthInsCode"));

        // Social insurance code
        col_socialIns.setCellValueFactory(new PropertyValueFactory<>("socialInsCode"));

        // Unemployment insurance code
        col_unemploymentIns.setCellValueFactory(new PropertyValueFactory<>("unemploymentInsCode"));

        // Meal support
        col_meal.setCellValueFactory(cellData -> {
            Boolean meal = cellData.getValue().getMealSupport();
            String mealStr = meal != null && meal ? "Y" : "N";
            return new SimpleStringProperty(mealStr);
        });

        // Transport support
        col_transport.setCellValueFactory(cellData -> {
            Boolean transport = cellData.getValue().getTransportSupport();
            String transportStr = transport != null && transport ? "Y" : "N";
            return new SimpleStringProperty(transportStr);
        });

        // Accommodation support
        col_accommodation.setCellValueFactory(cellData -> {
            Boolean accommodation = cellData.getValue().getAccommodationSupport();
            String accommodationStr = accommodation != null && accommodation ? "Y" : "N";
            return new SimpleStringProperty(accommodationStr);
        });

        // Number of dependents
        col_dependents.setCellValueFactory(new PropertyValueFactory<>("numDependents"));

        // Status - show "✓ Valid" or "✗ Error" with error message
        col_status.setCellValueFactory(cellData -> {
            ImportEmployeeExcelDTO item = cellData.getValue();
            if (item.isValid()) {
                return new SimpleStringProperty("✓ Hợp lệ");
            } else {
                String errorMsg = item.getErrorMessage() != null ? item.getErrorMessage() : "Lỗi không xác định";
                return new SimpleStringProperty("✗ " + errorMsg);
            }
        });

        UiUtils.gI().addTooltipToColumn(col_status, 15);
    }

    /**
     * Setup button listeners
     */
    private void setupListeners() {
        btnBack.setOnAction(e -> handleBack());
        btnFinalSubmit.setOnAction(e -> handleConfirmImport());
    }

    /**
     * Set import data for confirmation
     * Dữ liệu từ EmployeeImportController
     *
     * @param dataList list of imported employee data (both valid and invalid)
     */
    public void setImportData(List<ImportEmployeeExcelDTO> dataList) {
        this.importDataList = new ArrayList<>(dataList);

        // Separate valid and invalid records
        validDataList = new ArrayList<>();
        errorDataList = new ArrayList<>();

        for (ImportEmployeeExcelDTO dto : importDataList) {
            if (dto.isValid()) {
                validDataList.add(dto);
            } else {
                errorDataList.add(dto);
            }
        }

        // Display data in table
        displayImportData();

        // Update summary labels
        updateSummary();
    }

    /**
     * Display import data in table
     */
    private void displayImportData() {
        if (tvEmployeeConfirm != null && importDataList != null) {
            // Show all data (valid + invalid) for review
            tvEmployeeConfirm.setItems(FXCollections.observableArrayList(importDataList));
        }
    }

    /**
     * Update summary statistics
     */
    private void updateSummary() {
        int totalRecords = importDataList.size();
        int validRecords = validDataList.size();
        int errorRecords = errorDataList.size();

        if (lblTotalRows != null)
            lblTotalRows.setText(String.valueOf(totalRecords) + " dòng");
        if (lblValidRows != null)
            lblValidRows.setText("✓ " + validRecords + " dòng");
        if (lblErrorRows != null)
            lblErrorRows.setText("✗ " + errorRecords + " dòng");

        // Disable import button if there are errors
        if (btnFinalSubmit != null) {
            btnFinalSubmit.setDisable(errorRecords > 0);
            if (errorRecords > 0) {
                btnFinalSubmit.setStyle("-fx-opacity: 0.5;");
            }
        }
    }

    /**
     * Handle back button - close modal without saving
     */
    private void handleBack() {
        if (btnBack.getScene() != null && btnBack.getScene().getWindow() != null) {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Handle import confirmation - create all valid employees
     * Xác nhận import - tạo tất cả nhân viên hợp lệ vào database
     */
    private void handleConfirmImport() {
        // Validation
        if (validDataList == null || validDataList.isEmpty()) {
            NotificationUtils.showErrorAlert(
                    "Không có dữ liệu hợp lệ để import.",
                    AppMessages.DIALOG_TITLE);
            return;
        }

        // Ask for final confirmation
        boolean confirmImport = UiUtils.gI().showConfirmAlert(
                "Xác nhận import " + validDataList.size() + " nhân viên?\n\nKhông thể hoàn tác sau khi import.",
                AppMessages.DIALOG_TITLE);

        if (!confirmImport) {
            return;
        }

        // Execute import in background with permission check
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.EMPLOYEE_INSERT,
                () -> employeeBUS.insertFullEmployeeBatch(validDataList, importService),
                result -> {
                    isSaved = true;
                    resultMessage = result.getMessage();
                    NotificationUtils.showInfoAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                    handleBack();
                });
    }

    @Override
    public void setTypeModal(int type) {
        // Not used for this controller
        throw new UnsupportedOperationException("Unimplemented method 'setTypeModal'");
    }
}
