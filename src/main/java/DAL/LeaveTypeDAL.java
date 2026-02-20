package DAL;

import DTO.LeaveTypeDTO;
import java.sql.*;

public class LeaveTypeDAL extends BaseDAL<LeaveTypeDTO, Integer> {
    public static final LeaveTypeDAL INSTANCE = new LeaveTypeDAL();

    private LeaveTypeDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "leave_type", "id");
    }

    public static LeaveTypeDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected LeaveTypeDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new LeaveTypeDTO(
                resultSet.getInt("id"),
                resultSet.getString("type"),
                resultSet.getBigDecimal("fine_amount"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(LeaveTypeDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(type, fine_amount) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, LeaveTypeDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setObject(2, obj.getFineAmount());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET type = ?, fine_amount = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, LeaveTypeDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setObject(2, obj.getFineAmount());
        statement.setInt(3, obj.getId());
    }
}
