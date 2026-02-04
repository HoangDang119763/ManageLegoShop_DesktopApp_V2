package DAL;

import DTO.LeaveRequestDTO;
import java.sql.*;

public class LeaveRequestDAL extends BaseDAL<LeaveRequestDTO, Integer> {
    public static final LeaveRequestDAL INSTANCE = new LeaveRequestDAL();

    private LeaveRequestDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "leave_request", "id");
    }

    public static LeaveRequestDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected LeaveRequestDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new LeaveRequestDTO(
                resultSet.getInt("id"),
                resultSet.getString("type"),
                resultSet.getString("content"),
                resultSet.getDate("start_date") != null ? resultSet.getDate("start_date").toLocalDate() : null,
                resultSet.getDate("end_date") != null ? resultSet.getDate("end_date").toLocalDate() : null,
                resultSet.getInt("status_id"),
                resultSet.getInt("employee_id"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(LeaveRequestDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(type, content, start_date, end_date, status_id, employee_id) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, LeaveRequestDTO obj) throws SQLException {
        statement.setString(1, obj.getType());
        statement.setString(2, obj.getContent());
        statement.setDate(3, obj.getStartDate() != null ? java.sql.Date.valueOf(obj.getStartDate()) : null);
        statement.setDate(4, obj.getEndDate() != null ? java.sql.Date.valueOf(obj.getEndDate()) : null);
        statement.setInt(5, obj.getStatusId());
        statement.setInt(6, obj.getEmployeeId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET type = ?, content = ?, start_date = ?, end_date = ?, status_id = ?, employee_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, LeaveRequestDTO obj) throws SQLException {
        statement.setString(1, obj.getType());
        statement.setString(2, obj.getContent());
        statement.setDate(3, obj.getStartDate() != null ? java.sql.Date.valueOf(obj.getStartDate()) : null);
        statement.setDate(4, obj.getEndDate() != null ? java.sql.Date.valueOf(obj.getEndDate()) : null);
        statement.setInt(5, obj.getStatusId());
        statement.setInt(6, obj.getEmployeeId());
        statement.setInt(7, obj.getId());
    }

    @Override
    protected boolean hasSoftDelete() {
        return false;
    }
}
