package DAL;

import DTO.FineDTO;
import java.sql.*;

public class FineDAL extends BaseDAL<FineDTO, Integer> {
    public static final FineDAL INSTANCE = new FineDAL();

    private FineDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "fine", "id");
    }

    public static FineDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected FineDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new FineDTO(
                resultSet.getInt("id"),
                resultSet.getString("reason"),
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getString("fine_level"),
                resultSet.getString("type") != null ? resultSet.getString("type") : "DISCIPLINE",
                resultSet.getBigDecimal("amount"),
                resultSet.getBigDecimal("fine_pay"),
                resultSet.getInt("employee_id"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(FineDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(reason, created_at, fine_level, type, amount, fine_pay, employee_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, FineDTO obj) throws SQLException {
        statement.setString(1, obj.getReason());
        statement.setObject(2, obj.getCreatedAt());
        statement.setString(3, obj.getFineLevel());
        statement.setString(4, obj.getType());
        statement.setObject(5, obj.getAmount());
        statement.setObject(6, obj.getFinePay());
        statement.setInt(7, obj.getEmployeeId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET reason = ?, created_at = ?, fine_level = ?, type = ?, amount = ?, fine_pay = ?, employee_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, FineDTO obj) throws SQLException {
        statement.setString(1, obj.getReason());
        statement.setObject(2, obj.getCreatedAt());
        statement.setString(3, obj.getFineLevel());
        statement.setString(4, obj.getType());
        statement.setObject(5, obj.getAmount());
        statement.setObject(6, obj.getFinePay());
        statement.setInt(7, obj.getEmployeeId());
        statement.setInt(8, obj.getId());
    }

}
