package UTILS;

import INTERFACE.IModalController;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder Pattern để tạo Modal Window một cách clean và flexible
 * Hoạt động với bất kỳ controller nào implement IModalController
 * 
 * Ví dụ:
 * new ModalBuilder<ProductModalController>("/GUI/ProductModal.fxml",
 * ProductModalController.class)
 * .setTitle("Xem chi tiết sản phẩm")
 * .modeDetail()
 * .configure(c -> c.setProduct(selectedProduct))
 * .open();
 */
public class ModalBuilder<T extends IModalController> {
    private String fxmlPath;
    private String title;
    private List<Consumer<T>> configurations = new ArrayList<>();
    private Class<T> controllerClass;

    public ModalBuilder(String fxmlPath, Class<T> controllerClass) {
        this.fxmlPath = fxmlPath;
        this.controllerClass = controllerClass;
        this.title = fxmlPath; // Default title
    }

    /**
     * Set tiêu đề của modal
     */
    public ModalBuilder<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * Thêm configuration cho controller (có thể gọi nhiều lần)
     */
    public ModalBuilder<T> configure(Consumer<T> configuration) {
        this.configurations.add(configuration);
        return this;
    }

    /**
     * Mở modal Window
     */
    public T open() {
        // Tạo callback áp dụng tất cả configurations
        Consumer<T> mergedConfiguration = controller -> {
            for (Consumer<T> config : configurations) {
                config.accept(controller);
            }
        };

        return UiUtils.gI().openStageWithController(fxmlPath, mergedConfiguration, title);
    }

    /**
     * Shortcut: Set Mode = 0 (Add mode)
     * Generic cho tất cả controller implement IModalController
     */
    public ModalBuilder<T> modeAdd() {
        return configure(c -> c.setTypeModal(0));
    }

    /**
     * Shortcut: Set Mode = 1 (Edit mode)
     * Generic cho tất cả controller implement IModalController
     */
    public ModalBuilder<T> modeEdit() {
        return configure(c -> c.setTypeModal(1));
    }

    /**
     * Shortcut: Set Mode = 2 (Detail/View mode)
     * Generic cho tất cả controller implement IModalController
     */
    public ModalBuilder<T> modeDetail() {
        return configure(c -> c.setTypeModal(2));
    }
}
