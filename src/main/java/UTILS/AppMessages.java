package UTILS;

/**
 * Lớp AppMessages quản lý tập trung tất cả các thông báo (message) của ứng
 * dụng.
 * Mục đích: Tránh hardcode string ở nhiều nơi, dễ bảo trì và sửa chữa.
 * Tất cả messages được tổ chức theo từng module/chức năng để dễ quản lý.
 */
public class AppMessages {

    // ==================== MODULE ĐĂNG NHẬP (LOGIN) ====================
    /**
     * Các thông báo liên quan đến quá trình đăng nhập tài khoản
     */
    public static final String LOGIN_SUCCESS = "Đăng nhập thành công!";
    public static final String LOGIN_EMPTY_CREDENTIALS = "Vui lòng điền tài khoản và mật khẩu";
    public static final String LOGIN_INVALID_CREDENTIALS = "Tài khoản hoặc mật khẩu không chính xác!";
    public static final String LOGIN_ACCOUNT_LOCKED = "Tài khoản của bạn hiện đang bị khóa!";
    public static final String LOGIN_EMPLOYEE_INVALID = "Thông tin nhân viên không hợp lệ!";

    // ==================== MODULE BÁN HÀNG - CHI TIẾT HÓA ĐƠN ====================
    /**
     * Các thông báo liên quan đến quá trình bán hàng và quản lý hóa đơn
     */
    public static final String INVOICE_DETAIL_QUANTITY_EMPTY = "Số lượng không được để trống.";
    public static final String INVOICE_DETAIL_QUANTITY_MIN = "Số lượng phải lớn hơn hoặc bằng 1.";
    public static final String INVOICE_DETAIL_QUANTITY_EXCEED = "Vượt quá số lượng tồn kho!";
    public static final String INVOICE_DETAIL_QUANTITY_INVALID = "Số lượng phải là số nguyên hợp lệ.";
    public static final String INVOICE_DETAIL_ADD_ERROR = "Có lỗi khi thêm chi tiết hóa đơn. Vui lòng thử lại.";

    // ==================== MODULE NHẬP HÀNG ====================
    /**
     * Các thông báo liên quan đến quá trình nhập hàng và quản lý hóa đơn nhập
     */
    public static final String IMPORT_SUPPLIER_REQUIRED = "Vui lòng chọn nhà cung cấp";
    public static final String IMPORT_SUPPLIER_ADD_SUCCESS = "Thêm nhà cung cấp thành công";

    // ==================== MODULE NHÂN VIÊN (EMPLOYEE) ====================
    /**
     * Các thông báo liên quan đến quản lý thông tin nhân viên
     */
    public static final String EMPLOYEE_REFRESH_SUCCESS = "Làm mới thành công";
    public static final String EMPLOYEE_ADD_SUCCESS = "Thêm nhân viên thành công";
    public static final String EMPLOYEE_DELETE_SUCCESS = "Xóa nhân viên thành công!";
    public static final String EMPLOYEE_DELETE_NO_SELECTION = "Vui lòng chọn một nhân viên để xóa!";
    public static final String EMPLOYEE_UPDATE_SUCCESS = "Sửa nhân viên thành công";

    // ==================== MODULE KHÁCH HÀNG (CUSTOMER) ====================
    /**
     * Các thông báo liên quan đến quản lý thông tin khách hàng
     */
    public static final String CUSTOMER_REFRESH_SUCCESS = "Làm mới thành công";
    public static final String CUSTOMER_ADD_SUCCESS = "Thêm khách hàng thành công";
    public static final String CUSTOMER_DELETE_SUCCESS = "Xóa khách hàng thành công!";
    public static final String CUSTOMER_DELETE_NO_SELECTION = "Vui lòng chọn một khách hàng để xóa!";
    public static final String CUSTOMER_UPDATE_SUCCESS = "Sửa khách hàng thành công";

    // ==================== MODULE SẢN PHẨM (PRODUCT) ====================
    /**
     * Các thông báo liên quan đến quản lý sản phẩm và kho hàng
     */
    public static final String PRODUCT_NAME_EMPTY = "Tên sản phẩm không được để trống.";
    public static final String PRODUCT_NAME_INVALID = "Tên sản phẩm không hợp lệ (tối đa 100 ký tự).";
    public static final String PRODUCT_CATEGORY_EMPTY = "Loại sản phẩm không được để trống.";
    public static final String PRODUCT_PRICE_EMPTY = "Giá sản phẩm không được để trống.";
    public static final String PRODUCT_PRICE_INVALID = "Giá sản phẩm không hợp lệ.";
    public static final String PRODUCT_ADD_SUCCESS = "Thêm sản phẩm thành công";
    public static final String PRODUCT_DELETE_SUCCESS = "Xóa sản phẩm thành công!";
    public static final String PRODUCT_UPDATE_SUCCESS = "Sửa sản phẩm thành công";
    public static final String PRODUCT_ADD_NO_SELECTION = "Vui lòng chọn một sản phẩm để thêm!";

    // ==================== MODULE NHÀ CUNG CẤP (SUPPLIER) ====================
    /**
     * Các thông báo liên quan đến quản lý nhà cung cấp
     */
    public static final String SUPPLIER_NAME_EMPTY = "Tên nhà cung cấp không được để trống.";
    public static final String SUPPLIER_NAME_INVALID = "Tên nhà cung cấp không hợp lệ (tối đa 100 ký tự).";
    public static final String SUPPLIER_PHONE_EMPTY = "Số điện thoại không được để trống.";
    public static final String SUPPLIER_PHONE_INVALID = "Số điện thoại không hợp lệ (Số 0 đứng đầu và 10 số).";
    public static final String SUPPLIER_ADDRESS_EMPTY = "Địa chỉ không được để trống.";
    public static final String SUPPLIER_ADDRESS_INVALID = "Địa chỉ không hợp lệ (tối đa 255 ký tự).";
    public static final String SUPPLIER_ADD_ERROR = "Có lỗi khi thêm nhà cung cấp. Vui lòng thử lại.";
    public static final String SUPPLIER_ADD_DUPLICATE = "Đã có nhà cung cấp trong cơ sở dữ liệu.";
    public static final String SUPPLIER_ADD_NO_PERMISSION = "Không có quyền thêm nhà cung cấp.";
    public static final String SUPPLIER_ADD_DB_ERROR = "Thêm nhà cung cấp vào CSDL thất bại.";
    public static final String SUPPLIER_ADD_INVALID_DATA = "Dữ liệu nhập không hợp lệ.";
    public static final String SUPPLIER_UPDATE_ERROR = "Có lỗi khi cập nhật thông tin nhà cung cấp. Vui lòng thử lại.";
    public static final String SUPPLIER_UPDATE_DUPLICATE = "Thông tin nhà cung cấp bị trùng lặp.";
    public static final String SUPPLIER_UPDATE_NO_PERMISSION = "Không có quyền cập nhật thông tin nhà cung cấp.";
    public static final String SUPPLIER_UPDATE_FAILED = "Không thể cập nhật thông tin nhà cung cấp. Vui lòng thử lại.";

    // ==================== MODULE LOẠI SẢN PHẨM (CATEGORY) ====================
    /**
     * Các thông báo liên quan đến quản lý danh mục/loại sản phẩm
     */
    // Placeholder cho các messages về category

    // ==================== MODULE CHIẾT KHẤU (DISCOUNT) ====================
    /**
     * Các thông báo liên quan đến quản lý chiết khấu và khuyến mãi
     */
    // Placeholder cho các messages về discount

    // ==================== MODULE PHÂN QUYỀN (ROLE & PERMISSION)
    // ====================
    /**
     * Các thông báo liên quan đến quản lý vai trò và quyền hạn
     */
    // Placeholder cho các messages về role và permission

    // ==================== MODULE TÀI KHOẢN (ACCOUNT) ====================
    /**
     * Các thông báo liên quan đến quản lý tài khoản người dùng
     */
    // Placeholder cho các messages về account

    // ==================== MODULE THỐNG KÊ (STATISTIC) ====================
    /**
     * Các thông báo liên quan đến báo cáo và thống kê
     */
    // Placeholder cho các messages về thống kê

    // ==================== CÁC THÔNG BÁO CHUNG (GENERAL) ====================
    /**
     * Các thông báo chung dùng cho nhiều module
     */
    public static final String DIALOG_TITLE = "Thông báo";
    public static final String UNKNOWN_ERROR = "Lỗi không xác định. Vui lòng thử lại.";
    public static final String OPERATION_SUCCESS = "Thao tác thành công!";
    public static final String OPERATION_FAILED = "Thao tác thất bại!";
}
