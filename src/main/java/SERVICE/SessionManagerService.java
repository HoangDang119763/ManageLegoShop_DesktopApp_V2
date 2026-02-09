package SERVICE;

import BUS.EmployeeBUS;
import BUS.PermissionBUS;
import BUS.RolePermissionBUS;
import DTO.EmployeeDTO;
import DTO.PermissionDTO;
import DTO.RolePermissionDTO;
import ENUM.PermissionKey;

import java.util.HashSet;

public class SessionManagerService {
    private static SessionManagerService instance;
    private int employeeId;
    private int roleId;
    private HashSet<Integer> allowedModules;
    private HashSet<String> allowedPermissionKeys;

    private SessionManagerService() {
        this.employeeId = -1;
        this.roleId = -1;
        allowedModules = new HashSet<>();
        allowedPermissionKeys = new HashSet<>();
    }

    public static SessionManagerService getInstance() {
        if (instance == null) {
            instance = new SessionManagerService();
        }
        return instance;
    }

    public void setLoggedInEmployee(EmployeeDTO employee) {
        if (employee != null) {
            this.employeeId = employee.getId();
            this.roleId = employee.getRoleId();
        } else {
            this.employeeId = -1;
            this.roleId = -1;
        }
        loadPermissions();
    }

    public void logout() {
        this.employeeId = -1;
        this.roleId = -1;
        allowedModules.clear();
        allowedPermissionKeys.clear();
    }

    public boolean hasModuleAccess(int moduleId) {
        return allowedModules.contains(moduleId);
    }

    public boolean hasPermission(PermissionKey permissionKey) {
        if (permissionKey == null) {
            return false;
        }
        return allowedPermissionKeys.contains(permissionKey.name());
    }

    public boolean hasPermission(String permissionKey) {
        if (permissionKey == null || permissionKey.isEmpty()) {
            return false;
        }
        return allowedPermissionKeys.contains(permissionKey);
    }

    private void loadPermissions() {
        allowedModules.clear();
        allowedPermissionKeys.clear();
        if (employeeId <= 0)
            return;

        PermissionBUS permissionBUS = PermissionBUS.getInstance();
        RolePermissionBUS rolePermissionBUS = RolePermissionBUS.getInstance();

        if (permissionBUS.isLocalEmpty())
            permissionBUS.loadLocal();
        if (rolePermissionBUS.isLocalEmpty())
            rolePermissionBUS.loadLocal();

        for (RolePermissionDTO rp : rolePermissionBUS.getAllRolePermissionByRoleIdLocal(roleId)) {
            if (rp.isStatus()) {
                // Lấy permission để có được permission_key
                PermissionDTO permission = permissionBUS.getByIdLocal(rp.getPermissionId());
                if (permission != null) {
                    // Lưu permission key
                    if (permission.getPermissionKey() != null && !permission.getPermissionKey().isEmpty()) {
                        allowedPermissionKeys.add(permission.getPermissionKey());
                    }
                    // Lưu moduleId
                    allowedModules.add(permission.getModule_id());
                }
            }
        }
    }

    public EmployeeDTO currEmployee() {
        if (employeeId <= 0)
            return null;
        EmployeeDTO emp = EmployeeBUS.getInstance().getByIdLocal(employeeId);
        return emp != null ? new EmployeeDTO(emp) : null;
    }

    public int numAllowedModules() {
        return allowedModules.size();
    }

    public int employeeLoginId() {
        return employeeId;
    }

    public int employeeRoleId() {
        return roleId;
    }

    public boolean canManage() {
        // Có quyền quản lý nếu có quyền gì đó ngoài bán hàng (module 5) và nhập hàng
        // (module 6)
        for (Integer moduleId : allowedModules) {
            if (moduleId != 5 && moduleId != 6) {
                return true;
            }
        }
        return false;
    }

    public boolean canSelling() {
        return hasModuleAccess(5);
    }

    public boolean canImporting() {
        return hasModuleAccess(6);
    }

    public void updateCurrentEmployee() {
        if (employeeId <= 0)
            return;
        loadPermissions();
    }

}
