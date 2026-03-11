package BUS;

import DAL.PositionDAL;
import DTO.BUSResult;
import DTO.PositionDTO;
import ENUM.BUSOperationResult;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

public class PositionBUS extends BaseBUS<PositionDTO, Integer> {
    private static final PositionBUS INSTANCE = new PositionBUS();

    private PositionBUS() {
    }

    public static PositionBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<PositionDTO> getAll() {
        return PositionDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(PositionDTO obj) {
        return obj.getId();
    }

    @Override
    public PositionDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return PositionDAL.getInstance().getById(id);
    }

    // ===== CRUD cho Position dùng ở màn hình phòng ban & chức vụ =====

    public BUSResult insert(PositionDTO dto) {
        if (dto == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        ValidationUtils v = ValidationUtils.getInstance();
        String name = v.normalizeWhiteSpace(dto.getName());
        if (name == null || name.isEmpty() || !v.validateVietnameseText100(name)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }
        dto.setName(name);

        BigDecimal wage = dto.getWage();
        if (wage == null || !v.validateBigDecimal(wage, 15, 2, false) || wage.compareTo(BigDecimal.ZERO) <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, "Lương cơ bản không hợp lệ.");
        }

        String lower = name.toLowerCase(Locale.ROOT);
        boolean duplicated = PositionDAL.getInstance().getAll().stream()
                .anyMatch(p -> p.getName() != null && p.getName().toLowerCase(Locale.ROOT).equals(lower));
        if (duplicated) {
            return new BUSResult(BUSOperationResult.CONFLICT, "Tên chức vụ đã tồn tại.");
        }

        if (!PositionDAL.getInstance().insert(dto)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    public BUSResult update(PositionDTO dto) {
        if (dto == null || dto.getId() == null || dto.getId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        ValidationUtils v = ValidationUtils.getInstance();
        String name = v.normalizeWhiteSpace(dto.getName());
        if (name == null || name.isEmpty() || !v.validateVietnameseText100(name)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }
        dto.setName(name);

        BigDecimal wage = dto.getWage();
        if (wage == null || !v.validateBigDecimal(wage, 15, 2, false) || wage.compareTo(BigDecimal.ZERO) <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, "Lương cơ bản không hợp lệ.");
        }

        String lower = name.toLowerCase(Locale.ROOT);
        boolean duplicated = PositionDAL.getInstance().getAll().stream()
                .anyMatch(p -> !p.getId().equals(dto.getId()) && p.getName() != null
                        && p.getName().toLowerCase(Locale.ROOT).equals(lower));
        if (duplicated) {
            return new BUSResult(BUSOperationResult.CONFLICT, "Tên chức vụ đã tồn tại.");
        }

        if (!PositionDAL.getInstance().update(dto)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }

    public BUSResult delete(int id) {
        if (id <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        // TODO: có thể check ràng buộc với employee.position_id sau
        if (!PositionDAL.getInstance().delete(id)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.OPERATION_SUCCESS);
    }
}
