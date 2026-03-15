package GUI;

import BUS.FineBUS;
import DTO.FineDTO;
import ENUM.Status.FineType;
import UTILS.NotificationUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
        // Bạn có thể giữ danh sách các mức độ kỷ luật này
        cbDisciplineType.setItems(FXCollections.observableArrayList(
                "Cảnh cáo",
                "Nhắc nhở",
                "Giáng chức",
                "Tạm dừng hợp đồng",
                "Chấm dứt hợp đồng"
        ));
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
            String amountStr = txtFineAmount.getText().replace(",", "").trim();
            if (!amountStr.isEmpty()) {
                fineAmount = new BigDecimal(amountStr);
            }
        } catch (Exception e) {
            NotificationUtils.showErrorAlert("Lỗi", "Số tiền không hợp lệ");
            return;
        }

        // Create DTO
        FineDTO fine = new FineDTO();
        fine.setEmployeeId(employeeId);
        fine.setFineLevel(cbDisciplineType.getValue());
        fine.setReason(taReason.getText());
        fine.setAmount(fineAmount);
        fine.setFinePay(BigDecimal.ZERO); 
        fine.setCreatedAt(java.time.LocalDateTime.now());
        
        // SỬ DỤNG ENUM TẠI ĐÂY:
        // Thay vì dùng fine.setType("DISCIPLINE"), ta dùng Enum để đảm bảo chính xác
        fine.setType(FineType.DISCIPLINE.name()); 

        new Thread(() -> {
            // Giả định Admin thực hiện (Role=1, ID=1)
            if (fineBUS.insert(fine, 1, 1)) {
                Platform.runLater(() -> {
                    NotificationUtils.showInfoAlert("Thành công", "Thêm bản kỷ luật thành công");
                    if (parentController != null) parentController.loadEmployeeDisciplines(employeeId);
                    closeModal();
                });
            } else {
                Platform.runLater(() -> NotificationUtils.showErrorAlert("Thất bại", "Lỗi hệ thống hoặc bạn không có quyền thực hiện"));
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