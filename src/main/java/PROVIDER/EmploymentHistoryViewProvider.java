package PROVIDER;

import BUS.DepartmentBUS;
import BUS.EmploymentHistoryBUS;
import BUS.RoleBUS;
import DTO.DepartmentDTO;
import DTO.EmploymentHistoryDTO;
import DTO.EmploymentHistoryDetailDTO;
import DTO.RoleDTO;
import java.util.ArrayList;

/**
 * Provider để transform EmploymentHistoryDTO sang EmploymentHistoryDetailDTO
 * Thêm thông tin department name và role name từ lookup
 */
public class EmploymentHistoryViewProvider {
    private static final EmploymentHistoryViewProvider INSTANCE = new EmploymentHistoryViewProvider();

    private EmploymentHistoryViewProvider() {
    }

    public static EmploymentHistoryViewProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Get job history cho employee với thông tin chi tiết
     */
    public ArrayList<EmploymentHistoryDetailDTO> getJobHistoryByEmployeeIdDecrease(int employeeId) {
        EmploymentHistoryBUS historyBUS = EmploymentHistoryBUS.getInstance();
        DepartmentBUS departmentBUS = DepartmentBUS.getInstance();
        RoleBUS roleBUS = RoleBUS.getInstance();

        // Get job history for employee
        // ArrayList<EmploymentHistoryDTO> histories =
        // historyBUS.getByEmployeeIdDecrease(employeeId);
        ArrayList<EmploymentHistoryDetailDTO> details = new ArrayList<>();

        // for (EmploymentHistoryDTO history : histories) {
        // DepartmentDTO dept = departmentBUS.getById(history.getDepartmentId());
        // RoleDTO role = roleBUS.getById(history.getRoleId());

        // details.add(new EmploymentHistoryDetailDTO(
        // history.getId(),
        // history.getEmployeeId(),
        // history.getEffectiveDate(),
        // history.getDepartmentId(),
        // dept != null ? dept.getName() : "",
        // history.getRoleId(),
        // role != null ? role.getName() : "",
        // history.getReason(),
        // history.getCreatedAt(),
        // history.getApproverId()));
        // }

        return details;
    }
}
