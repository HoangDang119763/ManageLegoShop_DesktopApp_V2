package INTERFACE;

/**
 * Interface cho tất cả Modal Controller
 * Đảm bảo modal controller có method để set mode (Add/Edit/Detail)
 */
public interface IModalController {
    /**
     * Set modal type mode
     * 
     * @param mode 0 = Add, 1 = Edit, 2 = Detail/View
     */
    void setTypeModal(int mode);
}
