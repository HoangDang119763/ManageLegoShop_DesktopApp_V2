package GUI;

import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.UiUtils;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NavigatePermissionController {
    @FXML
    private Pane pItemPermissionSelling, pItemPermissionHrManage, pItemPermissionAuth;
    @FXML
    private Button closeBtn;

    // Init
    @FXML
    public void initialize() {
        hideButtonWithoutPermission();
        setupEventHandlers();
    }

    // Exit form
    private void setupEventHandlers() {
        closeBtn.setOnMouseClicked(e -> {
            if (!UiUtils.gI().showConfirmAlert(AppMessages.LOGOUT_CONFIRM, AppMessages.DIALOG_TITLE_CONFIRM))
                return;
            SessionManagerService.getInstance().logout();
            ParallelTransition animation = UiUtils.gI().createButtonAnimation(closeBtn);
            animation.setOnFinished(event -> ExitForm());
            animation.play();
        });
        pItemPermissionSelling.setOnMouseClicked(e -> openSelling());
        pItemPermissionHrManage.setOnMouseClicked(e -> openHrManage());
        pItemPermissionAuth.setOnMouseClicked(e -> openManage());

    }

    private void ExitForm() {
        UiUtils.gI().openStage("/GUI/LoginUI.fxml", "Đăng nhập");
        Platform.runLater(this::handleClose);
    }

    private void openSelling() {
        UiUtils.gI().openStage("/GUI/SellingProduct.fxml", "Bán hàng");
        handleClose();
    }

    @FXML
    private void openHrManage() {
        UiUtils.gI().openStage("/GUI/HrMainUI.fxml", "Quản lý nhân sự");
        handleClose();
    }

    @FXML
    private void openManage() {
        UiUtils.gI().openStage("/GUI/MainUI.fxml", "Lego Store");
        handleClose();
    }

    private void handleClose() {
        if (closeBtn.getScene() != null && closeBtn.getScene().getWindow() != null) {
            Stage stage = (Stage) closeBtn.getScene().getWindow();
            stage.close();
        }
    }

    public void hideButtonWithoutPermission() {
        // boolean canSelling = SessionManagerService.getInstance().canSelling();
        // boolean canImport = SessionManagerService.getInstance().canImporting();
        // boolean canManage = SessionManagerService.getInstance().canManage();

        // // Thiết lập trạng thái và độ mờ cho nút quyền bán hàng
        // pItemPermissionSelling.setDisable(!canSelling);
        // pItemPermissionSelling.setOpacity(canSelling ? 1.0 : 0.3);

        // // Thiết lập trạng thái và độ mờ cho nút quyền nhập hàng
        // pItemPermissionImporting.setDisable(!canImport);
        // pItemPermissionImporting.setOpacity(canImport ? 1.0 : 0.3);

        // // Thiết lập trạng thái và độ mờ cho nút quyền quản lý
        // pItemPermissionAuth.setDisable(!canManage);
        // pItemPermissionAuth.setOpacity(canManage ? 1.0 : 0.3);
    }

}
