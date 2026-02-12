
package BUS;

import DAL.EmployeeDAL;
import DTO.EmployeeDTO;
import DTO.BUSResult;
import ENUM.*;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.*;
import java.util.stream.Collectors;

public class EmployeeBUS extends BaseBUS<EmployeeDTO, Integer> {
    private static final EmployeeBUS INSTANCE = new EmployeeBUS();
    private final HashMap<Integer, EmployeeDTO> mapByAccountId = new HashMap<>();

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

    // Không lấy employeeId = 1 (system employee)
    @Override
    public ArrayList<EmployeeDTO> getAllLocal() {
        return arrLocal.stream()
                .filter(emp -> emp.getId() != 1)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void loadLocal() {
        super.loadLocal();
        mapByAccountId.clear();
        for (EmployeeDTO emp : arrLocal) {
            if (emp.getAccountId() != null && emp.getAccountId() > 0) {
                mapByAccountId.put(emp.getAccountId(), emp);
            }
        }
    }

    public EmployeeDTO getByAccountIdLocal(int id) {
        if (id <= 0)
            return null;
        EmployeeDTO emp = mapByAccountId.get(id);
        return emp != null ? new EmployeeDTO(emp) : null;
    }

    /**
     * Xóa nhân viên
     * ⚠️ Caller PHẢI kiểm tra quyền trước bằng AuthorizationService hoặc
     * SessionManager
     * 
     * @param id              ID của nhân viên cần xóa
     * @param employee_roleId Chức vụ của người thực hiện
     * @param employeeLoginId ID của người đăng nhập thực hiện xóa
     * @return BUSResult
     */
    public BUSResult delete(Integer id, int employee_roleId, int employeeLoginId) {
        if (id == null || id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);

        if (employeeLoginId == id)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_DELETE_SELF);

        if (id == 1)
            return new BUSResult(BUSOperationResult.FAIL, AppMessages.EMPLOYEE_CANNOT_DELETE_SYSTEM);

        if (employee_roleId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);

        EmployeeDTO targetEmployee = getByIdLocal(id);
        if (targetEmployee == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        if (!EmployeeDAL.getInstance().delete(id))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        for (EmployeeDTO employee : arrLocal) {
            if (Objects.equals(employee.getId(), id)) {
                // employee.setStatus(false);
                break;
            }
        }
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
            return new BUSResult(BUSOperationResult.INVALID_DATA);
        }

        if (!EmployeeDAL.getInstance().insert(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR);
        }
        arrLocal.add(new EmployeeDTO(obj));
        return new BUSResult(BUSOperationResult.SUCCESS);
    }

    // Cập nhật cache local (arrLocal, mapLocal, mapByAccountId)
    private void updateLocalCache(EmployeeDTO obj) {
        EmployeeDTO newEmp = new EmployeeDTO(obj);

        // Update primary map
        mapLocal.put(obj.getId(), newEmp);

        // Update secondary index
        if (obj.getAccountId() != null) {
            mapByAccountId.put(obj.getAccountId(), newEmp);
        }

        // Update arrLocal nếu cần giữ list
        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), obj.getId())) {
                arrLocal.set(i, newEmp);
                break;
            }
        }
    }

    private boolean isValidEmployeeInput(EmployeeDTO obj) {
        if (obj.getFirstName() == null || obj.getLastName() == null) {
            return false;
        }

        if (!RoleBUS.getInstance().isValidRole(obj.getRoleId())) {
            return false;
        }

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

        for (EmployeeDTO emp : arrLocal) {
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

    public int numEmployeeHasRoleId(int roleId) {
        if (roleId <= 0)
            return 0;

        int num = 0; // Khởi tạo biến đếm
        for (EmployeeDTO e : arrLocal) {
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

        EmployeeDTO existing = getByIdLocal(obj.getId());
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

        updateLocalCache(obj);
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

        EmployeeDTO existing = getByIdLocal(obj.getId());
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

        updateLocalCache(obj);
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

        EmployeeDTO existing = getByIdLocal(obj.getId());
        if (existing != null &&
                Objects.equals(existing.getDepartmentId(), obj.getDepartmentId()) &&
                existing.getStatusId() == obj.getStatusId()) {
            return new BUSResult(BUSOperationResult.NO_CHANGES);
        }

        if (!EmployeeDAL.getInstance().updateJobPosition(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR);
        }

        updateLocalCache(obj);
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

        if (obj.getRoleId() <= 0 || !RoleBUS.getInstance().isValidRole(obj.getRoleId())) {
            return new BUSResult(BUSOperationResult.INVALID_DATA);
        }

        EmployeeDTO existing = getByIdLocal(obj.getId());
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

        updateLocalCache(obj);
        return new BUSResult(BUSOperationResult.SUCCESS);
    }

    /**
     * Cập nhật TAB 4: Tài khoản hệ thống
     * Update: accountId
     * ⚠️ Caller PHẢI kiểm tra quyền: chỉ role 1 mới được phép
     * 
     * @return BUSResult
     */
    public BUSResult updateSystemAccount(EmployeeDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getId() <= 0 || employee_roleId <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        if (obj.getId() == 1 && employeeLoginId != 1) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS);
        }

        EmployeeDTO existing = getByIdLocal(obj.getId());
        if (existing != null && Objects.equals(existing.getAccountId(), obj.getAccountId())) {
            return new BUSResult(BUSOperationResult.NO_CHANGES);
        }

        if (!EmployeeDAL.getInstance().updateSystemAccount(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR);
        }

        updateLocalCache(obj);
        return new BUSResult(BUSOperationResult.SUCCESS);
    }

    private boolean isDuplicateEmployee(EmployeeDTO obj) {
        EmployeeDTO existingEm = getByIdLocal(obj.getId());
        ValidationUtils validate = ValidationUtils.getInstance();
        // Kiểm tra xem tên, mô tả, và hệ số lương có trùng không
        return existingEm != null &&
                Objects.equals(existingEm.getFirstName(), validate.normalizeWhiteSpace(obj.getFirstName())) &&
                Objects.equals(existingEm.getLastName(), validate.normalizeWhiteSpace(obj.getLastName())) &&
                Objects.equals(existingEm.getDateOfBirth(), obj.getDateOfBirth()) &&
                Objects.equals(existingEm.getStatusId(), obj.getStatusId()) &&
                Objects.equals(existingEm.getRoleId(), obj.getRoleId());
    }
}