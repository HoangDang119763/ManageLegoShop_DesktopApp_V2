package UTILS;

import BUS.*;
import DTO.CategoryDTO;
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

    /**
     * Kiểm tra xem role có hợp lệ không
     * - roleId phải > 0
     * - role phải tồn tại trong database
     * - role phải có đủ quyền hạn (RolePermission)
     */
    public boolean isValidRole(int roleId) {
        if (roleId <= 0)
            return false;

        if (RoleBUS.getInstance().isLocalEmpty())
            RoleBUS.getInstance().loadLocal();

        return RoleBUS.getInstance().getByIdLocal(roleId) != null && isValidRoleWithPermissions(roleId);
    }

    /**
     * Kiểm tra xem nhân viên có đủ điều kiện để tạo tài khoản không
     * 
     * @param employeeId - mã nhân viên
     * @param type       - 0: tạo tài khoản thường, 1: tạo tài khoản admin
     * @return true nếu hợp lệ
     * 
     *         Điều kiện:
     *         - Nếu type=0: tài khoản chưa tồn tại && role không phải admin (roleId
     *         != 1)
     *         - Nếu type=1: tài khoản chưa tồn tại
     */
    public boolean isValidForCreateAccount(int employeeId, int type) {
        if (employeeId <= 0 || (type != 0 && type != 1))
            return false;

        if (AccountBUS.getInstance().isLocalEmpty())
            AccountBUS.getInstance().loadLocal();
        if (EmployeeBUS.getInstance().isLocalEmpty())
            EmployeeBUS.getInstance().loadLocal();
        if (type == 0) {
            return AccountBUS.getInstance().getByIdLocal(employeeId) == null
                    && EmployeeBUS.getInstance().getByIdLocal(employeeId).getRoleId() != 1;
        } else {
            return AccountBUS.getInstance().getByIdLocal(employeeId) == null;
        }
    }

    /**
     * Kiểm tra xem tài khoản có tồn tại không dựa trên employee_id
     */
    public boolean isExistAccount(int employeeId) {
        if (employeeId <= 0)
            return false;

        if (AccountBUS.getInstance().isLocalEmpty())
            AccountBUS.getInstance().loadLocal();

        // Kiểm tra xem tài khoản có tồn tại không
        return AccountBUS.getInstance().getByIdLocal(employeeId) != null;
    }

    /**
     * Kiểm tra role có đủ các quyền hạn không
     * Yêu cầu: số lượng RolePermission của role phải bằng tổng số Permission trong
     * hệ thống
     */
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

    /**
     * Kiểm tra danh mục sản phẩm có hợp lệ không
     * - categoryId phải > 0
     * - danh mục phải tồn tại
     * - danh mục phải có trạng thái hoạt động (status = true)
     */
    public boolean isValidCategory(int categoryId) {
        if (categoryId <= 0)
            return false;

        // Tự động load nếu cache trống
        if (CategoryBUS.getInstance().isLocalEmpty())
            CategoryBUS.getInstance().loadLocal();
        if (StatusBUS.getInstance().isLocalEmpty())
            StatusBUS.getInstance().loadLocal();

        CategoryDTO temp = CategoryBUS.getInstance().getByIdLocal(categoryId);
        if (temp == null)
            return false;

        // Lấy đối tượng status an toàn
        StatusDTO activeStatus = StatusBUS.getInstance()
                .getByTypeAndStatusNameLocal(StatusType.CATEGORY, Status.Category.ACTIVE);

        // Kiểm tra null cho activeStatus trước khi so sánh ID
        return activeStatus != null && temp.getStatusId() == activeStatus.getId();
    }

    /**
     * Kiểm tra mã chiết khấu có đang được sử dụng trong hóa đơn không
     * Được dùng khi muốn xóa hoặc vô hiệu hóa mã chiết khấu
     * 
     * @return true nếu mã chiết khấu CHƯA được dùng (có thể xóa)
     *         false nếu mã chiết khấu đang được sử dụng (không thể xóa)
     */
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
