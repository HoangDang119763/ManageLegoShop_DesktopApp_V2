package BUS;

import DAL.DetailDiscountDAL;
import DTO.BUSResult;
import DTO.DetailDiscountDTO;
import ENUM.BUSOperationResult;
import ENUM.DiscountType;
import java.sql.*;
import UTILS.AppMessages;
import UTILS.ValidationUtils;

import java.math.BigDecimal;
import java.util.*;

public class DetailDiscountBUS extends BaseBUS<DetailDiscountDTO, String> {
    private static final DetailDiscountBUS INSTANCE = new DetailDiscountBUS();

    public static DetailDiscountBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<DetailDiscountDTO> getAll() {
        return DetailDiscountDAL.getInstance().getAll();
    }

    @Override
    protected String getKey(DetailDiscountDTO obj) {
        return obj.getDiscountCode();
    }

    public BUSResult getAllDetailDiscountByDiscountId(String discountCode) {
        if (discountCode == null || discountCode.isEmpty())
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS, new ArrayList<>());

        ArrayList<DetailDiscountDTO> detailDiscounts = DetailDiscountDAL.getInstance()
                .getAllDetailDiscountByDiscountCode(discountCode);
        if (detailDiscounts.isEmpty())
            return new BUSResult(BUSOperationResult.NOT_FOUND, AppMessages.NOT_FOUND, new ArrayList<>());
        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.DISCOUNT_DETAIL_LOAD_SUCCESS,
                detailDiscounts);
    }

    public boolean isValidDetail(DetailDiscountDTO detail, int discountType) {
        if (detail == null) {
            return false;
        }

        BigDecimal minInvoice = detail.getTotalPriceInvoice();
        BigDecimal amount = detail.getDiscountAmount();
        ValidationUtils validator = ValidationUtils.getInstance();

        // 1. Kiểm tra cơ bản: Không được null, phải > 0
        if (minInvoice == null || amount == null) {
            return false;
        }
        if (!validator.formatCurrency(minInvoice, 12, 2, false)) {
            return false; // (nếu điều kiện không hợp lệ)
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return false;
        }

        // 2. Kiểm tra theo kiểu khuyến mãi (Chặn đứng lỗi từ UI)
        if (discountType == DiscountType.PERCENTAGE.getCode()) {
            // Giảm phần trăm thì không được >= 100%
            return amount.compareTo(new BigDecimal("100")) < 0;
        } else {
            // Giảm tiền mặt thì số tiền giảm không được lớn hơn hoặc bằng ngưỡng hóa đơn
            // tối thiểu
            return amount.compareTo(minInvoice) < 0;
        }
    }

    public boolean insertDetailDiscountWithConn(Connection conn, String discountCode, int discountType,
            List<DetailDiscountDTO> list) throws SQLException {
        if (list == null || list.isEmpty() || discountCode == null)
            return true;

        Set<BigDecimal> seenPrices = new HashSet<>();

        for (DetailDiscountDTO dto : list) {
            // 1. Đồng bộ thông tin từ Master
            dto.setDiscountCode(discountCode);

            // 2. Nếu một dòng không hợp lệ, phải trả về false để ROLLBACK
            if (!isValidDetail(dto, discountType)) {
                System.err.println("[DEBUG] Detail validation failed for one record.");
                return false;
            }

            // 3. Nếu trùng ngưỡng hóa đơn, cũng phải trả về false
            if (!seenPrices.add(dto.getTotalPriceInvoice())) {
                System.err.println("[DEBUG] Duplicate TotalPriceInvoice found in list.");
                return false;
            }
        }

        // Sắp xếp lại danh sách
        list.sort(Comparator.comparing(DetailDiscountDTO::getTotalPriceInvoice));

        // 4. Gọi DAL: Bỏ dấu ! để trả về kết quả thật (Thành công = true)
        boolean result = DetailDiscountDAL.getInstance().insertAllDetailDiscount(conn, list);

        if (!result) {
            System.err.println("[DEBUG] DAL insertAllDetailDiscount returned false.");
        }

        return result;
    }

    public boolean deleteByDiscountCodeWithConn(Connection conn, String discountCode) throws SQLException {
        if (discountCode == null || discountCode.isEmpty())
            return true;
        return DetailDiscountDAL.getInstance().deleteByDiscountCode(conn, discountCode);
    }

    @Override
    public DetailDiscountDTO getById(String id) {
        if (id == null || id.isEmpty())
            return null;
        return DetailDiscountDAL.getInstance().getById(id);
    }

}
