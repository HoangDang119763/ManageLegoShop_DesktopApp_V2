package BUS;

import DAL.DiscountDAL;
import DTO.BUSResult;
import DTO.DiscountDTO;
import DTO.PagedResponse;
import ENUM.BUSOperationResult;
import ENUM.ServiceAccessCode;
import UTILS.ValidationUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class DiscountBUS extends BaseBUS<DiscountDTO, String> {
    private static final DiscountBUS INSTANCE = new DiscountBUS();

    private DiscountBUS() {
    }

    public static DiscountBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<DiscountDTO> getAll() {
        return DiscountDAL.getInstance().getAll();
    }

    @Override
    protected String getKey(DiscountDTO obj) {
        return obj.getCode();
    }

    public ArrayList<DiscountDTO> searchByCodeLocal(String keyword) {
        ArrayList<DiscountDTO> result = new ArrayList<>();

        return result;
    }

    public ArrayList<DiscountDTO> filterDiscountsAdvance(String discountName, int type, LocalDate startDate,
            LocalDate endDate) {
        ArrayList<DiscountDTO> filteredList = new ArrayList<>();

        // for (DiscountDTO dis : arrLocal) {
        // boolean matchesDate = true;
        // boolean matchesOther = false;

        // LocalDate discountStartDate = dis.getStartDate().toLocalDate();
        // LocalDate discountEndDate = dis.getEndDate().toLocalDate();

        // // Xử lý logic ngày
        // if (startDate != null && endDate != null) {
        // matchesDate = !discountEndDate.isAfter(endDate);
        // } else if (startDate != null) {
        // matchesDate = !discountStartDate.isBefore(startDate);
        // } else if (endDate != null) {
        // matchesDate = !discountEndDate.isAfter(endDate);
        // }

        // if (discountName != null && !discountName.isBlank()) {
        // if (dis.getName().toLowerCase().contains(discountName.toLowerCase())) {
        // matchesOther = true;
        // }
        // }

        // if (type != -1) {
        // if (dis.getType() == type) {
        // matchesOther = true;
        // }
        // }

        // // Nếu không nhập gì => mặc định true
        // if ((discountName == null || discountName.isBlank()) && type == -1) {
        // matchesOther = true;
        // }

        // if (matchesDate && matchesOther) {
        // filteredList.add(new DiscountDTO(dis));
        // }
        // }

        return filteredList;
    }

    public int insert(DiscountDTO obj, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE || obj == null)
            return 2;

        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setCode(obj.getCode().toUpperCase());
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        if (isDuplicateDiscountCode(obj.getCode()))
            return 4;

        if (!DiscountDAL.getInstance().insert(obj))
            return 5;
        return 1;
    }

    public boolean delete(String code, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE || code == null
                || code.isEmpty()) {
            return false;
        }

        if (!DiscountDAL.getInstance().delete(code)) {
            return false;
        }

        return true;
    }

    public int update(DiscountDTO obj, int employee_roleId, int employeeLoginId) {
        if (obj == null || obj.getCode().isEmpty() || employee_roleId <= 0) {
            return 2;
        }

        if (!isValidateDiscountInput(obj))
            return 4;

        ValidationUtils validate = ValidationUtils.getInstance();
        obj.setName(validate.normalizeWhiteSpace(obj.getName()));
        if (!DiscountDAL.getInstance().update(obj))
            return 6;
        return 1;
    }

    private boolean isDuplicateDiscountCode(String code) {
        if (code == null)
            return false;
        return false;
    }

    private boolean isValidateDiscountInput(DiscountDTO obj) {
        if (obj.getName() == null || obj.getCode() == null || obj.getStartDate() == null || obj.getEndDate() == null
                || (obj.getType() != 0 && obj.getType() != 1)) {
            return false;
        }

        ValidationUtils validator = ValidationUtils.getInstance();

        boolean isValidCode = validator.validateDiscountCode(obj.getCode(), 4, 50);
        boolean isValidName = validator.validateVietnameseText100(obj.getName());

        LocalDate today = LocalDate.now();
        LocalDate startDate = obj.getStartDate().toLocalDate();
        LocalDate endDate = obj.getEndDate().toLocalDate();

        //

        boolean isValidDate = !startDate.isAfter(endDate) &&
                !startDate.isBefore(today) &&
                !endDate.isBefore(today);

        return isValidCode && isValidName && isValidDate;
    }

    public ArrayList<DiscountDTO> filterDiscountsActive() {
        ArrayList<DiscountDTO> filteredList = new ArrayList<>();

        return filteredList;
    }

    @Override
    public DiscountDTO getById(String id) {
        if (id == null || id.isEmpty())
            return null;
        return DiscountDAL.getInstance().getById(id);
    }

    /**
     * [OPTIMIZED] Filter discounts with pagination for manage display
     */
    public BUSResult filterDiscountsPagedForManage(String keyword, int pageIndex, int pageSize) {
        int finalPageIndex = Math.max(0, pageIndex);
        int finalPageSize = (pageSize <= 0) ? DEFAULT_PAGE_SIZE : pageSize;
        String searchKeyword = (keyword == null) ? "" : keyword.trim();

        PagedResponse<DiscountDTO> pagedData = DiscountDAL.getInstance()
                .filterDiscountsPagedForManage(searchKeyword, finalPageIndex, finalPageSize);

        return new BUSResult(BUSOperationResult.SUCCESS, null, pagedData);
    }

}
