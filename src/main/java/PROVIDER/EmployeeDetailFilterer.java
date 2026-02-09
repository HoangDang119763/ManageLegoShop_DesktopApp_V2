package PROVIDER;

import DTO.EmployeeDetailDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter logic cho EmployeeDetailDTO
 * Single Responsibility: Chỉ xử lý filter, không xử lý transformation
 * Performance: Dùng Map O(n) thay vì Stream O(n*m)
 */
public class EmployeeDetailFilterer {
    private static final EmployeeDetailFilterer INSTANCE = new EmployeeDetailFilterer();

    private EmployeeDetailFilterer() {
    }

    public static EmployeeDetailFilterer getInstance() {
        return INSTANCE;
    }

    /**
     * Apply tất cả filters
     * 
     * @param data       Input data to filter
     * @param searchBy   Type of search (Mã nhân viên, Họ tên, Tài khoản, Email)
     * @param keyword    Search keyword
     * @param roleName   Role filter (empty = all)
     * @param statusDesc Status filter (empty = all)
     * @return Filtered data
     */
    public ArrayList<EmployeeDetailDTO> applyAllFilters(ArrayList<EmployeeDetailDTO> data,
            String searchBy, String keyword, String roleName, String statusDesc) {

        // Stage 1: Filter by keyword (nếu có)
        ArrayList<EmployeeDetailDTO> result = data;
        if (keyword != null && !keyword.isEmpty()) {
            result = filterByKeyword(result, searchBy, keyword);
        }

        // Stage 2: Filter by role (nếu có)
        if (roleName != null && !roleName.isEmpty() && !roleName.equals("Tất cả")) {
            result = filterByRole(result, roleName);
        }

        // Stage 3: Filter by status (nếu có)
        if (statusDesc != null && !statusDesc.isEmpty() && !statusDesc.startsWith("Tất cả")) {
            result = filterByStatus(result, statusDesc);
        }

        return result;
    }

    /**
     * Lọc theo keyword dựa trên searchBy type
     * O(n) - single pass through data
     */
    private ArrayList<EmployeeDetailDTO> filterByKeyword(ArrayList<EmployeeDetailDTO> data,
            String searchBy, String keyword) {
        String searchKey = keyword.toLowerCase().trim();
        ArrayList<EmployeeDetailDTO> result = new ArrayList<>();

        for (EmployeeDetailDTO emp : data) {
            boolean matches = switch (searchBy) {
                case "Mã nhân viên" -> String.valueOf(emp.getEmployeeId()).contains(searchKey);
                case "Họ tên" -> emp.getFullName().toLowerCase().contains(searchKey);
                case "Tài khoản" -> emp.getUsername() != null
                        && emp.getUsername().toLowerCase().contains(searchKey);
                case "Email" -> emp.getEmail() != null && emp.getEmail().toLowerCase().contains(searchKey);
                default -> false;
            };

            if (matches) {
                result.add(emp);
            }
        }

        return result;
    }

    /**
     * Lọc theo Role
     * Sử dụng Map để O(n) - tránh O(n*m) khi filter liên tiếp
     */
    private ArrayList<EmployeeDetailDTO> filterByRole(ArrayList<EmployeeDetailDTO> data,
            String roleName) {
        // Build map: roleName -> list of employees với role đó
        Map<String, ArrayList<EmployeeDetailDTO>> roleMap = new HashMap<>();

        for (EmployeeDetailDTO emp : data) {
            String role = emp.getRoleName() != null ? emp.getRoleName() : "";
            roleMap.computeIfAbsent(role, k -> new ArrayList<>()).add(emp);
        }

        // Return employees với role cần tìm
        return roleMap.getOrDefault(roleName, new ArrayList<>());
    }

    /**
     * Lọc theo Status
     * Sử dụng Map để O(n)
     */
    private ArrayList<EmployeeDetailDTO> filterByStatus(ArrayList<EmployeeDetailDTO> data,
            String statusDesc) {
        // Build map: statusDescription -> list of employees với status đó
        Map<String, ArrayList<EmployeeDetailDTO>> statusMap = new HashMap<>();

        for (EmployeeDetailDTO emp : data) {
            String status = emp.getStatusDescription() != null ? emp.getStatusDescription() : "";
            statusMap.computeIfAbsent(status, k -> new ArrayList<>()).add(emp);
        }

        // Return employees với status cần tìm
        return statusMap.getOrDefault(statusDesc, new ArrayList<>());
    }
}
