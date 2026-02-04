package DAL;

import DTO.RoleDTO;

import java.sql.*;

public class RoleDAL extends BaseDAL<RoleDTO, Integer> {
    private static final RoleDAL INSTANCE = new RoleDAL();

    private RoleDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "role", "id");
    }

    public static RoleDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected RoleDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new RoleDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getInt("start_experience"),
                resultSet.getInt("end_experience"),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("updated_at") != null ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null,
                resultSet.getObject("salary_id") != null ? resultSet.getInt("salary_id") : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(RoleDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, description, start_experience, end_experience, created_at, updated_at, salary_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, RoleDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getDescription());
        statement.setInt(3, obj.getStartExperience());
        statement.setInt(4, obj.getEndExperience());
        statement.setObject(5, obj.getCreatedAt());
        statement.setObject(6, obj.getUpdatedAt());
        statement.setObject(7, obj.getSalaryId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, description = ?, start_experience = ?, end_experience = ?, updated_at = ?, salary_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, RoleDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getDescription());
        statement.setInt(3, obj.getStartExperience());
        statement.setInt(4, obj.getEndExperience());
        statement.setObject(5, obj.getUpdatedAt());
        statement.setObject(6, obj.getSalaryId());
        statement.setInt(7, obj.getId());
    }

    public boolean updateBasic(RoleDTO obj) {
        String query = "UPDATE role SET name = ?, description = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getName());
            statement.setString(2, obj.getDescription());
            statement.setInt(3, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating basic role: " + e.getMessage());
            return false;
        }
    }
}
