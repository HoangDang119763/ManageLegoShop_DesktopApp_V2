import UTILS.TaskUtil;
import UTILS.UiUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("GUI/LoginUI.fxml"));
        Parent root = fxmlLoader.load(); // Gọi .load() để lấy root từ FXML

        UiUtils.gI().makeWindowDraggable(root, stage);

        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Đăng nhập");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        TaskUtil.shutdown();
        System.exit(0);
    }

    public static void main(String[] args) {
        // System.out.println(PasswordUtils.getInstance().hashPassword("123456"));
        launch(args);
    }
}
