package DAL;

import DTO.AccountDTO;

import java.sql.*;

public class AccountDAL extends BaseDAL<AccountDTO, Integer> {
    private static final AccountDAL INSTANCE = new AccountDAL();

    private AccountDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "account", "employee_id");
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
        return false;
    }

    @Override
    protected String getInsertQuery() {
        return "(id, username, password, created_at, last_login, status_id) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, AccountDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setString(2, obj.getUsername());
        statement.setString(3, obj.getPassword());
        statement.setObject(4, obj.getCreatedAt());
        statement.setObject(5, obj.getLastLogin());
        statement.setInt(6, obj.getStatusId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET password = ?, last_login = ?, status_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, AccountDTO obj) throws SQLException {
        statement.setString(1, obj.getPassword());
        statement.setObject(2, obj.getLastLogin());
        statement.setInt(3, obj.getStatusId());
        statement.setInt(4, obj.getEmployeeId());
    }
}
