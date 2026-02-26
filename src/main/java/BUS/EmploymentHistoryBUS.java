package BUS;

import DAL.EmploymentHistoryDAL;
import DTO.BUSResult;
import DTO.EmploymentHistoryDTO;
import DTO.EmploymentHistoryDetailBasicDTO;
import DTO.EmploymentHistoryDetailDTO;
import DTO.PagedResponse;
import ENUM.BUSOperationResult;
import UTILS.AppMessages;

import java.util.ArrayList;

public class EmploymentHistoryBUS extends BaseBUS<EmploymentHistoryDTO, Integer> {
    private static final EmploymentHistoryBUS INSTANCE = new EmploymentHistoryBUS();

    private EmploymentHistoryBUS() {
    }

    public static EmploymentHistoryBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<EmploymentHistoryDTO> getAll() {
        return EmploymentHistoryDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(EmploymentHistoryDTO obj) {
        return obj.getId();
    }

    public ArrayList<EmploymentHistoryDTO> getByEmployeeIdLocalIncrease(int employeeId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        if (employeeId <= 0)
            return result;
        return result;
    }

    public ArrayList<EmploymentHistoryDTO> getByEmployeeIdLocalDecrease(int employeeId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        if (employeeId <= 0)
            return result;
        return result;
    }

    public ArrayList<EmploymentHistoryDTO> getByStatusLocal(int statusId) {
        ArrayList<EmploymentHistoryDTO> result = new ArrayList<>();
        return result;
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

}
