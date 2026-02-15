package DAL;

import DTO.EmployeeDTO;
import DTO.EmployeeSessionDTO;

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
                resultSet.getObject("department_id") != null ? resultSet.getInt("department_id") : null,
                resultSet.getInt("status_id"),
                resultSet.getString("gender"),
                resultSet.getObject("account_id") != null ? resultSet.getInt("account_id") : null,
                resultSet.getString("health_ins_code"),
                resultSet.getBoolean("is_social_insurance"),
                resultSet.getBoolean("is_unemployment_insurance"),
                resultSet.getBoolean("is_personal_income_tax"),
                resultSet.getBoolean("is_transportation_support"),
                resultSet.getBoolean("is_accommodation_support"),
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("updated_at") != null
                        ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected String getInsertQuery() {
        return "(first_name, last_name, phone, email, date_of_birth, role_id, department_id, status_id, gender, account_id, health_ins_code, is_social_insurance, is_unemployment_insurance, is_personal_income_tax, is_transportation_support, is_accommodation_support) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, EmployeeDTO obj) throws SQLException {
        statement.setString(1, obj.getFirstName());
        statement.setString(2, obj.getLastName());
        statement.setString(3, obj.getPhone());
        statement.setString(4, obj.getEmail());
        statement.setObject(5, obj.getDateOfBirth());
        statement.setInt(6, obj.getRoleId());
        statement.setObject(7, obj.getDepartmentId());
        statement.setInt(8, obj.getStatusId());
        statement.setString(9, obj.getGender());
        statement.setObject(10, obj.getAccountId());
        statement.setString(11, obj.getHealthInsCode());
        statement.setBoolean(12, obj.isSocialInsurance());
        statement.setBoolean(13, obj.isUnemploymentInsurance());
        statement.setBoolean(14, obj.isPersonalIncomeTax());
        statement.setBoolean(15, obj.isTransportationSupport());
        statement.setBoolean(16, obj.isAccommodationSupport());
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Cannot update Employee records.");
    }

    public boolean updateAdvance(EmployeeDTO obj, boolean allowAdvanceChange) {
        String query = allowAdvanceChange
                ? "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, role_id = ?, department_id = ?, status_id = ?, gender = ?, account_id = ?, health_ins_code = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ?, updated_at = ? WHERE id = ?"
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
                statement.setObject(7, obj.getDepartmentId());
                statement.setInt(8, obj.getStatusId());
                statement.setString(9, obj.getGender());
                statement.setObject(10, obj.getAccountId());
                statement.setString(11, obj.getHealthInsCode());
                statement.setBoolean(12, obj.isSocialInsurance());
                statement.setBoolean(13, obj.isUnemploymentInsurance());
                statement.setBoolean(14, obj.isPersonalIncomeTax());
                statement.setBoolean(15, obj.isTransportationSupport());
                statement.setBoolean(16, obj.isAccommodationSupport());
                statement.setObject(17, obj.getUpdatedAt());
                statement.setInt(18, obj.getId());
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
                ? "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, gender = ?, account_id = ?, health_ins_code = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ?, role_id = ?, department_id = ?, status_id = ?, updated_at = ? WHERE id = ?"
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
                statement.setObject(15, obj.getDepartmentId());
                statement.setInt(16, obj.getStatusId());
                statement.setObject(17, obj.getUpdatedAt());
                statement.setInt(18, obj.getId());
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

    /**
     * Cập nhật TAB 1: Thông tin cá nhân
     * Update: firstName, lastName, phone, email, dateOfBirth, gender, healthInsCode
     */
    public boolean updatePersonalInfoBySelf(EmployeeDTO obj) {
        String query = "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getFirstName());
            statement.setString(2, obj.getLastName());
            statement.setString(3, obj.getPhone());
            statement.setString(4, obj.getEmail());
            statement.setDate(5, obj.getDateOfBirth() != null ? java.sql.Date.valueOf(obj.getDateOfBirth()) : null);
            statement.setInt(6, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating personal info (self): " + e.getMessage());
            return false;
        }
    }

    public boolean updatePersonalInfoByAdmin(EmployeeDTO obj) {
        String query = "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, gender = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getFirstName());
            statement.setString(2, obj.getLastName());
            statement.setString(3, obj.getPhone());
            statement.setString(4, obj.getEmail());
            statement.setDate(5, obj.getDateOfBirth() != null ? java.sql.Date.valueOf(obj.getDateOfBirth()) : null);
            statement.setString(6, obj.getGender());
            statement.setInt(7, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating personal info (admin): " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật TAB 2: Vị trí công tác
     * Update: departmentId, statusId trong employee table
     * Ghi nhật ký lịch sử điều chuyển trong employment_history table
     */
    public boolean updateJobPosition(EmployeeDTO obj) {
        String query = "UPDATE employee SET department_id = ?, status_id = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setObject(1, obj.getDepartmentId());
            statement.setInt(2, obj.getStatusId());
            statement.setObject(3, obj.getUpdatedAt());
            statement.setInt(4, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating job position: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật TAB 3: Lương & Bảo hiểm
     * Update: role_id, insurance flags (isSocialInsurance, isUnemploymentInsurance,
     * isPersonalIncomeTax, isTransportationSupport, isAccommodationSupport)
     * Lưu ý: baseSalary, coefficient sẽ được update qua SalaryDAL
     * numDependents sẽ được update qua TaxDAL
     */
    public boolean updatePayrollInfo(EmployeeDTO obj) {
        String query = "UPDATE employee SET role_id = ?, is_social_insurance = ?, is_unemployment_insurance = ?, is_personal_income_tax = ?, is_transportation_support = ?, is_accommodation_support = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, obj.getRoleId());
            statement.setBoolean(2, obj.isSocialInsurance());
            statement.setBoolean(3, obj.isUnemploymentInsurance());
            statement.setBoolean(4, obj.isPersonalIncomeTax());
            statement.setBoolean(5, obj.isTransportationSupport());
            statement.setBoolean(6, obj.isAccommodationSupport());
            statement.setObject(7, obj.getUpdatedAt());
            statement.setInt(8, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payroll info: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get employee by account ID - [STATELESS] Direct DB query, no cache
     * 
     * @param accountId Account ID to search for
     * @return EmployeeDTO or null if not found
     */
    public EmployeeDTO getByAccountId(int accountId) {
        String query = "SELECT * FROM employee WHERE account_id = ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, accountId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToObject(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting employee by account ID: " + e.getMessage());
        }
        return null;
    }

    public EmployeeSessionDTO getEmployeeSessionByAccountId(int accountId) {
        String sql = "SELECT a.id as account_id, e.id as employee_id, a.username, " +
                "e.first_name, e.last_name, r.id as role_id, r.name as role_name " +
                "FROM account a " +
                "JOIN employee e ON a.id = e.account_id " +
                "JOIN role r ON e.role_id = r.id " +
                "WHERE a.id = ? LIMIT 1";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EmployeeSessionDTO session = new EmployeeSessionDTO();
                    session.setAccountId(rs.getInt("account_id"));
                    session.setEmployeeId(rs.getInt("employee_id"));
                    session.setUsername(rs.getString("username"));
                    session.setFullName(rs.getString("first_name") + " " + rs.getString("last_name"));
                    session.setRoleId(rs.getInt("role_id"));
                    session.setRoleName(rs.getString("role_name"));

                    return session;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật TAB 4: Tài khoản hệ thống
     * Update: accountId
     */
    public boolean updateSystemAccount(EmployeeDTO obj) {
        String query = "UPDATE employee SET account_id = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setObject(1, obj.getAccountId());
            statement.setObject(2, obj.getUpdatedAt());
            statement.setInt(3, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating system account: " + e.getMessage());
            return false;
        }
    }

    public int countByRoleId(int roleId) {
        if (roleId <= 0) {
            return 0; // Trả về 0 nếu roleId không hợp lệ
        }

        String query = "SELECT COUNT(*) FROM employee WHERE role_id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, roleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1); // Lấy số lượng nhân viên
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting employees by role ID: " + e.getMessage());
        }
        return 0; // Trả về 0 nếu có lỗi xảy ra
    }
}
