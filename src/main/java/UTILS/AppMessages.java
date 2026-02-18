package UTILS;

/**
 * Lớp AppMessages quản lý tập trung tất cả các thông báo (message) của ứng
 * dụng.
 * Mục đích: Tránh hardcode string ở nhiều nơi, dễ bảo trì và sửa chữa.
 * Tất cả messages được tổ chức theo từng module/chức năng để dễ quản lý.
 */
public class AppMessages {
    // ==================== MODULE THÔNG TIN CÁ NHÂN NHÂN VIÊN (EMPLOYEE INFO)
    // ====================
    public static final String EMPLOYEE_NOT_FOUND = "Không tìm thấy thông tin nhân viên";
    public static final String EMPLOYEE_DETAIL_LOAD_ERROR = "Không thể tải thông tin chi tiết nhân viên";
    public static final String EMPLOYEE_PERSONAL_UPDATE_SUCCESS = "Cập nhật thông tin cá nhân thành công!";
    public static final String EMPLOYEE_PERSONAL_UPDATE_ERROR = "Có lỗi khi cập nhật thông tin cá nhân. Vui lòng thử lại.";
    public static final String EMPLOYEE_PERSONAL_INFO_LOAD_SUCCESS = "Tải thông tin hồ sơ nhân viên thành công.";
    public static final String EMPLOYEE_ACCOUNT_INFO_LOAD_SUCCESS = "Tải thông tin tài khoản hệ thống thành công.";
    public static final String EMPLOYEE_JOB_INFO_LOAD_SUCCESS = "Tải thông tin lương và công tác thành công.";
    public static final String EMPLOYEE_PAYROLL_INFO_LOAD_SUCCESS = "Tải thông tin quyền lợi và bảo hiểm thành công.";
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
    public static final String EMPLOYEE_CANNOT_DELETE_SYSTEM = "Không thể xóa nhân viên hệ thống";
    public static final String EMPLOYEE_CANNOT_DELETE_SELF = "Không thể xóa thông tin nhân viên của chính mình";

    // ==================== MODULE KHÁCH HÀNG (CUSTOMER) ====================
    /**
     * Các thông báo liên quan đến quản lý thông tin khách hàng
     */
    public static final String CUSTOMER_ADD_SUCCESS = "Thêm khách hàng thành công";
    public static final String CUSTOMER_ADD_DUPLICATE = "Đã có khách hàng trong cơ sở dữ liệu";
    public static final String CUSTOMER_DELETE_SUCCESS = "Xóa khách hàng thành công!";
    public static final String CUSTOMER_CANNOT_DELETE_SYSTEM = "Không thể xóa khách hàng vãng lai.";
    public static final String CUSTOMER_DELETE_CONFIRM = "Bạn chắc muốn xóa khách hàng này?";
    public static final String CUSTOMER_NO_SELECTION = "Vui lòng chọn một khách hàng để xóa!";
    public static final String CUSTOMER_UPDATE_DUPLICATE = "Đã có khách hàng trong cơ sở dữ liệu";
    public static final String CUSTOMER_UPDATE_SUCCESS = "Sửa khách hàng thành công";

    // ==================== MODULE SẢN PHẨM (PRODUCT) ====================
    /**
     * Các thông báo liên quan đến quản lý sản phẩm và kho hàng
     */
    public static final String PRODUCT_ADD_CATEGORY_INVALID = "Thể loại không còn được sử dụng";
    public static final String PRODUCT_ADD_SUCCESS = "Thêm sản phẩm thành công";
    public static final String PRODUCT_ADD_DUPLICATE = "Tên sản phẩm đã tồn tại trong hệ thống.";
    public static final String PRODUCT_NO_SELECTION = "Vui lòng chọn sản phẩm";
    public static final String PRODUCT_DELETE_WITH_STOCK = "Sản phẩm còn tồn kho, không thể ngừng kinh doanh hoặc xóa!";
    public static final String PRODUCT_DELETE_CONFIRM = "Bạn chắc muốn xóa sản phẩm này?";
    public static final String PRODUCT_DELETE_SUCCESS = "Xóa sản phẩm thành công!";
    public static final String PRODUCT_UPDATE_SUCCESS = "Sửa sản phẩm thành công";
    public static final String PRODUCT_UPDATE_DUPLICATE = "Tên sản phẩm đã tồn tại trong hệ thống.";
    public static final String PRODUCT_IMAGE_RESET_SUCCESS = "Xóa ảnh sản phẩm thành công.";
    public static final String PRODUCT_IMAGE_RESET_CONFIRM = "Bạn có chắc chắn muốn xóa ảnh không?";

    // ==================== MODULE NHÀ CUNG CẤP (SUPPLIER) ====================
    /**
     * Các thông báo liên quan đến quản lý nhà cung cấp
     */
    public static final String SUPPLIER_NO_SELECTION = "Vui lòng chọn nhà cung cấp.";
    public static final String SUPPLIER_DELETE_CONFIRM = "Bạn có chắc chắn muốn xóa nhà cung cấp này không?";
    public static final String SUPPLIER_ADD_SUCCESS = "Thêm nhà cung cấp thành công";
    public static final String SUPPLIER_ADD_DUPLICATE = "Đã có nhà cung cấp trong cơ sở dữ liệu";
    public static final String SUPPLIER_UPDATE_DUPLICATE = "Đã có nhà cung cấp trong cơ sở dữ liệu";
    public static final String SUPPLIER_UPDATE_SUCCESS = "Sửa nhà cung cấp thành công";
    // ==================== MODULE LOẠI SẢN PHẨM (CATEGORY) ====================
    /**
     * Các thông báo liên quan đến quản lý danh mục/loại sản phẩm
     */
    public static final String CATEGORY_ADD_DUPLICATE = "Tên thể loại đã tồn tại trong hệ thống.";
    public static final String CATEGORY_ADD_SUCCESS = "Thêm thể loại thành công";
    public static final String CATEGORY_CANNOT_DELETE_SYSTEM = "Không thể xóa thể loại gốc";
    public static final String CATEGORY_DELETE_SUCCESS = "Xóa thể loại thành công";
    public static final String CATEGORY_UPDATE_SUCCESS = "Cập nhật thể loại thành công";
    public static final String CATEGORY_CANNOT_UPDATE_SYSTEM = "Không thể sửa thể loại gốc!";
    public static final String CATEGORY_UPDATE_DUPLICATE = "Tên thể loại đã tồn tại trong hệ thống.";
    public static final String CATEGORY_DELETE_CONFIRM = "Bạn có chắc chắn muốn xóa thể loại này không?";
    public static final String CATEGORY_NO_SELECTION = "Vui lòng chọn thể loại.";
    public static final String CATEGORY_DELETED_WARNING = "Thể loại này đã ngưng dùng, không nên chọn cho sản phẩm";

    // ==================== MODULE PHÒNG BAN (DEPARTMENT) ====================
    /**
     * Các thông báo liên quan đến quản lý phòng ban
     */
    public static final String DEPARTMENT_DELETED_WARNING = "Phòng ban này đã ngưng dùng, không nên chọn cho nhân viên";

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
    public static final String ACCOUNT_PASSWORD_CHANGE_SUCCESS = "Đổi mật khẩu thành công!";
    public static final String ACCOUNT_DELETE_SUCCESS = "Xóa tài khoản thành công!";
    public static final String ACCOUNT_OLD_PASSWORD_WRONG = "Mật khẩu cũ không đúng. Vui lòng thử lại.";
    public static final String ACCOUNT_CANNOT_DELETE_SYSTEM = "Không thể xóa tài khoản gốc hệ thống";
    public static final String ACCOUNT_CANNOT_DELETE_SELF = "Không thể xóa tài khoản của chính mình";
    public static final String ACCOUNT_USERNAME_DUPLICATE = "Tài khoản đã tồn tại. Vui lòng chọn tên khác.";
    public static final String ACCOUNT_ADD_SUCCESS = "Thêm tài khoản thành công!";
    public static final String ACCOUNT_NOT_FOUND = "Tài khoản không tồn tại.";
    public static final String LOGIN_SUCCESS = "Đăng nhập thành công!";
    public static final String LOGIN_EMPTY_CREDENTIALS = "Vui lòng điền tài khoản và mật khẩu";
    public static final String LOGIN_INVALID_CREDENTIALS = "Tài khoản hoặc mật khẩu không chính xác!";
    public static final String LOGIN_ACCOUNT_LOCKED = "Tài khoản của bạn hiện đang bị khóa!";
    public static final String LOGIN_EMPLOYEE_INVALID = "Thông tin nhân viên không hợp lệ!";
    public static final String LOGOUT_CONFIRM = "Bạn chắc muốn đăng xuất?";
    // Placeholder cho các messages về account

    // ==================== MODULE THỐNG KÊ (STATISTIC) ====================
    /**
     * Các thông báo liên quan đến báo cáo và thống kê
     */
    // Placeholder cho các messages về thống kê

    // ==================== MODULE TRẠNG THÁI (STATUS) ====================
    public static final String STATUS_IDForType_INVALID = "Status ID không hợp lệ.";

    // ==================== CÁC THÔNG BÁO CHUNG (GENERAL) ====================
    /**
     * Các thông báo chung dùng cho nhiều module
     */
    public static final String DIALOG_TITLE = "Thông báo";
    public static final String DIALOG_TITLE_CONFIRM = "Thông báo xác nhận";
    public static final String GENERAL_REFRESH_SUCCESS = "Làm mới thành công";
    public static final String DB_ERROR = "Lỗi kết nối cơ sở dữ liệu. Vui lòng thử lại sau.";
    public static final String GENERAL_ERROR = "Lỗi không xác định. Thao tác thất bại.";
    public static final String UNKNOWN_ERROR = "Lỗi không xác định. Vui lòng thử lại.";
    public static final String OPERATION_SUCCESS = "Thao tác thành công!";
    public static final String INVALID_DATA = "Dữ liệu đầu vào không hợp lệ";
    public static final String INVALID_PARAMS = "Tham số không hợp lệ";
    public static final String NOT_FOUND = "Không tìm thấy dữ liệu yêu cầu";
    public static final String UNAUTHORIZED = "Bạn không có quyền để thực hiện thao tác này.";
    public static final String DATA_ALREADY_DELETED = "Dữ liệu này đã ở trạng thái Inactive (xóa hoặc ngừng hoạt động)";
    public static final String FORCE_RELOGIN = "Phiên làm việc hết hạn hoặc quyền đã thay đổi. Vui lòng đăng nhập lại.";
    public static final String REQUEST_RELOGIN = "Vui lòng đăng nhập lại.";
}
