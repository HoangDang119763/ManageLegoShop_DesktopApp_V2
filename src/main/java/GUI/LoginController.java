package GUI;

import BUS.AccountBUS;
import BUS.ModuleBUS;
import BUS.StatusBUS;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.TaskUtil;
import UTILS.UiUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import java.util.prefs.Preferences;

@Slf4j
public class LoginController {
    @FXML
    private Button closeBtn;
    @FXML
    private Button loginBtn;
    @FXML
    private AnchorPane main_form;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtUsername;
    @FXML
    private CheckBox ckbRememberMe;
    @FXML
    StackPane loadingOverlay;
    private Preferences prefs;

    @FXML
    public void initialize() {
        prefs = Preferences.userNodeForPackage(LoginController.class);
        // Tải 2 dữ liệu cứng
        StatusBUS.getInstance().loadCache();
        ModuleBUS.getInstance().loadCache();

        // Kiểm tra xem rememberMe có được bật không
        boolean rememberMe = prefs.getBoolean("rememberMe", false);
        ckbRememberMe.setSelected(rememberMe);

        if (rememberMe) {
            txtUsername.setText(prefs.get("savedUsername", ""));
            txtPassword.setText(prefs.get("savedPassword", ""));
        }

        closeBtn.setOnMouseClicked(e -> close());
        loginBtn.setOnMouseClicked(e -> handleLogin());
        txtUsername.setOnAction(e -> handleLogin());
        txtPassword.setOnAction(e -> handleLogin());
        loginBtn.setDefaultButton(true);
    }

    public void close() {
        System.exit(0);
    }

    public void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            NotificationUtils.showErrorAlert(AppMessages.LOGIN_EMPTY_CREDENTIALS, AppMessages.DIALOG_TITLE);
            return;
        }

        TaskUtil.executePublic(
                loadingOverlay,
                // 1. Chỉ truyền logic BUS thuần túy
                () -> AccountBUS.getInstance().authenticate(username, password),

                // 2. Xử lý khi thành công (Chạy trên UI Thread)
                result -> {
                    handleRememberMe(username, password);
                    loginBtn.getScene().getWindow().hide();
                    Stage navigateStage = UiUtils.gI().openStage1(
                            "/GUI/NavigatePermission.fxml",
                            "Danh sách chức năng");

                    if (navigateStage != null) {
                        NotificationUtils.showToast(
                                navigateStage,
                                result.getMessage());
                    }
                });
    }

    public void handleRememberMe(String username, String password) {
        // Xử lý Remember Me
        prefs.putBoolean("rememberMe", ckbRememberMe.isSelected());
        if (ckbRememberMe.isSelected()) {
            prefs.put("savedUsername", username);
            prefs.put("savedPassword", password);
        } else {
            prefs.remove("savedUsername");
            prefs.remove("savedPassword");
        }
    }
}
