package DAL;

import DTO.ReportDTO;
import java.sql.*;

public class ReportDAL extends BaseDAL<ReportDTO, Integer> {
    public static final ReportDAL INSTANCE = new ReportDAL();

    private ReportDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "report", "id");
    }

    public static ReportDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected ReportDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new ReportDTO(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getString("level"),
                resultSet.getString("category"),
                resultSet.getInt("employee_id"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(ReportDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(title, description, created_at, level, category, employee_id) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, ReportDTO obj) throws SQLException {
        statement.setString(1, obj.getTitle());
        statement.setString(2, obj.getDescription());
        statement.setObject(3, obj.getCreatedAt());
        statement.setString(4, obj.getLevel());
        statement.setString(5, obj.getCategory());
        statement.setInt(6, obj.getEmployeeId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET title = ?, description = ?, created_at = ?, level = ?, category = ?, employee_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, ReportDTO obj) throws SQLException {
        statement.setString(1, obj.getTitle());
        statement.setString(2, obj.getDescription());
        statement.setObject(3, obj.getCreatedAt());
        statement.setString(4, obj.getLevel());
        statement.setString(5, obj.getCategory());
        statement.setInt(6, obj.getEmployeeId());
        statement.setInt(7, obj.getId());
    }

    @Override
    protected boolean hasSoftDelete() {
        return false;
    }
}
