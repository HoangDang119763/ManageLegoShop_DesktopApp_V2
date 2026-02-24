package DAL;

import DTO.PermissionDTO;

import java.sql.*;
import java.util.ArrayList;

public class PermissionDAL extends BaseDAL<PermissionDTO, Integer> {
    private static final PermissionDAL INSTANCE = new PermissionDAL();

    private PermissionDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "permission", "id");
    }

    public static PermissionDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected PermissionDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new PermissionDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("permission_key"),
                resultSet.getInt("module_id"));
    }

    @Override
    protected String getInsertQuery() {
        throw new UnsupportedOperationException("Cannot insert permission records.");
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Cannot update permission records.");
    }

    @Override
    public boolean delete(Integer id) {
        throw new UnsupportedOperationException("Cannot delete permission records.");
    }

    public ArrayList<PermissionDTO> getAllRoleByModuleId(int moduleId) {
        final String query = "SELECT * FROM permission WHERE module_id = ?";
        ArrayList<PermissionDTO> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, moduleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSetToObject(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving " + table + ": " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm quyền theo permission_key
     * 
     * @param permissionKey Khóa quyền cần tìm
     * @return PermissionDTO hoặc null nếu không tìm thấy
     */
    public PermissionDTO getByPermissionKey(String permissionKey) {
        final String query = "SELECT * FROM permission WHERE permission_key = ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, permissionKey);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToObject(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving permission by key: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy danh sách permission_key của một role
     * 
     * @param roleId ID của role
     * @return ArrayList<String> chứa các permission_key
     */
    public ArrayList<String> getPermissionKeysByRoleId(int roleId) {
        String sql = "SELECT p.permission_key FROM role_permission rp " +
                "JOIN permission p ON rp.permission_id = p.id " +
                "WHERE rp.role_id = ?";
        ArrayList<String> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, roleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(resultSet.getString("permission_key"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving permissions by role: " + e.getMessage());
        }
        return list;
    }

    public boolean isRoleHavePermission(int roleId, String permissionKey) {
        // Dùng SELECT 1 và rs.next() là cách nhanh nhất để check tồn tại
        String sql = "SELECT 1 FROM role_permission rp " +
                "JOIN permission p ON rp.permission_id = p.id " +
                "WHERE rp.role_id = ? AND p.permission_key = ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, roleId);
            statement.setString(2, permissionKey);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Có dòng trả về tức là có quyền
            }
        } catch (SQLException e) {
            System.err.println("Error checking role permission: " + e.getMessage());
        }
        return false;
    }
}
