package GUI;

import BUS.DetailDiscountBUS;
import BUS.DiscountBUS;
import DTO.*;
import ENUM.DiscountType;
import ENUM.PermissionKey;
import INTERFACE.IModalController;
import UTILS.AppMessages;
import UTILS.ModalBuilder;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import UTILS.ValidationUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DiscountModalController implements IModalController {
    @FXML
    private Label modalName;
    @FXML
    private TextField txtDiscountCode;
    @FXML
    private TextField txtDiscountName;
    @FXML
    private ComboBox<String> cbTypeDiscount;
    @FXML
    private DatePicker dpStartDate;
    @FXML
    private DatePicker dpEndDate;
    @FXML
    private Button saveBtn, closeBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button deleteBtn;
    @FXML
    private TableView<DetailDiscountDTO> tblDetailDiscount;
    @FXML
    private TableColumn<DetailDiscountDTO, String> tlb_col_totalPriceInvoice;
    @FXML
    private TableColumn<DetailDiscountDTO, String> tlb_col_discountAmount;
    @FXML
    private StackPane loadingOverlay;

    private DetailDiscountDTO selectedDetailDiscount;
    private ArrayList<DetailDiscountDTO> arrDetailDiscount = new ArrayList<>();
    private DiscountDTO discount;

    @Getter
    private boolean isSaved;
    @Getter
    private String resultMessage = "";
    private int typeModal;

    @FXML
    public void initialize() {
        loadComboBox();
        setupListeners();
    }

    public void setupListeners() {
        saveBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
        addBtn.setOnAction(event -> handleAddBtn());
        deleteBtn.setOnAction(e -> handleDeleteBtn());
        editBtn.setOnAction(e -> handleEditBtn());
    }

    private void loadComboBox() {
        for (DiscountType type : DiscountType.values()) {
            cbTypeDiscount.getItems().add(type.getDisplayName());
        }
        cbTypeDiscount.getSelectionModel().selectFirst();
    }

    public void setDiscount(String code) {
        this.discount = DiscountBUS.getInstance().getById(code);
        txtDiscountCode.setText(discount.getCode());
        txtDiscountName.setText(discount.getName());
        cbTypeDiscount.getSelectionModel().select(DiscountType.fromCode(discount.getType()).getDisplayName());
        LocalDateTime startDate = discount.getStartDate();
        LocalDateTime endDate = discount.getEndDate();

        dpStartDate.setValue(startDate != null ? startDate.toLocalDate() : null);
        dpEndDate.setValue(endDate != null ? endDate.toLocalDate() : null);
        if (typeModal == 1) {
            TaskUtil.executeSecure(null, PermissionKey.DISCOUNT_UPDATE,
                    () -> DetailDiscountBUS.getInstance().getAllDetailDiscountByDiscountId(discount.getCode()),
                    result -> {
                        if (result.isSuccess()) {
                            arrDetailDiscount = result.getData();

                            loadTable();
                        } else {
                            NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                        }
                    });

        }
    }

    public void loadTable() {
        ValidationUtils validationUtils = ValidationUtils.getInstance();
        tlb_col_totalPriceInvoice.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getTotalPriceInvoice())));
        tlb_col_discountAmount.setCellValueFactory(cellData -> new SimpleStringProperty(
                validationUtils.formatCurrency(cellData.getValue().getDiscountAmount())
                        + (DiscountType.PERCENTAGE.getDisplayName().equals(cbTypeDiscount.getValue()) ? " %" : "")));
        UiUtils.gI().addTooltipToColumn(tlb_col_totalPriceInvoice, 10);
        UiUtils.gI().addTooltipToColumn(tlb_col_discountAmount, 10);
        tblDetailDiscount.setItems(FXCollections.observableArrayList(arrDetailDiscount));
        tblDetailDiscount.getSelectionModel().clearSelection();
    }

    private boolean isValidInput(boolean isEdit) {
        boolean isValid = true;
        String discountCode = txtDiscountCode.getText().trim();
        String discountName = txtDiscountName.getText().trim();
        LocalDate startDate = dpStartDate.getValue();
        LocalDate endDate = dpEndDate.getValue();

        ValidationUtils validator = ValidationUtils.getInstance();

        // 1. Kiểm tra mã và tên (Giữ nguyên logic cũ của bạn)
        if (discountCode.isEmpty()) {
            NotificationUtils.showErrorAlert("Mã khuyến mãi không được để trống.", "Thông báo");
            clearAndFocus(txtDiscountCode);
            return false;
        } else if (!validator.validateDiscountCode(discountCode, 4, 50)) {
            NotificationUtils.showErrorAlert(
                    "Mã khuyến mãi không hợp lệ (Tối thiểu 4 và tối đa 50 ký tự, chỉ chữ và số, không được chứa khoảng trắng).",
                    "Thông báo");
            clearAndFocus(txtDiscountCode);
            isValid = false;

        }
        // Kiểm tra tên khuyến mãi
        if (isValid && discountName.isEmpty()) {
            NotificationUtils.showErrorAlert("Tên khuyến mãi không được để trống ", "Thông báo");
            clearAndFocus(txtDiscountName);
            isValid = false;

        } else if (isValid && !validator.validateVietnameseText100(discountName)) {
            NotificationUtils.showErrorAlert(
                    "Tên khuyến mãi không hợp lệ (Tối đa 100 ký tự, chỉ chữ và số, \"_\", \"-\", \"/\").", "Thông báo");
            clearAndFocus(txtDiscountName);
            isValid = false;

        }

        // 2. Kiểm tra ngày bắt đầu/kết thúc
        if (startDate == null || endDate == null) {
            NotificationUtils.showErrorAlert("Ngày bắt đầu và ngày kết thúc không được bỏ trống.", "Thông báo");
            return false;
        }

        LocalDate today = LocalDate.now();

        // Lỗi: Bắt đầu > Kết thúc (Luôn luôn sai bất kể Add hay Edit)
        if (startDate.isAfter(endDate)) {
            NotificationUtils.showErrorAlert("Ngày bắt đầu không được sau ngày kết thúc.", "Thông báo");
            return false;
        }

        // Lỗi: Kết thúc ở quá khứ (Luôn luôn sai)
        if (endDate.isBefore(today)) {
            NotificationUtils.showErrorAlert("Ngày kết thúc phải là hôm nay hoặc sau hôm nay.", "Thông báo");
            return false;
        }

        // CHỈ KIỂM TRA KHI THÊM MỚI (ADD)
        if (!isEdit) {
            if (startDate.isBefore(today)) {
                NotificationUtils.showErrorAlert("Ngày bắt đầu phải là hôm nay hoặc sau hôm nay.", "Thông báo");
                return false;
            }
        }

        // 3. Kiểm tra chi tiết khuyến mãi
        if (arrDetailDiscount.isEmpty()) {
            NotificationUtils.showErrorAlert("Vui lòng thêm ít nhất 1 chi tiết khuyến mãi.", "Thông báo");
            return false;
        }

        return true;
    }

    private void handleSave() {
        if (typeModal == 0) {
            insertDiscount();
        } else {
            updateDiscount();
        }
    }

    public void setTypeModal(int type) {
        if (type != 0 && type != 1)
            handleClose();
        typeModal = type;
        if (typeModal == 0) {
            modalName.setText("Thêm khuyến mãi");
            dpStartDate.setValue(LocalDate.now());
            dpEndDate.setValue(LocalDate.now().plusDays(1));
        } else {
            if (discount == null)
                handleClose();
            modalName.setText("Sửa khuyến mãi");
            makeReadOnly(cbTypeDiscount);
            makeReadOnly(txtDiscountCode);
            txtDiscountCode.setStyle("-fx-background-color: #D3D3D3");
        }
    }

    private void updateDiscount() {
        if (!isValidInput(true))
            return;

        DiscountDTO temp = new DiscountDTO(
                txtDiscountCode.getText().trim(),
                txtDiscountName.getText().trim(),
                DiscountType.fromDisplayName(cbTypeDiscount.getValue()).getCode(),
                dpStartDate.getValue().atStartOfDay(),
                dpEndDate.getValue().atStartOfDay());
        // Copy danh sách detail để gửi đi
        ArrayList<DetailDiscountDTO> list = new ArrayList<>(arrDetailDiscount);

        // 3. Thực thi thông qua TaskUtil
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.DISCOUNT_UPDATE, () ->
        // Gọi trực tiếp BUS hoặc Service xử lý Transaction
        DiscountBUS.getInstance().updateFullDiscount(temp, list),
                result -> {
                    if (result.isSuccess()) {
                        // NotificationUtils.showInfoAlert(result.getMessage(), "Thành công");
                        this.isSaved = true;
                        this.resultMessage = result.getMessage();
                        handleClose(); // Đóng modal sau khi thêm thành công
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                        // Nếu trùng mã, focus lại ô nhập mã
                        if (result.getMessage().contains(AppMessages.DISCOUNT_ADD_DUPLICATE)) {
                            clearAndFocus(txtDiscountCode);
                        }
                    }
                });
    }

    private void insertDiscount() {
        if (!isValidInput(false))
            return;

        // 2. Đóng gói dữ liệu DTO
        DiscountDTO temp = new DiscountDTO(
                txtDiscountCode.getText().trim(),
                txtDiscountName.getText().trim(),
                DiscountType.fromDisplayName(cbTypeDiscount.getValue()).getCode(),
                dpStartDate.getValue().atStartOfDay(),
                dpEndDate.getValue().atStartOfDay());

        // Copy danh sách detail để gửi đi
        ArrayList<DetailDiscountDTO> list = new ArrayList<>(arrDetailDiscount);

        // 3. Thực thi thông qua TaskUtil
        TaskUtil.executeSecure(loadingOverlay, PermissionKey.DISCOUNT_INSERT, () ->
        // Gọi trực tiếp BUS hoặc Service xử lý Transaction
        DiscountBUS.getInstance().insertFullDiscount(temp, list),
                result -> {
                    if (result.isSuccess()) {
                        // NotificationUtils.showInfoAlert(result.getMessage(), "Thành công");
                        this.isSaved = true;
                        this.resultMessage = result.getMessage();
                        handleClose(); // Đóng modal sau khi thêm thành công
                    } else {
                        NotificationUtils.showErrorAlert(result.getMessage(), AppMessages.DIALOG_TITLE);
                        // Nếu trùng mã, focus lại ô nhập mã
                        if (result.getMessage().contains(AppMessages.DISCOUNT_ADD_DUPLICATE)) {
                            clearAndFocus(txtDiscountCode);
                        }
                    }
                });
    }

    private boolean isDuplicateDetailDiscount(DetailDiscountDTO obj, boolean isEdit) {
        for (DetailDiscountDTO dc : arrDetailDiscount) {
            // So sánh mốc hóa đơn đã có với mốc mới, bỏ qua chính nó nếu đang sửa
            if (dc.getTotalPriceInvoice().compareTo(obj.getTotalPriceInvoice()) == 0
                    && (isEdit ? !dc.getDiscountCode().equalsIgnoreCase(obj.getDiscountCode()) : true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate discount amount theo logic mốc giá:
     * - Mốc giá lớn hơn → discount amount phải ≥ discount amount của mốc giá nhỏ
     * hơn
     * - Mốc giá nhỏ hơn → discount amount phải ≤ discount amount của mốc giá lớn
     * hơn
     */
    private boolean isValidDetailDiscountAmount(DetailDiscountDTO newDiscount, boolean isEdit) {
        // Tạo list sắp xếp theo mốc giá tăng dần
        ArrayList<DetailDiscountDTO> sortedList = new ArrayList<>(arrDetailDiscount);

        // Nếu đang edit, loại bỏ detail cũ khỏi list để so sánh (dùng totalPrice để
        // identify)
        if (isEdit && selectedDetailDiscount != null) {
            sortedList.removeIf(
                    d -> d.getTotalPriceInvoice().compareTo(selectedDetailDiscount.getTotalPriceInvoice()) == 0);
        }

        sortedList.sort((d1, d2) -> d1.getTotalPriceInvoice().compareTo(d2.getTotalPriceInvoice()));

        BigDecimal newPrice = newDiscount.getTotalPriceInvoice();
        BigDecimal newAmount = newDiscount.getDiscountAmount();

        // Check tính hợp lệ với các mốc đã có
        for (DetailDiscountDTO existing : sortedList) {
            BigDecimal existingPrice = existing.getTotalPriceInvoice();
            BigDecimal existingAmount = existing.getDiscountAmount();

            // Nếu mốc mới LỚN HƠN mốc cũ → amount mới phải ≥ amount cũ
            if (newPrice.compareTo(existingPrice) > 0) {
                if (newAmount.compareTo(existingAmount) < 0) {
                    return false; // Mốc 15k không được discount < mốc 10k
                }
            }
            // Nếu mốc mới NHỎ HƠN mốc cũ → amount mới phải ≤ amount cũ
            else if (newPrice.compareTo(existingPrice) < 0) {
                if (newAmount.compareTo(existingAmount) > 0) {
                    return false; // Mốc 5k không được discount > mốc 10k
                }
            }
        }

        return true;
    }

    private void handleAddBtn() {
        DetailDiscountModalController modalController = new ModalBuilder<DetailDiscountModalController>(
                "/GUI/DetailDiscountModal.fxml", DetailDiscountModalController.class)
                .setTitle("Thêm chi tiết khuyến mãi")
                .configure(c -> c.setTypeModal(0,
                        DiscountType.fromDisplayName(cbTypeDiscount.getValue()).getCode()))
                .open();
        if (modalController != null && modalController.isSaved()) {
            DetailDiscountDTO temp = new DetailDiscountDTO(modalController.getDetailDiscount());
            if (isDuplicateDetailDiscount(temp, false)) {
                NotificationUtils.showErrorAlert(
                        "Mốc tổng tiền hóa đơn tối thiểu này đã tồn tại trong chương trình khuyến mãi.", "Thông báo");
                return;
            }
            // Validate discount amount theo logic mốc giá
            if (!isValidDetailDiscountAmount(temp, false)) {
                NotificationUtils.showErrorAlert(
                        "Số tiền giảm giá không hợp lệ. Mốc giá lớn hơn phải có discount ≥ mốc giá nhỏ hơn.",
                        "Thông báo");
                return;
            }
            arrDetailDiscount.add(temp);
            makeReadOnly(cbTypeDiscount);
            loadTable();
            Stage currentStage = (Stage) tblDetailDiscount.getScene().getWindow();
            NotificationUtils.showToast(currentStage, AppMessages.DISCOUNT_ADD_DETAIL_SUCCESS);
        }
    }

    private void handleEditBtn() {
        if (isNotSelectedDetailDiscount()) {
            NotificationUtils.showErrorAlert(AppMessages.DISCOUNT_DETAIL_NO_SELECTION, AppMessages.DIALOG_TITLE);
            return;
        }
        DetailDiscountDTO oldDC = new DetailDiscountDTO(selectedDetailDiscount);
        DetailDiscountModalController modalController = new ModalBuilder<DetailDiscountModalController>(
                "/GUI/DetailDiscountModal.fxml", DetailDiscountModalController.class)
                .setTitle("Sửa chi tiết khuyến mãi")
                .configure(c -> c.setTypeModal(1,
                        DiscountType.fromDisplayName(cbTypeDiscount.getValue()).getCode()))
                .configure(c -> c.setDetailDiscount(selectedDetailDiscount))
                .open();
        if (modalController != null && modalController.isSaved()) {
            if (isDuplicateDetailDiscount(modalController.getDetailDiscount(), true)) {
                NotificationUtils.showErrorAlert(
                        "Mốc tổng tiền hóa đơn tối thiểu này đã tồn tại trong chương trình khuyến mãi.", "Thông báo");
                modalController.getDetailDiscount().setTotalPriceInvoice(oldDC.getTotalPriceInvoice());
                modalController.getDetailDiscount().setDiscountAmount(oldDC.getDiscountAmount());
                return;
            }
            // Validate discount amount theo logic mốc giá
            if (!isValidDetailDiscountAmount(modalController.getDetailDiscount(), true)) {
                NotificationUtils.showErrorAlert(
                        "Số tiền giảm giá không hợp lệ. Mốc giá lớn hơn phải có discount ≥ mốc giá nhỏ hơn.",
                        "Thông báo");
                modalController.getDetailDiscount().setTotalPriceInvoice(oldDC.getTotalPriceInvoice());
                modalController.getDetailDiscount().setDiscountAmount(oldDC.getDiscountAmount());
                return;
            }
            loadTable();
            Stage currentStage = (Stage) tblDetailDiscount.getScene().getWindow();
            NotificationUtils.showToast(currentStage, AppMessages.DISCOUNT_UPDATE_DETAIL_SUCCESS);
        }
    }

    private void handleDeleteBtn() {
        if (!isNotSelectedDetailDiscount()) {
            // Nếu đang ở chế độ sửa và chỉ còn một chi tiết khuyến mãi, không cho phép xóa
            arrDetailDiscount.remove(selectedDetailDiscount);
            loadTable();
            Stage currentStage = (Stage) tblDetailDiscount.getScene().getWindow();
            NotificationUtils.showToast(currentStage, AppMessages.DISCOUNT_DELETE_DETAIL_SUCCESS);

            // Nếu không còn chi tiết nào, mở lại comboBox
            if (arrDetailDiscount.isEmpty()) {
                makeEditableForComboBox(cbTypeDiscount);
            }
        } else {
            NotificationUtils.showErrorAlert(AppMessages.DISCOUNT_DETAIL_NO_SELECTION, AppMessages.DIALOG_TITLE);
        }
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }

    private void clearAndFocus(TextField textField) {
        textField.requestFocus();
    }

    private void makeReadOnly(Node node) {
        node.setDisable(false); // Không làm xám hoàn toàn
        node.setMouseTransparent(true); // Không nhận tương tác chuột
        node.setFocusTraversable(false); // Không tab vào được
        node.setStyle("-fx-opacity: 0.75;"); // Làm mờ nhẹ

        // Nếu là ComboBox, chỉnh mũi tên
        if (node instanceof ComboBox) {
            Platform.runLater(() -> {
                Node arrow = ((ComboBox<?>) node).lookup(".arrow-button");
                if (arrow != null) {
                    arrow.setStyle("-fx-background-color: #cccccc; -fx-opacity: 0.75;");
                }
            });
        }
    }

    private void makeEditableForComboBox(ComboBox<?> comboBox) {
        comboBox.setMouseTransparent(false);
        comboBox.setFocusTraversable(true);
        comboBox.setEditable(false); // Giữ nguyên là không gõ tay (nếu muốn)

        comboBox.setStyle(""); // Reset style

        Platform.runLater(() -> {
            Node arrow = comboBox.lookup(".arrow-button");
            if (arrow != null) {
                arrow.setStyle("");
            }
        });
    }

    private boolean isNotSelectedDetailDiscount() {
        selectedDetailDiscount = tblDetailDiscount.getSelectionModel().getSelectedItem();
        return selectedDetailDiscount == null;
    }

}
