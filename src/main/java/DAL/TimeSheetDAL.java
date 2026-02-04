package DAL;

import DTO.TimeSheetDTO;
import java.sql.*;

public class TimeSheetDAL extends BaseDAL<TimeSheetDTO, Integer> {
    public static final TimeSheetDAL INSTANCE = new TimeSheetDAL();

    private TimeSheetDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "time_sheet", "id");
    }

    public static TimeSheetDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected TimeSheetDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new TimeSheetDTO(
                resultSet.getInt("id"),
                resultSet.getInt("employee_id"),
                resultSet.getTimestamp("check_in") != null
                        ? resultSet.getTimestamp("check_in").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("check_out") != null
                        ? resultSet.getTimestamp("check_out").toLocalDateTime()
                        : null,
                resultSet.getBigDecimal("work_hours"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(TimeSheetDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(employee_id, check_in, check_out, work_hours) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, TimeSheetDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getCheckIn());
        statement.setObject(3, obj.getCheckOut());
        statement.setObject(4, obj.getWorkHours());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, check_in = ?, check_out = ?, work_hours = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, TimeSheetDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getCheckIn());
        statement.setObject(3, obj.getCheckOut());
        statement.setObject(4, obj.getWorkHours());
        statement.setInt(5, obj.getId());
    }

    @Override
    protected boolean hasSoftDelete() {
        return false;
    }
}
