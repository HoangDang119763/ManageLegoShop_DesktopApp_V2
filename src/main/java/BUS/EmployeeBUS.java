
package BUS;

import DAL.EmployeeDAL;
import DTO.EmployeeDTO;
import DTO.ProductDTO;
import SERVICE.AuthorizationService;
// import SERVICE.ExcelService;
import UTILS.AvailableUtils;
import UTILS.ValidationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

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

    public EmployeeDTO getByIdLocal(int id) {
        if (id <= 0)
            return null;
        for (EmployeeDTO employee : arrLocal) {
            if (Objects.equals(employee.getId(), id)) {
                return new EmployeeDTO(employee);
            }
        }
        return null;
    }

    public EmployeeDTO getByAccountIdLocal(int id) {
        if (id <= 0)
            return null;
        for (EmployeeDTO employee : arrLocal) {
            if (Objects.equals(employee.getAccountId(), id)) {
                return new EmployeeDTO(employee);
            }
        }
        return null;
    }

    public int delete(Integer id, int employee_roleId, int employeeLoginId) {
        if (id == null || id <= 0)
            return 2;

        // Ngăn chặn tự xóa thông tin employee của chính mình
        if (employeeLoginId == id)
            return 3;

        // Ngăn chặn xóa thông tin nhân viên gốc (id = 1) để bảo vệ hệ thống
        if (id == 1) {
            // System.out.println("Không thể xóa nhân viên gốc (employeeId = 1)!");
            return 8;
        }

        // Nếu người thực hiện không có quyền 2, tự chối luôn
        if (employee_roleId <= 0
                || !AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 2))
            return 4;

        // Nếu employee đang bị xóa có quyền 2, chỉ cho phép role 1 xóa nó
        EmployeeDTO targetEmployee = getByIdLocal(id);
        if (targetEmployee == null)
            return 7;
        if (AuthorizationService.getInstance().hasPermission(targetEmployee.getId(), targetEmployee.getRoleId(), 2)
                && employee_roleId != 1)
            return 5;
        if (!EmployeeDAL.getInstance().delete(id)) {
            return 6;
        }
        for (EmployeeDTO employee : arrLocal) {
            if (Objects.equals(employee.getId(), id)) {
                employee.setStatus(false);
                break;
            }
        }
        return 1;
    }

    public int insert(EmployeeDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getRoleId() <= 0 || employee_roleId <= 0 || !isValidEmployeeInput(obj)) {
            return 2;
        }

        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 1))
            return 3;

        if (!EmployeeDAL.getInstance().insert(obj)) {
            return 4;
        }
        arrLocal.add(new EmployeeDTO(obj));
        return 1;
    }

    public int update(EmployeeDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getId() <= 0 || employee_roleId <= 0)
            return 2;

        // Không có quyền 3 thì không chỉnh chính mình hay người khác
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 3)) {
            return 3;
        }

        // Ngăn chặn cập nhật nhân viên gốc nếu không phải chính nó
        if (obj.getId() == 1 && employeeLoginId != 1) {
            // System.out.println("Không thể cập nhật nhân viên gốc (employeeId = 1)!");
            return 5;
        }

        // Kiểm tra đang cập nhật chính mình hay người khác
        boolean isSelfUpdate = (employeeLoginId == obj.getId());

        // Role 1: có quyền cao nhất nhưng vẫn không tự sửa role_id & status
        if (employee_roleId == 1) {
            boolean canUpdateAdvanced = !isSelfUpdate; // Chỉ cập nhật full nếu không phải chính mình
            if (isInvalidEmployeeUpdate(obj, canUpdateAdvanced, true)) {
                return 4;
            }
            if (isDuplicateEmployee(obj))
                return 1;
            if (!EmployeeDAL.getInstance().updateAdvance(obj, canUpdateAdvanced))
                return 7;
            updateLocalCache(obj);
            return 1;
        }

        // Các role khác
        if (isSelfUpdate) {
            // Chỉ có thể cập nhật basic của chính mình
            if (isInvalidEmployeeUpdate(obj, false, false))
                return 4;
            if (isDuplicateEmployee(obj))
                return 1;
            if (!EmployeeDAL.getInstance().updateBasic(obj, false))
                return 7;
        } else {
            // Nếu cập nhật người khác, chỉ được phép nếu người đó có quyền thấp hơn
            if (AuthorizationService.getInstance().hasPermission(obj.getId(), getByIdLocal(obj.getId()).getRoleId(), 3))
                return 6;
            if (isInvalidEmployeeUpdate(obj, true, false))
                return 4;
            if (isDuplicateEmployee(obj))
                return 1;
            if (!EmployeeDAL.getInstance().updateBasic(obj, true))
                return 7;
        }

        updateLocalCache(obj);
        return 1;
    }

    // Cập nhật cache local
    private void updateLocalCache(EmployeeDTO obj) {
        for (int i = 0; i < arrLocal.size(); i++) {
            if (Objects.equals(arrLocal.get(i).getId(), obj.getId())) {
                arrLocal.set(i, new EmployeeDTO(obj));
                break;
            }
        }
    }

    private boolean isValidEmployeeInput(EmployeeDTO obj) {
        if (obj.getFirstName() == null || obj.getLastName() == null) {
            return false;
        }

        if (!AvailableUtils.getInstance().isValidRole(obj.getRoleId())) {
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

    private boolean isInvalidEmployeeUpdate(EmployeeDTO obj, boolean allowAdvanceChange, boolean isAdvance) {
        if (obj.getFirstName() == null || obj.getLastName() == null) {
            return true;
        }

        if (allowAdvanceChange && obj.getRoleId() <= 0)
            return true;
        if (!AvailableUtils.getInstance().isValidRole(obj.getRoleId())) {
            return true;
        }

        obj.setDateOfBirth(obj.getDateOfBirth() != null ? obj.getDateOfBirth() : null);

        ValidationUtils validator = ValidationUtils.getInstance();
        if (obj.getDateOfBirth() != null && !validator.validateDateOfBirth(obj.getDateOfBirth())) {
            return true;
        }

        return !validator.validateVietnameseText100(obj.getFirstName()) ||
                !validator.validateVietnameseText100(obj.getLastName());
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
            boolean matchesStatus = (statusFilter == -1) || (emp.isStatus() == (statusFilter == 1)); // Sửa lỗi ở đây

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

    private boolean isDuplicateEmployee(EmployeeDTO obj) {
        EmployeeDTO existingEm = getByIdLocal(obj.getId());
        ValidationUtils validate = ValidationUtils.getInstance();
        // Kiểm tra xem tên, mô tả, và hệ số lương có trùng không
        return existingEm != null &&
                Objects.equals(existingEm.getFirstName(), validate.normalizeWhiteSpace(obj.getFirstName())) &&
                Objects.equals(existingEm.getLastName(), validate.normalizeWhiteSpace(obj.getLastName())) &&
                Objects.equals(existingEm.getDateOfBirth(), obj.getDateOfBirth()) &&
                Objects.equals(existingEm.isStatus(), obj.isStatus()) &&
                Objects.equals(existingEm.getRoleId(), obj.getRoleId());
    }
}