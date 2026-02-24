package DAL;

import DTO.AccountDTO;
import java.sql.*;

public class AccountDAL extends BaseDAL<AccountDTO, Integer> {
    private static final AccountDAL INSTANCE = new AccountDAL();

    private AccountDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "account", "id");
    }

    public static AccountDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected AccountDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        int statusId = resultSet.getInt("status_id");

        return new AccountDTO(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("last_login") != null ? resultSet.getTimestamp("last_login").toLocalDateTime()
                        : null,
                statusId);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(AccountDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(username, password, created_at, last_login, status_id) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, AccountDTO obj) throws SQLException {
        statement.setString(1, obj.getUsername());
        statement.setString(2, obj.getPassword());
        statement.setObject(3, obj.getCreatedAt());
        statement.setObject(4, obj.getLastLogin());
        statement.setInt(5, obj.getStatusId());
    }

    @Override
    protected String getUpdateQuery() {
        return "(username, password, last_login, status_id) VALUES (?, ?, ?, ?)";
    }

    public boolean updatePasswordAndForceRelogin(String username, String hashedNewPassword) {
        // Gộp chung vào 1 query để đảm bảo đồng bộ 100%
        String query = "UPDATE account SET password = ?, require_relogin = 1 WHERE username = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, hashedNewPassword);
            statement.setString(2, username);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật mật khẩu và cờ relogin: " + e.getMessage());
            return false;
        }
    }

    // Cập nhật thời gian đăng nhập cuối cùng
    public void updateLastLogin(int accountId) {
        String query = "UPDATE account SET last_login = ? WHERE id = ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            statement.setInt(2, accountId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }

    /**
     * Check require_relogin flag - used for security-sensitive operations
     * If true, user must re-authenticate (re-enter password) before proceeding
     * 
     * @param accountId Employee ID to check
     * @return true if user needs to re-login, false otherwise
     */
    public boolean isRequireRelogin(int accountId) {
        String query = "SELECT require_relogin FROM account WHERE id = ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, accountId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("require_relogin");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking require_relogin: " + e.getMessage());
        }
        return false;
    }

    public AccountDTO getByUsername(String username) {
        String query = "SELECT id, username, password, status_id, require_relogin FROM account WHERE LOWER(username) = LOWER(?) LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    AccountDTO acc = new AccountDTO();
                    acc.setId(resultSet.getInt("id"));
                    acc.setUsername(resultSet.getString("username"));
                    acc.setPassword(resultSet.getString("password")); // Hash lưu trong DB
                    acc.setStatusId(resultSet.getInt("status_id"));
                    acc.setRequireRelogin(resultSet.getBoolean("require_relogin"));
                    return acc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set require_relogin flag - called by admin when sensitive data changed
     * 
     * @param accountId      Employee ID
     * @param requireRelogin true to force re-login on next action
     */
    public boolean setRequireRelogin(int accountId, boolean requireRelogin) {
        String query = "UPDATE account SET require_relogin = ? WHERE id = ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, requireRelogin);
            statement.setInt(2, accountId);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error setting require_relogin: " + e.getMessage());
        }
        return false;
    }

    public boolean setRequireReloginByRoleId(int roleId, boolean value) {

        String query = "UPDATE account a " +
                "JOIN employee e ON a.id = e.account_id " +
                "SET a.require_relogin = ? " +
                "WHERE e.role_id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBoolean(1, value);
            statement.setInt(2, roleId);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error in Batch Update require_relogin: " + e.getMessage());
            return false;
        }
    }

    public boolean insertWithConn(Connection conn, AccountDTO obj) throws SQLException {
        String sql = "INSERT INTO account (username, password, status_id) VALUES (?, ?, ?)";

        // Sử dụng Statement.RETURN_GENERATED_KEYS để lấy ID sau khi chèn
        try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 1. Gán các tham số (Dùng lại hàm setInsertParameters đã có ở trên)
            statement.setString(1, obj.getUsername());
            statement.setString(2, obj.getPassword());
            statement.setInt(3, obj.getStatusId());
            // 2. Thực thi
            int affectedRows = statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                setGeneratedKey(obj, generatedKeys);
            }
            return affectedRows > 0;
        }
    }

    /**
     * Cập nhật trạng thái tài khoản
     * Update: status_id trong account table
     * ⚠️ Caller PHẢI kiểm tra quyền EMPLOYEE_ACCOUNT_UPDATE_STATUS trước khi gọi
     */
    public boolean updateAccountStatus(int accountId, int statusId) {
        String query = "UPDATE account SET status_id = ? WHERE id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, statusId);
            statement.setInt(2, accountId);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account status: " + e.getMessage());
            return false;
        }
    }

    public boolean updateAccountSecurity(Connection conn, int accountId, boolean relogin, Integer statusId)
            throws SQLException {
        // Tự động xây dựng câu SQL tùy theo việc có update status hay không
        StringBuilder sql = new StringBuilder("UPDATE account SET require_relogin = ?");
        if (statusId != null)
            sql.append(", status_id = ?");
        sql.append(" WHERE id = ?");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setBoolean(idx++, relogin);
            if (statusId != null)
                ps.setInt(idx++, statusId);
            ps.setInt(idx++, accountId);

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Reset mật khẩu tài khoản
     * Update: password ONLY (KHÔNG set require_relogin - để caller gọi
     * forceLogoutAndSecurityUpdate riêng)
     * ⚠️ Caller PHẢI gọi forceLogoutAndSecurityUpdate() sau để set require_relogin
     * 
     * @param username       Username của account
     * @param hashedPassword Mật khẩu đã hash
     * @return true nếu thành công, false nếu thất bại
     */
    public boolean resetPassword(String username, String hashedPassword) {
        String query = "UPDATE account SET password = ? WHERE username = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, hashedPassword);
            statement.setString(2, username);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }

}
