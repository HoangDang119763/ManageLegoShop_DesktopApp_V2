package BUS;

import DAL.EmploymentHistoryDAL;
import DTO.BUSResult;
import DTO.EmploymentHistoryDTO;
import DTO.EmploymentHistoryDetailBasicDTO;
import DTO.EmploymentHistoryDetailDTO;
import DTO.PagedResponse;
import DTO.StatusDTO;
import ENUM.BUSOperationResult;
import ENUM.Status;
import ENUM.StatusType;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.sql.SQLException;
import java.util.ArrayList;

public class EmploymentHistoryBUS extends BaseBUS<EmploymentHistoryDTO, Integer> {
    private static final EmploymentHistoryBUS INSTANCE = new EmploymentHistoryBUS();

    private EmploymentHistoryBUS() {
    }

    public static EmploymentHistoryBUS getInstance() {
        return INSTANCE;
    }

    public int nextId() {
        return EmploymentHistoryDAL.getInstance().getLastIdEver() + 1;
    }

    /**
     * Insert new employment history record
     */
    public BUSResult insert(EmploymentHistoryDTO obj) {
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // 1. Validate input
        if (!isValidEmploymentHistoryInput(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        // 2. Insert to database
        if (!EmploymentHistoryDAL.getInstance().insert(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, "Tạo quyết định điều chuyển thành công");
    }

    /**
     * Validate employment history input
     * Department and Position are optional (can be NULL to use current values)
     * But if provided, must be > 0
     */
    private boolean isValidEmploymentHistoryInput(EmploymentHistoryDTO obj) {
        if (obj == null)
            return false;

        // Check employee ID (required)
        if (obj.getEmployeeId() <= 0)
            return false;

        // Check department ID (optional, but if provided must be > 0)
        if (obj.getDepartmentId() < 0)
            return false;

        // Check position ID (optional, but if provided must be > 0)
        if (obj.getPositionId() < 0)
            return false;

        // Check effective date (required)
        if (obj.getEffectiveDate() == null)
            return false;

        // Check status ID (required)
        if (obj.getStatusId() <= 0)
            return false;

        if (obj.getReason() != null && !obj.getReason().trim().isEmpty()
                && !ValidationUtils.getInstance().validateVietnameseText255(obj.getReason())) {
            return false;
        }
        return true;
    }

    @Override
    public ArrayList<EmploymentHistoryDTO> getAll() {
        return EmploymentHistoryDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(EmploymentHistoryDTO obj) {
        return obj.getId();
    }

    @Override
    public EmploymentHistoryDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return EmploymentHistoryDAL.getInstance().getById(id);
    }

    /**
     * Lấy chi tiết lịch sử công tác của nhân viên (với tên phòng ban, chức vụ)
     * Có phân trang và cấu trúc dữ liệu EmploymentHistoryDetailBasicDTO
     */
    public BUSResult getDetailsByEmployeeIdPaged(int employeeId, int pageIndex, int pageSize) {
        // 1. Xác định PageSize chuẩn trước khi làm bất cứ việc gì
        int finalPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;

        // 2. Validate tham số
        if (employeeId <= 0 || pageIndex < 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS,
                    new PagedResponse<>(new ArrayList<>(), 0, pageIndex, finalPageSize));
        }

        // 3. Gọi DAL để lấy dữ liệu chi tiết với JOIN
        PagedResponse<EmploymentHistoryDetailBasicDTO> pagedData = EmploymentHistoryDAL.getInstance()
                .getDetailsByEmployeeIdPaged(employeeId, pageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    public BUSResult getDetailsFullByEmployeeIdPaged(int employeeId, int pageIndex, int pageSize) {
        // 1. Xác định PageSize chuẩn trước khi làm bất cứ việc gì
        int finalPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;

        // 2. Validate tham số
        if (employeeId <= 0 || pageIndex < 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS,
                    new PagedResponse<>(new ArrayList<>(), 0, pageIndex, finalPageSize));
        }

        // 3. Gọi DAL để lấy dữ liệu chi tiết với JOIN
        PagedResponse<EmploymentHistoryDetailDTO> pagedData = EmploymentHistoryDAL.getInstance()
                .getDetailsFullByEmployeeIdPaged(employeeId, pageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    /**
     * Lọc và phân trang lịch sử điều chuyển để hiển thị trong bảng quản lý
     */
    public BUSResult filterEmploymentHistoryPagedForManageDisplay(
            String keyword,
            Integer employeeId,
            Integer departmentId,
            Integer positionId,
            Integer statusId,
            int pageIndex,
            int pageSize) {

        // 1. Xác định PageSize chuẩn
        int finalPageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;

        // 2. Validate tham số cơ bản
        if (pageIndex < 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS,
                    new PagedResponse<>(new ArrayList<>(), 0, pageIndex, finalPageSize));
        }

        // 3. Gọi DAL để lấy dữ liệu có filter
        PagedResponse<DTO.EmploymentHistoryDisplayDTO> pagedData = EmploymentHistoryDAL.getInstance()
                .filterEmploymentHistoryPagedForManageDisplay(keyword, employeeId, departmentId, positionId, statusId,
                        pageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

    /**
     * Delete employment history with different logic based on status:
     * - PENDING: hard delete
     * - APPROVED: change status to CANCELLED
     * - EFFECTIVE: cannot delete
     */
    public BUSResult delete(int id) {
        if (id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        EmploymentHistoryDTO obj = EmploymentHistoryDAL.getInstance().getById(id);
        if (obj == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, "Quyết định không tồn tại");

        // Get status names
        StatusDTO approvedStatus = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.EMPLOYMENT_HISTORY, Status.EmploymentHistory.APPROVED);
        StatusDTO effectiveStatus = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.EMPLOYMENT_HISTORY, Status.EmploymentHistory.EFFECTIVE);
        StatusDTO cancelledStatus = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.EMPLOYMENT_HISTORY, Status.EmploymentHistory.CANCELLED);

        // Check status and execute appropriate action
        if (effectiveStatus != null && obj.getStatusId() == effectiveStatus.getId()) {
            // Cannot delete EFFECTIVE
            return new BUSResult(BUSOperationResult.FAIL, "Không thể xóa quyết định đã có hiệu lực");
        } else if (approvedStatus != null && obj.getStatusId() == approvedStatus.getId()) {
            // Change APPROVED to CANCELLED
            if (!EmploymentHistoryDAL.getInstance().updateStatus(id, cancelledStatus.getId()))
                return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
            return new BUSResult(BUSOperationResult.SUCCESS, "Hủy quyết định thành công");
        } else {
            // Hard delete for PENDING or other statuses
            if (!EmploymentHistoryDAL.getInstance().delete(id))
                return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
            return new BUSResult(BUSOperationResult.SUCCESS, "Xóa quyết định thành công");
        }
    }

    /**
     * Approve employment history (change status to APPROVED)
     */
    public BUSResult approve(int id) {
        if (id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        EmploymentHistoryDTO obj = EmploymentHistoryDAL.getInstance().getById(id);
        if (obj == null)
            return new BUSResult(BUSOperationResult.NOT_FOUND, "Quyết định không tồn tại");

        StatusDTO approvedStatus = StatusBUS.getInstance()
                .getByTypeAndStatusName(StatusType.EMPLOYMENT_HISTORY, Status.EmploymentHistory.APPROVED);

        if (approvedStatus == null)
            return new BUSResult(BUSOperationResult.FAIL, "Không tìm thấy trạng thái Approved");

        if (!EmploymentHistoryDAL.getInstance().updateStatus(id, approvedStatus.getId()))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        // Call sync procedure to update employee if effective date is reached
        syncEmploymentChanges();

        return new BUSResult(BUSOperationResult.SUCCESS, "Duyệt quyết định thành công");
    }

    /**
     * Call stored procedure to sync employment changes
     * Updates employee position/department when effective date is reached
     */
    public BUSResult syncEmploymentChanges() {
        try {
            EmploymentHistoryDAL.getInstance().callSyncEmploymentChanges();
            return new BUSResult(BUSOperationResult.SUCCESS, "Đồng bộ thay đổi công tác thành công");
        } catch (SQLException e) {
            System.err.println("Lỗi đồng bộ thay đổi công tác: " + e.getMessage());
            return new BUSResult(BUSOperationResult.DB_ERROR, "Lỗi đồng bộ thay đổi công tác");
        }
    }
}
