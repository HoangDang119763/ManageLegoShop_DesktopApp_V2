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
                resultSet.getDate("date_of_birth") != null
                        ? resultSet.getDate("date_of_birth").toLocalDate()
                        : null,
                resultSet.getInt("role_id"),
                resultSet.getInt("status_id"),
                resultSet.getString("gender"),
                resultSet.getObject("account_id") != null ? resultSet.getInt("account_id") : null,
                resultSet.getString("health_ins_code"),
                resultSet.getBoolean("is_social_insurance"),
                resultSet.getBoolean("is_unemployment_insurance"),
                resultSet.getBoolean("is_personal_income_tax"),
                resultSet.getBoolean("is_transportation_support"),
                resultSet.getBoolean("is_accommodation_support"),
                resultSet.getTimestamp("updated_at") != null
                        ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected String getInsertQuery() {
        return "(first_name, last_name, phone, email, date_of_birth, role_id, status_id, gender, account_id, health_ins_code, is_social_insurance, is_unemployment_insurance, is_personal_income_tax, is_transportation_support, is_accommodation_support) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, EmployeeDTO obj) throws SQLException {
        statement.setString(1, obj.getFirstName());
        statement.setString(2, obj.getLastName());
        statement.setString(3, obj.getPhone());
        statement.setString(4, obj.getEmail());
        statement.setObject(5, obj.getDateOfBirth());
        statement.setInt(6, obj.getRoleId());
        statement.setInt(7, obj.getStatusId());
        statement.setString(8, obj.getGender());
        statement.setObject(9, obj.getAccountId());
        statement.setString(10, obj.getHealthInsCode());
        statement.setBoolean(11, obj.isSocialInsurance());
        statement.setBoolean(12, obj.isUnemploymentInsurance());
        statement.setBoolean(13, obj.isPersonalIncomeTax());
        statement.setBoolean(14, obj.isTransportationSupport());
        statement.setBoolean(15, obj.isAccommodationSupport());
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
                ? "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, role_id = ?, status_id = ?, gender = ?, account_id = ?, health_ins_code = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ?, updated_at = ? WHERE id = ?"
                : "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, gender = ?, account_id = ?, health_ins_code = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getFirstName());
            statement.setString(2, obj.getLastName());
            statement.setString(3, obj.getPhone());
            statement.setString(4, obj.getEmail());
            statement.setDate(5,
                    obj.getDateOfBirth() != null ? java.sql.Date.valueOf(obj.getDateOfBirth()) : null);

            if (allowAdvanceChange) {
                statement.setInt(6, obj.getRoleId());
                statement.setInt(7, obj.getStatusId());
                statement.setString(8, obj.getGender());
                statement.setObject(9, obj.getAccountId());
                statement.setString(10, obj.getHealthInsCode());
                statement.setBoolean(11, obj.isSocialInsurance());
                statement.setBoolean(12, obj.isUnemploymentInsurance());
                statement.setBoolean(13, obj.isPersonalIncomeTax());
                statement.setBoolean(14, obj.isTransportationSupport());
                statement.setBoolean(15, obj.isAccommodationSupport());
                statement.setObject(16, obj.getUpdatedAt());
                statement.setInt(17, obj.getId());
            } else {
                statement.setString(6, obj.getGender());
                statement.setObject(7, obj.getAccountId());
                statement.setString(8, obj.getHealthInsCode());
                statement.setBoolean(9, obj.isSocialInsurance());
                statement.setBoolean(10, obj.isUnemploymentInsurance());
                statement.setBoolean(11, obj.isPersonalIncomeTax());
                statement.setBoolean(12, obj.isTransportationSupport());
                statement.setBoolean(13, obj.isAccommodationSupport());
                statement.setObject(14, obj.getUpdatedAt());
                statement.setInt(15, obj.getId());
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating advance employee: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBasic(EmployeeDTO obj, boolean allowAdvanceChange) {
        String query = allowAdvanceChange
                ? "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, gender = ?, account_id = ?, health_ins_code = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ?, role_id = ?, status_id = ?, updated_at = ? WHERE id = ?"
                : "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, gender = ?, account_id = ?, health_ins_code = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getFirstName());
            statement.setString(2, obj.getLastName());
            statement.setString(3, obj.getPhone());
            statement.setString(4, obj.getEmail());
            statement.setDate(5,
                    obj.getDateOfBirth() != null ? java.sql.Date.valueOf(obj.getDateOfBirth()) : null);
            statement.setString(6, obj.getGender());
            statement.setObject(7, obj.getAccountId());
            statement.setString(8, obj.getHealthInsCode());
            statement.setBoolean(9, obj.isSocialInsurance());
            statement.setBoolean(10, obj.isUnemploymentInsurance());
            statement.setBoolean(11, obj.isPersonalIncomeTax());
            statement.setBoolean(12, obj.isTransportationSupport());
            statement.setBoolean(13, obj.isAccommodationSupport());

            if (allowAdvanceChange) {
                statement.setInt(14, obj.getRoleId());
                statement.setInt(15, obj.getStatusId());
                statement.setObject(16, obj.getUpdatedAt());
                statement.setInt(17, obj.getId());
            } else {
                statement.setObject(14, obj.getUpdatedAt());
                statement.setInt(15, obj.getId());
            }

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating basic employee: " + e.getMessage());
            return false;
        }
    }
}
