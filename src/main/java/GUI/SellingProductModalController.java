package GUI;

import BUS.ProductBUS;
import DTO.ProductDTO;
import DTO.TempDetailInvoiceDTO;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;

import java.math.BigDecimal;

public class SellingProductModalController {
    @FXML
    private Label modalName;
    @FXML
    private TextField txtQuantity;
    @FXML
    private TextField txtProductName;
    @FXML
    private Button saveBtn, closeBtn;
    @Getter
    private boolean isSaved;
    private int typeModal;
    @Getter
    private TempDetailInvoiceDTO tempDetailInvoice;

    @FXML
    public void initialize() {
        setupListeners();
    }

    public void setupListeners() {
        saveBtn.setOnAction(e -> handleSave());
        closeBtn.setOnAction(e -> handleClose());
    }

    // add handle for selling here
    public void setTypeModal(int type) {
        if (type != 0 && type != 1)
            handleClose();
        typeModal = type;
        if (typeModal == 0) {
            modalName.setText("Thêm sản phẩm");
        } else if (typeModal == 1) {
            if (tempDetailInvoice == null)
                handleClose();
            modalName.setText("Sửa số lượng sản phẩm");
        }
    }

    public void setTempDetailInvoice(TempDetailInvoiceDTO tempDetailInvoice) {
        this.tempDetailInvoice = tempDetailInvoice;
        txtQuantity.setText(String.valueOf(tempDetailInvoice.getQuantity()));
        txtProductName.setText(tempDetailInvoice.getName());
    }

    public void setProduct(ProductDTO product) {
        txtProductName.setText(product.getName());
        this.tempDetailInvoice = new TempDetailInvoiceDTO(
                0,
                product.getId(),
                product.getName(),
                1,
                product.getSellingPrice(),
                BigDecimal.ZERO);
    }

    private boolean isValidInput() {
        boolean isValid = true;
        String quantity = txtQuantity.getText().trim();
        int stockQuantity = ProductBUS.getInstance().getByIdLocal(tempDetailInvoice.getProductId()).getStockQuantity();

        if (quantity.isEmpty()) {
            NotificationUtils.showErrorAlert(AppMessages.INVOICE_DETAIL_QUANTITY_EMPTY, AppMessages.DIALOG_TITLE);
            clearAndFocus(txtQuantity);
            isValid = false;
        } else {
            try {
                int quantityValue = Integer.parseInt(quantity);
                if (quantityValue < 1) {
                    NotificationUtils.showErrorAlert(AppMessages.INVOICE_DETAIL_QUANTITY_MIN, AppMessages.DIALOG_TITLE);
                    clearAndFocus(txtQuantity);
                    isValid = false;
                } else if (quantityValue > stockQuantity) {
                    NotificationUtils.showErrorAlert(AppMessages.INVOICE_DETAIL_QUANTITY_EXCEED,
                            AppMessages.DIALOG_TITLE);
                    clearAndFocus(txtQuantity);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                NotificationUtils.showErrorAlert(AppMessages.INVOICE_DETAIL_QUANTITY_INVALID, AppMessages.DIALOG_TITLE);
                clearAndFocus(txtQuantity);
                isValid = false;
            }
        }

        return isValid;
    }

    private void handleSave() {
        if (typeModal == 0) {
            insertDetailInvoice();
        } else {
            updateDetailInvoice();
        }
    }

    private void insertDetailInvoice() {
        if (isValidInput()) {
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            BigDecimal price = tempDetailInvoice.getPrice();
            BigDecimal totalPrice = price.multiply(BigDecimal.valueOf(quantity));
            TempDetailInvoiceDTO temp = new TempDetailInvoiceDTO(
                    tempDetailInvoice.getInvoiceId(),
                    tempDetailInvoice.getProductId(),
                    tempDetailInvoice.getName(),
                    quantity,
                    price,
                    totalPrice);

            isSaved = true;
            tempDetailInvoice = new TempDetailInvoiceDTO(temp);
            handleClose();
        }
    }

    private void updateDetailInvoice() {
        if (isValidInput()) {
            int quantity = Integer.parseInt(txtQuantity.getText().trim());
            isSaved = true;
            tempDetailInvoice.setQuantity(quantity);
            tempDetailInvoice.setTotalPrice(
                    tempDetailInvoice.getPrice().multiply(BigDecimal.valueOf(tempDetailInvoice.getQuantity())));
            handleClose();
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
}
