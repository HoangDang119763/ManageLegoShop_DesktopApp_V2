package BUS;

import DTO.AllowanceDTO;
import DAL.AllowanceDAL;
import UTILS.ValidationUtils;
import java.util.ArrayList;

public class AllowanceBUS extends BaseBUS<AllowanceDTO, Integer> {
    public static final AllowanceBUS INSTANCE = new AllowanceBUS();

    private AllowanceBUS() {
    }

    public static AllowanceBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<AllowanceDTO> getAll() {
        return AllowanceDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(AllowanceDTO obj) {
        return obj.getId();
    }

    public AllowanceDTO getById(Integer id) {
        return AllowanceDAL.getInstance().getById(id);
    }

    public AllowanceDTO getByName(String name) {
        ArrayList<AllowanceDTO> allAllowances = getAll();
        for (AllowanceDTO allowance : allAllowances) {
            if (allowance.getName() != null && allowance.getName().equalsIgnoreCase(name)) {
                return allowance;
            }
        }
        return null;
    }

    public ArrayList<AllowanceDTO> searchByName(String keyword) {
        ArrayList<AllowanceDTO> allAllowances = getAll();
        ArrayList<AllowanceDTO> result = new ArrayList<>();
        for (AllowanceDTO allowance : allAllowances) {
            if (allowance.getName() != null && allowance.getName().toLowerCase().contains(keyword.toLowerCase())) {
                result.add(allowance);
            }
        }
        return result;
    }

    public boolean insert(AllowanceDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidAllowanceInput(obj)) {
            return false;
        }
        return AllowanceDAL.getInstance().insert(obj);
    }

    public boolean update(AllowanceDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidAllowanceInput(obj)) {
            return false;
        }
        return AllowanceDAL.getInstance().update(obj);
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }
        return AllowanceDAL.getInstance().delete(id);
    }

    private boolean isValidAllowanceInput(AllowanceDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getName() == null || obj.getName().trim().isEmpty()) {
            return false;
        }
        if (obj.getName().length() > 100) {
            return false;
        }
        ValidationUtils validator = ValidationUtils.getInstance();
        if (obj.getAmount() != null && !validator.validateBigDecimal(obj.getAmount(), 15, 2, true)) {
            return false;
        }
        return true;
    }
}
