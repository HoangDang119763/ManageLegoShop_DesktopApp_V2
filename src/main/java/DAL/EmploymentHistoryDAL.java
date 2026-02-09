package DAL;

import DTO.EmploymentHistoryDTO;
import java.sql.*;

public class EmploymentHistoryDAL extends BaseDAL<EmploymentHistoryDTO, Integer> {
    public static final EmploymentHistoryDAL INSTANCE = new EmploymentHistoryDAL();

    private EmploymentHistoryDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "employment_history", "id");
    }

    public static EmploymentHistoryDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected EmploymentHistoryDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new EmploymentHistoryDTO(
                resultSet.getInt("id"),
                resultSet.getInt("employee_id"),
                resultSet.getInt("department_id"),
                resultSet.getInt("role_id"),
                resultSet.getDate("effective_date") != null ? resultSet.getDate("effective_date").toLocalDate() : null,
                resultSet.getObject("approver_id") != null ? resultSet.getInt("approver_id") : null,
                resultSet.getInt("status_id"),
                resultSet.getString("reason"),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(EmploymentHistoryDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(employee_id, department_id, role_id, effective_date, approver_id, status_id, reason) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, EmploymentHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setInt(2, obj.getDepartmentId());
        statement.setInt(3, obj.getRoleId());
        statement.setDate(4, obj.getEffectiveDate() != null ? java.sql.Date.valueOf(obj.getEffectiveDate()) : null);
        statement.setObject(5, obj.getApproverId());
        statement.setInt(6, obj.getStatusId());
        statement.setString(7, obj.getReason());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, department_id = ?, role_id = ?, effective_date = ?, approver_id = ?, status_id = ?, reason = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, EmploymentHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setInt(2, obj.getDepartmentId());
        statement.setInt(3, obj.getRoleId());
        statement.setDate(4, obj.getEffectiveDate() != null ? java.sql.Date.valueOf(obj.getEffectiveDate()) : null);
        statement.setObject(5, obj.getApproverId());
        statement.setInt(6, obj.getStatusId());
        statement.setString(7, obj.getReason());
        statement.setInt(8, obj.getId());
    }

}
