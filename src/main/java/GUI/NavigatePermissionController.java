package GUI;

import ENUM.PermissionKey;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
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
        if (!canOpenSelling()) {
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }
        UiUtils.gI().openStage("/GUI/SellingProduct.fxml", "Bán hàng");
        handleClose();
    }

    @FXML
    private void openHrManage() {
        if (!canOpenHrManage()) {
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }
        UiUtils.gI().openStage("/GUI/HrMainUI.fxml", "Quản lý nhân sự");
        handleClose();
    }

    @FXML
    private void openManage() {
        if (!canOpenManage()) {
            NotificationUtils.showErrorAlert(AppMessages.UNAUTHORIZED, AppMessages.DIALOG_TITLE);
            return;
        }
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
        applyAccessState(pItemPermissionSelling, canOpenSelling());
        applyAccessState(pItemPermissionHrManage, canOpenHrManage());
        applyAccessState(pItemPermissionAuth, canOpenManage());
    }

    private void applyAccessState(Pane pane, boolean allowed) {
        pane.setDisable(!allowed);
        pane.setOpacity(allowed ? 1.0 : 0.3);
    }

    private boolean canOpenSelling() {
        return SessionManagerService.getInstance().hasPermission(PermissionKey.INVOICE_INSERT);
    }

    private boolean canOpenHrManage() {
        return hasAnyModuleAccess(1, 11, 12, 13, 15, 16, 17, 18);
    }

    private boolean canOpenManage() {
        return hasAnyModuleAccess(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    }

    private boolean hasAnyModuleAccess(int... moduleIds) {
        SessionManagerService session = SessionManagerService.getInstance();
        for (int moduleId : moduleIds) {
            if (session.hasModuleAccess(moduleId))
                return true;
        }
        return false;
    }

}
