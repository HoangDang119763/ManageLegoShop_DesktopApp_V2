package DAL;

import DTO.DepartmentDTO;
import java.sql.*;
import java.util.ArrayList;

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

    public boolean existsByIdAndStatus(int departmentId, int statusId) {
        // SELECT 1 là cách nhanh nhất để check tồn tại
        String query = "SELECT 1 FROM department WHERE id = ? AND status_id = ? LIMIT 1";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, departmentId);
            ps.setInt(2, statusId);

            try (ResultSet rs = ps.executeQuery()) {
                // Chỉ cần rs.next() là đủ để biết roleId có tồn tại hay không
                return rs.next();
            }
        } catch (SQLException e) {
            // Log lỗi rõ ràng hơn để dễ debug
            System.err.println(
                    "[ERROR] Error checking department existence for ID: " + departmentId + ", Status ID: " + statusId);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get all departments with a specific status ID
     */
    public ArrayList<DepartmentDTO> getByStatusId(int statusId) {
        ArrayList<DepartmentDTO> list = new ArrayList<>();
        String query = "SELECT * FROM department WHERE status_id = ? ORDER BY id";
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, statusId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting departments by status: " + e.getMessage());
        }
        return list;
    }
}
