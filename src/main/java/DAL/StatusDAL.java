package DAL;

import DTO.StatusDTO;
import java.sql.*;

public class StatusDAL extends BaseDAL<StatusDTO, Integer> {
    public static final StatusDAL INSTANCE = new StatusDAL();

    private StatusDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "status", "id");
    }

    public static StatusDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected StatusDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new StatusDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("type"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(StatusDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, description, type) VALUES (?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, StatusDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getDescription());
        statement.setString(3, obj.getType());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, description = ?, type = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, StatusDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getDescription());
        statement.setString(3, obj.getType());
        statement.setInt(4, obj.getId());
    }
}
