package BUS;

import DAL.ImportDAL;
import DTO.ImportDTO;
import ENUM.ServiceAccessCode;
import UTILS.ValidationUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class ImportBUS extends BaseBUS<ImportDTO, Integer> {
    private static final ImportBUS INSTANCE = new ImportBUS();

    public static ImportBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<ImportDTO> getAll() {
        return ImportDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(ImportDTO obj) {
        return obj.getId();
    }

    public boolean delete(Integer id, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.IMPORT_DETAILIMPORT_SERVICE || id == null || id <= 0)
            return false;

        if (!ImportDAL.getInstance().delete(id)) {
            return false;
        }

        return true;
    }

    public boolean insert(ImportDTO obj, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.IMPORT_DETAILIMPORT_SERVICE || obj == null)
            return false;

        obj.setCreateDate(LocalDateTime.now());

        if (!ImportDAL.getInstance().insert(obj))
            return false;
        return true;
    }

    private boolean isValidateImportInput(ImportDTO obj) {
        if (obj.getEmployeeId() <= 0 || obj.getSupplierId() <= 0)
            return false;

        ValidationUtils validator = ValidationUtils.getInstance();
        return validator.validateBigDecimal(obj.getTotalPrice(), 12, 2, false);
    }

    public boolean isProductInAnyImport(String productId) {
        DetailImportBUS detailImportBUS = DetailImportBUS.getInstance();

        // 2. Sử dụng Stream để tìm kiếm nhanh và hiện đại
        // Kiểm tra xem có bất kỳ dòng chi tiết nhập hàng nào chứa mã sản phẩm này không
        return detailImportBUS.getAll().stream()
                .anyMatch(detail -> detail.getProductId().equals(productId));
    }

    @Override
    public ImportDTO getById(Integer id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

}
