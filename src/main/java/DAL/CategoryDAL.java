package DAL;

import DTO.CategoryDTO;
import ENUM.Status;
import java.sql.*;

public class CategoryDAL extends BaseDAL<CategoryDTO, Integer> {
    public static final CategoryDAL INSTANCE = new CategoryDAL();

    private CategoryDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "category", "id");
    }

    public static CategoryDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected CategoryDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new CategoryDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
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
    protected void setGeneratedKey(CategoryDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, status_id) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, CategoryDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setInt(2, obj.getStatusId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, status_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, CategoryDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setInt(2, obj.getStatusId());
        statement.setInt(3, obj.getId());
    }

}
