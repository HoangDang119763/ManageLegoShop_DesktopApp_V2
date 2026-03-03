package BUS;

import DTO.FineDTO;
import DAL.FineDAL;
import ENUM.BUSOperationResult;
import UTILS.ValidationUtils;
import java.math.BigDecimal;
import java.util.ArrayList;

public class FineBUS extends BaseBUS<FineDTO, Integer> {
    public static final FineBUS INSTANCE = new FineBUS();

    private FineBUS() {}

    public static FineBUS getInstance() { return INSTANCE; }

    @Override
    public ArrayList<FineDTO> getAll() {
        return FineDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(FineDTO obj) { return obj.getId(); }

    public FineDTO getById(Integer id) {
        return FineDAL.getInstance().getById(id);
    }

    public ArrayList<FineDTO> getByEmployeeId(int employeeId) {
        if (employeeId <= 0) return new ArrayList<>();
        // Tối ưu: Gọi trực tiếp từ DAL nếu DAL có hàm getByEmployeeId, 
        // nếu chưa có thì lọc thủ công từ getAll() như bạn đã làm.
        return (ArrayList<FineDTO>) getAll().stream()
                .filter(f -> f.getEmployeeId() == employeeId)
                .collect(java.util.stream.Collectors.toList());
    }

    // VỊ TRÍ SỬA: Thực thi gọi DAL thay vì return false
    public boolean insert(FineDTO obj, int roleId, int loginId) {
        if (!isValidFineInput(obj)) return false;
        return FineDAL.getInstance().insert(obj);
    }

    public boolean update(FineDTO obj, int roleId, int loginId) {
        if (!isValidFineInput(obj) || obj.getId() <= 0) return false;
        return FineDAL.getInstance().update(obj);
    }

    public boolean delete(Integer id, int roleId, int loginId) {
        if (id == null || id <= 0) return false;
        return FineDAL.getInstance().delete(id);
    }

    private boolean isValidFineInput(FineDTO obj) {
        if (obj == null || obj.getEmployeeId() <= 0) return false;
        if (obj.getReason() == null || obj.getReason().trim().isEmpty()) return false;
        
        // Đảm bảo type luôn hợp lệ
        if (obj.getType() == null) obj.setType("DISCIPLINE");
        
        ValidationUtils validator = ValidationUtils.getInstance();
        return validator.validateVietnameseText255(obj.getReason()) 
            && validator.validateBigDecimal(obj.getAmount(), 15, 2, false);
    }
}