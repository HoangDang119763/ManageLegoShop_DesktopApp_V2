package SERVICE;

import BUS.*;
import DTO.*;
import ENUM.ServiceAccessCode;

import java.util.ArrayList;

public class DiscountService {
    private static final DiscountService INSTANCE = new DiscountService();

    public static DiscountService getInstance() {
        return INSTANCE;
    }

    public int createDiscountWithDetailDiscount(DiscountDTO discount, int employee_roleId,
            ArrayList<DetailDiscountDTO> list, int employeeLoginId) {
        DiscountBUS disBus = DiscountBUS.getInstance();
        DetailDiscountBUS ddipBus = DetailDiscountBUS.getInstance();

        int resultr = disBus.insert(discount, employee_roleId,
                ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE, employeeLoginId);
        if (resultr != 1) {
            return resultr;
        }

        // Không quan trọng invoiceId của từng thằng trong list. vì sẽ set lại dưới
        // database
        if (!ddipBus.createDetailDiscountByDiscountCode(discount.getCode(), employee_roleId, list,
                ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE, employeeLoginId)) {
            // Rollback nếu lỗi
            ddipBus.delete(discount.getCode(), employee_roleId,
                    ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE, employeeLoginId);
            return 2;
        }

        return 1;
    }

    public int deleteDiscountWithDetailDiscounts(String discountCode, int employee_roleId, int employeeLoginId) {
        DiscountBUS disBus = DiscountBUS.getInstance();
        DetailDiscountBUS ddipBus = DetailDiscountBUS.getInstance();

        ArrayList<DetailDiscountDTO> tempList = ddipBus.getAllDetailDiscountByDiscountId(discountCode);

        int resultrp = ddipBus.delete(discountCode, employee_roleId,
                ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE, employeeLoginId);
        if (resultrp != 1)
            return resultrp;

        // Xóa role sau
        if (!disBus.delete(discountCode, employee_roleId, ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE,
                employeeLoginId)) {
            System.err.println("Failed to delete discount. Attempting to roll back role permissions.");

            // Khôi phục role_permission nếu role không xóa được
            return ddipBus.insertRollbackDetailDiscount(tempList, 1,
                    ServiceAccessCode.DISCOUNT_DETAILDISCOUNT_SERVICE, 1) ? 1 : 2;
        }

        return 1;
    }
}
