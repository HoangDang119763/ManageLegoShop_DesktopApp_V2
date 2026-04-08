package GUI;

import BUS.FineBUS;
import BUS.EmployeeBUS;
import DTO.FineDTO;
import ENUM.Status.FineType; // Import Enum mới
import UTILS.NotificationUtils;
import UTILS.ValidationUtils;
import SERVICE.SessionManagerService;
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
import java.util.List;

public class DisciplineTabNestedController {
    private static final Logger log = LoggerFactory.getLogger(DisciplineTabNestedController.class);

    @FXML
    private VBox containerDiscipline;

    @FXML
    private ComboBox<Object> cbFilterType; // Cập nhật để chứa String "Tất cả" và FineType

    @FXML
    private TableView<FineDTO> tblDiscipline;

    @FXML
    private TableColumn<FineDTO, String> colId;
    @FXML
    private TableColumn<FineDTO, String> colDate;
    @FXML
    private TableColumn<FineDTO, String> colType; // Kỷ luật / Khen thưởng
    @FXML
    private TableColumn<FineDTO, String> colAmount;
    @FXML
    private TableColumn<FineDTO, String> colReason;
    @FXML
    private TableColumn<FineDTO, String> colAction;

    @FXML
    private Button btnAdd;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;

    private FineBUS fineBUS;
    private EmployeeBUS employeeBUS;
    private ValidationUtils validationUtils;
    private SessionManagerService sessionManager;

    private int currentEmployeeId = -1;
    private ObservableList<FineDTO> disciplineList;

    @FXML
    public void initialize() {
        log.info("Initializing DisciplineTabNestedController");
        fineBUS = FineBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        validationUtils = ValidationUtils.getInstance();
        sessionManager = SessionManagerService.getInstance();
        disciplineList = FXCollections.observableArrayList();

        setupTable();
        setupFilters(); // Khởi tạo bộ lọc
        setupButtons();
    }

    private void setupTable() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getId())));
        
        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                validationUtils.formatDateTime(cellData.getValue().getCreatedAt())));

        // Cập nhật hiển thị cột Loại bằng Enum FineType (có màu sắc)
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getType()));
        colType.setCellFactory(column -> new TableCell<FineDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    FineType type = FineType.fromString(item);
                    setText(type.getLabel());
                    // Tô màu xanh cho Khen thưởng, đỏ cho Kỷ luật
                    setStyle("-fx-text-fill: " + type.getColor() + "; -fx-font-weight: bold;");
                }
            }
        });

        colAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getAmount())));
        
        colReason.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getReason() != null ? cellData.getValue().getReason() : ""));

        colAction.setCellFactory(param -> createActionCell());

        tblDiscipline.setItems(disciplineList);
    }

    private void setupFilters() {
        if (cbFilterType != null) {
            ObservableList<Object> filterItems = FXCollections.observableArrayList();
            filterItems.add("Tất cả");
            filterItems.addAll(FineType.values());
            cbFilterType.setItems(filterItems);
            cbFilterType.getSelectionModel().selectFirst();
            
            // Lắng nghe sự kiện lọc (nếu cần thiết triển khai lọc local)
            cbFilterType.setOnAction(e -> applyLocalFilter());
        }
    }

    private void applyLocalFilter() {
        Object selected = cbFilterType.getValue();
        if (selected == null || selected.equals("Tất cả")) {
            tblDiscipline.setItems(disciplineList);
        } else {
            FineType type = (FineType) selected;
            ObservableList<FineDTO> filtered = disciplineList.filtered(f -> f.getType().equals(type.name()));
            tblDiscipline.setItems(filtered);
        }
    }

    private TableCell<FineDTO, String> createActionCell() {
        return new TableCell<FineDTO, String>() {
            private final Button btnView = new Button("Xem");
            private final Button btnDel = new Button("Xóa");

            {
                btnView.setStyle("-fx-padding: 5; -fx-font-size: 11;");
                btnDel.setStyle("-fx-padding: 5; -fx-font-size: 11;");

                btnView.setOnAction(event -> {
                    FineDTO fine = getTableView().getItems().get(getIndex());
                    showDisciplineDetails(fine);
                });

                btnDel.setOnAction(event -> {
                    FineDTO fine = getTableView().getItems().get(getIndex());
                    deleteDiscipline(fine);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new javafx.scene.layout.HBox(5, btnView, btnDel));
            }
        };
    }

    private void setupButtons() {
        btnAdd.setOnAction(event -> addNewDiscipline());
        btnEdit.setOnAction(event -> editSelectedDiscipline());
        btnDelete.setOnAction(event -> deleteSelectedDiscipline());
    }

    public void loadEmployeeDisciplines(int employeeId) {
        currentEmployeeId = employeeId;
        log.info("Loading records for employee: {}", employeeId);

        new Thread(() -> {
            try {
                List<FineDTO> fines = fineBUS.getByEmployeeId(employeeId);
                Platform.runLater(() -> {
                    disciplineList.setAll(fines != null ? fines : FXCollections.observableArrayList());
                    applyLocalFilter(); // Áp dụng lại filter hiện tại sau khi load
                });
            } catch (Exception e) {
                log.error("Error loading records", e);
                Platform.runLater(() -> NotificationUtils.showErrorAlert("Lỗi", "Không thể tải dữ liệu"));
            }
        }).start();
    }

    private void showDisciplineDetails(FineDTO fine) {
        if (fine == null) return;
        FineType type = FineType.fromString(fine.getType());
        
        String details = String.format("ID: %d\nLoại: %s\nMức Độ: %s\nNgày: %s\nSố tiền: %s\nLý do: %s",
                fine.getId(),
                type.getLabel(),
                fine.getFineLevel() != null ? fine.getFineLevel() : "Không xác định",
                validationUtils.formatDateTime(fine.getCreatedAt()),
                validationUtils.formatCurrency(fine.getAmount()),
                fine.getReason() != null ? fine.getReason() : "Không có");

        NotificationUtils.showInfoAlert("Chi tiết bản ghi", details);
    }

    private void deleteDiscipline(FineDTO fine) {
        if (fine == null || !NotificationUtils.showConfirmAlert("Xác nhận xóa bản ghi này?")) return;

        new Thread(() -> {
            try {
                boolean success = fineBUS.delete(fine.getId(), 
                                                sessionManager.employeeRoleId(), 
                                                sessionManager.employeeLoginId());
                Platform.runLater(() -> {
                    if (success) {
                        NotificationUtils.showInfoAlert("Thành công", "Đã xóa bản ghi");
                        loadEmployeeDisciplines(currentEmployeeId);
                    } else {
                        NotificationUtils.showErrorAlert("Thất bại", "Lỗi phân quyền hoặc hệ thống");
                    }
                });
            } catch (Exception e) {
                log.error("Error delete", e);
            }
        }).start();
    }

    private void deleteSelectedDiscipline() {
        FineDTO selected = tblDiscipline.getSelectionModel().getSelectedItem();
        if (selected != null) deleteDiscipline(selected);
        else NotificationUtils.showInfoAlert("Cảnh báo", "Vui lòng chọn một bản ghi");
    }

    private void addNewDiscipline() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/DisciplineModal.fxml"));
            VBox modalRoot = loader.load();
            DisciplineModalController modalController = loader.getController();
            modalController.setEmployeeId(currentEmployeeId);
            modalController.setParentController(this);

            Stage modalStage = new Stage();
            modalStage.setTitle("Thêm Bản Khen thưởng / Kỷ luật");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();
        } catch (IOException e) {
            log.error("Error opening modal", e);
            NotificationUtils.showErrorAlert("Lỗi", "Không thể mở form");
        }
    }

    private void editSelectedDiscipline() {
        FineDTO selected = tblDiscipline.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showInfoAlert("Cảnh báo", "Vui lòng chọn một bản ghi");
        } else {
            // Có thể mở Modal tương tự như addNew nhưng truyền dữ liệu vào để sửa
            NotificationUtils.showInfoAlert("Thông báo", "Chức năng chỉnh sửa đang được cập nhật");
        }
    }
}