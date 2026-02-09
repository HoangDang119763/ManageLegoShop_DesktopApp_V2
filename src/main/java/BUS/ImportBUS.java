package BUS;

import DAL.ImportDAL;
import DTO.ImportDTO;
import ENUM.ServiceAccessCode;
import SERVICE.AuthorizationService;
import UTILS.ValidationUtils;

import java.math.BigDecimal;
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
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 16)) {
            return false;
        }

        if (!ImportDAL.getInstance().delete(id)) {
            return false;
        }
        arrLocal.removeIf(role -> Objects.equals(role.getId(), id));
        return true;
    }

    public boolean insert(ImportDTO obj, int employee_roleId, ServiceAccessCode codeAccess, int employeeLoginId) {
        if (codeAccess != ServiceAccessCode.IMPORT_DETAILIMPORT_SERVICE || obj == null)
            return false;
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employee_roleId, 15)
                || !isValidateImportInput(obj)) {
            return false;
        }

        obj.setCreateDate(LocalDateTime.now());

        if (!ImportDAL.getInstance().insert(obj))
            return false;
        arrLocal.add(new ImportDTO(obj));
        return true;
    }

    private boolean isValidateImportInput(ImportDTO obj) {
        if (obj.getEmployeeId() <= 0 || obj.getSupplierId() <= 0)
            return false;

        ValidationUtils validator = ValidationUtils.getInstance();
        return validator.validateBigDecimal(obj.getTotalPrice(), 12, 2, false);
    }

}
