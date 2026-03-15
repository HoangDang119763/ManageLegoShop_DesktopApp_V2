package BUS;

import DAL.DepartmentDAL;
import DTO.BUSResult;
import DTO.DepartmentDTO;
import DTO.StatusDTO;
import ENUM.BUSOperationResult;
import ENUM.Status;
import ENUM.StatusType;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.ArrayList;
import java.util.Locale;

public class DepartmentBUS extends BaseBUS<DepartmentDTO, Integer> {
    private static final DepartmentBUS INSTANCE = new DepartmentBUS();

    private DepartmentBUS() {
    }

    public static DepartmentBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<DepartmentDTO> getAll() {
        return DepartmentDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(DepartmentDTO obj) {
        return obj.getId();
    }

    @Override
    public DepartmentDTO getById(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return DepartmentDAL.getInstance().getById(id);
    }

    public boolean isDepartmentActive(int departmentId) {
        StatusDTO activeStatus = StatusBUS.getInstance().getByTypeAndStatusName(StatusType.DEPARTMENT,
                Status.Department.ACTIVE);
        if (activeStatus == null) {
            return false; // Không tìm thấy trạng thái ACTIVE cho DEPARTMENT
        }
        return DepartmentDAL.getInstance().existsByIdAndStatus(departmentId, activeStatus.getId());
    }

    /**
     * Get all departments with ACTIVE status
     */
    public ArrayList<DepartmentDTO> getActiveDepartments() {
        StatusDTO activeStatus = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.DEPARTMENT, Status.Department.ACTIVE);
        if (activeStatus == null) {
            return new ArrayList<>();
        }
        return DepartmentDAL.getInstance().getByStatusId(activeStatus.getId());
    }

    // ===== CRUD cho Department dùng ở màn hình phòng ban & chức vụ =====

    public BUSResult insert(DepartmentDTO dto) {
        if (dto == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        ValidationUtils v = ValidationUtils.getInstance();
        String name = v.normalizeWhiteSpace(dto.getName());
        if (name == null || name.isEmpty() || !v.validateVietnameseText100(name)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }
        dto.setName(name);

        // Đảm bảo statusId hợp lệ, nếu chưa set thì dùng ACTIVE mặc định
        if (dto.getStatusId() <= 0) {
            StatusDTO active = StatusBUS.getInstance()
                    .getByTypeAndStatusName(StatusType.DEPARTMENT, Status.Department.ACTIVE);
            if (active == null) {
                return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
            }
            dto.setStatusId(active.getId());
        }

        // Check trùng tên cơ bản (case-insensitive)
        String lower = name.toLowerCase(Locale.ROOT);
        boolean duplicated = DepartmentDAL.getInstance().getAll().stream()
                .anyMatch(d -> d.getName() != null && d.getName().toLowerCase(Locale.ROOT).equals(lower));
        if (duplicated) {
            return new BUSResult(BUSOperationResult.CONFLICT, "Tên phòng ban đã tồn tại.");
        }

        if (!DepartmentDAL.getInstance().insert(dto)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    public BUSResult update(DepartmentDTO dto) {
        if (dto == null || dto.getId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        ValidationUtils v = ValidationUtils.getInstance();
        String name = v.normalizeWhiteSpace(dto.getName());
        if (name == null || name.isEmpty() || !v.validateVietnameseText100(name)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        dto.setName(name);

        String lower = name.toLowerCase(Locale.ROOT);
        boolean duplicated = DepartmentDAL.getInstance().getAll().stream()
                .anyMatch(d -> d.getId() != dto.getId() && d.getName() != null
                        && d.getName().toLowerCase(Locale.ROOT).equals(lower));
        if (duplicated) {
            return new BUSResult(BUSOperationResult.CONFLICT, "Tên phòng ban đã tồn tại.");
        }

        if (!DepartmentDAL.getInstance().update(dto)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    public BUSResult delete(int id) {
        if (id <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        // TODO: Có thể check ràng buộc với employee/position sau
        if (!DepartmentDAL.getInstance().delete(id)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }
}
