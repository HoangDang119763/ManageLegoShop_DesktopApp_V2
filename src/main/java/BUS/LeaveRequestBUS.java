package BUS;

import DAL.LeaveRequestDAL;
import DTO.LeaveRequestDTO;
import ENUM.BUSOperationResult;
import UTILS.ValidationUtils;
import java.util.ArrayList;
import SERVICE.SessionManagerService;
import ENUM.PermissionKey;

public class LeaveRequestBUS extends BaseBUS<LeaveRequestDTO, Integer> {

    private static final LeaveRequestBUS INSTANCE = new LeaveRequestBUS();

    private LeaveRequestBUS() {}

    public static LeaveRequestBUS getInstance() {
        return INSTANCE;
    }

    // ================= GET =================

    @Override
    public ArrayList<LeaveRequestDTO> getAll() {
        return LeaveRequestDAL.getInstance().getAll();
    }

    @Override
    public LeaveRequestDTO getById(Integer id) {
        if (id == null || id <= 0) return null;
        return LeaveRequestDAL.getInstance().getById(id);
    }

    public ArrayList<LeaveRequestDTO> getByEmployeeId(int employeeId) {
        if (employeeId <= 0) return new ArrayList<>();
        return LeaveRequestDAL.getInstance().getByEmployeeId(employeeId);
    }

    // ================= INSERT =================

    public BUSOperationResult insert(LeaveRequestDTO obj) {
        if (!isValid(obj)) return BUSOperationResult.INVALID_DATA;

        obj.setStatusId(20); // pending

        if (obj.getContent() != null) {
            obj.setContent(ValidationUtils.getInstance()
                    .normalizeWhiteSpace(obj.getContent()));
        }

        boolean success = LeaveRequestDAL.getInstance().insert(obj);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    // ================= UPDATE =================

    public BUSOperationResult update(LeaveRequestDTO obj) {
        if (obj == null || obj.getId() <= 0) return BUSOperationResult.INVALID_PARAMS;

        LeaveRequestDTO old = getById(obj.getId());
        if (old == null) return BUSOperationResult.NOT_FOUND;

        if (old.getStatusId() != 20) return BUSOperationResult.CONFLICT;

        if (!isValid(obj)) return BUSOperationResult.INVALID_DATA;

        boolean success = LeaveRequestDAL.getInstance().update(obj);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    // ================= DELETE =================

    public BUSOperationResult delete(Integer id) {
        if (id == null || id <= 0) return BUSOperationResult.INVALID_PARAMS;

        LeaveRequestDTO existing = getById(id);
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        boolean success = LeaveRequestDAL.getInstance().delete(id);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    // ================= STATUS =================

    public BUSOperationResult approve(int id) {
        if (!SessionManagerService.getInstance()
                .hasPermission(PermissionKey.EMPLOYEE_LEAVE_REQUEST_MANAGE)) {
            return BUSOperationResult.UNAUTHORIZED;
        }

        LeaveRequestDTO existing = getById(id);
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        if (existing.getStatusId() != 20) return BUSOperationResult.CONFLICT;

        boolean success = LeaveRequestDAL.getInstance().approve(id);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    public BUSOperationResult reject(int id) {
        if (!SessionManagerService.getInstance()
                .hasPermission(PermissionKey.EMPLOYEE_LEAVE_REQUEST_MANAGE)) {
            return BUSOperationResult.UNAUTHORIZED;
        }

        LeaveRequestDTO existing = getById(id);
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        if (existing.getStatusId() != 20) return BUSOperationResult.CONFLICT;

        boolean success = LeaveRequestDAL.getInstance().reject(id);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    public BUSOperationResult cancel(int id, int employeeId) {
        LeaveRequestDTO existing = getById(id);
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        if (existing.getEmployeeId() != employeeId) {
            return BUSOperationResult.UNAUTHORIZED;
        }

        if (existing.getStatusId() != 20) return BUSOperationResult.CONFLICT;

        boolean success = LeaveRequestDAL.getInstance().cancel(id);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    // ================= VALIDATION =================

    private boolean isValid(LeaveRequestDTO obj) {
        if (obj == null) return false;

        if (obj.getLeaveTypeId() <= 0) return false;

        if (obj.getStartDate() == null || obj.getEndDate() == null) return false;

        if (obj.getEndDate().isBefore(obj.getStartDate())) return false;

        if (obj.getContent() != null && !obj.getContent().isEmpty()) {
            return ValidationUtils.getInstance()
                    .validateVietnameseText255(obj.getContent());
        }

        return true;
    }

    @Override
    protected Integer getKey(LeaveRequestDTO obj) {
        return obj.getId();
    }
}