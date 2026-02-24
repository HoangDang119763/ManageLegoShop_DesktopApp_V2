package GUI;

import BUS.FineBUS;
import BUS.EmployeeBUS;
import DTO.FineDTO;
import DTO.EmployeeDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import UTILS.NotificationUtils;
import UTILS.UiUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class FineTabController {
    @FXML
    private TableView<FineDTO> tblFine;
    @FXML
    private TableColumn<FineDTO, Integer> colId;
    @FXML
    private TableColumn<FineDTO, String> colReason;
    @FXML
    private TableColumn<FineDTO, LocalDateTime> colDate;
    @FXML
    private TableColumn<FineDTO, String> colLevel;
    @FXML
    private TableColumn<FineDTO, BigDecimal> colAmount;

    @FXML
    private Button btnAdd, btnEdit, btnDelete, btnRefresh;

    @FXML
    private TextField txtReason;
    @FXML
    private ComboBox<String> cbLevel; // LEVEL_1, LEVEL_2, LEVEL_3
    @FXML
    private TextField txtAmount;
    @FXML
    private Label lblEmployeeName;

    private FineBUS fineBUS;
    private EmployeeBUS employeeBUS;
    private int currentEmployeeId;

    @FXML
    public void initialize() {
        fineBUS = FineBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();

        setupTable();
        setupLevelCombo();
        setupListeners();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("fineLevel"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    private void setupLevelCombo() {
        cbLevel.setItems(FXCollections.observableArrayList("LEVEL_1", "LEVEL_2", "LEVEL_3"));
    }

    private void setupListeners() {
        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadFines());
        tblFine.setOnMouseClicked(e -> loadSelectedFine());
    }

    public void loadEmployeeFines(int employeeId) {
        this.currentEmployeeId = employeeId;
        EmployeeDTO emp = employeeBUS.getById(employeeId);
        if (emp != null) {
            lblEmployeeName.setText(emp.getFirstName() + " " + emp.getLastName());
        }
        loadFines();
    }

    private void loadFines() {
        ArrayList<FineDTO> fines = fineBUS.getByEmployeeId(currentEmployeeId);
        tblFine.setItems(FXCollections.observableArrayList(fines));
    }

    private void loadSelectedFine() {
        FineDTO selected = tblFine.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtReason.setText(selected.getReason());
            cbLevel.setValue(selected.getFineLevel());
            txtAmount.setText(selected.getAmount() != null ? selected.getAmount().toString() : "0");
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) {
            NotificationUtils.showErrorAlert("Vui lòng kiểm tra lại dữ liệu", "Lỗi nhập liệu");
            return;
        }

        FineDTO fine = new FineDTO();
        fine.setEmployeeId(currentEmployeeId);
        fine.setReason(txtReason.getText());
        fine.setFineLevel(cbLevel.getValue());
        fine.setAmount(new BigDecimal(txtAmount.getText()));
        fine.setCreatedAt(LocalDateTime.now());

        if (fineBUS.insert(fine, 1, 1)) {
            NotificationUtils.showInfoAlert("Thêm phạt/thưởng thành công", "Thành công");
            clearInputs();
            loadFines();
        } else {
            NotificationUtils.showErrorAlert("Không thể thêm phạt/thưởng", "Lỗi");
        }
    }

    @FXML
    private void handleEdit() {
        FineDTO selected = tblFine.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn phạt/thưởng để sửa", "Cảnh báo");
            return;
        }

        selected.setReason(txtReason.getText());
        selected.setFineLevel(cbLevel.getValue());
        selected.setAmount(new BigDecimal(txtAmount.getText()));

        if (fineBUS.update(selected, 1, 1)) {
            NotificationUtils.showInfoAlert("Cập nhật phạt/thưởng thành công", "Thành công");
            clearInputs();
            loadFines();
        } else {
            NotificationUtils.showErrorAlert("Không thể cập nhật phạt/thưởng", "Lỗi");
        }
    }

    @FXML
    private void handleDelete() {
        FineDTO selected = tblFine.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn phạt/thưởng để xóa", "Cảnh báo");
            return;
        }

        boolean confirmed = UiUtils.gI().showConfirmAlert("Xóa phạt/thưởng này?", "Xác nhận xóa");
        if (confirmed) {
            if (fineBUS.delete(selected.getId(), 1, 1)) {
                NotificationUtils.showInfoAlert("Xóa phạt/thưởng thành công", "Thành công");
                clearInputs();
                loadFines();
            }
        }
    }

    private boolean validateInputs() {
        if (txtReason.getText().isEmpty())
            return false;
        if (cbLevel.getValue() == null)
            return false;

        try {
            BigDecimal val = new BigDecimal(txtAmount.getText());
            return val.signum() != 0; // Khác 0 mới hợp lệ
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void clearInputs() {
        txtReason.clear();
        cbLevel.setValue(null);
        txtAmount.clear();
        tblFine.getSelectionModel().clearSelection();
    }
}
