package DAL;

import DTO.AllowanceDTO;
import java.sql.*;

public class AllowanceDAL extends BaseDAL<AllowanceDTO, Integer> {
    public static final AllowanceDAL INSTANCE = new AllowanceDAL();

    private AllowanceDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "allowance", "id");
    }

    public static AllowanceDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected AllowanceDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new AllowanceDTO(
                resultSet.getInt("id"),
                resultSet.getInt("employee_id"),
                resultSet.getDate("salary_period") != null
                        ? resultSet.getDate("salary_period").toLocalDate()
                        : null,
                resultSet.getBigDecimal("attendance_bonus"),
                resultSet.getBigDecimal("annual_leave_days"),
                resultSet.getBigDecimal("transportation_support"),
                resultSet.getBigDecimal("accommodation_support"),
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("updated_at") != null
                        ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(AllowanceDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(employee_id, salary_period, attendance_bonus, annual_leave_days, transportation_support, accommodation_support, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, AllowanceDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getSalaryPeriod());
        statement.setObject(3, obj.getAttendanceBonus());
        statement.setObject(4, obj.getAnnualLeaveDays());
        statement.setObject(5, obj.getTransportationSupport());
        statement.setObject(6, obj.getAccommodationSupport());
        statement.setObject(7, obj.getCreatedAt());
        statement.setObject(8, obj.getUpdatedAt());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, salary_period = ?, attendance_bonus = ?, annual_leave_days = ?, transportation_support = ?, accommodation_support = ?, created_at = ?, updated_at = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, AllowanceDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getSalaryPeriod());
        statement.setObject(3, obj.getAttendanceBonus());
        statement.setObject(4, obj.getAnnualLeaveDays());
        statement.setObject(5, obj.getTransportationSupport());
        statement.setObject(6, obj.getAccommodationSupport());
        statement.setObject(7, obj.getCreatedAt());
        statement.setObject(8, obj.getUpdatedAt());
        statement.setInt(9, obj.getId());
    }

}
