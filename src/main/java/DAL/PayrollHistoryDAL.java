package DAL;

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
                resultSet.getBigDecimal("temporary_salary"),
                resultSet.getBigDecimal("overtime_amount"),
                resultSet.getBigDecimal("total_allowance"),
                resultSet.getBigDecimal("total_bonus"),
                resultSet.getBigDecimal("total_deduction"),
                resultSet.getBigDecimal("total_fine"),
                resultSet.getBigDecimal("net_salary"),
                resultSet.getTimestamp("paid_date") != null
                        ? resultSet.getTimestamp("paid_date").toLocalDateTime()
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
        return "(employee_id, salary_period, temporary_salary, overtime_amount, total_allowance, total_bonus, total_deduction, total_fine, net_salary, paid_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, PayrollHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getSalaryPeriod());
        statement.setObject(3, obj.getTemporarySalary());
        statement.setObject(4, obj.getOvertimeAmount());
        statement.setObject(5, obj.getTotalAllowance());
        statement.setObject(6, obj.getTotalBonus());
        statement.setObject(7, obj.getTotalDeduction());
        statement.setObject(8, obj.getTotalFine());
        statement.setObject(9, obj.getNetSalary());
        statement.setObject(10, obj.getPaidDate());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, salary_period = ?, temporary_salary = ?, overtime_amount = ?, total_allowance = ?, total_bonus = ?, total_deduction = ?, total_fine = ?, net_salary = ?, paid_date = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, PayrollHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getSalaryPeriod());
        statement.setObject(3, obj.getTemporarySalary());
        statement.setObject(4, obj.getOvertimeAmount());
        statement.setObject(5, obj.getTotalAllowance());
        statement.setObject(6, obj.getTotalBonus());
        statement.setObject(7, obj.getTotalDeduction());
        statement.setObject(8, obj.getTotalFine());
        statement.setObject(9, obj.getNetSalary());
        statement.setObject(10, obj.getPaidDate());
        statement.setInt(11, obj.getId());
    }
}
