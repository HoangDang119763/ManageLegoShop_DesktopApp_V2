package DAL;

import DTO.EmployeeDTO;
import java.sql.*;

public class EmployeeDAL extends BaseDAL<EmployeeDTO, Integer> {
    public static final EmployeeDAL INSTANCE = new EmployeeDAL();

    private EmployeeDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "employee", "id");
    }

    public static EmployeeDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected EmployeeDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new EmployeeDTO(
                resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getBigDecimal("salary"),
                resultSet.getDate("date_of_birth") != null
                        ? resultSet.getDate("date_of_birth").toLocalDate()
                        : null,
                resultSet.getInt("role_id"),
                resultSet.getInt("status_id"),
                resultSet.getString("gender"),
                resultSet.getInt("position_id"),
                resultSet.getObject("account_id") != null ? resultSet.getInt("account_id") : null,
                resultSet.getBoolean("is_health_insurance"),
                resultSet.getBoolean("is_social_insurance"),
                resultSet.getBoolean("is_unemployment_insurance"),
                resultSet.getBoolean("is_personal_income_tax"),
                resultSet.getBoolean("is_transportation_support"),
                resultSet.getBoolean("is_accommodation_support"));
    }

    @Override
    protected String getInsertQuery() {
        return "(first_name, last_name, phone, email, salary, date_of_birth, role_id, status_id, gender, position_id, account_id, is_health_insurance, is_social_insurance, is_unemployment_insurance, is_personal_income_tax, is_transportation_support, is_accommodation_support) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, EmployeeDTO obj) throws SQLException {
        statement.setString(1, obj.getFirstName());
        statement.setString(2, obj.getLastName());
        statement.setString(3, obj.getPhone());
        statement.setString(4, obj.getEmail());
        statement.setBigDecimal(5, obj.getSalary());
        statement.setObject(6, obj.getDateOfBirth());
        statement.setInt(7, obj.getRoleId());
        statement.setInt(8, obj.getStatusId());
        statement.setString(9, obj.getGender());
        statement.setInt(10, obj.getPositionId());
        statement.setObject(11, obj.getAccountId());
        statement.setBoolean(12, obj.isHealthInsurance());
        statement.setBoolean(13, obj.isSocialInsurance());
        statement.setBoolean(14, obj.isUnemploymentInsurance());
        statement.setBoolean(15, obj.isPersonalIncomeTax());
        statement.setBoolean(16, obj.isTransportationSupport());
        statement.setBoolean(17, obj.isAccommodationSupport());
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Cannot update Employee records.");
    }

    @Override
    protected boolean hasSoftDelete() {
        return true;
    }

    public boolean updateAdvance(EmployeeDTO obj, boolean allowAdvanceChange) {
        String query = allowAdvanceChange
                ? "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, salary = ?, date_of_birth = ?, role_id = ?, status = ?, gender = ?, position_id = ?, account_id = ?, is_health_insurance = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ? WHERE id = ?"
                : "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, salary = ?, date_of_birth = ?, gender = ?, position_id = ?, account_id = ?, is_health_insurance = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getFirstName());
            statement.setString(2, obj.getLastName());
            statement.setString(3, obj.getPhone());
            statement.setString(4, obj.getEmail());
            statement.setBigDecimal(5, obj.getSalary());
            statement.setDate(6,
                    obj.getDateOfBirth() != null ? java.sql.Date.valueOf(obj.getDateOfBirth()) : null);
            statement.setString(7, obj.getGender());
            statement.setInt(8, obj.getPositionId());
            statement.setObject(9, obj.getAccountId());
            statement.setBoolean(10, obj.isHealthInsurance());
            statement.setBoolean(11, obj.isSocialInsurance());
            statement.setBoolean(12, obj.isUnemploymentInsurance());
            statement.setBoolean(13, obj.isPersonalIncomeTax());
            statement.setBoolean(14, obj.isTransportationSupport());
            statement.setBoolean(15, obj.isAccommodationSupport());

            if (allowAdvanceChange) {
                statement.setInt(16, obj.getRoleId());
                statement.setInt(17, obj.getStatusId());
                statement.setInt(18, obj.getId());
            } else {
                statement.setInt(16, obj.getId());
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating advance employee: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBasic(EmployeeDTO obj, boolean allowAdvanceChange) {
        String query = allowAdvanceChange
                ? "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, gender = ?, position_id = ?, account_id = ?, is_health_insurance = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ?, role_id = ?, status = ? WHERE id = ?"
                : "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, gender = ?, position_id = ?, account_id = ?, is_health_insurance = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getFirstName());
            statement.setString(2, obj.getLastName());
            statement.setString(3, obj.getPhone());
            statement.setString(4, obj.getEmail());
            statement.setDate(5,
                    obj.getDateOfBirth() != null ? java.sql.Date.valueOf(obj.getDateOfBirth()) : null);
            statement.setString(6, obj.getGender());
            statement.setInt(7, obj.getPositionId());
            statement.setObject(8, obj.getAccountId());
            statement.setBoolean(9, obj.isHealthInsurance());
            statement.setBoolean(10, obj.isSocialInsurance());
            statement.setBoolean(11, obj.isUnemploymentInsurance());
            statement.setBoolean(12, obj.isPersonalIncomeTax());
            statement.setBoolean(13, obj.isTransportationSupport());
            statement.setBoolean(14, obj.isAccommodationSupport());

            if (allowAdvanceChange) {
                statement.setInt(15, obj.getRoleId());
                statement.setInt(16, obj.getStatusId());
                statement.setInt(17, obj.getId());
            } else {
                statement.setInt(15, obj.getId());
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating basic employee: " + e.getMessage());
            return false;
        }
    }
}
