package GUI;

import BUS.DeductionBUS;
import BUS.EmployeeBUS;
import DTO.DeductionDTO;
import DTO.EmployeeDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class DeductionTabController {
    @FXML
    private TableView<DeductionDTO> tblDeduction;
    @FXML
    private TableColumn<DeductionDTO, Integer> colId;
    @FXML
    private TableColumn<DeductionDTO, LocalDate> colPeriod;
    @FXML
    private TableColumn<DeductionDTO, BigDecimal> colHealthIns;
    @FXML
    private TableColumn<DeductionDTO, BigDecimal> colSocialIns;
    @FXML
    private TableColumn<DeductionDTO, BigDecimal> colUnemploymentIns;
    @FXML
    private TableColumn<DeductionDTO, BigDecimal> colPersonalIncomeTax;

    @FXML
    private ComboBox<YearMonth> cbPeriod;
    @FXML
    private Button btnAdd, btnEdit, btnDelete, btnRefresh;

    @FXML
    private TextField txtHealthIns;
    @FXML
    private TextField txtSocialIns;
    @FXML
    private TextField txtUnemploymentIns;
    @FXML
    private TextField txtPersonalIncomeTax;
    @FXML
    private Label lblEmployeeName;

    private DeductionBUS deductionBUS;
    private EmployeeBUS employeeBUS;
    private int currentEmployeeId;

    @FXML
    public void initialize() {
        deductionBUS = DeductionBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();

        setupTable();
        setupPeriodCombo();
        setupListeners();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPeriod.setCellValueFactory(new PropertyValueFactory<>("salaryPeriod"));
        colHealthIns.setCellValueFactory(new PropertyValueFactory<>("healthInsurance"));
        colSocialIns.setCellValueFactory(new PropertyValueFactory<>("socialInsurance"));
        colUnemploymentIns.setCellValueFactory(new PropertyValueFactory<>("unemploymentInsurance"));
        colPersonalIncomeTax.setCellValueFactory(new PropertyValueFactory<>("personalIncomeTax"));
    }

    private void setupPeriodCombo() {
        ArrayList<YearMonth> periods = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            periods.add(current.minusMonths(i));
        }
        cbPeriod.setItems(FXCollections.observableArrayList(periods));
        cbPeriod.valueProperty().addListener((obs, oldVal, newVal) -> loadDeductions());
    }

    private void setupListeners() {
        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadDeductions());
        tblDeduction.setOnMouseClicked(e -> loadSelectedDeduction());
    }

    public void loadEmployeeDeductions(int employeeId) {
        this.currentEmployeeId = employeeId;
        EmployeeDTO emp = employeeBUS.getById(employeeId);
        if (emp != null) {
            lblEmployeeName.setText(emp.getFirstName() + " " + emp.getLastName());
            // AUTO-LOAD INSURANCE CODES AND DISPLAY PARTICIPATION STATUS
            displayInsuranceCodeStatus(emp);
        }
        loadDeductions();
    }

    private void displayInsuranceCodeStatus(EmployeeDTO emp) {
        StringBuilder status = new StringBuilder("Mã bảo hiểm: ");
        
        // Health Insurance
        if (emp.getHealthInsCode() != null && !emp.getHealthInsCode().isEmpty() && !emp.getHealthInsCode().equals("0")) {
            status.append("BHYT: ").append(emp.getHealthInsCode()).append(" ✓ | ");
        } else {
            status.append("BHYT: Chưa tham gia | ");
        }
        
        // Social Insurance
        if (emp.getSocialInsuranceCode() != null && !emp.getSocialInsuranceCode().isEmpty() && !emp.getSocialInsuranceCode().equals("0")) {
            status.append("BHXH: ").append(emp.getSocialInsuranceCode()).append(" ✓ | ");
        } else {
            status.append("BHXH: Chưa tham gia | ");
        }
        
        // Unemployment Insurance
        if (emp.getUnemploymentInsuranceCode() != null && !emp.getUnemploymentInsuranceCode().isEmpty() && !emp.getUnemploymentInsuranceCode().equals("0")) {
            status.append("BHTN: ").append(emp.getUnemploymentInsuranceCode()).append(" ✓");
        } else {
            status.append("BHTN: Chưa tham gia");
        }
        
        // Display in UI - create a tooltip or label
        lblEmployeeName.setTooltip(new Tooltip(status.toString()));
    }

    private void loadDeductions() {
        ArrayList<DeductionDTO> deductions = deductionBUS.getByEmployeeId(currentEmployeeId);
        tblDeduction.setItems(FXCollections.observableArrayList(deductions));
    }

    private void loadSelectedDeduction() {
        DeductionDTO selected = tblDeduction.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtHealthIns.setText(selected.getHealthInsurance() != null && selected.getHealthInsurance().signum() != 0 ? 
                selected.getHealthInsurance().toString() : "0");
            txtSocialIns.setText(selected.getSocialInsurance() != null && selected.getSocialInsurance().signum() != 0 ? 
                selected.getSocialInsurance().toString() : "0");
            txtUnemploymentIns.setText(selected.getUnemploymentInsurance() != null && selected.getUnemploymentInsurance().signum() != 0 ? 
                selected.getUnemploymentInsurance().toString() : "0");
            txtPersonalIncomeTax.setText(selected.getPersonalIncomeTax() != null && selected.getPersonalIncomeTax().signum() != 0 ? 
                selected.getPersonalIncomeTax().toString() : "0");
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) {
            NotificationUtils.showErrorAlert("Vui lòng kiểm tra lại dữ liệu", "Lỗi nhập liệu");
            return;
        }

        DeductionDTO deduction = new DeductionDTO();
        deduction.setEmployeeId(currentEmployeeId);
        deduction.setSalaryPeriod(cbPeriod.getValue().atDay(1));
        
        // Chỉ set khi khác 0 (tham gia bảo hiểm)
        deduction.setHealthInsurance(getValueOrZero(txtHealthIns));
        deduction.setSocialInsurance(getValueOrZero(txtSocialIns));
        deduction.setUnemploymentInsurance(getValueOrZero(txtUnemploymentIns));
        deduction.setPersonalIncomeTax(getValueOrZero(txtPersonalIncomeTax));

        if (deductionBUS.insert(deduction, 1, 1)) {
            NotificationUtils.showInfoAlert("Thêm bảo hiểm thành công", "Thành công");
            clearInputs();
            loadDeductions();
        } else {
            NotificationUtils.showErrorAlert("Không thể thêm bảo hiểm", "Lỗi");
        }
    }

    @FXML
    private void handleEdit() {
        DeductionDTO selected = tblDeduction.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn bảo hiểm để sửa", "Cảnh báo");
            return;
        }

        selected.setHealthInsurance(getValueOrZero(txtHealthIns));
        selected.setSocialInsurance(getValueOrZero(txtSocialIns));
        selected.setUnemploymentInsurance(getValueOrZero(txtUnemploymentIns));
        selected.setPersonalIncomeTax(getValueOrZero(txtPersonalIncomeTax));

        if (deductionBUS.update(selected, 1, 1)) {
            NotificationUtils.showInfoAlert("Cập nhật bảo hiểm thành công", "Thành công");
            clearInputs();
            loadDeductions();
        } else {
            NotificationUtils.showErrorAlert("Không thể cập nhật bảo hiểm", "Lỗi");
        }
    }

    @FXML
    private void handleDelete() {
        DeductionDTO selected = tblDeduction.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn bảo hiểm để xóa", "Cảnh báo");
            return;
        }

        boolean confirmed = NotificationUtils.showConfirmAlert("Xóa bảo hiểm này?", new ArrayList<>(), "Xác nhận xóa", "");
        if (confirmed) {
            if (deductionBUS.delete(selected.getId(), 1, 1)) {
                NotificationUtils.showInfoAlert("Xóa bảo hiểm thành công", "Thành công");
                clearInputs();
                loadDeductions();
            }
        }
    }

    private BigDecimal getValueOrZero(TextField txtField) {
        if (txtField.getText().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(txtField.getText());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean validateInputs() {
        try {
            if (!txtHealthIns.getText().isEmpty()) {
                BigDecimal val = new BigDecimal(txtHealthIns.getText());
                if (val.signum() < 0) return false;
            }
            if (!txtSocialIns.getText().isEmpty()) {
                BigDecimal val = new BigDecimal(txtSocialIns.getText());
                if (val.signum() < 0) return false;
            }
            if (!txtUnemploymentIns.getText().isEmpty()) {
                BigDecimal val = new BigDecimal(txtUnemploymentIns.getText());
                if (val.signum() < 0) return false;
            }
            if (!txtPersonalIncomeTax.getText().isEmpty()) {
                BigDecimal val = new BigDecimal(txtPersonalIncomeTax.getText());
                if (val.signum() < 0) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void clearInputs() {
        txtHealthIns.clear();
        txtSocialIns.clear();
        txtUnemploymentIns.clear();
        txtPersonalIncomeTax.clear();
        tblDeduction.getSelectionModel().clearSelection();
    }
}
