package BUS;

import DTO.TimeSheetDTO;
import ENUM.BUSOperationResult;
import DAL.TimeSheetDAL;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

public class TimeSheetBUS extends BaseBUS<TimeSheetDTO, Integer> {
    public static final TimeSheetBUS INSTANCE = new TimeSheetBUS();

    private TimeSheetBUS() {}

    public static TimeSheetBUS getInstance() {
        return INSTANCE;
    }

    // SỬA: Gọi hàm có JOIN và hỗ trợ tham số lọc từ DAL
    public ArrayList<TimeSheetDTO> getAll(String keyword, Integer deptId, Integer month, Integer year) {
        return TimeSheetDAL.getInstance().getFullTimeSheet(keyword, deptId, month, year);
    }

    @Override
    public ArrayList<TimeSheetDTO> getAll() {
        return getAll(null, null, null, null);
    }

    public BUSOperationResult bulkInsert(ArrayList<TimeSheetDTO> list) {
        if (list == null || list.isEmpty()) return BUSOperationResult.INVALID_PARAMS;

        for (TimeSheetDTO dto : list) {
            // 1. Kiểm tra logic: Giờ ra phải sau giờ vào
            if (dto.getCheckOut() != null && dto.getCheckIn() != null) {
                if (dto.getCheckOut().isBefore(dto.getCheckIn())) {
                    return BUSOperationResult.INVALID_DATA; // Hoặc trả về lỗi kèm dòng cụ thể
                }

                // 2. Tính toán giờ làm (Work Hours & OT)
                double totalMinutes = java.time.Duration.between(dto.getCheckIn(), dto.getCheckOut()).toMinutes();
                double totalHours = totalMinutes / 60.0;
                
                // Giả định ca làm việc tiêu chuẩn là 8 tiếng
                double stdHours = 8.0;
                dto.setWorkHours(java.math.BigDecimal.valueOf(Math.min(totalHours, stdHours)));
                dto.setOtHours(java.math.BigDecimal.valueOf(Math.max(0, totalHours - stdHours)));
            } else {
                // Trường hợp quên chưa check-out
                dto.setWorkHours(java.math.BigDecimal.ZERO);
                dto.setOtHours(java.math.BigDecimal.ZERO);
            }
        }

        // 3. Thực thi Batch Insert xuống DAL
        int result = TimeSheetDAL.getInstance().insertBatch(list);
        return result > 0 ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    // Các hàm CRUD khác nên gọi DAL thay vì return false mặc định
    public BUSOperationResult insert(TimeSheetDTO obj) {
        if (!isValidTimeSheetInput(obj)) return BUSOperationResult.INVALID_DATA;
        return TimeSheetDAL.getInstance().insert(obj) ? BUSOperationResult.SUCCESS : BUSOperationResult.DB_ERROR;
    }

    public ArrayList<TimeSheetDTO> getByEmployeeId(int employeeId) {
        // Tận dụng hàm lọc của DAL để tối ưu hiệu suất
        return TimeSheetDAL.getInstance().getFullTimeSheet(String.valueOf(employeeId), null, null, null);
    }

    private boolean isValidTimeSheetInput(TimeSheetDTO obj) {
        return obj != null && obj.getEmployeeId() > 0 && obj.getCheckIn() != null;
    }

    @Override
    protected Integer getKey(TimeSheetDTO obj) {
        return obj.getId();
    }

    @Override
    public TimeSheetDTO getById(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }
    public boolean isEmployeeInAnyTimeSheet(int employeeId) {
        if (employeeId <= 0)
            return false;
        return TimeSheetDAL.getInstance().existsByEmployeeId(employeeId);
    }
}