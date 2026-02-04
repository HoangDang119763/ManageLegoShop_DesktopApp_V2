package DAL;

import DTO.DeductionDTO;
import java.sql.*;

public class DeductionDAL extends BaseDAL<DeductionDTO, Integer> {
    public static final DeductionDAL INSTANCE = new DeductionDAL();

    private DeductionDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "deduction", "id");
    }

    public static DeductionDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected DeductionDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new DeductionDTO(
                resultSet.getInt("id"),
                resultSet.getInt("employee_id"),
                resultSet.getDate("salary_period") != null
                        ? resultSet.getDate("salary_period").toLocalDate()
                        : null,
                resultSet.getBigDecimal("health_insurance"),
                resultSet.getBigDecimal("social_insurance"),
                resultSet.getBigDecimal("unemployment_insurance"),
                resultSet.getBigDecimal("personal_income_tax"),
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
    protected void setGeneratedKey(DeductionDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(employee_id, salary_period, health_insurance, social_insurance, unemployment_insurance, personal_income_tax, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, DeductionDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getSalaryPeriod());
        statement.setObject(3, obj.getHealthInsurance());
        statement.setObject(4, obj.getSocialInsurance());
        statement.setObject(5, obj.getUnemploymentInsurance());
        statement.setObject(6, obj.getPersonalIncomeTax());
        statement.setObject(7, obj.getCreatedAt());
        statement.setObject(8, obj.getUpdatedAt());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, salary_period = ?, health_insurance = ?, social_insurance = ?, unemployment_insurance = ?, personal_income_tax = ?, created_at = ?, updated_at = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, DeductionDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getSalaryPeriod());
        statement.setObject(3, obj.getHealthInsurance());
        statement.setObject(4, obj.getSocialInsurance());
        statement.setObject(5, obj.getUnemploymentInsurance());
        statement.setObject(6, obj.getPersonalIncomeTax());
        statement.setObject(7, obj.getCreatedAt());
        statement.setObject(8, obj.getUpdatedAt());
        statement.setInt(9, obj.getId());
    }

    @Override
    protected boolean hasSoftDelete() {
        return false;
    }
}
