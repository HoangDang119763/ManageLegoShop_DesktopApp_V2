
package BUS;

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
import SERVICE.SessionManagerService;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import javax.management.relation.Role;

import com.mysql.cj.Session;

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

    public BUSResult delete(Integer id, int employee_roleId, int employeeLoginId) {
        if (id == null || id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);

        if (employeeLoginId == id)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_DELETE_SELF);

        if (id == 1)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_DELETE_SYSTEM);

        if (employee_roleId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);

        // [STATELESS] Use DAL to check existence
        EmployeeDTO targetEmployee = getById(id);
        if (targetEmployee == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        if (!EmployeeDAL.getInstance().delete(id))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        // [STATELESS] No cache update needed - data will refresh from DB on next
        // getAll()
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

        // 4. Kiểm tra Email (CHỈ KIỂM TRA NẾU CÓ NHẬP)
        String email = obj.getEmail();
        if (email != null && !email.trim().isEmpty()) {
            if (!validator.validateEmail(email)) {
                return false;
            }
        }

        // 5. Kiểm tra Ngày sinh (CHỈ KIỂM TRA NẾU ĐÃ CÓ)
        if (obj.getDateOfBirth() != null) {
            if (!validator.validateDateOfBirth(obj.getDateOfBirth())) {
                return false;
            }
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

        // Gọi DAL với JOIN để lấy dữ liệu hoàn chỉnh
        PagedResponse<EmployeeDisplayDTO> pagedData = EmployeeDAL.getInstance()
                .filterEmployeesPagedForManageDisplay(cleanKeyword, finalRoleId, finalStatusId, finalPageIndex,
                        finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    /**
     * Cập nhật TAB 1: Thông tin cá nhân (tự sửa)
     * Update: firstName, lastName, phone, email, dateOfBirth (KHÔNG thay đổi
     * gender)
     * ⚠️ Caller PHẢI kiểm tra quyền trước: người tự sửa
     * 
     * @return BUSResult
     */
    public BUSResult updatePersonalInfoBySelf(EmployeeDTO obj) {
        if (obj == null || obj.getId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        if (obj.getFirstName() == null || obj.getLastName() == null ||
                obj.getDateOfBirth() == null || obj.getFirstName().trim().isEmpty() ||
                obj.getPhone() == null || obj.getEmail() == null) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        ValidationUtils validator = ValidationUtils.getInstance();
        if (!validator.validateDateOfBirth(obj.getDateOfBirth())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        if (!validator.validateVietnameseText100(obj.getFirstName()) ||
                !validator.validateVietnameseText100(obj.getLastName())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        if (!obj.getPhone().isEmpty() &&
                !validator.validateVietnamesePhoneNumber(obj.getPhone())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        if (!obj.getEmail().isEmpty() &&
                !validator.validateEmail(obj.getEmail())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        // [STATELESS] Fetch fresh data from DB
        EmployeeDTO existing = getById(obj.getId());
        if (existing != null &&
                Objects.equals(existing.getFirstName(), validator.normalizeWhiteSpace(obj.getFirstName())) &&
                Objects.equals(existing.getLastName(), validator.normalizeWhiteSpace(obj.getLastName())) &&
                Objects.equals(existing.getPhone(), obj.getPhone()) &&
                Objects.equals(existing.getEmail(), obj.getEmail()) &&
                Objects.equals(existing.getDateOfBirth(), obj.getDateOfBirth())) {
            return new BUSResult(BUSOperationResult.NO_CHANGES, AppMessages.EMPLOYEE_PERSONAL_UPDATE_SUCCESS);
        }

        if (!EmployeeDAL.getInstance().updatePersonalInfoBySelf(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_PERSONAL_UPDATE_SUCCESS);
    }

    /**
     * Cập nhật TAB 1: Thông tin cá nhân (quản trị viên)
     * Update: firstName, lastName, phone, email, dateOfBirth, gender
     * ⚠️ Caller PHẢI kiểm tra quyền trước: có quyền EMPLOYEE_PERSONAL_UPDATE
     * 
     * @return BUSResult
     */
    public BUSResult updatePersonalInfoByAdmin(EmployeeDTO obj) {
        if (obj == null || obj.getId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        if (obj.getId() == 1) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        if (obj.getFirstName() == null || obj.getLastName() == null ||
                obj.getDateOfBirth() == null || obj.getFirstName().trim().isEmpty() ||
                obj.getPhone() == null || obj.getEmail() == null ||
                obj.getGender() == null) {
            return new BUSResult(BUSOperationResult.INVALID_DATA);
        }

        ValidationUtils validator = ValidationUtils.getInstance();
        if (!validator.validateDateOfBirth(obj.getDateOfBirth())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA);
        }

        if (!validator.validateVietnameseText100(obj.getFirstName()) ||
                !validator.validateVietnameseText100(obj.getLastName())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA);
        }

        if (!obj.getPhone().isEmpty() &&
                !validator.validateVietnamesePhoneNumber(obj.getPhone())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA);
        }

        if (!obj.getEmail().isEmpty() &&
                !validator.validateEmail(obj.getEmail())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA);
        }

        if (!obj.getGender().equals("Nam") && !obj.getGender().equals("Nữ")
                && !obj.getGender().equals("Khác")) {
            return new BUSResult(BUSOperationResult.INVALID_DATA);
        }

        // [STATELESS] Fetch fresh data from DB
        EmployeeDTO existing = getById(obj.getId());
        if (existing != null &&
                Objects.equals(existing.getFirstName(), validator.normalizeWhiteSpace(obj.getFirstName())) &&
                Objects.equals(existing.getLastName(), validator.normalizeWhiteSpace(obj.getLastName())) &&
                Objects.equals(existing.getPhone(), obj.getPhone()) &&
                Objects.equals(existing.getEmail(), obj.getEmail()) &&
                Objects.equals(existing.getDateOfBirth(), obj.getDateOfBirth()) &&
                Objects.equals(existing.getGender(), obj.getGender())) {
            return new BUSResult(BUSOperationResult.NO_CHANGES);
        }

        if (!EmployeeDAL.getInstance().updatePersonalInfoByAdmin(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS);
    }

    /**
     * Cập nhật TAB 2: Vị trí công tác
     * Update: departmentId, statusId
     * ⚠️ Caller PHẢI kiểm tra quyền: chỉ role 1 mới được phép
     * 
     * @return BUSResult
     */
    public BUSResult updateJobPosition(EmployeeDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getId() <= 0 || employee_roleId <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        if (obj.getId() == 1 && employeeLoginId != 1) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        // [STATELESS] Fetch fresh data from DB
        EmployeeDTO existing = getById(obj.getId());
        if (existing != null &&
                Objects.equals(existing.getDepartmentId(), obj.getDepartmentId()) &&
                existing.getStatusId() == obj.getStatusId()) {
            return new BUSResult(BUSOperationResult.NO_CHANGES);
        }

        if (!EmployeeDAL.getInstance().updateJobPosition(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS);
    }

    /**
     * Cập nhật TAB 3: Lương & Bảo hiểm
     * Update (EmployeeDTO): role_id, insurance flags
     * ⚠️ Caller PHẢI kiểm tra quyền: chỉ role 1 mới được phép
     * 
     * @return BUSResult
     */
    public BUSResult updatePayrollInfo(EmployeeDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getId() <= 0 || employee_roleId <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        if (obj.getId() == 1 && employeeLoginId != 1) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        // if (obj.getRoleId() <= 0 ||
        // !RoleBUS.getInstance().isValidRole(obj.getRoleId())) {
        // return new BUSResult(BUSOperationResult.INVALID_DATA);
        // }

        // [STATELESS] Fetch fresh data from DB
        EmployeeDTO existing = getById(obj.getId());
        if (existing != null &&
                existing.getRoleId() == obj.getRoleId() &&
                existing.isSocialInsurance() == obj.isSocialInsurance() &&
                existing.isUnemploymentInsurance() == obj.isUnemploymentInsurance() &&
                existing.isPersonalIncomeTax() == obj.isPersonalIncomeTax() &&
                existing.isTransportationSupport() == obj.isTransportationSupport() &&
                existing.isAccommodationSupport() == obj.isAccommodationSupport()) {
            return new BUSResult(BUSOperationResult.NO_CHANGES);
        }

        if (!EmployeeDAL.getInstance().updatePayrollInfo(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS);
    }

    public BUSResult updateSystemAccount(EmployeeDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getId() <= 0 || employee_roleId <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        if (obj.getId() == 1 && employeeLoginId != 1) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        // [STATELESS] Fetch fresh data from DB
        EmployeeDTO existing = getById(obj.getId());
        if (existing != null && Objects.equals(existing.getAccountId(), obj.getAccountId())) {
            return new BUSResult(BUSOperationResult.NO_CHANGES);
        }

        if (!EmployeeDAL.getInstance().updateSystemAccount(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS);
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