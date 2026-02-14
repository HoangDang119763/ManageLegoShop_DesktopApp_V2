package DAL;

import DTO.CategoryDTO;
import DTO.CustomerDTO;
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
        // Thêm created_at và updated_at vào danh sách cột
        return "(name, status_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, CategoryDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setInt(2, obj.getStatusId());

        // Nạp thời gian từ Java truyền xuống
        statement.setTimestamp(3, obj.getCreatedAt() != null ? java.sql.Timestamp.valueOf(obj.getCreatedAt()) : null);
        statement.setTimestamp(4, obj.getUpdatedAt() != null ? java.sql.Timestamp.valueOf(obj.getUpdatedAt()) : null);
    }

    @Override
    protected String getUpdateQuery() {
        // Thêm updated_at vào câu lệnh SET
        return "SET name = ?, status_id = ?, updated_at = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, CategoryDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setInt(2, obj.getStatusId());

        // Tham số thứ 3 là thời gian cập nhật
        statement.setTimestamp(3, obj.getUpdatedAt() != null ? java.sql.Timestamp.valueOf(obj.getUpdatedAt()) : null);

        // Tham số cuối là ID để WHERE
        statement.setInt(4, obj.getId());
    }
    public boolean updateStatus(int id, int newStatusId) {
        String query = "UPDATE category SET status_id = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, newStatusId);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating category status: " + e.getMessage());
            return false;
        }
    }

}
