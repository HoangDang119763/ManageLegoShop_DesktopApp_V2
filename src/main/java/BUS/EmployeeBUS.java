
package BUS;

import DAL.AccountDAL;
import DAL.ConnectApplication;
import DAL.EmployeeDAL;
import DTO.EmployeeDTO;
import DTO.EmployeeSessionDTO;
import DTO.PagedResponse;
import DTO.EmployeeDetailDTO;
import DTO.EmployeeDisplayDTO;
import DTO.EmployeePersonalInfoDTO;
import DTO.EmployeeAccountInfoDTO;
import DTO.EmployeeJobInfoDTO;
import DTO.EmployeePayrollInfoDTO;
import DTO.EmployeePersonalInfoBundle;
import DTO.EmployeeJobHistoryBundle;
import DTO.BUSResult;
import DTO.AccountDTO;
import DTO.TaxDTO;
import ENUM.*;
import ENUM.Status.Account;
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

public class EmployeeBUS extends BaseBUS<EmployeeDTO, Integer> {
    private static final EmployeeBUS INSTANCE = new EmployeeBUS();

    private EmployeeBUS() {
    }

    public static EmployeeBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<EmployeeDTO> getAll() {
        return EmployeeDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(EmployeeDTO obj) {
        return obj.getId();
    }

    public int nextId() {
        return EmployeeDAL.getInstance().getLastIdEver() + 1;
    }

    public EmployeeDTO getByAccountId(int accountId) {
        if (accountId <= 0)
            return null;
        return EmployeeDAL.getInstance().getByAccountId(accountId);
    }

    public EmployeeSessionDTO getEmployeeSessionByAccountId(int accountId) {
        if (accountId <= 0)
            return null;
        return EmployeeDAL.getInstance().getEmployeeSessionByAccountId(accountId);
    }

    @Override
    public EmployeeDTO getById(Integer id) {
        if (id <= 0)
            return null;
        return EmployeeDAL.getInstance().getById(id);
    }

    public BUSResult delete(int id) {
        if (id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);

        SessionManagerService session = SessionManagerService.getInstance();
        int currentLoginId = session.employeeLoginId();
        int currentUserRoleId = session.getRoleId();
        // 1. Không cho tự xóa chính mình
        if (currentLoginId == id)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_DELETE_SELF);

        // 2. Không cho xóa IT Admin hệ thống (ID = 1)
        if (id == 1)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_DELETE_SYSTEM);

        EmployeeDTO targetEmployee = getById(id);
        if (targetEmployee == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        if (currentUserRoleId != 1) {
            // Kiểm tra xem đối tượng bị xóa (target) có quyền xóa nhân viên hay không
            // Nếu target cũng có quyền xóa => Coi như ngang quyền => Chặn
            boolean targetHasDeletePermission = PermissionBUS.getInstance()
                    .isRoleHavePermission(targetEmployee.getRoleId(), PermissionKey.EMPLOYEE_DELETE);

            if (targetHasDeletePermission) {
                return new BUSResult(BUSOperationResult.FAIL,
                        AppMessages.EMPLOYEE_ERROR_DELETE_SAME_AUTHORITY);
            }
        }
        int inactiveId = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.EMPLOYEE, Status.Employee.INACTIVE).getId();

        boolean hasInvoice = InvoiceBUS.getInstance().isEmployeeInAnyInvoice(id);
        boolean hasImport = ImportBUS.getInstance().isEmployeeInAnyImport(id);
        boolean hasTimeSheet = TimeSheetBUS.getInstance().isEmployeeInAnyTimeSheet(id);
        // Có thể có thêm các ràng buộc khác ....
        boolean hasAnyTransaction = hasInvoice || hasImport || hasTimeSheet;
        if (targetEmployee.getStatusId() == inactiveId && hasAnyTransaction) {
            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DATA_ALREADY_DELETED);
        }
        boolean success = hasAnyTransaction ? EmployeeDAL.getInstance().updateStatus(id, inactiveId)
                : EmployeeDAL.getInstance().delete(id);
        if (!success)
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_DELETE_SUCCESS);
    }

    public BUSResult insertEmployeeFull(EmployeeDTO employee, AccountDTO account, TaxDTO tax) {
        // 1. Validate tham số đầu vào
        if (employee == null || account == null || tax == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }
        if (SessionManagerService.getInstance().getRoleId() != 1 && employee.getRoleId() == 1) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }
        // 2. Chuẩn hóa dữ liệu
        ValidationUtils validate = ValidationUtils.getInstance();
        employee.setEmail(validate.convertEmptyStringToNull(employee.getEmail()));
        employee.setFirstName(validate.normalizeWhiteSpace(employee.getFirstName()));
        employee.setLastName(validate.normalizeWhiteSpace(employee.getLastName()));
        employee.setPhone(validate.normalizeWhiteSpace(employee.getPhone()));

        // 3. Validate logic nghiệp vụ (Master/Account/Tax)
        if (!isValidEmployeeInputForInsert(employee))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        // 4. Kiểm tra ràng buộc hệ thống (Status, Dept, Username) trước khi mở
        // Connection
        if (!StatusBUS.getInstance().isValidStatusIdForType(StatusType.EMPLOYEE, employee.getStatusId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.STATUS_IDForType_INVALID);

        if (!DepartmentBUS.getInstance().isDepartmentActive(employee.getDepartmentId()))
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.EMPLOYEE_ADD_DEPARTMENT_INVALID);

        if (AccountBUS.getInstance().existsByUsername(account.getUsername()))
            return new BUSResult(BUSOperationResult.CONFLICT, AppMessages.ACCOUNT_USERNAME_DUPLICATE);

        Connection conn = null;
        BUSResult finalResult = null;

        try {
            conn = ConnectApplication.getInstance().getConnectionFactory().newConnection();
            conn.setAutoCommit(false);

            // Bước A: Insert Account trước để lấy account_id (vì employee phụ thuộc vào
            // account_id)
            if (!AccountBUS.getInstance().insertWithConn(conn, account)) {
                throw new Exception("ACCOUNT_FAIL");
            }

            // Bước B: Gán ID tài khoản vừa tạo cho nhân viên và Insert Employee
            employee.setAccountId(account.getId());
            if (!EmployeeDAL.getInstance().insertWithConn(conn, employee)) {
                throw new Exception("EMPLOYEE_FAIL");
            }

            // Bước C: Gán ID nhân viên vừa tạo cho bản ghi thuế và Insert Tax
            tax.setEmployeeId(employee.getId());
            if (!TaxBUS.getInstance().insertWithConn(conn, tax)) {
                throw new Exception("TAX_FAIL");
            }

            conn.commit();
            finalResult = new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_ADD_SUCCESS);

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }

            // Phân loại lỗi trả về đồng bộ với phong cách Discount
            String errorMsg = e.getMessage();
            if ("ACCOUNT_FAIL".equals(errorMsg) || "EMPLOYEE_FAIL".equals(errorMsg) || "TAX_FAIL".equals(errorMsg)) {
                finalResult = new BUSResult(BUSOperationResult.FAIL, AppMessages.UNKNOWN_ERROR);
            } else {
                finalResult = new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
            }

            System.err.println("[DEBUG] Transaction failed at: " + errorMsg);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return finalResult;
    }

    public boolean isValidEmployeeInputForInsert(EmployeeDTO obj) {
        if (obj == null) {
            return false;
        }

        ValidationUtils validator = ValidationUtils.getInstance();

        // 1. Kiểm tra Họ đệm (Bắt buộc)
        String firstName = obj.getFirstName();
        if (firstName == null || firstName.trim().isEmpty()) {
            return false;
        }
        if (!validator.validateVietnameseText100(firstName)) {
            return false;
        }

        // 2. Kiểm tra Tên (Bắt buộc)
        String lastName = obj.getLastName();
        if (lastName == null || lastName.trim().isEmpty()) {
            return false;
        }
        if (!validator.validateVietnameseText100(lastName)) {
            return false;
        }

        // 3. Kiểm tra Số điện thoại (Bắt buộc)
        String phone = obj.getPhone();
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        if (!validator.validateVietnamesePhoneNumber(phone)) {
            return false;
        }

        // 4. Kiểm tra Email (BẮT BUỘC)
        String email = obj.getEmail();
        // Email không được null, không được rỗng và phải đúng định dạng
        if (email == null || email.trim().isEmpty() || !validator.validateEmail(email)) {
            return false;
        }

        // 5. Kiểm tra Ngày sinh (BẮT BUỘC)
        LocalDate dob = obj.getDateOfBirth();
        // Ngày sinh không được null và phải thỏa mãn logic (ví dụ: đủ 18 tuổi)
        if (dob == null || !validator.validateDateOfBirth(dob)) {
            return false;
        }

        return true;
    }

    public BUSResult filterEmployeesPagedForManageDisplay(String keyword, int roleIdFilter, int statusFilter,
            int pageIndex, int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int finalRoleId = (roleIdFilter <= 0) ? -1 : roleIdFilter;
        int finalStatusId = (statusFilter <= 0) ? -1 : statusFilter;
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        int employeeId = SessionManagerService.getInstance().employeeLoginId();
        // Gọi DAL với JOIN để lấy dữ liệu hoàn chỉnh
        PagedResponse<EmployeeDisplayDTO> pagedData = EmployeeDAL.getInstance()
                .filterEmployeesPagedForManageDisplay(cleanKeyword, finalRoleId, finalStatusId, finalPageIndex,
                        finalPageSize, employeeId);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    public BUSResult updatePersonalInfoBySelf(EmployeeDTO obj) {
        // 1. Kiểm tra tham số cơ bản
        if (obj == null || obj.getId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        ValidationUtils validator = ValidationUtils.getInstance();

        // 2. Validate Họ và Tên (Bắt buộc)
        if (obj.getFirstName() == null || obj.getFirstName().trim().isEmpty() ||
                obj.getLastName() == null || obj.getLastName().trim().isEmpty() ||
                !validator.validateVietnameseText100(obj.getFirstName()) ||
                !validator.validateVietnameseText100(obj.getLastName())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 3. Validate Ngày sinh (Bắt buộc)
        if (obj.getDateOfBirth() == null || !validator.validateDateOfBirth(obj.getDateOfBirth())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 4. Validate Số điện thoại (Bắt buộc - Đã bỏ check !isEmpty)
        if (obj.getPhone() == null || obj.getPhone().trim().isEmpty() ||
                !validator.validateVietnamesePhoneNumber(obj.getPhone())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 5. Validate Email (Bắt buộc - Đã bỏ check !isEmpty)
        if (obj.getEmail() == null || obj.getEmail().trim().isEmpty() ||
                !validator.validateEmail(obj.getEmail())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 6. Thực thi Update
        if (!EmployeeDAL.getInstance().updatePersonalInfoBySelf(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_PERSONAL_UPDATE_SUCCESS);
    }

    public BUSResult updatePersonalInfoByAdmin(EmployeeDTO obj) {
        // 1. Kiểm tra tham số đầu vào cơ bản
        if (obj == null || obj.getId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        SessionManagerService session = SessionManagerService.getInstance();
        int currentLoginId = session.employeeLoginId();
        int currentUserRoleId = session.getRoleId();

        // 2. Kiểm tra đối tượng đặc biệt
        if (obj.getId() == 1) { // Bảo vệ IT Admin hệ thống
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SYSTEM);
        }

        if (obj.getId() == currentLoginId) {
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SELF);
        }

        // 3. Lấy thông tin hiện tại của người bị sửa để check quyền
        EmployeeDTO targetEmployee = getById(obj.getId());
        if (targetEmployee == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }

        // 4. CHECK QUYỀN NGANG HÀNG (Logic của bạn)
        if (currentUserRoleId != 1) {
            boolean targetHasAdminPermission = PermissionBUS.getInstance()
                    .isRoleHavePermission(targetEmployee.getRoleId(), PermissionKey.EMPLOYEE_PERSONAL_UPDATE);

            if (targetHasAdminPermission) {
                return new BUSResult(BUSOperationResult.FAIL,
                        AppMessages.EMPLOYEE_ERROR_UPDATE_SAME_AUTHORITY);
            }
        }

        // 5. Validate logic "Bắt buộc điền" (Đã đồng bộ)
        ValidationUtils validator = ValidationUtils.getInstance();

        // Check null/empty cho các trường bắt buộc
        if (obj.getFirstName() == null || obj.getFirstName().trim().isEmpty() ||
                obj.getLastName() == null || obj.getLastName().trim().isEmpty() ||
                obj.getPhone() == null || obj.getPhone().trim().isEmpty() ||
                obj.getEmail() == null || obj.getEmail().trim().isEmpty() ||
                obj.getGender() == null || obj.getDateOfBirth() == null) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // Check định dạng
        if (!validator.validateDateOfBirth(obj.getDateOfBirth()) ||
                !validator.validateVietnameseText100(obj.getFirstName()) ||
                !validator.validateVietnameseText100(obj.getLastName()) ||
                !validator.validateVietnamesePhoneNumber(obj.getPhone()) ||
                !validator.validateEmail(obj.getEmail())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // Check giới tính hợp lệ
        if (!obj.getGender().equals(Gender.MALE.getDisplayName()) &&
                !obj.getGender().equals(Gender.FEMALE.getDisplayName())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 6. Thực thi xuống DB
        if (!EmployeeDAL.getInstance().updatePersonalInfoByAdmin(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_PERSONAL_UPDATE_SUCCESS);
    }

    public BUSResult updateJobInfo(EmployeeDTO obj) {
        // 1. Validate & Security (Giữ nguyên các lớp chặn của bạn)
        if (obj == null || obj.getId() <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        SessionManagerService session = SessionManagerService.getInstance();
        if (obj.getId() == 1)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SYSTEM);
        if (obj.getId() == session.employeeLoginId())
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SELF);

        EmployeeDTO target = getById(obj.getId());
        if (target == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        if (session.getRoleId() != 1) {
            boolean isTargetAdmin = PermissionBUS.getInstance().isRoleHavePermission(target.getRoleId(),
                    PermissionKey.EMPLOYEE_JOB_UPDATE);
            if (isTargetAdmin)
                return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_ERROR_UPDATE_SAME_AUTHORITY);
        }

        // 2. Validate dữ liệu đầu vào
        if (obj.getDepartmentId() == null || obj.getDepartmentId() <= 0 || obj.getRoleId() <= 0
                || obj.getStatusId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // 3. Thực thi Transaction
        try (Connection conn = ConnectApplication.getInstance().getConnectionFactory().newConnection()) {
            conn.setAutoCommit(false);
            try {
                // A. Cập nhật thông tin công việc
                if (!EmployeeDAL.getInstance().updateJobInfo(conn, obj))
                    throw new Exception("UPDATE_JOB_FAIL");

                // B. Xử lý Relogin (Nếu đổi Role hoặc Nghỉ việc)
                int activeId = StatusBUS.getInstance()
                        .getByTypeAndStatusName(StatusType.EMPLOYEE, Status.Employee.ACTIVE).getId();
                Integer accId = target.getAccountId();
                if (accId != null && accId > 0) {
                    boolean isChanged = (obj.getRoleId() != target.getRoleId()) || (obj.getStatusId() != activeId);
                    if (isChanged) {
                        AccountBUS.getInstance().forceLogoutAndSecurityUpdate(conn, accId,
                                obj.getStatusId() != activeId);
                    }
                }

                conn.commit();
                return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_JOB_UPDATE_SUCCESS);
            } catch (Exception e) {
                conn.rollback();
                throw e; // Đẩy ra catch bên ngoài xử lý message
            }
        } catch (Exception e) {
            System.err.println("Lỗi updateJobInfo: " + e.getMessage());
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }

    /**
     * Cập nhật TAB 4: Lương & Bảo hiểm
     * Update (EmployeeDTO): role_id, insurance flags
     * ⚠️ Caller PHẢI kiểm tra quyền: EMPLOYEE_PAYROLLINFO_UPDATE
     * 
     * @return BUSResult
     */
    public BUSResult updatePayrollInfo(EmployeeDTO employee, int numDependent) {
        // 1. Validate & Security
        if (employee == null || employee.getId() <= 0 || numDependent < 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        SessionManagerService session = SessionManagerService.getInstance();
        if (employee.getId() == 1)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SYSTEM);
        if (employee.getId() == session.employeeLoginId())
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SELF);

        EmployeeDTO target = getById(employee.getId());
        if (target == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        if (session.getRoleId() != 1) {
            boolean isTargetAdmin = PermissionBUS.getInstance().isRoleHavePermission(target.getRoleId(),
                    PermissionKey.EMPLOYEE_PAYROLLINFO_UPDATE);
            if (isTargetAdmin)
                return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_ERROR_UPDATE_SAME_AUTHORITY);
        }

        // 2. Thực thi Transaction
        try (Connection conn = ConnectApplication.getInstance().getConnectionFactory().newConnection()) {
            conn.setAutoCommit(false);
            try {
                // A. Cập nhật các cờ bảo hiểm tại Employee
                if (!EmployeeDAL.getInstance().updatePayrollInfo(conn, employee))
                    throw new Exception("EMPLOYEE_PAYROLL_FAIL");

                // B. Cập nhật số người phụ thuộc tại Tax
                TaxDTO tax = new TaxDTO();
                tax.setEmployeeId(employee.getId());
                tax.setNumDependents(numDependent);

                // Sử dụng hàm updateNumDependents có truyền Connection
                if (!TaxBUS.getInstance().updateNumDependents(conn, tax))
                    throw new Exception("TAX_UPDATE_FAIL");

                conn.commit();
                return new BUSResult(BUSOperationResult.SUCCESS,
                        AppMessages.EMPLOYEE_PAYROLL_INFO_UPDATE_STATUS_SUCCESS);
            } catch (Exception e) {
                conn.rollback();
                // Phân loại lỗi cụ thể nếu cần
                if ("TAX_UPDATE_FAIL".equals(e.getMessage())) {
                    return new BUSResult(BUSOperationResult.FAIL,
                            "Không thể cập nhật thông tin thuế. Vui lòng kiểm tra lại.");
                }
                throw e;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Payroll Update failed: " + e.getMessage());
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }

    public BUSResult updateAccountStatus(int employeeId, int accountStatusId) {
        // 1. Validate & Security
        if (employeeId <= 0 || accountStatusId <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        SessionManagerService session = SessionManagerService.getInstance();
        if (employeeId == 1)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SYSTEM);
        if (employeeId == session.employeeLoginId())
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SELF);

        EmployeeDTO target = getById(employeeId);
        if (target == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        if (session.getRoleId() != 1) {
            boolean isTargetAdmin = PermissionBUS.getInstance().isRoleHavePermission(target.getRoleId(),
                    PermissionKey.EMPLOYEE_ACCOUNT_UPDATE_STATUS);
            if (isTargetAdmin)
                return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_ERROR_UPDATE_SAME_AUTHORITY);
        }

        if (!StatusBUS.getInstance().isValidStatusIdForType(StatusType.ACCOUNT, accountStatusId)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.STATUS_IDForType_INVALID);
        }

        // 2. Kiểm tra Account có tồn tại và Status có thay đổi không
        Integer accId = target.getAccountId();
        if (accId == null || accId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        AccountDTO currentAcc = AccountBUS.getInstance().getById(accId);
        if (currentAcc == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        if (currentAcc.getStatusId() == accountStatusId) {
            return new BUSResult(BUSOperationResult.NO_CHANGES);
        }

        // 3. Execute Transaction - Cập nhật status + ép relogin
        try (Connection conn = ConnectApplication.getInstance().getConnectionFactory().newConnection()) {
            conn.setAutoCommit(false);
            try {
                // A. Cập nhật status_id thực tế vào account table
                if (!AccountBUS.getInstance().updateAccountStatus(accId, accountStatusId)) {
                    throw new Exception("UPDATE_STATUS_FAIL");
                }

                // B. Kiểm tra nếu update sang LOCKED status thì khóa luôn
                int lockedId = StatusBUS.getInstance()
                        .getByTypeAndStatusName(StatusType.ACCOUNT, Status.Account.LOCKED).getId();
                boolean isLockedStatus = (accountStatusId == lockedId);

                // C. Ép relogin + set require_relogin flag
                AccountBUS.getInstance().forceLogoutAndSecurityUpdate(conn, accId, isLockedStatus);

                conn.commit();
                return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_ACCOUNT_UPDATE_STATUS_SUCCESS);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Lỗi updateAccountStatus: " + e.getMessage());
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }

    public BUSResult resetAccountPassword(int employeeId) {
        // 1. Validate & Security
        if (employeeId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        SessionManagerService session = SessionManagerService.getInstance();
        if (employeeId == 1)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SYSTEM);
        if (employeeId == session.employeeLoginId())
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_UPDATE_SELF);

        EmployeeDTO existing = getById(employeeId);
        if (existing == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        Integer accId = existing.getAccountId();
        if (accId == null || accId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        // Kiểm tra quyền ngang hàng
        if (session.getRoleId() != 1) {
            boolean isTargetAdmin = PermissionBUS.getInstance().isRoleHavePermission(existing.getRoleId(),
                    PermissionKey.EMPLOYEE_ACCOUNT_RESET_PASSWORD);
            if (isTargetAdmin)
                return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_ERROR_UPDATE_SAME_AUTHORITY);
        }

        // 2. Execute Transaction - Reset password + ép relogin
        try (Connection conn = ConnectApplication.getInstance().getConnectionFactory().newConnection()) {
            conn.setAutoCommit(false);
            try {
                AccountDTO acc = AccountBUS.getInstance().getById(accId);
                if (acc == null)
                    throw new Exception("ACCOUNT_NOT_FOUND");

                // A. Reset password (hash + update DB)
                if (!AccountBUS.getInstance().resetPassword(acc.getUsername())) {
                    throw new Exception("RESET_PASSWORD_FAIL");
                }

                // B. Ép relogin + set require_relogin flag
                AccountBUS.getInstance().forceLogoutAndSecurityUpdate(conn, accId, false);

                conn.commit();
                return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_ACCOUNT_RESET_PASSWORD_SUCCESS);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Lỗi resetAccountPassword: " + e.getMessage());
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }

    // ==================== 4 NEW GETTER METHODS (per tab) ====================
    public BUSResult getPersonalInfo(int employeeId) {
        if (employeeId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        EmployeePersonalInfoDTO data = EmployeeDAL.getInstance().getPersonalInfo(employeeId);

        if (data == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_PERSONAL_INFO_LOAD_SUCCESS, data);
    }

    public BUSResult getAccountInfo(int employeeId) {
        if (employeeId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        EmployeeAccountInfoDTO data = EmployeeDAL.getInstance().getAccountInfo(employeeId);

        if (data == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_ACCOUNT_INFO_LOAD_SUCCESS, data);
    }

    public BUSResult getJobInfo(int employeeId) {
        if (employeeId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        EmployeeJobInfoDTO data = EmployeeDAL.getInstance().getJobInfo(employeeId);

        if (data == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_JOB_INFO_LOAD_SUCCESS, data);
    }

    public BUSResult getPayrollInfo(int employeeId) {
        if (employeeId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        EmployeePayrollInfoDTO data = EmployeeDAL.getInstance().getPayrollInfo(employeeId);

        if (data == null) {
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
        }
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_PAYROLL_INFO_LOAD_SUCCESS, data);
    }

    // ==================== BUNDLE METHODS (Composite Data) ====================

    /**
     * Lấy toàn bộ dữ liệu cho Tab 1: Hồ sơ nhân viên
     * Bundle: PersonalInfo + JobInfo + PayrollInfo (3 DTOs)
     * Giảm số lần gọi BUS từ 3 xuống 1
     * 
     * @param employeeId ID của employee
     * @return BUSResult chứa EmployeePersonalInfoBundle hoặc error
     */
    public BUSResult getPersonalInfoComplete(int employeeId) {
        if (employeeId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        try {
            EmployeePersonalInfoDTO personalInfo = EmployeeDAL.getInstance().getPersonalInfo(employeeId);
            EmployeeJobInfoDTO jobInfo = EmployeeDAL.getInstance().getJobInfo(employeeId);
            EmployeePayrollInfoDTO payrollInfo = EmployeeDAL.getInstance().getPayrollInfo(employeeId);

            if (personalInfo == null || jobInfo == null || payrollInfo == null) {
                return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
            }

            EmployeePersonalInfoBundle bundle = EmployeePersonalInfoBundle.builder()
                    .personalInfo(personalInfo)
                    .jobInfo(jobInfo)
                    .payrollInfo(payrollInfo)
                    .build();

            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_PERSONAL_INFO_LOAD_SUCCESS, bundle);
        } catch (Exception e) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }

    /**
     * Lấy dữ liệu cho Tab 2: Lương & Công tác
     * Bundle: JobInfo + PayrollInfo (2 DTOs)
     * Giảm số lần gọi BUS từ 2 xuống 1
     * 
     * @param employeeId ID của employee
     * @return BUSResult chứa EmployeeJobHistoryBundle hoặc error
     */
    public BUSResult getJobAndPayrollInfo(int employeeId) {
        if (employeeId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        try {
            EmployeeJobInfoDTO jobInfo = EmployeeDAL.getInstance().getJobInfo(employeeId);
            EmployeePayrollInfoDTO payrollInfo = EmployeeDAL.getInstance().getPayrollInfo(employeeId);

            if (jobInfo == null || payrollInfo == null) {
                return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);
            }

            EmployeeJobHistoryBundle bundle = EmployeeJobHistoryBundle.builder()
                    .jobInfo(jobInfo)
                    .payrollInfo(payrollInfo)
                    .build();

            return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_JOB_INFO_LOAD_SUCCESS, bundle);
        } catch (Exception e) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
    }

    public int countByRoleId(int roleId) {
        return EmployeeDAL.getInstance().countByRoleId(roleId);
    }

    /**
     * Get EmployeeDetailDTO - tất cả thông tin cần thiết với 1 query
     * Thay thế cho EmployeeViewProvider
     * 
     * @param employeeId ID của employee
     * @return EmployeeDetailDTO hoặc null nếu không tìm thấy
     */
    public EmployeeDetailDTO getDetailById(int employeeId) {
        if (employeeId <= 0)
            return null;
        return EmployeeDAL.getInstance().getDetailById(employeeId);
    }
}