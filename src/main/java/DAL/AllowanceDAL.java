package DAL;

import DTO.AllowanceDTO;
import java.sql.*;

public class AllowanceDAL extends BaseDAL<AllowanceDTO, Integer> {
    public static final AllowanceDAL INSTANCE = new AllowanceDAL();

    private AllowanceDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "allowance", "id");
    }

    public static AllowanceDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected AllowanceDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new AllowanceDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getBigDecimal("amount"),
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("updated_at") != null
                        ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(AllowanceDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, amount, created_at, updated_at) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, AllowanceDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setObject(2, obj.getAmount());
        statement.setObject(3, obj.getCreatedAt());
        statement.setObject(4, obj.getUpdatedAt());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, amount = ?, created_at = ?, updated_at = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, AllowanceDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setObject(2, obj.getAmount());
        statement.setObject(3, obj.getCreatedAt());
        statement.setObject(4, obj.getUpdatedAt());
        statement.setInt(5, obj.getId());
    }

}
