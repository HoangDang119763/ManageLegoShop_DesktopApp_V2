package DAL;

import DTO.HrStatisticDTO;
import DTO.PayrollHistoryDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PayrollHistoryDAL extends BaseDAL<PayrollHistoryDTO, Integer> {
    public static final PayrollHistoryDAL INSTANCE = new PayrollHistoryDAL();

    private PayrollHistoryDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "payroll_history", "id");
    }

    public static PayrollHistoryDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected PayrollHistoryDTO mapResultSetToObject(ResultSet rs) throws SQLException {
        return new PayrollHistoryDTO(
                rs.getInt("id"),
                rs.getInt("employee_id"),
                rs.getDate("salary_period") != null ? rs.getDate("salary_period").toLocalDate() : null,
                rs.getBigDecimal("base_salary"),
                rs.getInt("standard_work_days"),
                rs.getBigDecimal("actual_work_days"),
                rs.getBigDecimal("bhxh_amount"),
                rs.getBigDecimal("bhyt_amount"),
                rs.getBigDecimal("bhtn_amount"),
                rs.getBigDecimal("total_insurance"),
                rs.getBigDecimal("violation_amount"),
                rs.getBigDecimal("reward_amount"),
                rs.getBigDecimal("total_allowance"),
                rs.getBigDecimal("overtime_amount"),
                rs.getBigDecimal("taxable_income"),
                rs.getBigDecimal("tax_percent"),
                rs.getBigDecimal("tax_amount"),
                rs.getBigDecimal("net_salary"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(PayrollHistoryDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(employee_id, salary_period, base_salary, standard_work_days, actual_work_days, bhxh_amount, bhyt_amount, bhtn_amount, total_insurance, violation_amount, reward_amount, total_allowance, overtime_amount, taxable_income, tax_percent, tax_amount, net_salary, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, PayrollHistoryDTO obj) throws SQLException {
        ps.setInt(1, obj.getEmployeeId());

        // ✅ FIX LocalDate
        ps.setDate(2, obj.getSalaryPeriod() != null ? Date.valueOf(obj.getSalaryPeriod()) : null);

        ps.setBigDecimal(3, obj.getBaseSalary());
        ps.setInt(4, obj.getStandardWorkDays());
        ps.setBigDecimal(5, obj.getActualWorkDays());
        ps.setBigDecimal(6, obj.getBhxhAmount());
        ps.setBigDecimal(7, obj.getBhytAmount());
        ps.setBigDecimal(8, obj.getBhtnAmount());
        ps.setBigDecimal(9, obj.getTotalInsurance());
        ps.setBigDecimal(10, obj.getViolationAmount());
        ps.setBigDecimal(11, obj.getRewardAmount());
        ps.setBigDecimal(12, obj.getTotalAllowance());
        ps.setBigDecimal(13, obj.getOvertimeAmount());
        ps.setBigDecimal(14, obj.getTaxableIncome());
        ps.setBigDecimal(15, obj.getTaxPercent());
        ps.setBigDecimal(16, obj.getTaxAmount());
        ps.setBigDecimal(17, obj.getNetSalary());

        ps.setTimestamp(18, obj.getCreatedAt() != null
                ? Timestamp.valueOf(obj.getCreatedAt())
                : null);
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, salary_period = ?, base_salary = ?, standard_work_days = ?, actual_work_days = ?, bhxh_amount = ?, bhyt_amount = ?, bhtn_amount = ?, total_insurance = ?, violation_amount = ?, reward_amount = ?, total_allowance = ?, overtime_amount = ?, taxable_income = ?, tax_percent = ?, tax_amount = ?, net_salary = ?, created_at = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, PayrollHistoryDTO obj) throws SQLException {
        ps.setInt(1, obj.getEmployeeId());
        ps.setDate(2, obj.getSalaryPeriod() != null ? Date.valueOf(obj.getSalaryPeriod()) : null);
        ps.setBigDecimal(3, obj.getBaseSalary());
        ps.setInt(4, obj.getStandardWorkDays());
        ps.setBigDecimal(5, obj.getActualWorkDays());
        ps.setBigDecimal(6, obj.getBhxhAmount());
        ps.setBigDecimal(7, obj.getBhytAmount());
        ps.setBigDecimal(8, obj.getBhtnAmount());
        ps.setBigDecimal(9, obj.getTotalInsurance());
        ps.setBigDecimal(10, obj.getViolationAmount());
        ps.setBigDecimal(11, obj.getRewardAmount());
        ps.setBigDecimal(12, obj.getTotalAllowance());
        ps.setBigDecimal(13, obj.getOvertimeAmount());
        ps.setBigDecimal(14, obj.getTaxableIncome());
        ps.setBigDecimal(15, obj.getTaxPercent());
        ps.setBigDecimal(16, obj.getTaxAmount());
        ps.setBigDecimal(17, obj.getNetSalary());
        ps.setTimestamp(18, obj.getCreatedAt() != null ? Timestamp.valueOf(obj.getCreatedAt()) : null);
        ps.setInt(19, obj.getId());
    }

    // =========================
    // ✅ NEW METHODS FOR BUS
    // =========================

    public ArrayList<PayrollHistoryDTO> getByEmployeeId(int employeeId) {
        String sql = "SELECT * FROM payroll_history WHERE employee_id = ? ORDER BY salary_period DESC";
        ArrayList<PayrollHistoryDTO> list = new ArrayList<>();

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToObject(rs));
            }

        } catch (Exception e) {
            System.err.println("Error getByEmployeeId: " + e.getMessage());
        }

        return list;
    }

    public PayrollHistoryDTO getByEmployeeAndPeriod(int employeeId, LocalDate period) {
        String sql = "SELECT * FROM payroll_history WHERE employee_id = ? AND salary_period = ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setDate(2, Date.valueOf(period));

            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return mapResultSetToObject(rs);

        } catch (Exception e) {
            System.err.println("Error getByEmployeeAndPeriod: " + e.getMessage());
        }

        return null;
    }

    public ArrayList<PayrollHistoryDTO> getByEmployeeAndYear(int employeeId, int year) {
        String sql = "SELECT * FROM payroll_history WHERE employee_id = ? AND YEAR(salary_period) = ? ORDER BY salary_period ASC";

        ArrayList<PayrollHistoryDTO> list = new ArrayList<>();

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setInt(2, year);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToObject(rs));
            }

        } catch (Exception e) {
            System.err.println("Error getByEmployeeAndYear: " + e.getMessage());
        }

        return list;
    }

    // =========================
    // HR STATISTIC
    // =========================

    public HrStatisticDTO.SalaryStat getSalaryStatForMonth(int month, int year) {
        String sql = """
                SELECT COUNT(*) AS emp_count,
                       COALESCE(SUM(net_salary), 0) AS total_net,
                       COALESCE(AVG(net_salary), 0) AS avg_net,
                       COALESCE(MAX(net_salary), 0) AS max_net,
                       COALESCE(MIN(net_salary), 0) AS min_net,
                       COALESCE(SUM(total_insurance), 0) AS total_ins
                FROM payroll_history
                WHERE MONTH(salary_period) = ? AND YEAR(salary_period) = ?
                """;

        HrStatisticDTO.SalaryStat stat = new HrStatisticDTO.SalaryStat();

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, month);
            ps.setInt(2, year);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                stat.setEmployeeCount(rs.getInt("emp_count"));
                stat.setTotalNet(rs.getBigDecimal("total_net"));
                stat.setAvgNet(rs.getBigDecimal("avg_net"));
                stat.setMaxNet(rs.getBigDecimal("max_net"));
                stat.setMinNet(rs.getBigDecimal("min_net"));
                stat.setTotalInsurance(rs.getBigDecimal("total_ins"));
            }

        } catch (Exception e) {
            System.err.println("Error getting salary stat: " + e.getMessage());
        }

        return stat;
    }

    public ArrayList<PayrollHistoryDTO> getByEmployeeAndMonth(int empId, int month, int year) {
        String sql = """
                    SELECT * FROM payroll_history
                    WHERE employee_id = ?
                    AND MONTH(salary_period) = ?
                    AND YEAR(salary_period) = ?
                    ORDER BY salary_period ASC
                """;

        ArrayList<PayrollHistoryDTO> list = new ArrayList<>();

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, empId);
            ps.setInt(2, month);
            ps.setInt(3, year);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSetToObject(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public java.util.List<HrStatisticDTO.SalaryRow> getSalaryRows(int month, int year) {
        String sql = """
                SELECT CONCAT(e.first_name, ' ', e.last_name) AS full_name,
                       COALESCE(d.name, '—') AS dept_name,
                       COALESCE(p.name, '—') AS pos_name,
                       ph.base_salary, ph.net_salary,
                       ph.actual_work_days, ph.overtime_amount,
                       ph.total_insurance
                FROM payroll_history ph
                LEFT JOIN employee e ON e.id = ph.employee_id
                LEFT JOIN department d ON d.id = e.department_id
                LEFT JOIN position p ON p.id = e.position_id
                WHERE MONTH(ph.salary_period) = ? AND YEAR(ph.salary_period) = ?
                ORDER BY ph.net_salary DESC
                """;
        java.util.List<HrStatisticDTO.SalaryRow> list = new java.util.ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HrStatisticDTO.SalaryRow(
                            rs.getString("full_name"),
                            rs.getString("dept_name"),
                            rs.getString("pos_name"),
                            rs.getBigDecimal("base_salary"),
                            rs.getBigDecimal("net_salary"),
                            rs.getBigDecimal("actual_work_days"),
                            rs.getBigDecimal("overtime_amount"),
                            rs.getBigDecimal("total_insurance")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting salary rows: " + e.getMessage());
        }
        return list;
    }

    public java.math.BigDecimal sumNetSalaryByMonthYear(int month, int year) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        final String sql = """
                SELECT COALESCE(SUM(net_salary), 0) AS total_net
                FROM payroll_history
                WHERE MONTH(salary_period) = ? AND YEAR(salary_period) = ?
                """;
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    java.math.BigDecimal dbTotal = rs.getBigDecimal("total_net");
                    if (dbTotal != null) {
                        total = dbTotal;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error summing net salary by month/year: " + e.getMessage());
        }
        return total;
    }
}