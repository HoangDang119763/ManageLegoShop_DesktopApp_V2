package BUS;

import DAL.PermissionDAL;
import DTO.PermissionDTO;
import java.util.ArrayList;
import java.util.List;

public class PermissionBUS extends BaseBUS<PermissionDTO, Integer> {
    private static final PermissionBUS INSTANCE = new PermissionBUS();

    private PermissionBUS() {
    }

    public static PermissionBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<PermissionDTO> getAll() {
        return PermissionDAL.getInstance().getAll();
    }

    @Override
    public PermissionDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return PermissionDAL.getInstance().getById(id);
    }

    @Override
    protected Integer getKey(PermissionDTO obj) {
        return obj.getId();
    }

    /**
     * Tìm quyền theo permission_key (gọi DB trực tiếp, không cache)
     * 
     * @param permissionKey Khóa quyền cần tìm
     * @return PermissionDTO hoặc null nếu không tìm thấy
     */
    public PermissionDTO getByPermissionKey(String permissionKey) {
        if (permissionKey == null || permissionKey.trim().isEmpty())
            return null;
        return PermissionDAL.getInstance().getByPermissionKey(permissionKey);
    }

    /**
     * Lấy tất cả quyền của một module
     * 
     * @param moduleId ID của module
     * @return Danh sách quyền
     */
    public ArrayList<PermissionDTO> getByModuleId(int moduleId) {
        if (moduleId <= 0)
            return new ArrayList<>();
        return PermissionDAL.getInstance().getAllRoleByModuleId(moduleId);
    }

    public List<String> getPermissionKeysByRoleId(int roleId) {
        if (roleId <= 0)
            return new ArrayList<>();
        return PermissionDAL.getInstance().getPermissionKeysByRoleId(roleId);
    }
}
