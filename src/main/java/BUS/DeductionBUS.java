package BUS;

import DTO.DeductionDTO;
import DAL.DeductionDAL;
import UTILS.ValidationUtils;
import java.time.LocalDate;
import java.util.ArrayList;

public class DeductionBUS extends BaseBUS<DeductionDTO, Integer> {
    public static final DeductionBUS INSTANCE = new DeductionBUS();

    private DeductionBUS() {
    }

    public static DeductionBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<DeductionDTO> getAll() {
        return DeductionDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(DeductionDTO obj) {
        return obj.getId();
    }

    public DeductionDTO getById(Integer id) {
        return DeductionDAL.getInstance().getById(id);
    }

    public ArrayList<DeductionDTO> getByEmployeeId(int employeeId) {
        ArrayList<DeductionDTO> allDeductions = getAll();
        ArrayList<DeductionDTO> result = new ArrayList<>();
        for (DeductionDTO deduction : allDeductions) {
            if (deduction.getEmployeeId() == employeeId) {
                result.add(deduction);
            }
        }
        return result;
    }

    public ArrayList<DeductionDTO> getBySalaryPeriod(LocalDate salaryPeriod) {
        ArrayList<DeductionDTO> allDeductions = getAll();
        ArrayList<DeductionDTO> result = new ArrayList<>();
        for (DeductionDTO deduction : allDeductions) {
            if (deduction.getSalaryPeriod() != null && deduction.getSalaryPeriod().equals(salaryPeriod)) {
                result.add(deduction);
            }
        }
        return result;
    }

    public DeductionDTO getByEmployeeAndPeriod(int employeeId, LocalDate salaryPeriod) {
        ArrayList<DeductionDTO> allDeductions = getAll();
        for (DeductionDTO deduction : allDeductions) {
            if (deduction.getEmployeeId() == employeeId &&
                    deduction.getSalaryPeriod() != null &&
                    deduction.getSalaryPeriod().equals(salaryPeriod)) {
                return deduction;
            }
        }
        return null;
    }

    public boolean insert(DeductionDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidDeductionInput(obj)) {
            return false;
        }

        return false;
    }

    public boolean update(DeductionDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidDeductionInput(obj)) {
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

    private boolean isValidDeductionInput(DeductionDTO obj) {
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
        if (obj.getHealthInsurance() != null && !validator.validateBigDecimal(obj.getHealthInsurance(), 15, 2, true)) {
            return false;
        }
        if (obj.getSocialInsurance() != null && !validator.validateBigDecimal(obj.getSocialInsurance(), 15, 2, true)) {
            return false;
        }
        if (obj.getUnemploymentInsurance() != null
                && !validator.validateBigDecimal(obj.getUnemploymentInsurance(), 15, 2, true)) {
            return false;
        }
        if (obj.getPersonalIncomeTax() != null
                && !validator.validateBigDecimal(obj.getPersonalIncomeTax(), 15, 2, true)) {
            return false;
        }
        return true;
    }
}
