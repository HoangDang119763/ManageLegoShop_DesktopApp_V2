package BUS;

import DAL.AccountDAL;
import DTO.AccountDTO;
import DTO.BUSResult;
import DTO.EmployeeSessionDTO;
import ENUM.*;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.PasswordUtils;
import UTILS.ValidationUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class AccountBUS extends BaseBUS<AccountDTO, Integer> {
    private static final AccountBUS INSTANCE = new AccountBUS();

    private AccountBUS() {
    }

    public static AccountBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<AccountDTO> getAll() {
        return AccountDAL.getInstance().getAll();
    }

    @Override
    public AccountDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return AccountDAL.getInstance().getById(id);
    }

    @Override
    protected Integer getKey(AccountDTO obj) {
        return obj.getId();
    }

    // ============================================================
    // NGHIỆP VỤ CRUD (Đã bỏ tham số roleId/loginId thừa)
    // ============================================================

    public BUSResult insert(AccountDTO obj) {
        if (obj == null || !isValidUsername(obj.getUsername()) || !isValidPassword(obj.getPassword())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // Validate roleId
        if (obj.getRoleId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        RoleBUS roleBUS = RoleBUS.getInstance();
        if (roleBUS.getById(obj.getRoleId()) == null) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // Validate statusId
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.ACCOUNT, obj.getStatusId())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // Kiểm tra trùng username trực tiếp dưới DB
        if (AccountDAL.getInstance().getByUsername(obj.getUsername()) != null) {
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.ACCOUNT_USERNAME_DUPLICATE);
        }

        // Logic xử lý dữ liệu trước khi lưu
        obj.setUsername(obj.getUsername().toLowerCase());
        obj.setPassword(PasswordUtils.getInstance().hashPassword(obj.getPassword()));
        obj.setRequireRelogin(false); // Mặc định tài khoản mới không cần relogin

        if (!AccountDAL.getInstance().insert(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.ACCOUNT_ADD_SUCCESS);
    }

    public boolean insertWithConn(Connection conn, AccountDTO obj) throws SQLException {
        if (obj == null || !isValidUsername(obj.getUsername()))
            return false;
        StatusBUS statusBus = StatusBUS.getInstance();
        if (!statusBus.isValidStatusIdForType(StatusType.ACCOUNT, obj.getStatusId()))
            return false;

        // Logic nghiệp vụ đặc trưng của Account
        obj.setUsername(obj.getUsername().toLowerCase().trim());
        obj.setPassword(PasswordUtils.getInstance().hashPassword("123456"));

        return AccountDAL.getInstance().insertWithConn(conn, obj);
    }

    public BUSResult delete(Integer id, int currentLoginId) {
        if (id == null || id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);

        if (id == 1)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.ACCOUNT_CANNOT_DELETE_SYSTEM);
        if (id == currentLoginId)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.ACCOUNT_CANNOT_DELETE_SELF);

        if (!AccountDAL.getInstance().delete(id)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.ACCOUNT_DELETE_SUCCESS);
    }

    // ============================================================
    // BẢO MẬT & XÁC THỰC
    // ============================================================

    public BUSResult authenticate(String username, String password) {
        AccountDAL accountDAL = AccountDAL.getInstance();
        // 1. Lấy dữ liệu tươi từ DB
        AccountDTO acc = accountDAL.getByUsername(username);
        if (acc == null)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.ACCOUNT_NOT_FOUND);

        // 2. Kiểm tra trạng thái Active (Sử dụng Static Cache từ StatusBUS)
        int lockedId = StatusBUS.getInstance().getByTypeAndStatusName(StatusType.ACCOUNT, Status.Account.LOCKED)
                .getId();
        if (acc.getStatusId() == lockedId)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.LOGIN_ACCOUNT_LOCKED);

        // 3. So khớp mật khẩu
        if (!PasswordUtils.getInstance().verifyPassword(password, acc.getPassword()))
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.LOGIN_INVALID_CREDENTIALS);

        // 4. RESET FLAG require_relogin sau khi đăng nhập thành công
        accountDAL.setRequireRelogin(acc.getId(), false);
        accountDAL.updateLastLogin(acc.getId());
        // 5. Set session
        EmployeeSessionDTO session = EmployeeBUS.getInstance().getEmployeeSessionByAccountId(acc.getId());
        // Sau khi có session, tải permission và moduleIds vào session luôn để sau này
        // check quyền chỉ cần check trong "cái thẻ" này
        if (session != null) {
            session.setPermissions(PermissionBUS.getInstance().getPermissionKeysByRoleId(session.getRoleId()));
            session.setAllowedModuleIds(
                    RolePermissionBUS.getInstance().getAllowedModuleIdsByRoleId(session.getRoleId()));
        }
        SessionManagerService.getInstance().login(session);
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.LOGIN_SUCCESS + " - Chào "
                + SessionManagerService.getInstance().getLoggedName());
    }

    public BUSResult changePasswordBySelf(String username, String oldPassword, String newPassword) {
        if (username == null || oldPassword == null || newPassword == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }
        AccountDAL accountDAL = AccountDAL.getInstance();
        AccountDTO existingAcc = accountDAL.getByUsername(username);
        if (existingAcc == null
                || !PasswordUtils.getInstance().verifyPassword(oldPassword, existingAcc.getPassword())) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.ACCOUNT_OLD_PASSWORD_WRONG);
        }

        // Hash mật khẩu mới và lưu
        String hashedNewPassword = PasswordUtils.getInstance().hashPassword(newPassword);
        if (!accountDAL.updatePasswordAndForceRelogin(username, hashedNewPassword)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.ACCOUNT_PASSWORD_CHANGE_SUCCESS);
    }

    // ============================================================
    // CƠ CHẾ ĐỒNG BỘ (RELOGIN FLAG)
    // ============================================================

    public boolean isRequireRelogin(int accountId) {
        if (accountId <= 0)
            return false;
        return AccountDAL.getInstance().isRequireRelogin(accountId);
    }

    /**
     * Kích hoạt yêu cầu đăng nhập lại cho TẤT CẢ nhân viên thuộc một Role
     * Dùng khi Manager thay đổi phân quyền của Role đó.
     */
    public boolean setRequireReloginByRoleId(int roleId, boolean requireRelogin) {
        if (roleId <= 0)
            return false;
        return AccountDAL.getInstance().setRequireReloginByRoleId(roleId, requireRelogin);
    }

    public boolean setRequireRelogin(int accountId, boolean requireRelogin) {
        if (accountId <= 0)
            return false;
        return AccountDAL.getInstance().setRequireRelogin(accountId, requireRelogin);
    }

    /**
     * Kiểm tra username có tồn tại hay không
     *
     * @param username Username cần kiểm tra
     * @return true nếu username đã tồn tại, false nếu chưa
     */
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return AccountDAL.getInstance().getByUsername(username) != null;
    }

    private boolean isValidUsername(String username) {
        ValidationUtils validator = ValidationUtils.getInstance();
        return username != null && validator.validateUsername(username, 4, 50);
    }

    private boolean isValidPassword(String password) {
        ValidationUtils validator = ValidationUtils.getInstance();
        return password != null && validator.validatePassword(password, 6, 255);
    }

    /**
     * Cập nhật trạng thái tài khoản (TAB 2: ACCOUNT INFO)
     * Update: status_id
     * ⚠️ Caller PHẢI kiểm tra quyền EMPLOYEE_ACCOUNT_UPDATE_STATUS trước khi gọi
     * 
     * @param accountId ID của tài khoản
     * @param statusId  ID trạng thái mới
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean updateAccountStatus(int accountId, int statusId) {
        if (accountId <= 0 || statusId <= 0) {
            return false;
        }

        if (accountId == 1) {
            return false;
        }

        return AccountDAL.getInstance().updateAccountStatus(accountId, statusId);
    }

    /**
     * Reset mật khẩu tài khoản thành mặc định (123456)
     * ⚠️ Chỉ update password, KHÔNG set require_relogin (gọi
     * forceLogoutAndSecurityUpdate riêng)
     * 
     * @param username Username của tài khoản
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean resetPassword(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        // Hash mật khẩu mặc định
        String defaultPassword = "123456";
        String hashedPassword = PasswordUtils.getInstance().hashPassword(defaultPassword);

        // Chỉ update password, KHÔNG set require_relogin
        return AccountDAL.getInstance().resetPassword(username, hashedPassword);
    }

    public boolean forceLogoutAndSecurityUpdate(Connection conn, int accountId, boolean isDeactivate)
            throws SQLException {
        if (isDeactivate) {
            // Nếu là nghỉ việc: Văng ra + Khóa luôn Account
            int lockedId = StatusBUS.getInstance()
                    .getByTypeAndStatusName(StatusType.ACCOUNT, Status.Account.LOCKED).getId();
            return AccountDAL.getInstance().updateAccountSecurity(conn, accountId, true, lockedId);
        }
        // Nếu chỉ đổi quyền: Chỉ văng ra để nạp lại session
        return AccountDAL.getInstance().updateAccountSecurity(conn, accountId, true, null);
    }

}