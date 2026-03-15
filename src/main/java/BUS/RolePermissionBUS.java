package BUS;

import DAL.RolePermissionDAL;
import DTO.BUSResult;
import DTO.RolePermissionDTO;
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

    /**
     * Cập nhật toàn bộ danh sách quyền cho một Role dựa trên tập permissionId mới.
     * Được dùng khi nhấn nút "Lưu" trong màn phân quyền.
     */
    public BUSResult updateRolePermissions(int roleId, Set<Integer> newPermissionIds) {
        if (roleId <= 0 || newPermissionIds == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        // Không cho chỉnh sửa quyền của Admin hệ thống
        if (roleId == 1) {
            return new BUSResult(BUSOperationResult.FAIL, "Không thể chỉnh sửa quyền của Admin hệ thống");
        }

        // Lấy danh sách hiện tại từ DB
        ArrayList<RolePermissionDTO> current = getAllByRoleId(roleId);
        Set<Integer> currentIds = new HashSet<>();
        for (RolePermissionDTO rp : current) {
            currentIds.add(rp.getPermissionId());
        }

        // Tính toán tập cần thêm / cần xóa
        Set<Integer> toInsert = new HashSet<>(newPermissionIds);
        toInsert.removeAll(currentIds);

        Set<Integer> toDelete = new HashSet<>(currentIds);
        toDelete.removeAll(newPermissionIds);

        boolean success = true;

        if (!toInsert.isEmpty()) {
            success = RolePermissionDAL.getInstance()
                    .insertListRolePermission(roleId, new ArrayList<>(toInsert));
        }

        if (success && !toDelete.isEmpty()) {
            for (Integer permissionId : toDelete) {
                if (!RolePermissionDAL.getInstance().revokePermission(roleId, permissionId)) {
                    success = false;
                    break;
                }
            }
        }

        if (!success) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        // Đánh dấu yêu cầu đăng nhập lại cho các User thuộc Role này
        AccountBUS.getInstance().setRequireReloginByRoleId(roleId, true);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    public int countByRoleId(int roleId) {
        if (roleId <= 0) {
            return 0;
        }
        return RolePermissionDAL.getInstance().countByRoleId(roleId);
    }

    public boolean deleteAllByRoleId(int roleId) {
        if (roleId <= 0) return false;
        return RolePermissionDAL.getInstance().deleteAllByRoleId(roleId);
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