package SERVICE;

import BUS.AccountBUS;
import BUS.PermissionBUS;
import BUS.RolePermissionBUS;
import DTO.EmployeeDTO;
import DTO.PermissionDTO;
import DTO.RolePermissionDTO;
import ENUM.PermissionKey;

import java.util.HashSet;

public class SessionManagerService {
    private static SessionManagerService instance;
    private EmployeeDTO loggedInEmployee;
    private HashSet<Integer> allowedModules;
    private HashSet<String> allowedPermissionKeys;

    private SessionManagerService() {
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
        this.loggedInEmployee = employee;
        loadPermissions();
    }

    public void logout() {
        this.loggedInEmployee = null;
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
        if (loggedInEmployee == null)
            return;

        PermissionBUS permissionBUS = PermissionBUS.getInstance();
        RolePermissionBUS rolePermissionBUS = RolePermissionBUS.getInstance();

        if (permissionBUS.isLocalEmpty())
            permissionBUS.loadLocal();
        if (rolePermissionBUS.isLocalEmpty())
            rolePermissionBUS.loadLocal();

        for (RolePermissionDTO rp : rolePermissionBUS.getAllRolePermissionByRoleIdLocal(loggedInEmployee.getRoleId())) {
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
        return loggedInEmployee != null ? new EmployeeDTO(loggedInEmployee) : null;
    }

    public int numAllowedModules() {
        return allowedModules.size();
    }

    public int employeeLoginId() {
        return loggedInEmployee.getId();
    }

    public int employeeRoleId() {
        return loggedInEmployee.getRoleId();
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
}
