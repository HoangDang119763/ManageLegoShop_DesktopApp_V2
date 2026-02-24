package BUS;

import DTO.AllowanceDTO;
import DAL.AllowanceDAL;
import UTILS.ValidationUtils;
import java.time.LocalDate;
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

    public ArrayList<AllowanceDTO> getByEmployeeId(int employeeId) {
        ArrayList<AllowanceDTO> allAllowances = getAll();
        ArrayList<AllowanceDTO> result = new ArrayList<>();
        for (AllowanceDTO allowance : allAllowances) {
            if (allowance.getEmployeeId() == employeeId) {
                result.add(allowance);
            }
        }
        return result;
    }

    public ArrayList<AllowanceDTO> getBySalaryPeriod(LocalDate salaryPeriod) {
        ArrayList<AllowanceDTO> allAllowances = getAll();
        ArrayList<AllowanceDTO> result = new ArrayList<>();
        for (AllowanceDTO allowance : allAllowances) {
            if (allowance.getSalaryPeriod() != null && allowance.getSalaryPeriod().equals(salaryPeriod)) {
                result.add(allowance);
            }
        }
        return result;
    }

    public AllowanceDTO getByEmployeeAndPeriod(int employeeId, LocalDate salaryPeriod) {
        ArrayList<AllowanceDTO> allAllowances = getAll();
        for (AllowanceDTO allowance : allAllowances) {
            if (allowance.getEmployeeId() == employeeId &&
                    allowance.getSalaryPeriod() != null &&
                    allowance.getSalaryPeriod().equals(salaryPeriod)) {
                return allowance;
            }
        }
        return null;
    }

    public boolean insert(AllowanceDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidAllowanceInput(obj)) {
            return false;
        }
        // if (AllowanceDAL.getInstance().insert(obj)) {
        // if (arrLocal.isEmpty()) {
        // loadLocal();
        // } else {
        // arrLocal.add(new AllowanceDTO(obj));
        // }
        // return true;
        // }
        return false;
    }

    public boolean update(AllowanceDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidAllowanceInput(obj)) {
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

    private boolean isValidAllowanceInput(AllowanceDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getEmployeeId() <= 0) {
            return false;
        }
        if (obj.getSalaryPeriod() == null) {
            return false;
        }
        ValidationUtils validator = ValidationUtils.getInstance();
        if (obj.getAttendanceBonus() != null && !validator.validateBigDecimal(obj.getAttendanceBonus(), 15, 2, true)) {
            return false;
        }
        if (obj.getAnnualLeaveDays() != null && !validator.validateBigDecimal(obj.getAnnualLeaveDays(), 5, 1, true)) {
            return false;
        }
        if (obj.getTransportationSupport() != null
                && !validator.validateBigDecimal(obj.getTransportationSupport(), 15, 2, true)) {
            return false;
        }
        if (obj.getAccommodationSupport() != null
                && !validator.validateBigDecimal(obj.getAccommodationSupport(), 15, 2, true)) {
            return false;
        }
        return true;
    }
}
