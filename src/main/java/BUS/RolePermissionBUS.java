package BUS;

import DAL.RolePermissionDAL;
import DTO.RolePermissionDTO;
import DTO.BUSResult;
import ENUM.BUSOperationResult;
import UTILS.AppMessages;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RolePermissionBUS extends BaseBUS<RolePermissionDTO, Integer> {
    private static final RolePermissionBUS INSTANCE = new RolePermissionBUS();

    private RolePermissionBUS() {
    }

    public static RolePermissionBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<RolePermissionDTO> getAll() {
        return RolePermissionDAL.getInstance().getAll();
    }

    @Override
    public RolePermissionDTO getById(Integer id) {
        // Với bảng quan hệ n-n, getById thường không dùng theo 1 ID duy nhất
        return null;
    }

    public ArrayList<RolePermissionDTO> getAllByRoleId(int roleId) {
        return RolePermissionDAL.getInstance().getAllRolePermissionByRoleId(roleId);
    }

    /**
     * Logic Mới: Cập nhật quyền (Toggle)
     * Nếu isSelected = true -> Thêm vào DB
     * Nếu isSelected = false -> Xóa khỏi DB
     */
    public BUSResult togglePermission(int roleId, int permissionId, boolean isSelected) {
        if (roleId <= 0 || permissionId <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // Chặn chỉnh sửa Admin (RoleID = 1)
        if (roleId == 1)
            return new BUSResult(BUSOperationResult.FAIL, "Không thể chỉnh sửa quyền của Admin hệ thống");

        boolean success;
        if (isSelected) {
            // Cấp quyền: Thêm bản ghi
            if (RolePermissionDAL.getInstance().exists(roleId, permissionId))
                return new BUSResult(BUSOperationResult.SUCCESS, "Đã có quyền này");
            success = RolePermissionDAL.getInstance().insert(new RolePermissionDTO(roleId, permissionId));
        } else {
            // Tước quyền: Xóa bản ghi
            success = RolePermissionDAL.getInstance().revokePermission(roleId, permissionId);
        }

        if (!success)
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        // Đánh dấu yêu cầu đăng nhập lại cho các User thuộc Role này
        AccountBUS.getInstance().setRequireReloginByRoleId(roleId, true);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    public boolean hasPermission(int roleId, int permissionId) {
        return RolePermissionDAL.getInstance().exists(roleId, permissionId);
    }

    @Override
    protected Integer getKey(RolePermissionDTO obj) {
        return obj.getPermissionId();
    }

    public Set<Integer> getAllowedModuleIdsByRoleId(int roleId) {
        if (roleId <= 0)
            return new HashSet<>();
        return RolePermissionDAL.getInstance().getAllowedModuleIdsByRoleId(roleId);
    }
}