package GUI;

import BUS.FineBUS;
import BUS.EmployeeBUS;
import DTO.FineDTO;
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
    private ComboBox<String> cbFilterType; // Filter: All, Discipline, Reward

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

    // BUS instances
    private FineBUS fineBUS;
    private EmployeeBUS employeeBUS;
    private ValidationUtils validationUtils;
    private SessionManagerService sessionManager;

    private int currentEmployeeId = -1;
    private ObservableList<FineDTO> disciplineList;

    /**
     * Initialize Discipline nested controller
     */
    @FXML
    public void initialize() {
        log.info("Initializing DisciplineTabNestedController");
        fineBUS = FineBUS.getInstance();
        employeeBUS = EmployeeBUS.getInstance();
        validationUtils = ValidationUtils.getInstance();
        sessionManager = SessionManagerService.getInstance();
        disciplineList = FXCollections.observableArrayList();

        setupTable();
        setupButtons();
    }

    /**
     * Setup table columns
     */
    private void setupTable() {
        colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(cellData.getValue().getId())));
        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                validationUtils.formatDateTime(cellData.getValue().getCreatedAt())));
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFineLevel() != null ? cellData.getValue().getFineLevel() : ""));
        colAmount.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getAmount())));
        colReason.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getReason() != null ? cellData.getValue().getReason() : ""));

        // Action column
        colAction.setCellFactory(param -> createActionCell());

        tblDiscipline.setItems(disciplineList);
    }

    /**
     * Create action cell with View/Delete buttons
     */
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

    /**
     * Setup button actions
     */
    private void setupButtons() {
        btnAdd.setOnAction(event -> addNewDiscipline());
        btnEdit.setOnAction(event -> editSelectedDiscipline());
        btnDelete.setOnAction(event -> deleteSelectedDiscipline());
    }

    /**
     * Load discipline records for specified employee
     */
    public void loadEmployeeDiscipline(int employeeId) {
        loadEmployeeDisciplines(employeeId);
    }

    public void loadEmployeeDisciplines(int employeeId) {
        currentEmployeeId = employeeId;
        log.info("Loading discipline records for employee: {}", employeeId);

        new Thread(() -> {
            try {
                List<FineDTO> fines = fineBUS.getByEmployeeId(employeeId);
                Platform.runLater(() -> {
                    if (fines != null && !fines.isEmpty()) {
                        disciplineList.setAll(fines);
                    } else {
                        disciplineList.clear();
                    }
                });
            } catch (Exception e) {
                log.error("Error loading discipline records", e);
                Platform.runLater(() -> {
                    NotificationUtils.showErrorAlert("Lỗi tải kỷ luật/khen thưởng",
                            "Chi tiết: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Show discipline record details
     */
    private void showDisciplineDetails(FineDTO fine) {
        if (fine == null)
            return;

        String details = String.format("ID: %d\nMức Độ: %s\nNgày: %s\nSố tiền: %s\nLý do: %s",
                fine.getId(),
                fine.getFineLevel() != null ? fine.getFineLevel() : "Không xác định",
                validationUtils.formatDateTime(fine.getCreatedAt()),
                validationUtils.formatCurrency(fine.getAmount()),
                fine.getReason() != null ? fine.getReason() : "Không có");

        NotificationUtils.showInfoAlert("Chi tiết " + (fine.getFineLevel() != null ? fine.getFineLevel() : ""),
                details);
    }

    /**
     * Delete discipline record
     */
    private void deleteDiscipline(FineDTO fine) {
        if (fine == null)
            return;

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Xóa bản ghi kỷ luật/khen thưởng");
        confirmDialog.setContentText("Bạn có chắc muốn xóa bản ghi này?");

        if (confirmDialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    if (fineBUS.delete(fine.getId(), sessionManager.employeeRoleId(),
                            sessionManager.employeeLoginId())) {
                        Platform.runLater(() -> {
                            NotificationUtils.showInfoAlert("Thành công", "Xóa thành công");
                            loadEmployeeDiscipline(currentEmployeeId);
                        });
                    } else {
                        Platform.runLater(() -> {
                            NotificationUtils.showErrorAlert("Thất bại", "Không thể xóa bản ghi");
                        });
                    }
                } catch (Exception e) {
                    log.error("Error deleting discipline record", e);
                    Platform.runLater(() -> {
                        NotificationUtils.showErrorAlert("Lỗi", "Chi tiết: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void deleteSelectedDiscipline() {
        FineDTO selected = tblDiscipline.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteDiscipline(selected);
        } else {
            NotificationUtils.showInfoAlert("Cảnh báo", "Vui lòng chọn một bản ghi");
        }
    }

    private void addNewDiscipline() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GUI/DisciplineModal.fxml"));
            VBox modalRoot = loader.load();
            DisciplineModalController modalController = loader.getController();
            modalController.setEmployeeId(currentEmployeeId);
            modalController.setParentController(this);

            Stage modalStage = new Stage();
            modalStage.setTitle("Thêm Bản Kỷ Luật");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.showAndWait();
        } catch (IOException e) {
            log.error("Error opening discipline modal", e);
            NotificationUtils.showErrorAlert("Lỗi", "Không thể mở form thêm bản kỷ luật");
        }
    }

    private void editSelectedDiscipline() {
        FineDTO selected = tblDiscipline.getSelectionModel().getSelectedItem();
        if (selected == null) {
            NotificationUtils.showInfoAlert("Cảnh báo", "Vui lòng chọn một bản ghi");
        } else {
            NotificationUtils.showInfoAlert("Thông báo", "Chức năng chỉnh sửa sẽ được thêm vào");
        }
    }
}
