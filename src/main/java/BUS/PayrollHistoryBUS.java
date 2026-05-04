package BUS;

import DTO.PayrollHistoryDTO;
import DAL.PayrollHistoryDAL;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

public class PayrollHistoryBUS extends BaseBUS<PayrollHistoryDTO, Integer> {
    public static final PayrollHistoryBUS INSTANCE = new PayrollHistoryBUS();

    private PayrollHistoryBUS() {}

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

    // ✅ FIX: gọi trực tiếp DAL thay vì filter Java
    public ArrayList<PayrollHistoryDTO> getByEmployeeId(int employeeId) {
        ArrayList<PayrollHistoryDTO> result = PayrollHistoryDAL.getInstance().getByEmployeeId(employeeId);

        // sort newest first
        result.sort((p1, p2) -> Comparator
                .nullsLast(LocalDate::compareTo)
                .reversed()
                .compare(p1.getSalaryPeriod(), p2.getSalaryPeriod()));

        return result;
    }

    public PayrollHistoryDTO getByEmployeeAndPeriod(int employeeId, LocalDate salaryPeriod) {
        return PayrollHistoryDAL.getInstance().getByEmployeeAndPeriod(employeeId, salaryPeriod);
    }

    // ✅ FIX: thêm validate role
    public boolean insert(PayrollHistoryDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidPayrollInput(obj)) return false;

        if (!hasPermission(employeeRoleId)) return false;

        return PayrollHistoryDAL.getInstance().insert(obj);
    }

    public boolean update(PayrollHistoryDTO obj, int employeeRoleId, int employeeLoginId) {
        if (!isValidPayrollInput(obj)) return false;

        if (!hasPermission(employeeRoleId)) return false;

        return PayrollHistoryDAL.getInstance().update(obj);
    }

    public boolean delete(Integer id, int employeeRoleId, int employeeLoginId) {
        if (id == null || id <= 0) return false;

        if (!hasPermission(employeeRoleId)) return false;

        return PayrollHistoryDAL.getInstance().delete(id);
    }

    // ✅ đơn giản hóa permission (bạn có thể thay bằng Role enum)
    private boolean hasPermission(int roleId) {
        return roleId == 1; // ví dụ: 1 = admin
    }

    private boolean isValidPayrollInput(PayrollHistoryDTO obj) {
        if (obj == null) return false;
        if (obj.getEmployeeId() <= 0) return false;
        if (obj.getSalaryPeriod() == null) return false;
        if (obj.getNetSalary() == null || obj.getNetSalary().signum() < 0) return false;
        return true;
    }

    public ArrayList<PayrollHistoryDTO> getByEmployeeAndYear(int employeeId, int year) {
        ArrayList<PayrollHistoryDTO> result = PayrollHistoryDAL.getInstance()
                .getByEmployeeAndYear(employeeId, year);

        // sort theo tháng tăng dần
        result.sort(Comparator.comparing(PayrollHistoryDTO::getSalaryPeriod,
                Comparator.nullsLast(LocalDate::compareTo)));

        return result;
    }

    public ArrayList<PayrollHistoryDTO> getByEmployeeAndMonth(int empId, int month, int year) {
    ArrayList<PayrollHistoryDTO> result =
            PayrollHistoryDAL.getInstance().getByEmployeeAndMonth(empId, month, year);

    result.sort(Comparator.comparing(PayrollHistoryDTO::getSalaryPeriod));
    return result;
}
}