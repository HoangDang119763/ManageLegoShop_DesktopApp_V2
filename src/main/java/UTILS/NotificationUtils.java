package UTILS;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Optional;

public class NotificationUtils {

    public static void showErrorAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); 
    }

    public static void showInfoAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Hiển thị hộp thoại xác nhận (Yes/No)
     * @param message Nội dung câu hỏi
     * @param title Tiêu đề cửa sổ
     * @return true nếu chọn OK, false nếu chọn Cancel hoặc đóng cửa sổ
     */
    public static boolean showConfirmAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Chờ người dùng phản hồi
        Optional<ButtonType> result = alert.showAndWait();
        
        // Trả về true nếu nhấn OK
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    // Overload để tương thích với lời gọi chỉ có 1 tham số trong code của bạn
    public static boolean showConfirmAlert(String message) {
        return showConfirmAlert(message, "Xác nhận hệ thống");
    }

    public static void showToast(Stage owner, String message) {
        Platform.runLater(() -> {
            Stage toastStage = new Stage();
            toastStage.initOwner(owner);
            toastStage.initStyle(StageStyle.TRANSPARENT);

            Label label = new Label(message);
            label.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; " +
                    "-fx-padding: 10px 20px; -fx-background-radius: 5px; " +
                    "-fx-font-weight: bold; -fx-font-size: 13px; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(root);
            scene.setFill(null);
            toastStage.setScene(scene);

            toastStage.setOpacity(0);
            toastStage.show();

            double x = owner.getX() + owner.getWidth() - root.getWidth() - 5;
            double y = owner.getY() + 35;

            toastStage.setX(x);
            toastStage.setY(y);
            toastStage.setOpacity(1);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), root);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.millis(1000));
            fadeOut.setOnFinished(e -> toastStage.close());
            fadeOut.play();
        });
    }
}