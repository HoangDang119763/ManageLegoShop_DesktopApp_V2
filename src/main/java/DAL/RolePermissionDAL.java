package DAL;

import DTO.RolePermissionDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class RolePermissionDAL extends BaseDAL<RolePermissionDTO, Integer> {
    private static final RolePermissionDAL INSTANCE = new RolePermissionDAL();

    private RolePermissionDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "role_permission", "role_id");
    }

    public static RolePermissionDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected RolePermissionDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new RolePermissionDTO(
                resultSet.getInt("role_id"),
                resultSet.getInt("permission_id"));
    }

    @Override
    protected String getInsertQuery() {
        // Chỉ còn 2 cột ID
        return "(role_id, permission_id) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, RolePermissionDTO obj) throws SQLException {
        statement.setInt(1, obj.getRoleId());
        statement.setInt(2, obj.getPermissionId());
    }

    /**
     * Trong mô hình mới, ta không dùng UPDATE.
     * Để tước quyền, ta dùng DELETE theo RoleID và PermissionID.
     */
    public boolean revokePermission(int roleId, int permissionId) {
        String query = "DELETE FROM role_permission WHERE role_id = ? AND permission_id = ?";
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roleId);
            ps.setInt(2, permissionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean exists(int roleId, int permissionId) {
        String query = "SELECT 1 FROM role_permission WHERE role_id = ? AND permission_id = ?";
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, roleId);
            ps.setInt(2, permissionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean insertListRolePermission(int roleId, ArrayList<Integer> permissionIds) {
        String query = "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)";
        try (Connection connection = connectionFactory.newConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Integer pId : permissionIds) {
                    statement.setInt(1, roleId);
                    statement.setInt(2, pId);
                    statement.addBatch();
                }
                statement.executeBatch();
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<RolePermissionDTO> getAllRolePermissionByRoleId(int roleID) {
        String query = "SELECT * FROM role_permission WHERE role_id = ?";
        ArrayList<RolePermissionDTO> list = new ArrayList<>();
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, roleID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSetToObject(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Set<Integer> getAllowedModuleIdsByRoleId(int roleId) {
        Set<Integer> moduleIds = new HashSet<>();
        // SQL: Lấy tất cả module_id duy nhất mà roleId này có quyền
        String sql = "SELECT DISTINCT p.module_id " +
                "FROM role_permission rp " +
                "JOIN permission p ON rp.permission_id = p.id " +
                "WHERE rp.role_id = ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    moduleIds.add(rs.getInt("module_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy allowedModuleIds: " + e.getMessage());
        }
        return moduleIds;
    }
}