package PROVIDER;

import DTO.EmployeeDetailDTO;

import java.util.ArrayList;

/**
 * Filter logic cho EmployeeDetailDTO
 * Single Responsibility: Chỉ xử lý filter, không xử lý transformation
 * Performance: O(n) - single pass through data
 */
public class EmployeeDetailFilterer {
    private static final EmployeeDetailFilterer INSTANCE = new EmployeeDetailFilterer();

    private EmployeeDetailFilterer() {
    }

    public static EmployeeDetailFilterer getInstance() {
        return INSTANCE;
    }

    /**
     * Apply tất cả filters trong 1 vòng lặp duy nhất
     * Single pass O(n) - tránh multiple passes qua data
     * 
     * @param data     Input data to filter
     * @param searchBy Type of search (Mã nhân viên, Họ tên, Tài khoản, Email)
     * @param keyword  Search keyword
     * @param roleId   Role filter (-1 = all)
     * @param statusId Status filter (-1 = all)
     * @return Filtered data
     */
    public ArrayList<EmployeeDetailDTO> applyAllFilters(ArrayList<EmployeeDetailDTO> data,
            String searchBy, String keyword, int roleId, int statusId) {

        ArrayList<EmployeeDetailDTO> result = new ArrayList<>();
        String searchKey = (keyword != null && !keyword.isEmpty())
                ? keyword.toLowerCase().trim()
                : "";

        for (EmployeeDetailDTO emp : data) {
            // Stage 1: Check keyword match (nếu có keyword)
            boolean matchesKeyword = true;
            if (!searchKey.isEmpty()) {
                matchesKeyword = switch (searchBy) {
                    case "Mã nhân viên" -> String.valueOf(emp.getEmployeeId()).contains(searchKey);
                    case "Họ tên" -> emp.getFullName() != null
                            && emp.getFullName().toLowerCase().contains(searchKey);
                    case "Tài khoản" -> emp.getUsername() != null
                            && emp.getUsername().toLowerCase().contains(searchKey);
                    case "Email" -> emp.getEmail() != null
                            && emp.getEmail().toLowerCase().contains(searchKey);
                    default -> false;
                };
            }

            // Stage 2: Check role match (nếu có roleId filter)
            boolean matchesRole = (roleId == -1) || (emp.getRoleId() == roleId);

            // Stage 3: Check status match (nếu có statusId filter)
            boolean matchesStatus = (statusId == -1) || (emp.getStatusId() == statusId);

            // Thêm vào result nếu thỏa tất cả điều kiện
            if (matchesKeyword && matchesRole && matchesStatus) {
                result.add(emp);
            }
        }

        return result;
    }
}
