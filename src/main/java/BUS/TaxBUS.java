package BUS;

import DTO.TaxDTO;
import DAL.TaxDAL;
import SERVICE.AuthorizationService;
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

    @Override
    public void loadLocal() {
        super.loadLocal();
        mapByEmployeeId.clear();
        for (TaxDTO tax : arrLocal) {
            if (tax.getEmployeeId() > 0) {
                mapByEmployeeId.put(tax.getEmployeeId(), tax);
            }
        }
    }

    public TaxDTO getById(Integer id) {
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
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (TaxDAL.getInstance().insert(obj)) {
            if (arrLocal.isEmpty()) {
                loadLocal();
            } else {
                arrLocal.add(new TaxDTO(obj));
            }
            return true;
        }
        return false;
    }

    public boolean update(TaxDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidTaxInput(obj)) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (TaxDAL.getInstance().update(obj)) {
            for (TaxDTO tax : arrLocal) {
                if (tax.getId() == obj.getId()) {
                    tax.setEmployeeId(obj.getEmployeeId());
                    tax.setNumDependents(obj.getNumDependents());
                    break;
                }
            }
            return true;
        }
        return false;
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }
        if (!AuthorizationService.getInstance().hasPermission(employeeLoginId, employeeRoleId, 1)) {
            return false;
        }
        if (TaxDAL.getInstance().delete(id)) {
            arrLocal.removeIf(tax -> tax.getId() == id);
            return true;
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
