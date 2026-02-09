package UTILS;

import BUS.*;
import DTO.CategoryDTO;
import DTO.DetailInvoiceDTO;
import DTO.InvoiceDTO;
import DTO.StatusDTO;
import ENUM.StatusType;
import ENUM.Status;

import java.util.ArrayList;

/**
 * AvailableUtils: Lớp chứa các hàm kiểm tra tính hợp lệ (validation)
 * Được sử dụng bởi nhiều Service khác nhau (cross-cutting validation)
 * Đảm bảo logic kiểm tra không bị lặp lại ở nhiều nơi (DRY principle)
 */
public class AvailableUtils {
    private static final AvailableUtils INSTANCE = new AvailableUtils();

    public static AvailableUtils getInstance() {
        return INSTANCE;
    }

    public boolean isValidRoleWithPermissions(int roleId) {
        if (roleId <= 0)
            return false;
        if (PermissionBUS.getInstance().isLocalEmpty())
            PermissionBUS.getInstance().loadLocal();
        if (RolePermissionBUS.getInstance().isLocalEmpty())
            RolePermissionBUS.getInstance().loadLocal();

        int rolePermissionCount = RolePermissionBUS.getInstance().getAllRolePermissionByRoleIdLocal(roleId).size();
        int totalPermissions = PermissionBUS.getInstance().getAllLocal().size();
        // Nếu role không có đủ quyền, từ chối ngay
        return (rolePermissionCount == totalPermissions);
    }

    public boolean isNotUsedDiscount(String discountCode) {
        if (discountCode == null || discountCode.isEmpty()) {
            return false;
        }

        // Load local nếu chưa có
        InvoiceBUS invoiceBUS = InvoiceBUS.getInstance();
        if (invoiceBUS.isLocalEmpty()) {
            invoiceBUS.loadLocal();
        }

        // Kiểm tra xem có hóa đơn nào dùng mã này không
        ArrayList<InvoiceDTO> invoicesUsingDiscount = invoiceBUS.filterInvoicesByDiscountCode(discountCode);

        // Nếu không có => được phép xóa
        return invoicesUsingDiscount.isEmpty();
    }

    /**
     * Lấy ID của status từ loại status và tên status
     * 
     * @param type       StatusType (CUSTOMER, PRODUCT, EMPLOYEE, SUPPLIER, ACCOUNT,
     *                   CATEGORY, ...)
     * @param statusEnum Enum chứa tên status (Status.Customer.ACTIVE,
     *                   Status.Product.INACTIVE, ...)
     * @return ID của status, hoặc 0 nếu không tìm thấy
     */
    public int getStatusIdByTypeAndName(StatusType type, Enum<?> statusEnum) {
        if (type == null || statusEnum == null) {
            return 0;
        }

        StatusDTO status = StatusBUS.getInstance().getByTypeAndStatusNameLocal(type, statusEnum);
        return status != null ? status.getId() : 0;
    }

    /**
     * Lấy tên status từ ID
     * 
     * @param statusId ID của status
     * @return Tên status, hoặc null nếu không tìm thấy
     */
    public String getStatusNameById(int statusId) {
        if (statusId <= 0) {
            return null;
        }

        StatusDTO status = StatusBUS.getInstance().getByIdLocal(statusId);
        return status != null ? status.getName() : null;
    }

    /**
     * Kiểm tra xem status có tên cụ thể hay không
     * 
     * @param statusId     ID của status
     * @param expectedName Tên kỳ vọng
     * @return true nếu status name trùng với expectedName
     */
    public boolean isStatusName(int statusId, String expectedName) {
        if (statusId <= 0 || expectedName == null) {
            return false;
        }

        StatusDTO status = StatusBUS.getInstance().getByIdLocal(statusId);
        return status != null && status.getName().equalsIgnoreCase(expectedName);
    }

}
