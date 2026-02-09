package DAL;

import DTO.HolidayDTO;
import java.sql.*;

public class HolidayDAL extends BaseDAL<HolidayDTO, Integer> {
    public static final HolidayDAL INSTANCE = new HolidayDAL();

    private HolidayDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "holiday", "id");
    }

    public static HolidayDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected HolidayDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new HolidayDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getDate("date") != null
                        ? resultSet.getDate("date").toLocalDate()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(HolidayDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, date) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, HolidayDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setObject(2, obj.getDate());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, date = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, HolidayDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setObject(2, obj.getDate());
        statement.setInt(3, obj.getId());
    }

}
