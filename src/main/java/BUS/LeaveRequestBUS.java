package BUS;

import DAL.LeaveRequestDAL;
import DTO.LeaveRequestDTO;
import ENUM.BUSOperationResult;
import ENUM.Status;
import UTILS.ValidationUtils;

import java.util.ArrayList;

public class LeaveRequestBUS extends BaseBUS<LeaveRequestDTO, Integer> {

    private static final LeaveRequestBUS INSTANCE = new LeaveRequestBUS();

    private LeaveRequestBUS() {}

    public static LeaveRequestBUS getInstance() {
        return INSTANCE;
    }

    // =========================================================
    // GET OPERATIONS
    // =========================================================

    @Override
    public ArrayList<LeaveRequestDTO> getAll() {
        // DAL đã Join với bảng Status và LeaveType, lấy trực tiếp về dùng
        return LeaveRequestDAL.getInstance().getAll();
    }

    @Override
    public LeaveRequestDTO getById(Integer id) {
        if (id == null || id <= 0) return null;
        // Tận dụng phương thức kế thừa từ BaseDAL qua DAL Singleton
        return LeaveRequestDAL.getInstance().getById(id);
    }

    public ArrayList<LeaveRequestDTO> getByEmployeeId(int employeeId) {
        if (employeeId <= 0) return new ArrayList<>();
        return LeaveRequestDAL.getInstance().getByEmployeeId(employeeId);
    }

    // =========================================================
    // WRITE OPERATIONS
    // =========================================================

    public BUSOperationResult insert(LeaveRequestDTO obj) {
        // Chặn insert nếu dữ liệu không hợp lệ
        if (!isValidLeaveRequestInput(obj)) {
            return BUSOperationResult.INVALID_DATA;
        }

        // Normalize nội dung trước khi lưu
        if (obj.getContent() != null) {
            obj.setContent(ValidationUtils.getInstance().normalizeWhiteSpace(obj.getContent()));
        }

        // Luôn gán trạng thái mặc định là Pending (20) cho đơn mới
        obj.setStatusId(20); 

        boolean success = LeaveRequestDAL.getInstance().insert(obj);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    public BUSOperationResult update(LeaveRequestDTO obj) {
        if (obj == null || obj.getId() <= 0) return BUSOperationResult.INVALID_PARAMS;

        LeaveRequestDTO existing = getById(obj.getId());
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        // Chỉ cho phép sửa đơn nếu đang ở trạng thái PENDING (20)
        if (existing.getStatusId() != 20) {
            return BUSOperationResult.CONFLICT; // Đơn đã duyệt/hủy không được sửa
        }

        if (!isValidLeaveRequestInput(obj)) return BUSOperationResult.INVALID_DATA;

        boolean success = LeaveRequestDAL.getInstance().update(obj);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    public BUSOperationResult delete(Integer id) {
        if (id == null || id <= 0) return BUSOperationResult.INVALID_PARAMS;

        LeaveRequestDTO existing = getById(id);
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        boolean success = LeaveRequestDAL.getInstance().delete(id);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    // =========================================================
    // STATUS TRANSITION OPERATIONS (Duyệt/Hủy)
    // =========================================================

    public BUSOperationResult approve(int id) {
        LeaveRequestDTO existing = getById(id);
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        if (existing.getStatusId() != 20) {
            return BUSOperationResult.CONFLICT;
        }

        boolean success = LeaveRequestDAL.getInstance().approve(id);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    public BUSOperationResult reject(int id) {
        LeaveRequestDTO existing = getById(id);
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        if (existing.getStatusId() != 20) {
            return BUSOperationResult.CONFLICT;
        }

        boolean success = LeaveRequestDAL.getInstance().reject(id);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    public BUSOperationResult cancel(int id, int employeeLoginId) {
        LeaveRequestDTO existing = getById(id);
        if (existing == null) return BUSOperationResult.NOT_FOUND;

        // Kiểm tra quyền sở hữu đơn
        if (existing.getEmployeeId() != employeeLoginId) {
            return BUSOperationResult.UNAUTHORIZED;
        }

        // Gọi phương thức cancel (ID 23) từ DAL
        boolean success = LeaveRequestDAL.getInstance().cancel(id);
        return success ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    // =========================================================
    // VALIDATION HELPER
    // =========================================================

    private boolean isValidLeaveRequestInput(LeaveRequestDTO obj) {
        if (obj == null || obj.getStartDate() == null || obj.getEndDate() == null) {
            return false;
        }

        // Ngày bắt đầu không được sau ngày kết thúc
        if (obj.getEndDate().isBefore(obj.getStartDate())) {
            return false;
        }

        // Validate nội dung tiếng Việt tối đa 255 ký tự
        if (obj.getContent() != null && !obj.getContent().isEmpty()) {
            return ValidationUtils.getInstance().validateVietnameseText255(obj.getContent());
        }

        return true;
    }

    @Override
    protected Integer getKey(LeaveRequestDTO obj) {
        return obj.getId();
    }
}