package PROVIDER;

import BUS.RoleBUS;
import BUS.SalaryBUS;
import BUS.StatusBUS;
import DTO.EmployeeDTO;
import DTO.EmployeeTableDTO;
import DTO.RoleDTO;
import DTO.SalaryDTO;
import DTO.StatusDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provider để transform dữ liệu Employee sang dạng hiển thị trong Table
 * Join dữ liệu từ Employee (BUS), Role (BUS), Salary (BUS), Status (BUS)
 * Không cần tạo thêm BUS/DAL mới
 */
public class EmployeeViewProvider {
    private static final EmployeeViewProvider INSTANCE = new EmployeeViewProvider();

    private EmployeeViewProvider() {
    }

    public static EmployeeViewProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Transform danh sách EmployeeDTO thành EmployeeTableDTO
     * Join với Role, Salary, Status để lấy đầy đủ thông tin hiển thị
     */
    public ArrayList<EmployeeTableDTO> toTableDTOs(List<EmployeeDTO> employees) {
        RoleBUS roleBUS = RoleBUS.getInstance();
        SalaryBUS salaryBUS = SalaryBUS.getInstance();
        StatusBUS statusBUS = StatusBUS.getInstance();

        return employees.stream()
                .map(emp -> transformToTableDTO(emp, roleBUS, salaryBUS, statusBUS))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Transform 1 EmployeeDTO thành EmployeeTableDTO
     */
    private EmployeeTableDTO transformToTableDTO(EmployeeDTO emp, RoleBUS roleBUS,
            SalaryBUS salaryBUS, StatusBUS statusBUS) {
        // Lấy thông tin Role
        RoleDTO role = roleBUS.getByIdLocal(emp.getRoleId());
        String roleName = role != null ? role.getName() : "";

        // Lấy thông tin Salary từ Role
        SalaryDTO salary = null;
        if (role != null && role.getSalaryId() != null) {
            salary = salaryBUS.getByIdLocal(role.getSalaryId());
        }

        // Lấy thông tin Status
        StatusDTO status = statusBUS.getByIdLocal(emp.getStatusId());
        String statusDescription = status != null ? status.getDescription() : "";

        // Build fullName
        String fullName = (emp.getFirstName() != null ? emp.getFirstName() : "") + " " +
                (emp.getLastName() != null ? emp.getLastName() : "");

        return new EmployeeTableDTO(
                emp.getId(),
                fullName.trim(),
                roleName,
                salary != null ? salary.getBase() : null,
                salary != null ? salary.getCoefficient() : null,
                emp.getPhone(),
                emp.getEmail(),
                emp.getGender(),
                statusDescription);
    }

    /**
     * Lọc theo keyword (tên, SDT, email)
     */
    public ArrayList<EmployeeTableDTO> filterByKeyword(ArrayList<EmployeeTableDTO> data, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return data;
        }

        String searchKey = keyword.toLowerCase().trim();
        return data.stream()
                .filter(emp -> emp.getFullName().toLowerCase().contains(searchKey) ||
                        (emp.getPhone() != null && emp.getPhone().toLowerCase().contains(searchKey)) ||
                        (emp.getEmail() != null && emp.getEmail().toLowerCase().contains(searchKey)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Lọc theo mã nhân viên
     */
    public ArrayList<EmployeeTableDTO> filterByEmployeeId(ArrayList<EmployeeTableDTO> data, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return data;
        }

        return data.stream()
                .filter(emp -> String.valueOf(emp.getEmployeeId()).contains(keyword))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Lọc theo Role
     */
    public ArrayList<EmployeeTableDTO> filterByRole(ArrayList<EmployeeTableDTO> data, String roleName) {
        if (roleName == null || roleName.isEmpty() || roleName.equals("Tất cả")) {
            return data;
        }

        return data.stream()
                .filter(emp -> emp.getRoleName().equals(roleName))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Lọc theo Status
     */
    public ArrayList<EmployeeTableDTO> filterByStatus(ArrayList<EmployeeTableDTO> data, String statusDesc) {
        if (statusDesc == null || statusDesc.isEmpty() || statusDesc.startsWith("Tất cả")) {
            return data;
        }

        return data.stream()
                .filter(emp -> emp.getStatusDescription().equals(statusDesc))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Apply tất cả filters
     */
    public ArrayList<EmployeeTableDTO> applyAllFilters(List<EmployeeDTO> employees,
            String searchBy, String keyword, String roleName, String statusDesc) {

        // Transform to table DTOs
        ArrayList<EmployeeTableDTO> tableData = toTableDTOs(employees);

        // Apply filters
        if (!keyword.isEmpty()) {
            tableData = switch (searchBy) {
                case "Mã nhân viên" -> filterByEmployeeId(tableData, keyword);
                case "Họ tên" -> filterByKeyword(tableData, keyword);
                case "SDT" -> filterBySDT(tableData, keyword);
                case "Email" -> filterByEmail(tableData, keyword);
                default -> tableData;
            };
        }

        tableData = filterByRole(tableData, roleName);
        tableData = filterByStatus(tableData, statusDesc);

        return tableData;
    }

    /**
     * Lọc theo SDT
     */
    private ArrayList<EmployeeTableDTO> filterBySDT(ArrayList<EmployeeTableDTO> data, String keyword) {
        return data.stream()
                .filter(emp -> emp.getPhone() != null && emp.getPhone().contains(keyword))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Lọc theo Email
     */
    private ArrayList<EmployeeTableDTO> filterByEmail(ArrayList<EmployeeTableDTO> data, String keyword) {
        return data.stream()
                .filter(emp -> emp.getEmail() != null && emp.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
