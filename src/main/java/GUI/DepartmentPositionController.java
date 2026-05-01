package GUI;

import BUS.DepartmentBUS;
import BUS.PositionBUS;
import DTO.BUSResult;
import DTO.DepartmentDTO;
import DTO.PositionDTO;
import ENUM.BUSOperationResult;
import INTERFACE.IController;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DepartmentPositionController implements IController {
    @FXML
    private TextField txtSearchDepartment;
    @FXML
    private TextField txtSearchPosition;

    @FXML
    private Button btnDeptRefresh;
    @FXML
    private Button btnDeptAdd;
    @FXML
    private Button btnDeptEdit;
    @FXML
    private Button btnDeptDelete;

    @FXML
    private Button btnPosAdd;
    @FXML
    private Button btnPosEdit;
    @FXML
    private Button btnPosDelete;

    @FXML
    private TableView<DepartmentDTO> tblDepartment;
    @FXML
    private TableColumn<DepartmentDTO, Number> colDeptId;
    @FXML
    private TableColumn<DepartmentDTO, String> colDeptName;
    @FXML
    private TableColumn<DepartmentDTO, String> colDeptDescription;

    @FXML
    private TableView<PositionDTO> tblPosition;
    @FXML
    private TableColumn<PositionDTO, Number> colPosId;
    @FXML
    private TableColumn<PositionDTO, String> colPosName;
    @FXML
    private TableColumn<PositionDTO, String> colPosWage;

    private final DepartmentBUS departmentBUS = DepartmentBUS.getInstance();
    private final PositionBUS positionBUS = PositionBUS.getInstance();

    private ObservableList<DepartmentDTO> allDepartments = FXCollections.observableArrayList();
    private ObservableList<PositionDTO> allPositions = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadTable();
        setupListeners();
        reloadDepartments();
        reloadPositions();
    }

    @Override
    public void loadTable() {
        colDeptId.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()));
        colDeptName
                .setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getName()));
        colDeptDescription.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDescription()));

        colPosId.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()));
        colPosName
                .setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getName()));
        colPosWage.setCellValueFactory(cell -> {
            BigDecimal wage = cell.getValue().getWage();
            String text = wage != null ? wage.toPlainString() : "0";
            return new javafx.beans.property.SimpleStringProperty(text);
        });
    }

    @Override
    public void setupListeners() {
        txtSearchDepartment.textProperty()
                .addListener((obs, oldV, newV) -> applyFilters());
        txtSearchPosition.textProperty()
                .addListener((obs, oldV, newV) -> applyFilters());

        btnDeptRefresh.setOnAction(e -> {
            txtSearchDepartment.clear();
            txtSearchPosition.clear();
            reloadDepartments();
            reloadPositions();
            NotificationUtils.showInfoAlert(AppMessages.GENERAL_REFRESH_SUCCESS, AppMessages.DIALOG_TITLE);
        });

        btnDeptAdd.setOnAction(e -> handleAddDepartment());
        btnDeptEdit.setOnAction(e -> handleEditDepartment());
        btnDeptDelete.setOnAction(e -> handleDeleteDepartment());

        btnPosAdd.setOnAction(e -> handleAddPosition());
        btnPosEdit.setOnAction(e -> handleEditPosition());
        btnPosDelete.setOnAction(e -> handleDeletePosition());

        tblDepartment.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                filterPositionsByDepartment(newSel.getId());
            } else {
                tblPosition.setItems(allPositions);
            }
        });
    }

    private void reloadDepartments() {
        List<DepartmentDTO> list = departmentBUS.getAll();
        allDepartments.setAll(list != null ? list : List.of());
        applyFilters();
    }

    private void reloadPositions() {
        List<PositionDTO> list = positionBUS.getAll();
        allPositions.setAll(list != null ? list : List.of());
        applyFilters();
    }

    @Override
    public void applyFilters() {
        String deptKeyword = txtSearchDepartment.getText() != null
                ? txtSearchDepartment.getText().toLowerCase(Locale.ROOT).trim()
                : "";
        String posKeyword = txtSearchPosition.getText() != null
                ? txtSearchPosition.getText().toLowerCase(Locale.ROOT).trim()
                : "";

        List<DepartmentDTO> filteredDept = allDepartments.stream().filter(d -> {
            if (deptKeyword.isEmpty()) {
                return true;
            }
            String name = d.getName() != null ? d.getName().toLowerCase(Locale.ROOT) : "";
            return String.valueOf(d.getId()).contains(deptKeyword) || name.contains(deptKeyword);
        }).collect(Collectors.toList());

        tblDepartment.setItems(FXCollections.observableArrayList(filteredDept));

        List<PositionDTO> filteredPos = allPositions.stream().filter(p -> {
            if (posKeyword.isEmpty()) {
                return true;
            }
            String name = p.getName() != null ? p.getName().toLowerCase(Locale.ROOT) : "";
            return String.valueOf(p.getId()).contains(posKeyword) || name.contains(posKeyword);
        }).collect(Collectors.toList());

        tblPosition.setItems(FXCollections.observableArrayList(filteredPos));
    }

    private void filterPositionsByDepartment(int departmentId) {
        List<PositionDTO> filtered = allPositions.stream()
                .filter(p -> p.getId() != null) // placeholder, sẽ thay bằng departmentId khi có cột này
                .collect(Collectors.toList());
        tblPosition.setItems(FXCollections.observableArrayList(filtered));
    }

    @Override
    public void resetFilters() {
        txtSearchDepartment.clear();
        txtSearchPosition.clear();
        applyFilters();
    }

    @Override
    public void hideButtonWithoutPermission() {
        // Có thể ẩn/hiện nút theo PermissionKey sau
    }

    // ====== HANDLERS FOR DEPARTMENT CRUD ======

    private DepartmentDTO getSelectedDepartment() {
        return tblDepartment.getSelectionModel().getSelectedItem();
    }

    private PositionDTO getSelectedPosition() {
        return tblPosition.getSelectionModel().getSelectedItem();
    }

    private void handleAddDepartment() {
        DepartmentModalController modal = UiUtils.gI().openStageWithController(
                "/GUI/DepartmentModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm phòng ban");
        if (modal != null && modal.isSaved()) {
            reloadDepartments();
        }
    }

    private void handleEditDepartment() {
        DepartmentDTO selected = getSelectedDepartment();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn phòng ban.", AppMessages.DIALOG_TITLE);
            return;
        }
        DepartmentModalController modal = UiUtils.gI().openStageWithController(
                "/GUI/DepartmentModal.fxml",
                controller -> {
                    controller.setDepartment(new DepartmentDTO(selected));
                    controller.setTypeModal(1);
                },
                "Sửa phòng ban");
        if (modal != null && modal.isSaved()) {
            reloadDepartments();
        }
    }

    private void handleDeleteDepartment() {
        DepartmentDTO selected = getSelectedDepartment();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn phòng ban.", AppMessages.DIALOG_TITLE);
            return;
        }
        if (!UiUtils.gI().showConfirmAlert("Bạn chắc muốn xóa phòng ban này?", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }
        BUSResult result = departmentBUS.delete(selected.getId());
        if (result.getCode() == BUSOperationResult.SUCCESS) {
            NotificationUtils.showInfoAlert(AppMessages.OPERATION_SUCCESS, AppMessages.DIALOG_TITLE);
            reloadDepartments();
        } else {
            String message = result.getMessage() != null ? result.getMessage() : AppMessages.UNKNOWN_ERROR;
            NotificationUtils.showErrorAlert(message, AppMessages.DIALOG_TITLE);
        }
    }

    // ====== HANDLERS FOR POSITION CRUD ======

    private void handleAddPosition() {
        PositionModalController modal = UiUtils.gI().openStageWithController(
                "/GUI/PositionModal.fxml",
                controller -> controller.setTypeModal(0),
                "Thêm vị trí");
        if (modal != null && modal.isSaved()) {
            reloadPositions();
        }
    }

    private void handleEditPosition() {
        PositionDTO selected = getSelectedPosition();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn vị trí.", AppMessages.DIALOG_TITLE);
            return;
        }
        PositionModalController modal = UiUtils.gI().openStageWithController(
                "/GUI/PositionModal.fxml",
                controller -> {
                    controller.setPosition(new PositionDTO(
                            selected.getId(),
                            selected.getName(),
                            selected.getWage(),
                            selected.getMinExperience(),
                            selected.getMaxExperience(),
                            selected.getCreatedAt(),
                            selected.getUpdatedAt()));
                    controller.setTypeModal(1);
                },
                "Sửa vị trí");
        if (modal != null && modal.isSaved()) {
            reloadPositions();
        }
    }

    private void handleDeletePosition() {
        PositionDTO selected = getSelectedPosition();
        if (selected == null) {
            NotificationUtils.showErrorAlert("Vui lòng chọn vị trí.", AppMessages.DIALOG_TITLE);
            return;
        }
        if (!UiUtils.gI().showConfirmAlert("Bạn chắc muốn xóa vị trí này?", AppMessages.DIALOG_TITLE_CONFIRM)) {
            return;
        }
        BUSResult result = positionBUS.delete(selected.getId());
        if (result.getCode() == BUSOperationResult.SUCCESS) {
            NotificationUtils.showInfoAlert(AppMessages.OPERATION_SUCCESS, AppMessages.DIALOG_TITLE);
            reloadPositions();
        } else {
            String message = result.getMessage() != null ? result.getMessage() : AppMessages.UNKNOWN_ERROR;
            NotificationUtils.showErrorAlert(message, AppMessages.DIALOG_TITLE);
        }
    }
}
