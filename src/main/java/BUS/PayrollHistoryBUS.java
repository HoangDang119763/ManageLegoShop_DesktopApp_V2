package BUS;

import DTO.PayrollHistoryDTO;
import DAL.PayrollHistoryDAL;
import java.time.LocalDate;
import java.util.ArrayList;

public class PayrollHistoryBUS extends BaseBUS<PayrollHistoryDTO, Integer> {
    public static final PayrollHistoryBUS INSTANCE = new PayrollHistoryBUS();

    private PayrollHistoryBUS() {
    }

    public static PayrollHistoryBUS getInstance() {
        return INSTANCE;
    }

    @Override
    public ArrayList<PayrollHistoryDTO> getAll() {
        return PayrollHistoryDAL.getInstance().getAll();
    }

    @Override
    protected Integer getKey(PayrollHistoryDTO obj) {
        return obj.getId();
    }

    public PayrollHistoryDTO getById(Integer id) {
        return PayrollHistoryDAL.getInstance().getById(id);
    }

    public ArrayList<PayrollHistoryDTO> getByEmployeeId(int employeeId) {
        ArrayList<PayrollHistoryDTO> allPayrolls = getAll();
        ArrayList<PayrollHistoryDTO> result = new ArrayList<>();
        for (PayrollHistoryDTO payroll : allPayrolls) {
            if (payroll.getEmployeeId() == employeeId) {
                result.add(payroll);
            }
        }
        // Sort by salary period descending (newest first)
        result.sort((p1, p2) -> {
            if (p1.getSalaryPeriod() == null || p2.getSalaryPeriod() == null) return 0;
            return p2.getSalaryPeriod().compareTo(p1.getSalaryPeriod());
        });
        return result;
    }

    public PayrollHistoryDTO getByEmployeeAndPeriod(int employeeId, LocalDate salaryPeriod) {
        ArrayList<PayrollHistoryDTO> allPayrolls = getAll();
        for (PayrollHistoryDTO payroll : allPayrolls) {
            if (payroll.getEmployeeId() == employeeId &&
                    payroll.getSalaryPeriod() != null &&
                    payroll.getSalaryPeriod().equals(salaryPeriod)) {
                return payroll;
            }
        }
        return null;
    }

    public boolean insert(PayrollHistoryDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidPayrollInput(obj)) {
            return false;
        }
        return PayrollHistoryDAL.getInstance().insert(obj);
    }

    public boolean update(PayrollHistoryDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidPayrollInput(obj)) {
            return false;
        }
        return PayrollHistoryDAL.getInstance().update(obj);
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) {
            return false;
        }
        return PayrollHistoryDAL.getInstance().delete(id);
    }

    private boolean isValidPayrollInput(PayrollHistoryDTO obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getEmployeeId() <= 0) {
            return false;
        }
        if (obj.getSalaryPeriod() == null) {
            return false;
        }
        if (obj.getNetSalary() == null || obj.getNetSalary().signum() < 0) {
            return false;
        }
        return true;
    }
}
