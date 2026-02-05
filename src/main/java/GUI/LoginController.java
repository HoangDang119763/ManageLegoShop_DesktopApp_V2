package GUI;

import DTO.AccountDTO;
import SERVICE.LoginService;
import UTILS.AppMessages;
import UTILS.NotificationUtils;
import UTILS.UiUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
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
    private Preferences prefs;

    @FXML
    public void initialize() {
        prefs = Preferences.userNodeForPackage(LoginController.class);

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

        AccountDTO account = new AccountDTO(-1, username, password, -1);
        int loginResult = LoginService.getInstance().checkLogin(account);
        if (loginResult > 0) { // Đăng nhập thành công (ID > 0)
            NotificationUtils.showInfoAlert(AppMessages.LOGIN_SUCCESS, AppMessages.DIALOG_TITLE);

            // Xử lý Remember Me
            prefs.putBoolean("rememberMe", ckbRememberMe.isSelected());
            if (ckbRememberMe.isSelected()) {
                prefs.put("savedUsername", username);
                prefs.put("savedPassword", password);
            } else {
                prefs.remove("savedUsername");
                prefs.remove("savedPassword");
            }

            // Chuyển màn hình
            loginBtn.getScene().getWindow().hide();
            UiUtils.gI().openStage("/GUI/NavigatePermission.fxml", "Danh sách chức năng");

        } else if (loginResult == -2) {
            NotificationUtils.showErrorAlert(AppMessages.LOGIN_ACCOUNT_LOCKED, AppMessages.DIALOG_TITLE);
        } else if (loginResult == -3) {
            NotificationUtils.showErrorAlert(AppMessages.LOGIN_EMPLOYEE_INVALID, AppMessages.DIALOG_TITLE);
        } else {
            NotificationUtils.showErrorAlert(AppMessages.LOGIN_INVALID_CREDENTIALS, AppMessages.DIALOG_TITLE);
        }
    }
}
