package DAL;

import DTO.DepartmentDTO;
import java.sql.*;

public class DepartmentDAL extends BaseDAL<DepartmentDTO, Integer> {
    public static final DepartmentDAL INSTANCE = new DepartmentDAL();

    private DepartmentDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "department", "id");
    }

    public static DepartmentDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected DepartmentDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new DepartmentDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getInt("status_id"),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("updated_at") != null ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(DepartmentDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, description, status_id) VALUES (?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, DepartmentDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getDescription());
        statement.setInt(3, obj.getStatusId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, description = ?, status_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, DepartmentDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getDescription());
        statement.setInt(3, obj.getStatusId());
        statement.setInt(4, obj.getId());
    }
}
