package BUS;

import DTO.TaxDTO;
import DAL.TaxDAL;
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

    public boolean insert(TaxDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidTaxInput(obj)) {
            return false;
        }

        return false;
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
}
