package DAL;

import DTO.RoleDTO;
import java.sql.*;
import java.time.LocalDateTime;

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

    // --- CẤU HÌNH INSERT ---

    @Override
    protected String getInsertQuery() {
        // Bao gồm tất cả các trường để Java nắm quyền kiểm soát thời gian
        return "(name, description, start_experience, end_experience, created_at, updated_at, salary_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, RoleDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getDescription());
        statement.setInt(3, obj.getStartExperience());
        statement.setInt(4, obj.getEndExperience());

        // Chuyển đổi LocalDateTime sang Timestamp để SQL hiểu
        statement.setTimestamp(5, obj.getCreatedAt() != null ? Timestamp.valueOf(obj.getCreatedAt()) : null);
        statement.setTimestamp(6, obj.getUpdatedAt() != null ? Timestamp.valueOf(obj.getUpdatedAt()) : null);

        if (obj.getSalaryId() != null)
            statement.setInt(7, obj.getSalaryId());
        else
            statement.setNull(7, Types.INTEGER);
    }

    // --- CẤU HÌNH UPDATE ---

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
        statement.setTimestamp(5, obj.getUpdatedAt() != null ? Timestamp.valueOf(obj.getUpdatedAt()) : null);

        if (obj.getSalaryId() != null)
            statement.setInt(6, obj.getSalaryId());
        else
            statement.setNull(6, Types.INTEGER);

        statement.setInt(7, obj.getId());
    }

    // --- CÁC HÀM BỔ SUNG CHO STATELESS BUS ---

    /**
     * Kiểm tra trùng tên chức vụ (Thay thế hoàn toàn cho việc duyệt arrLocal)
     */
    public boolean existsByName(String name, int excludeId) {
        String query = "SELECT COUNT(*) FROM role WHERE LOWER(name) = LOWER(?) AND id != ?";
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, name);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật nhanh thông tin cơ bản
     * Đã thêm updated_at để Java quản lý thời gian đồng bộ
     */
    public boolean updateBasic(RoleDTO obj) {
        String query = "UPDATE role SET name = ?, description = ?, updated_at = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getName());
            statement.setString(2, obj.getDescription());
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.setInt(4, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating basic role: " + e.getMessage());
            return false;
        }
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
}