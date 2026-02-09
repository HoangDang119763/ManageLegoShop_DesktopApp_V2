package DAL;

import DTO.AccountDTO;
import DTO.EmployeeDTO;

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
        throw new UnsupportedOperationException("Cannot update Employee records.");
    }

    public boolean changePasswordBySelf(AccountDTO obj) {
        String query = "UPDATE account SET username = ?, password = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getUsername());
            statement.setString(2, obj.getPassword());
            statement.setInt(3, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating account (self): " + e.getMessage());
            return false;
        }
    }

    // Cập nhật thời gian đăng nhập cuối cùng
    public void updateLastLogin(int accountId) {
        String query = "UPDATE account SET last_login = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, new java.sql.Timestamp(System.currentTimeMillis()));
            statement.setInt(2, accountId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }
}
