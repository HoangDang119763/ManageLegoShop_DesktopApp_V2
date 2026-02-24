package UTILS;

import DTO.TempDetailImportDTO;
import DTO.TempDetailInvoiceDTO;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.Optional;

public class NotificationUtils {
    public static void showErrorAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // KHÔNG dùng showAndWait
    }

    public static void showInfoAlert(String message, String title) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showToast(Stage owner, String message) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initOwner(owner);
            toastStage.initStyle(StageStyle.TRANSPARENT);

            Label label = new Label(message);
            // Style cho thông báo góc phải thường dùng góc bo ít hơn hoặc vuông vắn cho
            // hiện đại
            label.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                    "-fx-padding: 10px 20px; -fx-background-radius: 5px; " +
                    "-fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(root);
            scene.setFill(null);
            toastStage.setScene(scene);

            toastStage.setOpacity(0); // Ẩn tạm thời để không bị nháy
            toastStage.show();

            // X = (Tọa độ X cửa sổ) + (Chiều rộng cửa sổ) - (Chiều rộng Toast) - (Khoảng lề
            // phải 20px)
            double x = owner.getX() + owner.getWidth() - root.getWidth() - 5;
            // Y = (Tọa độ Y cửa sổ) + (Khoảng lề trên 50px - né thanh tiêu đề)
            double y = owner.getY() + 35;

            toastStage.setX(x);
            toastStage.setY(y);
            toastStage.setOpacity(1); // Hiện lại sau khi đã đặt đúng chỗ

            // --- HIỆU ỨNG TRƯỢT (NẾU MUỐN) HOẶC CHỈ MỜ DẦN ---
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.millis(1000));
            fadeOut.setOnFinished(e -> toastStage.close());
            fadeOut.play();
        });
    }

}
