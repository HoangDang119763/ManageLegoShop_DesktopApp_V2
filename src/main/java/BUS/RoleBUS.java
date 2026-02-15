package BUS;

import DAL.RoleDAL;
import DTO.BUSResult;
import DTO.RoleDTO;
import ENUM.BUSOperationResult;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

public class RoleBUS extends BaseBUS<RoleDTO, Integer> {
    private static final RoleBUS INSTANCE = new RoleBUS();

    public static RoleBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<RoleDTO> getAll() {
        // Luôn gọi DB để lấy danh sách mới nhất
        return RoleDAL.getInstance().getAll();
    }

    // Lấy danh sách cho UI (thường loại bỏ Admin để tránh sửa nhầm)
    public ArrayList<RoleDTO> getAllForUI() {
        return RoleDAL.getInstance().getAll().stream()
                .filter(role -> role.getId() != 1)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // @Override
    // public RoleDTO getById(Integer id) {
    // return RoleDAL.getInstance().getById(id);
    // }

    @Override
    protected Integer getKey(RoleDTO obj) {
        return obj.getId();
    }

    // --- NGHIỆP VỤ CRUD ---

    public BUSResult insert(RoleDTO obj) {
        // 1. Validate đầu vào (Static validation)
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 2. Chuẩn hóa dữ liệu
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        // 3. Check logic nghiệp vụ trên DB (Không dùng Local nữa)
        // if (isNotValidRoleInput(obj))
        // return new BUSResult(BUSOperationResult.INVALID_DATA,
        // AppMessages.INVALID_DATA);

        if (isDuplicateRoleName(-1, obj.getName()))
            return new BUSResult(BUSOperationResult.CONFLICT, "Tên chức vụ đã tồn tại!");

        // 4. Thực thi
        if (!RoleDAL.getInstance().insert(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    public BUSResult update(RoleDTO obj) {
        if (obj == null || obj.getId() <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // Chặn chỉnh sửa Admin hệ thống (ID 1)
        if (obj.getId() == 1)
            return new BUSResult(BUSOperationResult.FAIL, "Không thể chỉnh sửa chức vụ hệ thống!");

        // // 1. Kiểm tra tồn tại và lấy dữ liệu tươi từ DB
        // RoleDTO existing = getById(obj.getId());
        // if (existing == null)
        // return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND);

        // // 2. Kiểm tra thay đổi dữ liệu (So sánh Object)
        // if (isDataUnchanged(obj, existing))
        // return new BUSResult(BUSOperationResult.SUCCESS,
        // AppMessages.OPERATION_SUCCESS);

        // 3. Validate & Check trùng tên (Chọc DB)
        if (isDuplicateRoleName(obj.getId(), obj.getName()))
            return new BUSResult(BUSOperationResult.CONFLICT, "Tên chức vụ đã tồn tại!");

        // 4. Ghi xuống DB
        if (!RoleDAL.getInstance().update(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    public BUSResult delete(int roleId) {
        if (roleId <= 1)
            return new BUSResult(BUSOperationResult.FAIL, "Không thể xóa chức vụ này!");

        // Check xem có nhân viên nào đang giữ Role này không (Ràng buộc HRM)
        if (EmployeeBUS.getInstance().countByRoleId(roleId) > 0) {
            return new BUSResult(BUSOperationResult.FAIL, "Còn nhân viên thuộc chức vụ này, không thể xóa!");
        }

        if (!RoleDAL.getInstance().delete(roleId))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    // --- CÁC HÀM HỖ TRỢ (PRIVATE - LUÔN CHỌC DB) ---

    private boolean isDuplicateRoleName(int id, String name) {
        // Gọi DAL thực hiện: SELECT COUNT(*) FROM role WHERE name = ? AND id != ?
        return RoleDAL.getInstance().existsByName(name.trim(), id);
    }

    private boolean isDataUnchanged(RoleDTO obj, RoleDTO existing) {
        ValidationUtils v = ValidationUtils.getInstance();
        return Objects.equals(existing.getName(), v.normalizeWhiteSpace(obj.getName())) &&
                Objects.equals(existing.getDescription(), v.normalizeWhiteSpace(obj.getDescription())) &&
                Objects.equals(existing.getSalaryId(), obj.getSalaryId());
    }

    @Override
    public RoleDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return RoleDAL.getInstance().getById(id);
    }

}
