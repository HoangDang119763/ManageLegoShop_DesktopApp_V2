package DTO;

import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeSessionDTO {
    private int accountId;
    private int employeeId;
    private String username;
    private String fullName;
    private int roleId;
    private String roleName;
    private List<String> permissions; // Danh sách các Permission Key (ví dụ: "PRODUCT_INSERT")
    private Set<Integer> allowedModuleIds; // Danh sách các Module ID mà nhân viên có quyền truy cập

    // Constructor không tham số
    public EmployeeSessionDTO() {
    }

    // Constructor đầy đủ tham số
    public EmployeeSessionDTO(int accountId, int employeeId, String username, String fullName,
            int roleId, String roleName, List<String> permissions, Set<Integer> allowedModules) {
        this.accountId = accountId;
        this.employeeId = employeeId;
        this.username = username;
        this.fullName = fullName;
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissions = permissions;
        this.allowedModuleIds = allowedModules;
    }

    @Override
    public String toString() {
        return "EmployeeSessionDTO{" +
                "accountId=" + accountId +
                ", employeeId=" + employeeId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", permissions=" + permissions +
                ", allowedModuleIds=" + allowedModuleIds +
                '}';
    }
}
