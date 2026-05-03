package BUS;

import DAL.HolidayDAL;
import DTO.BUSResult;
import DTO.HolidayDTO;
import DTO.PagedResponse;
import ENUM.BUSOperationResult;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.util.ArrayList;

public class HolidayBUS extends BaseBUS<HolidayDTO, Integer> {
    private static final HolidayBUS INSTANCE = new HolidayBUS();

    private HolidayBUS() {
    }

    public static HolidayBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<HolidayDTO> getAll() {
        return HolidayDAL.getInstance().getAll();
    }

    @Override
    public HolidayDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return HolidayDAL.getInstance().getById(id);
    }

    @Override
    protected Integer getKey(HolidayDTO obj) {
        return obj.getId();
    }

    public int nextId() {
        return HolidayDAL.getInstance().getLastIdEver() + 1;
    }

    public BUSResult insert(HolidayDTO obj) {
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // Normalize
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        if (!isValidHolidayInput(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        if (HolidayDAL.getInstance().existsByNameAndDate(obj.getName(), obj.getDate(), -1))
            return new BUSResult(BUSOperationResult.CONFLICT, "Ngày lễ này (tên và ngày) đã tồn tại trong hệ thống");

        if (!HolidayDAL.getInstance().insert(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, "Thêm ngày lễ thành công");
    }

    public BUSResult update(HolidayDTO obj) {
        if (obj == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        // Normalize
        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));

        if (!isValidHolidayInput(obj))
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);

        if (HolidayDAL.getInstance().existsByNameAndDate(obj.getName(), obj.getDate(), obj.getId()))
            return new BUSResult(BUSOperationResult.CONFLICT, "Ngày lễ này (tên và ngày) đã tồn tại trong hệ thống");

        if (!HolidayDAL.getInstance().update(obj))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, "Cập nhật ngày lễ thành công");
    }

    public BUSResult delete(int id) {
        if (id <= 0)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);

        HolidayDTO holiday = HolidayDAL.getInstance().getById(id);
        if (holiday == null)
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, "Ngày lễ không tồn tại");

        // Kiểm tra ngày lễ đã qua chưa - chỉ cho xóa nếu ngày lễ chưa qua (future)
        if (HolidayDAL.getInstance().isHolidayPassed(holiday.getDate()))
            return new BUSResult(BUSOperationResult.FAIL, "Không thể xóa ngày lễ đã qua");

        if (!HolidayDAL.getInstance().delete(id))
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);

        return new BUSResult(BUSOperationResult.SUCCESS, "Xóa ngày lễ thành công");
    }

    private boolean isValidHolidayInput(HolidayDTO obj) {
        if (obj.getName() == null || obj.getName().trim().isEmpty())
            return false;

        if (obj.getName().length() > 255)
            return false;

        if (obj.getDate() == null)
            return false;

        return true;
    }

    public BUSResult filterHolidaysPagedForManageDisplay(String keyword, int pageIndex, int pageSize) {
        String cleanKeyword = (keyword == null) ? "" : keyword.trim().toLowerCase();
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? 15 : pageSize;

        PagedResponse<HolidayDTO> pagedResponse = HolidayDAL.getInstance()
                .filterHolidaysPagedDisplay(cleanKeyword, finalPageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedResponse);
    }
}
