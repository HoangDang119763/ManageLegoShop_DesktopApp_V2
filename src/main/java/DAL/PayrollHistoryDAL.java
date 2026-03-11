package DAL;

import DTO.HrStatisticDTO;
import DTO.PayrollHistoryDTO;
import java.sql.*;

public class PayrollHistoryDAL extends BaseDAL<PayrollHistoryDTO, Integer> {
    public static final PayrollHistoryDAL INSTANCE = new PayrollHistoryDAL();

    private PayrollHistoryDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "payroll_history", "id");
    }

    public static PayrollHistoryDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected PayrollHistoryDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new PayrollHistoryDTO(
                resultSet.getInt("id"),
                resultSet.getInt("employee_id"),
                resultSet.getDate("salary_period") != null
                        ? resultSet.getDate("salary_period").toLocalDate()
                        : null,
                resultSet.getBigDecimal("base_salary"),
                resultSet.getInt("standard_work_days"),
                resultSet.getBigDecimal("actual_work_days"),
                resultSet.getBigDecimal("bhxh_amount"),
                resultSet.getBigDecimal("bhyt_amount"),
                resultSet.getBigDecimal("bhtn_amount"),
                resultSet.getBigDecimal("total_insurance"),
                resultSet.getBigDecimal("violation_amount"),
                resultSet.getBigDecimal("reward_amount"),
                resultSet.getBigDecimal("total_allowance"),
                resultSet.getBigDecimal("overtime_amount"),
                resultSet.getBigDecimal("taxable_income"),
                resultSet.getBigDecimal("tax_percent"),
                resultSet.getBigDecimal("tax_amount"),
                resultSet.getBigDecimal("net_salary"),
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
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
    protected void setInsertParameters(PreparedStatement statement, PayrollHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getSalaryPeriod());
        statement.setObject(3, obj.getBaseSalary());
        statement.setInt(4, obj.getStandardWorkDays());
        statement.setObject(5, obj.getActualWorkDays());
        statement.setObject(6, obj.getBhxhAmount());
        statement.setObject(7, obj.getBhytAmount());
        statement.setObject(8, obj.getBhtnAmount());
        statement.setObject(9, obj.getTotalInsurance());
        statement.setObject(10, obj.getViolationAmount());
        statement.setObject(11, obj.getRewardAmount());
        statement.setObject(12, obj.getTotalAllowance());
        statement.setObject(13, obj.getOvertimeAmount());
        statement.setObject(14, obj.getTaxableIncome());
        statement.setObject(15, obj.getTaxPercent());
        statement.setObject(16, obj.getTaxAmount());
        statement.setObject(17, obj.getNetSalary());
        statement.setObject(18, obj.getCreatedAt());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, salary_period = ?, base_salary = ?, standard_work_days = ?, actual_work_days = ?, bhxh_amount = ?, bhyt_amount = ?, bhtn_amount = ?, total_insurance = ?, violation_amount = ?, reward_amount = ?, total_allowance = ?, overtime_amount = ?, taxable_income = ?, tax_percent = ?, tax_amount = ?, net_salary = ?, created_at = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, PayrollHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getSalaryPeriod());
        statement.setObject(3, obj.getBaseSalary());
        statement.setInt(4, obj.getStandardWorkDays());
        statement.setObject(5, obj.getActualWorkDays());
        statement.setObject(6, obj.getBhxhAmount());
        statement.setObject(7, obj.getBhytAmount());
        statement.setObject(8, obj.getBhtnAmount());
        statement.setObject(9, obj.getTotalInsurance());
        statement.setObject(10, obj.getViolationAmount());
        statement.setObject(11, obj.getRewardAmount());
        statement.setObject(12, obj.getTotalAllowance());
        statement.setObject(13, obj.getOvertimeAmount());
        statement.setObject(14, obj.getTaxableIncome());
        statement.setObject(15, obj.getTaxPercent());
        statement.setObject(16, obj.getTaxAmount());
        statement.setObject(17, obj.getNetSalary());
        statement.setObject(18, obj.getCreatedAt());
        statement.setInt(19, obj.getId());
    }

    // ===== HR STATISTIC HELPERS =====

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
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stat.setEmployeeCount(rs.getInt("emp_count"));
                    stat.setTotalNet(rs.getBigDecimal("total_net"));
                    stat.setAvgNet(rs.getBigDecimal("avg_net"));
                    stat.setMaxNet(rs.getBigDecimal("max_net"));
                    stat.setMinNet(rs.getBigDecimal("min_net"));
                    stat.setTotalInsurance(rs.getBigDecimal("total_ins"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting salary stat: " + e.getMessage());
        }
        return stat;
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
