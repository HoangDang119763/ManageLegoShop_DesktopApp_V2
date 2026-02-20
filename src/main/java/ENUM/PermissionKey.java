package ENUM;

public enum PermissionKey {
    // === 🧩 MODULE NHÂN VIÊN (Phân tách theo đúng 4 Tab trên UI) ===
    EMPLOYEE_LIST_VIEW,
    // Tab 1: Thông tin cá nhân
    EMPLOYEE_PERSONAL_VIEW,
    EMPLOYEE_PERSONAL_UPDATE,

    // Tab 2: Đơn vị công tác & Lịch sử
    EMPLOYEE_JOB_VIEW,
    EMPLOYEE_JOB_UPDATE,

    // Tab 3: Bảo hiểm & Thu nhập
    EMPLOYEE_PAYROLLINFO_VIEW,
    EMPLOYEE_PAYROLLINFO_UPDATE,

    // Tab 4: Tài khoản hệ thống (Chuyển từ ACCOUNT qua đây)
    EMPLOYEE_ACCOUNT_VIEW,
    // Quyền quản trị mật khẩu (Dùng để hiện nút Reset mật khẩu cho nhân viên khác)
    EMPLOYEE_ACCOUNT_RESET_PASSWORD,
    // Quyền quản trị trạng thái (Khóa/Mở khóa tài khoản nhân viên)
    EMPLOYEE_ACCOUNT_UPDATE_STATUS,

    // Quyền thao tác danh sách
    EMPLOYEE_INSERT,
    EMPLOYEE_DELETE,

    // === HR TAB: Đơn nghỉ phép ===
    EMPLOYEE_LEAVE_REQUEST_VIEW,
    EMPLOYEE_LEAVE_REQUEST_CREATE,
    EMPLOYEE_LEAVE_REQUEST_MANAGE,

    // === HR TAB: Kỷ luật & khen thưởng ===
    EMPLOYEE_FINE_REWARD_VIEW,
    EMPLOYEE_FINE_REWARD_MANAGE,

    // === HR TAB: Chấm công ===
    EMPLOYEE_ATTENDANCE_VIEW,
    EMPLOYEE_ATTENDANCE_MANAGE,

    // === HR TAB: Chức vụ/Vị trí ===
    EMPLOYEE_ROLE_POSITION_UPDATE,

    // === 👥 MODULE KHÁCH HÀNG ===
    CUSTOMER_LIST_VIEW, CUSTOMER_INSERT, CUSTOMER_UPDATE, CUSTOMER_DELETE,

    // === 📦 MODULE SẢN PHẨM ===
    PRODUCT_LIST_VIEW, PRODUCT_INSERT, PRODUCT_UPDATE, PRODUCT_DELETE,

    // === 🏭 MODULE NHÀ CUNG CẤP ===
    SUPPLIER_LIST_VIEW, SUPPLIER_INSERT, SUPPLIER_UPDATE, SUPPLIER_DELETE,

    // === 💰 MODULE GIAO DỊCH ===
    ORDER_LIST_VIEW, ORDER_CREATE,
    IMPORT_LIST_VIEW, IMPORT_CREATE,

    // === 📑 MODULE DANH MỤC & KHUYẾN MÃI ===
    CATEGORY_LIST_VIEW, CATEGORY_INSERT, CATEGORY_UPDATE, CATEGORY_DELETE,
    PROMOTION_LIST_VIEW, PROMOTION_INSERT, PROMOTION_UPDATE, PROMOTION_DELETE,

    // === ⚙️ HỆ THỐNG (Chỉ còn lại Role và Permission) ===
    ROLE_LIST_VIEW, ROLE_INSERT, ROLE_UPDATE, ROLE_DELETE,
    PERMISSION_LIST_VIEW, PERMISSION_UPDATE,

    // === 📊 THỐNG KÊ ===
    STATISTICS_VIEW;
}