package GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaginationController {

    @FXML
    @Getter
    private Pagination pagination;

    @FXML
    private TextField txtJumpPage;

    // Thay Runnable bằng Consumer để truyền trang mới về cho cha
    @Setter
    private Consumer<Integer> onPageChanged;

    @FXML
    public void initialize() {
        setupNumericFilter(); // Ngăn nhập chữ
        setupPaginationListener();
        setupTextFieldListener();

        // Mặc định hiển thị trang 1
        txtJumpPage.setText("1");
    }

    /**
     * Chỉ cho phép nhập số nguyên dương
     */
    private void setupNumericFilter() {
        txtJumpPage.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        // Reset lỗi khi người dùng bắt đầu gõ lại
        txtJumpPage.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.isEmpty()) {
                txtJumpPage.setStyle("");
                txtJumpPage.setPromptText("...");
            }
        });
    }

    private void setupPaginationListener() {
        pagination.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            int pageIndex = newVal.intValue();

            // ĐỒNG BỘ: Cập nhật số hiển thị trong ô Jump
            txtJumpPage.setText(String.valueOf(pageIndex + 1));
            txtJumpPage.setStyle("");

            if (onPageChanged != null) {
                onPageChanged.accept(pageIndex);
            }
        });
    }

    private void setupTextFieldListener() {
        txtJumpPage.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleJumpToPage();
                txtJumpPage.getParent().requestFocus(); // Bỏ focus sau khi enter
            }
        });
    }

    private void handleJumpToPage() {
        String input = txtJumpPage.getText().trim();
        if (input.isEmpty())
            return;

        try {
            int pageNumber = Integer.parseInt(input);
            int maxPage = pagination.getPageCount();

            if (pageNumber < 1 || pageNumber > maxPage) {
                markTextFieldError("1-" + maxPage);
                return;
            }

            pagination.setCurrentPageIndex(pageNumber - 1);
        } catch (NumberFormatException e) {
            markTextFieldError("!");
        }
    }

    private void markTextFieldError(String errorMessage) {
        txtJumpPage.setStyle("-fx-border-color: #ff4444; -fx-border-width: 1.5;");
        txtJumpPage.setText("");
        txtJumpPage.setPromptText(errorMessage);
    }

    // ==================== PUBLIC METHODS ====================

    public void init(int totalItems, int pageSize, Consumer<Integer> callback) {
        int pageCount = (int) Math.ceil((double) totalItems / pageSize);
        this.pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        this.onPageChanged = callback;
    }

    public void setPageCount(int pageCount) {
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
    }

    public int getCurrentPage() {
        return pagination.getCurrentPageIndex();
    }

    // Thêm vào PaginationController.java
    public void setCurrentPage(int index) {
        pagination.setCurrentPageIndex(index);
    }
}