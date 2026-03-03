package BUS;

import DAL.AttendanceDAL;
import DTO.AttendanceDTO;
import ENUM.BUSOperationResult;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AttendanceBUS extends BaseBUS<AttendanceDTO, Integer> {
    public static final AttendanceBUS INSTANCE = new AttendanceBUS();
    private AttendanceBUS() {}
    public static AttendanceBUS getInstance() { return INSTANCE; }

    // Lọc dữ liệu nâng cao
    public ArrayList<AttendanceDTO> getFilteredAttendance(String keyword, Integer deptId, Integer month, Integer year, java.time.LocalDate date) {
        java.sql.Date sqlDate = (date != null) ? java.sql.Date.valueOf(date) : null;
        return AttendanceDAL.getInstance().getFullCompanyAttendance(keyword, deptId, month, year, sqlDate);
    }

    // Logic tính toán giờ làm việc tự động trước khi lưu
    public void calculateWorkingHours(AttendanceDTO dto) {
        if (dto.getCheckIn() != null && dto.getCheckOut() != null) {
            long minutes = Duration.between(dto.getCheckIn(), dto.getCheckOut()).toMinutes();
            double hours = minutes / 60.0;
            
            // Giả định ca làm việc chuẩn là 8 tiếng
            double standardHours = 8.0;
            dto.setWorkHours(BigDecimal.valueOf(Math.min(hours, standardHours)));
            dto.setOtHours(BigDecimal.valueOf(Math.max(0, hours - standardHours)));
        }
    }

    // Thực hiện Import hàng loạt
    public int importFromExcel(ArrayList<AttendanceDTO> list) {
        int count = 0;
        for (AttendanceDTO dto : list) {
            calculateWorkingHours(dto);
            if (AttendanceDAL.getInstance().insert(dto)) {
                count++;
            }
        }
        return count;
    }

    @Override public ArrayList<AttendanceDTO> getAll() { return AttendanceDAL.getInstance().getAll(); }
    @Override protected Integer getKey(AttendanceDTO obj) { return obj.getId(); }
    @Override
    public AttendanceDTO getById(Integer id) {
      // TODO Auto-generated method stub
      throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }
}