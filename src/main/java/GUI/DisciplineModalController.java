package GUI;

import BUS.FineBUS;
import DTO.FineDTO;
import UTILS.NotificationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DisciplineModalController {
    private static final Logger log = LoggerFactory.getLogger(DisciplineModalController.class);

    @FXML
    private ComboBox<String> cbDisciplineType;

    @FXML
    private TextArea taReason;

    @FXML
    private DatePicker dpDisciplineDate;

    @FXML
    private TextField txtFineAmount;

    @FXML
    private Button btnSave;

    @FXML
    private Button btnCancel;

    private FineBUS fineBUS;
    private int employeeId;
    private DisciplineTabNestedController parentController;

    @FXML
    public void initialize() {
        log.info("Initializing DisciplineModalController");
        fineBUS = FineBUS.getInstance();

        setupButtons();
        setupDisciplineTypes();
        dpDisciplineDate.setValue(LocalDate.now());
    }

    private void setupButtons() {
        btnSave.setOnAction(event -> saveDiscipline());
        btnCancel.setOnAction(event -> closeModal());
    }

    private void setupDisciplineTypes() {
        cbDisciplineType.getItems().addAll(
                "Cảnh cáo",
                "Nhắc nhở",
                "Giáng chức",
                "Tạm dừng hợp đồng",
                "Chấm dứt hợp đồng");
    }

    private void saveDiscipline() {
        // Validation
        if (cbDisciplineType.getValue() == null || cbDisciplineType.getValue().isEmpty()) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn loại kỷ luật");
            return;
        }

        if (taReason.getText() == null || taReason.getText().isEmpty()) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng nhập lý do kỷ luật");
            return;
        }

        if (dpDisciplineDate.getValue() == null) {
            NotificationUtils.showErrorAlert("Lỗi", "Vui lòng chọn ngày kỷ luật");
            return;
        }

        // Parse amount
        BigDecimal fineAmount = BigDecimal.ZERO;
        try {
            fineAmount = new BigDecimal(txtFineAmount.getText().replace(",", ""));
        } catch (Exception e) {
            NotificationUtils.showErrorAlert("Lỗi", "Số tiền không hợp lệ");
            return;
        }

        FineDTO fine = new FineDTO();
        fine.setEmployeeId(employeeId);
        fine.setFineLevel(cbDisciplineType.getValue());
        fine.setReason(taReason.getText());
        fine.setAmount(fineAmount);
        fine.setFinePay(BigDecimal.ZERO); // Mặc định chưa thanh toán
        fine.setCreatedAt(java.time.LocalDateTime.now());
        fine.setType("DISCIPLINE"); // QUAN TRỌNG: Gán type để đồng bộ với Database

        new Thread(() -> {
            // Sử dụng tham số Role và ID người dùng hiện tại (giả định Admin = 1)
            if (fineBUS.insert(fine, 1, 1)) {
                Platform.runLater(() -> {
                    NotificationUtils.showInfoAlert("Thành công", "Thêm bản kỷ luật thành công");
                    if (parentController != null) parentController.loadEmployeeDisciplines(employeeId);
                    closeModal();
                });
            } else {
                Platform.runLater(() -> NotificationUtils.showErrorAlert("Thất bại", "Lỗi dữ liệu hoặc phân quyền"));
            }
        }).start();
    }

    private void closeModal() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public void setParentController(DisciplineTabNestedController parentController) {
        this.parentController = parentController;
    }
}
