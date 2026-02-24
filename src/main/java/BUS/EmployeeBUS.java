
package BUS;

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
import ENUM.*;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
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

    public EmployeeDTO getByAccountIdLocal(int id) {
        if (id <= 0)
            return null;
        return EmployeeDAL.getInstance().getByAccountId(id);
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

    /**
     * Thêm nhân viên mới
     * ⚠️ Caller PHẢI kiểm tra quyền trước bằng AuthorizationService hoặc
     * SessionManager
     * 
     * @param obj             Dữ liệu nhân viên cần thêm
     * @param employee_roleId Chức vụ của người thực hiện
     * @param employeeLoginId ID của người đăng nhập thực hiện
     * @return BUSResult
     */
    public BUSResult insert(EmployeeDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getRoleId() <= 0 || employee_roleId <= 0 || !isValidEmployeeInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        if (!EmployeeDAL.getInstance().insert(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }
        // [STATELESS] No cache update needed - data will refresh from DB on next
        // getAll()
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_ADD_SUCCESS);
    }

    private boolean isValidEmployeeInput(EmployeeDTO obj) {
        if (obj.getFirstName() == null || obj.getLastName() == null) {
            return false;
        }

        // if (!RoleBUS.getInstance().isValidRole(obj.getRoleId())) {
        // return false;
        // }

        obj.setDateOfBirth(obj.getDateOfBirth() != null ? obj.getDateOfBirth() : null);

        ValidationUtils validator = ValidationUtils.getInstance();
        if (obj.getDateOfBirth() != null && !validator.validateDateOfBirth(obj.getDateOfBirth())) {
            return false;
        }
        return validator.validateVietnameseText100(obj.getFirstName()) &&
                validator.validateVietnameseText100(obj.getLastName());
    }

    public ArrayList<EmployeeDTO> filterEmployees(String searchBy, String keyword, int roleIdFilter, int statusFilter) {
        ArrayList<EmployeeDTO> filteredList = new ArrayList<>();

        if (keyword == null)
            keyword = "";
        if (searchBy == null)
            searchBy = "";

        keyword = keyword.trim().toLowerCase();

        // [STATELESS] Use getAll() from DAL instead of arrLocal
        for (EmployeeDTO emp : getAll()) {
            boolean matchesSearch = true;
            boolean matchesRole = (roleIdFilter == -1) || (emp.getRoleId() == roleIdFilter);
            boolean matchesStatus = (statusFilter == -1) || (emp.getStatusId() == statusFilter); // Sửa lỗi ở đây

            // Kiểm tra null tránh lỗi khi gọi .toLowerCase()
            String firstName = emp.getFirstName() != null ? emp.getFirstName().toLowerCase() : "";
            String lastName = emp.getLastName() != null ? emp.getLastName().toLowerCase() : "";
            String employeeId = String.valueOf(emp.getId());

            if (!keyword.isEmpty()) {
                switch (searchBy) {
                    case "Mã nhân viên" -> matchesSearch = employeeId.contains(keyword);
                    case "Họ đệm" -> matchesSearch = firstName.contains(keyword);
                    case "Tên" -> matchesSearch = lastName.contains(keyword);
                }
            }

            if (matchesSearch && matchesRole && matchesStatus) {
                filteredList.add(emp);
            }
        }

        return filteredList;
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

    public int numEmployeeHasRoleId(int roleId) {
        if (roleId <= 0)
            return 0;

        int num = 0; // Khởi tạo biến đếm
        // [STATELESS] Use getAll() from DAL instead of arrLocal
        for (EmployeeDTO e : getAll()) {
            if (e.getRoleId() == roleId) {
                num++;
            }
        }
        return num;
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

    private boolean isDuplicateEmployee(EmployeeDTO obj) {
        EmployeeDTO existingEm = getById(obj.getId());
        ValidationUtils validate = ValidationUtils.getInstance();
        // Kiểm tra xem tên, mô tả, và hệ số lương có trùng không
        return existingEm != null &&
                Objects.equals(existingEm.getFirstName(), validate.normalizeWhiteSpace(obj.getFirstName())) &&
                Objects.equals(existingEm.getLastName(), validate.normalizeWhiteSpace(obj.getLastName())) &&
                Objects.equals(existingEm.getDateOfBirth(), obj.getDateOfBirth()) &&
                Objects.equals(existingEm.getStatusId(), obj.getStatusId()) &&
                Objects.equals(existingEm.getRoleId(), obj.getRoleId());
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
        if (employeeId <= 0) {
            log.debug("Invalid employeeId: {}", employeeId);
            return null;
        }
        log.debug("Fetching employee detail for ID: {}", employeeId);
        EmployeeDetailDTO result = EmployeeDAL.getInstance().getDetailById(employeeId);
        log.debug("Employee detail result: {}", result);
        return result;
    }
}