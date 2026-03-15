package GUI;

import BUS.AllowanceBUS;
import DTO.AllowanceDTO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import UTILS.NotificationUtils;
import UTILS.UiUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AllowanceTabController {
    @FXML
    private TableView<AllowanceDTO> tblAllowance;
    @FXML
    private TableColumn<AllowanceDTO, Integer> colId;
    @FXML
    private TableColumn<AllowanceDTO, String> colName;
    @FXML
    private TableColumn<AllowanceDTO, BigDecimal> colAmount;
    @FXML
    private TableColumn<AllowanceDTO, LocalDateTime> colCreatedAt;
    @FXML
    private TableColumn<AllowanceDTO, LocalDateTime> colUpdatedAt;

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtAmount;
    @FXML
    private Button btnAdd, btnEdit, btnDelete, btnRefresh;

    private AllowanceBUS allowanceBUS;

    @FXML
    public void initialize() {
        allowanceBUS = AllowanceBUS.getInstance();

        setupTable();
        setupListeners();
        loadAllowances();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        colUpdatedAt.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
    }

    private void setupListeners() {
        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadAllowances());
        tblAllowance.setOnMouseClicked(e -> loadSelectedAllowance());
    }

    private void loadAllowances() {
        ArrayList<AllowanceDTO> allowances = allowanceBUS.getAll();
        tblAllowance.setItems(FXCollections.observableArrayList(allowances));
    }

    private void loadSelectedAllowance() {
        AllowanceDTO selected = tblAllowance.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtName.setText(selected.getName() != null ? selected.getName() : "");
            txtAmount.setText(selected.getAmount() != null ? selected.getAmount().toString() : "0");
        }
    }

    @FXML
    private void handleAdd() {
        if (!validateInputs()) {
            NotificationUtils.showErrorAlert("Vui lòng kiểm tra lại dữ liệu", "Lỗi nhập liệu");
            return;
        }

        AllowanceDTO allowance = new AllowanceDTO();
        allowance.setName(txtName.getText());
        allowance.setAmount(new BigDecimal(txtAmount.getText().isEmpty() ? "0" : txtAmount.getText()));
        allowance.setCreatedAt(LocalDateTime.now());
        allowance.setUpdatedAt(LocalDateTime.now());

        if (allowanceBUS.insert(allowance, 1, 1)) {
            NotificationUtils.showInfoAlert("Thêm trợ cấp thành công", "Thành công");
            clearInputs();
            loadAllowances();
        } else {
            NotificationUtils.showErrorAlert("Không thể thêm trợ cấp", "Lỗi");
        }
    }

    @FXML
    private void handleEdit() {
        AllowanceDTO selected = tblAllowance.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn trợ cấp để sửa", "Cảnh báo");
            return;
        }

        if (!validateInputs()) {
            NotificationUtils.showErrorAlert("Vui lòng kiểm tra lại dữ liệu", "Lỗi nhập liệu");
            return;
        }

        selected.setName(txtName.getText());
        selected.setAmount(new BigDecimal(txtAmount.getText().isEmpty() ? "0" : txtAmount.getText()));
        selected.setUpdatedAt(LocalDateTime.now());

        if (allowanceBUS.update(selected, 1, 1)) {
            NotificationUtils.showInfoAlert("Cập nhật trợ cấp thành công", "Thành công");
            clearInputs();
            loadAllowances();
        } else {
            NotificationUtils.showErrorAlert("Không thể cập nhật trợ cấp", "Lỗi");
        }
    }

    @FXML
    private void handleDelete() {
        AllowanceDTO selected = tblAllowance.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn trợ cấp để xóa", "Cảnh báo");
            return;
        }

        boolean confirmed = UiUtils.gI().showConfirmAlert("Xóa trợ cấp này?", "Xác nhận xóa");
        if (confirmed) {
            if (allowanceBUS.delete(selected.getId(), 1, 1)) {
                NotificationUtils.showInfoAlert("Xóa trợ cấp thành công", "Thành công");
                clearInputs();
                loadAllowances();
            } else {
                NotificationUtils.showErrorAlert("Không thể xóa trợ cấp", "Lỗi");
            }
        }
    }

    private boolean validateInputs() {
        if (txtName.getText().isEmpty()) {
            return false;
        }

        if (txtName.getText().length() > 100) {
            return false;
        }

        if (!txtAmount.getText().isEmpty()) {
            try {
                BigDecimal val = new BigDecimal(txtAmount.getText());
                if (val.signum() < 0)
                    return false;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }

    private void clearInputs() {
        txtName.clear();
        txtAmount.clear();
        tblAllowance.getSelectionModel().clearSelection();
    }
}
