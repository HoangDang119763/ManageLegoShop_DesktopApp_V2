
package UTILS;

import GUI.LoginController;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class UiUtils {
    private static final UiUtils INSTANCE = new UiUtils();

    private double xOffset = 0;
    private double yOffset = 0;
    private static final Map<TextField, Timeline> debounceMap = new HashMap<>();

    private UiUtils() {

    }

    public static UiUtils gI() {
        return INSTANCE;
    }

    public <S, T> void addTooltipToColumn(TableColumn<S, T> column, int maxLength,
            Function<T, String> toStringFunction) {
        column.setCellFactory(tc -> new TableCell<>() {
            private final Tooltip tooltip = new Tooltip();

            {
                // Cấu hình tooltip một lần
                tooltip.setShowDelay(Duration.millis(100));
                tooltip.setHideDelay(Duration.millis(50));
                tooltip.setWrapText(true); // Cho phép hiển thị nhiều dòng nếu cần
                tooltip.setMaxWidth(300); // Giới hạn độ rộng tooltip
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    String text = toStringFunction.apply(item);
                    if (text == null)
                        text = ""; // Tránh lỗi null

                    if (text.length() > maxLength) {
                        setText(text.substring(0, maxLength) + "...");
                        tooltip.setText(text);
                        setTooltip(tooltip);
                    } else {
                        setText(text);
                        setTooltip(null);
                    }
                }
            }
        });
    }

    public void setReadOnlyItem(Control control) {
        control.setMouseTransparent(true);
        control.setFocusTraversable(false);
    }

    public void setVisibleItem(Control control) {
        control.setVisible(false);
        control.setManaged(false);
    }

    public void setVisibleItem(HBox control) {
        control.setVisible(false);
        control.setManaged(false);
    }

    public void setVisibleItem(VBox control) {
        control.setVisible(false);
        control.setManaged(false);
    }

    public void setReadOnlyComboBox(ComboBox<?> comboBox) {
        comboBox.addEventFilter(javafx.scene.input.MouseEvent.ANY, e -> {
            if (e.getEventType() == javafx.scene.input.MouseEvent.MOUSE_PRESSED ||
                    e.getEventType() == javafx.scene.input.MouseEvent.MOUSE_RELEASED ||
                    e.getEventType() == javafx.scene.input.MouseEvent.MOUSE_CLICKED) {
                e.consume();
            }
        });

        comboBox.addEventFilter(javafx.scene.input.ScrollEvent.ANY, javafx.event.Event::consume);

        comboBox.addEventFilter(javafx.scene.input.KeyEvent.ANY, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE || e.getCode() == javafx.scene.input.KeyCode.ENTER) {
                e.consume();
            }
        });
        comboBox.setFocusTraversable(false);
    }

    public void setReadOnlyItem(Button button) {
        button.setVisible(false);
        button.setManaged(false);
    }

    public <S> void addTooltipToColumn(TableColumn<S, String> column, int maxLength) {
        addTooltipToColumn(column, maxLength, Function.identity());
    }

    public <T> void addTooltipToComboBox(ComboBox<T> comboBox, int maxLength, Function<T, String> toStringFunction) {
        comboBox.setCellFactory(lv -> new ListCell<>() {
            private final Tooltip tooltip = new Tooltip();

            {
                tooltip.setShowDelay(Duration.millis(100));
                tooltip.setHideDelay(Duration.millis(50));
                tooltip.setWrapText(true);
                tooltip.setMaxWidth(300);
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    String text = toStringFunction.apply(item);
                    if (text == null)
                        text = "";

                    if (text.length() > maxLength) {
                        setText(text.substring(0, maxLength) + "...");
                        tooltip.setText(text);
                        setTooltip(tooltip);
                    } else {
                        setText(text);
                        setTooltip(null);
                    }
                }
            }
        });
    }

    public <T> void addTooltipToComboBoxValue(ComboBox<T> comboBox, int maxLength,
            Function<T, String> toStringFunction) {
        // 1. Vẫn dùng setButtonCell để lo việc hiển thị dấu "..." trên nút
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String text = toStringFunction.apply(item);
                    if (text != null && text.length() > maxLength) {
                        setText(text.substring(0, maxLength) + "...");
                    } else {
                        setText(text);
                    }
                }
            }
        });

        // 2. Lắng nghe thay đổi giá trị để gắn Tooltip cho toàn bộ ComboBox
        comboBox.valueProperty().addListener(
                (obs, oldVal, newVal) -> updateComboBoxTooltip(comboBox, newVal, maxLength, toStringFunction));

        // 3. Gọi cập nhật ngay lập tức cho trường hợp ComboBox đã có giá trị sẵn (lúc
        // load dữ liệu)
        updateComboBoxTooltip(comboBox, comboBox.getValue(), maxLength, toStringFunction);
    }

    /**
     * Hàm hỗ trợ cập nhật Tooltip cho ComboBox node
     */
    private <T> void updateComboBoxTooltip(ComboBox<T> comboBox, T item, int maxLength,
            Function<T, String> toStringFunction) {
        if (item == null) {
            comboBox.setTooltip(null);
            return;
        }

        String fullText = toStringFunction.apply(item);
        if (fullText != null && fullText.length() > maxLength) {
            Tooltip tooltip = new Tooltip(fullText);
            tooltip.setShowDelay(javafx.util.Duration.millis(150));
            tooltip.setHideDelay(javafx.util.Duration.millis(50));
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(300);

            // Gán Tooltip cho chính ComboBox thay vì Cell
            comboBox.setTooltip(tooltip);
        } else {
            comboBox.setTooltip(null);
        }
    }

    public <T> void addTooltipToComboBox(ComboBox<T> comboBox, int maxLength) {
        addTooltipToComboBox(comboBox, maxLength, T::toString);
    }

    public void makeWindowDraggable(Parent root, Stage stage) {
        root.setOnMousePressed((MouseEvent e) -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        root.setOnMouseDragged((MouseEvent e) -> {
            stage.setX(e.getScreenX() - xOffset);
            stage.setY(e.getScreenY() - yOffset);
            stage.setOpacity(0.8); // Làm mờ khi kéo
        });

        root.setOnMouseReleased((MouseEvent e) -> {
            stage.setOpacity(1); // Trả lại độ trong suốt bình thường
        });
    }

    public void applyButtonAnimation(Button btn) {
        ParallelTransition animation = createButtonAnimation(btn);
        animation.play();
    }

    public ParallelTransition createButtonAnimation(Button btn) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), btn);
        scaleTransition.setFromX(1);
        scaleTransition.setFromY(1);
        scaleTransition.setToX(1.1);
        scaleTransition.setToY(1.1);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), btn);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.8);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(2);

        return new ParallelTransition(scaleTransition, fadeTransition);
    }

    public <T> T openStageWithController(String fxmlFile, Consumer<T> onLoad, String title) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(UiUtils.class.getResource(fxmlFile));
            Parent root = loader.load();

            T controller = loader.getController();
            if (controller != null && onLoad != null) {
                onLoad.accept(controller);
            }

            Stage stage = new Stage();
            UiUtils.gI().makeWindowDraggable(root, stage);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle(title);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean showConfirmAlert(String s, String tile) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(tile);
        alert.setHeaderText(null);
        alert.setContentText(s);
        return alert.showAndWait().map(buttonType -> buttonType == ButtonType.OK).orElse(false);
    }

    public void openStage(String fxmlFile, String title) {
        try {
            // Sử dụng getClass() để lấy resource tương đối từ root của project
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            // Set fill là TRANSPARENT để hỗ trợ các giao diện bo góc/đổ bóng từ CSS
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            makeWindowDraggable(root, stage);
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            stage.requestFocus();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage openStage1(String fxmlFile, String title) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = fxmlLoader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            makeWindowDraggable(root, stage);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.show();
            return stage; // Trả về stage để Controller gọi xong có thể dùng ngay
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // for show double form
    public void openStage(String fxmlFile, String title, Stage owner) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(LoginController.class.getResource(fxmlFile));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root);

            UiUtils.gI().makeWindowDraggable(root, stage);
            stage.initStyle(StageStyle.TRANSPARENT);

            stage.setTitle(title);
            stage.setScene(scene);

            // Thêm dòng này để khóa form cha
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(owner); // Gán form cha

            stage.showAndWait(); // Đợi đến khi form con đóng mới tiếp tục

        } catch (IOException e) {
            System.err.println("error");
            e.printStackTrace();
        }
    }

    public <T> void formatInactiveComboBox(
            ComboBox<T> comboBox,
            Function<T, String> nameExtractor,
            Function<T, Integer> statusExtractor,
            int inactiveStatusId) {
        javafx.util.Callback<javafx.scene.control.ListView<T>, ListCell<T>> cellFactory = lv -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String name = nameExtractor.apply(item);
                    int statusId = statusExtractor.apply(item);

                    if (statusId == inactiveStatusId) {
                        Label lblName = new Label(name);
                        lblName.setStyle("-fx-text-fill: black;");

                        Label lblStatus = new Label(" (Ngừng dùng/Vô hiệu)");
                        lblStatus.setStyle("-fx-text-fill: #444444; -fx-font-style: italic; -fx-font-size: 0.9em;");

                        HBox container = new HBox(lblName, lblStatus);
                        container.setPadding(new javafx.geometry.Insets(5, 0, 5, 0));

                        container.setSpacing(2);
                        setGraphic(container);
                        setText(null);
                    } else {
                        setText(name);
                        setGraphic(null);
                    }
                }
            }
        };

        comboBox.setCellFactory(cellFactory);
        comboBox.setButtonCell(cellFactory.call(null));
    }

    public <T> void addSmartInactiveWarningListener(
            ComboBox<T> comboBox,
            Function<T, Integer> idExtractor, // Để lấy ID của đối tượng
            Function<T, Integer> statusExtractor, // Để lấy StatusId
            int inactiveStatusId,
            int initialId, // ID ban đầu của sản phẩm (nếu thêm mới thì truyền -1)
            String message) {
        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int currentId = idExtractor.apply(newVal);
                int currentStatusId = statusExtractor.apply(newVal);

                // CHỈ CẢNH BÁO KHI:
                // 1. ID mới khác ID ban đầu (người dùng có sự thay đổi)
                // 2. Trạng thái mới là Inactive
                if (currentId != initialId && currentStatusId == inactiveStatusId) {
                    comboBox.setStyle("-fx-border-color: #ff0000; -fx-border-width: 2px;"); // Màu cam cảnh báo
                } else {
                    comboBox.setStyle(null); // Reset về bình thường
                }
            }
        });
    }

    /**
     * Tạo ImageView từ file
     * Background loading = true để UI không bị treo khi đọc file
     */
    public ImageView createImageView(String imageUrl, double width, double height) {
        Image image = null;

        // 1. Load từ file
        try {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                File imageFile = new File(imageUrl);
                if (imageFile.exists()) {
                    image = new Image(imageFile.toURI().toString(), width, height, true, true, true);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi load ảnh: " + e.getMessage());
        }

        // 2. Nếu không có ảnh hoặc lỗi, dùng ảnh default từ resource
        if (image == null || image.isError()) {
            try {
                URL defaultUrl = getClass().getResource("/images/default/default.png");
                if (defaultUrl != null) {
                    image = new Image(defaultUrl.toExternalForm(), width, height, true, true, true);
                }
            } catch (Exception e) {
                return new ImageView(); // Trả về view trống nếu cả default cũng lỗi
            }
        }

        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);

        return imageView;
    }

    public void applySearchDebounce(TextField textField, int delayMillis, Runnable action) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Hủy bộ đếm cũ nếu có
            if (debounceMap.containsKey(textField)) {
                debounceMap.get(textField).stop();
            }

            // Tạo bộ đếm mới
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(delayMillis), e -> {
                action.run();
                debounceMap.remove(textField); // Chạy xong thì dọn dẹp
            }));

            debounceMap.put(textField, timeline);
            timeline.play();
        });
    }
}
