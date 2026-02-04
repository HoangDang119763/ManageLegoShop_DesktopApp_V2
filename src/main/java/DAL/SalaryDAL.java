package DAL;

import DTO.SalaryDTO;
import java.sql.*;

public class SalaryDAL extends BaseDAL<SalaryDTO, Integer> {
    public static final SalaryDAL INSTANCE = new SalaryDAL();

    private SalaryDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "salary", "id");
    }

    public static SalaryDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected SalaryDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new SalaryDTO(
                resultSet.getInt("id"),
                resultSet.getBigDecimal("base"),
                resultSet.getBigDecimal("coefficient"),
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
    protected void setGeneratedKey(SalaryDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(base, coefficient) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, SalaryDTO obj) throws SQLException {
        statement.setObject(1, obj.getBase());
        statement.setBigDecimal(2, obj.getCoefficient());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET base = ?, coefficient = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, SalaryDTO obj) throws SQLException {
        statement.setObject(1, obj.getBase());
        statement.setBigDecimal(2, obj.getCoefficient());
        statement.setInt(3, obj.getId());
    }
}
