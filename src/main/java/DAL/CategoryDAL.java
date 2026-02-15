package DAL;

import DTO.CategoryDTO;
import java.sql.*;
import java.util.ArrayList;

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

    public int getMaxId() {
        String query = "SELECT MAX(id) as max_id FROM category";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("max_id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting max category ID: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Kiểm tra xem tên category đã tồn tại hay chưa (loại trừ ID được chỉ định)
     * 
     * @param name      Tên category cần kiểm tra
     * @param excludeId ID để loại trừ (dùng -1 nếu không muốn loại trừ)
     * @return true nếu tên đã tồn tại, false nếu chưa
     */
    public boolean existsByName(String name, int excludeId) {
        String query = "SELECT 1 FROM category WHERE LOWER(name) = LOWER(?) AND id != ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name != null ? name.trim() : "");
            statement.setInt(2, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking category name existence: " + e.getMessage());
        }
        return false;
    }

    public ArrayList<CategoryDTO> getFiltered(String keyword, int status, int page, int pageSize) {
        ArrayList<CategoryDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM category WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();

        // 1. Lọc theo trạng thái
        if (status != -1) {
            sql.append(" AND status_id = ?");
            params.add(status);
        }

        // 2. Tìm kiếm thông minh (Mã hoặc Tên)
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (CAST(id AS CHAR) LIKE ? OR LOWER(name) LIKE ?)");
            String dbKeyword = "%" + keyword + "%"; // Keyword đã được BUS xử lý lowercase
            params.add(dbKeyword);
            params.add(dbKeyword);
        }

        // 4. PHÂN TRANG (PAGINATION)
        if (page > 0 && pageSize > 0) {
            sql.append(" LIMIT ? OFFSET ?");
            int offset = (page - 1) * pageSize;
            params.add(pageSize);
            params.add(offset);
        }

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean existsByIdAndStatus(int id, int activeStatusId) {
        String sql = "SELECT 1 FROM category WHERE id = ? AND status_id = ? LIMIT 1";
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, activeStatusId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}
