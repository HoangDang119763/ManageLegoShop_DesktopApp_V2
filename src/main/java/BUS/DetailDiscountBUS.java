package BUS;

import DAL.DetailDiscountDAL;
import DTO.DetailDiscountDTO;
import ENUM.ServiceAccessCode;

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

    public int delete(String code, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE || code == null || code.isEmpty())
            return 2;

        // if (!AvailableUtils.getInstance().isNotUsedDiscount(code)) {
        // return 4;
        // }

        if (!DetailDiscountDAL.getInstance().deleteAllDetailDiscountByDiscountCode(code)) {
            return 5;
        }
        return 1;
    }

    public ArrayList<DetailDiscountDTO> getAllDetailDiscountByDiscountId(String discountCode) {
        if (discountCode == null || discountCode.isEmpty())
            return null;

        return DetailDiscountDAL.getInstance().getAllDetailDiscountByDiscountCode(discountCode);
    }

    public boolean createDetailDiscountByDiscountCode(String discountCode, int employee_roleId,
            ArrayList<DetailDiscountDTO> list, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE || list == null || list.isEmpty()
                || discountCode == null || discountCode.isEmpty())
            return false;
        Set<BigDecimal> seenPrices = new HashSet<>();
        for (DetailDiscountDTO dto : list) {
            if (!seenPrices.add(dto.getTotalPriceInvoice())) {
                return false;
            }
        }

        list.sort(Comparator.comparing(DetailDiscountDTO::getTotalPriceInvoice));
        if (!DetailDiscountDAL.getInstance().insertAllDetailDiscountByDiscountCode(discountCode, list)) {
            return false;
        }
        ArrayList<DetailDiscountDTO> newDetailDiscount = DetailDiscountDAL.getInstance()
                .getAllDetailDiscountByDiscountCode(discountCode);
        return true;
    }

    public boolean insertRollbackDetailDiscount(ArrayList<DetailDiscountDTO> list, int employee_roleId,
            ServiceAccessCode codeAccess,
            int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.IMPORT_DETAILIMPORT_SERVICE || list == null || list.isEmpty())
            return false;
        if (!DetailDiscountDAL.getInstance().insertAllDetailDiscountByDiscountCode(list.get(0).getDiscountCode(),
                list)) {
            return false;
        }
        ArrayList<DetailDiscountDTO> newDetailDiscount = DetailDiscountDAL.getInstance()
                .getAllDetailDiscountByDiscountCode(list.get(0).getDiscountCode());
        return true;
    }

    @Override
    public DetailDiscountDTO getById(String id) {
        if (id == null || id.isEmpty())
            return null;
        return DetailDiscountDAL.getInstance().getById(id);
    }

}
