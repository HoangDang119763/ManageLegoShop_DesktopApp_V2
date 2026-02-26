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
}
