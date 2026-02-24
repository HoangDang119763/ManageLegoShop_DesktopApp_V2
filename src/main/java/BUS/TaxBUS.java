package BUS;

import DTO.TaxDTO;
import DTO.BUSResult;
import DAL.TaxDAL;
import ENUM.BUSOperationResult;
import UTILS.AppMessages;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class TaxBUS extends BaseBUS<TaxDTO, Integer> {
    public static final TaxBUS INSTANCE = new TaxBUS();
    private final HashMap<Integer, TaxDTO> mapByEmployeeId = new HashMap<>();

    private TaxBUS() {
    }

    public static TaxBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<TaxDTO> getAll() {
        return TaxDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(TaxDTO obj) {
        return obj.getId();
    }

    public TaxDTO getById(Integer id) {
        if (id == null || id <= 0)
            return null;
        return TaxDAL.getInstance().getById(id);
    }

    public TaxDTO getByEmployeeId(int employeeId) {
        if (employeeId <= 0)
            return null;
        return mapByEmployeeId.get(employeeId);
    }

    /**
     * Insert Tax record - dùng cho EmployeeBUS.insertEmployeeFull()
     * 
     * @param obj TaxDTO cần insert
     * @return BUSResult với chi tiết kết quả
     */
    public BUSResult insert(TaxDTO obj) {
        if (obj == null) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        if (!isValidTaxInput(obj)) {
            return new BUSResult(BUSOperationResult.INVALID_DATA, AppMessages.INVALID_DATA);
        }

        if (obj.getEmployeeId() <= 0) {
            return new BUSResult(BUSOperationResult.INVALID_PARAMS, AppMessages.INVALID_PARAMS);
        }

        if (!TaxDAL.getInstance().insert(obj)) {
            return new BUSResult(BUSOperationResult.DB_ERROR, AppMessages.DB_ERROR);
        }

        return new BUSResult(BUSOperationResult.SUCCESS, AppMessages.EMPLOYEE_ADD_SUCCESS);
    }

    public boolean insertWithConn(Connection conn, TaxDTO obj) throws SQLException {
        if (!isValidTaxInput(obj))
            return false;

        // Gọi DAL trực tiếp với connection được truyền từ Master BUS
        return TaxDAL.getInstance().insertWithConn(conn, obj);
    }

    public boolean update(TaxDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidTaxInput(obj)) {
            return false;
        }

        return false;
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }

        return false;
    }

    private boolean isValidTaxInput(TaxDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getEmployeeId() <= 0) {
            return false;
        }
        if (obj.getNumDependents() < 0) {
            return false;
        }
        return true;
    }

    public boolean updateNumDependents(Connection conn, TaxDTO obj) {
        if (obj.getEmployeeId() <= 0 || obj.getNumDependents() < 0 || !isValidTaxInput(obj))
            return false;
        try {
            return TaxDAL.getInstance().updateNumDependent(conn, obj.getEmployeeId(), obj.getNumDependents());
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
