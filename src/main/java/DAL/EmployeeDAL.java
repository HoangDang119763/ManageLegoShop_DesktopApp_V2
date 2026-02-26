package DAL;

import DTO.EmployeeDTO;
import DTO.EmployeeSessionDTO;
import DTO.PagedResponse;
import DTO.EmployeeDetailDTO;
import DTO.EmployeeDisplayDTO;
import DTO.EmployeePersonalInfoDTO;
import DTO.EmployeeAccountInfoDTO;
import DTO.EmployeeJobInfoDTO;
import DTO.EmployeePayrollInfoDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                resultSet.getObject("department_id") != null ? resultSet.getInt("department_id") : null,
                resultSet.getInt("status_id"),
                resultSet.getString("gender"),
                resultSet.getObject("account_id") != null ? resultSet.getInt("account_id") : null,
                resultSet.getString("avatar_url"),
                resultSet.getObject("position_id") != null ? resultSet.getInt("position_id") : null,
                resultSet.getString("health_insurance_code"),
                resultSet.getString("social_insurance_code"),
                resultSet.getString("unemployment_insurance_code"),
                resultSet.getBoolean("is_meal_support"),
                resultSet.getBoolean("is_transportation_support"),
                resultSet.getBoolean("is_accommodation_support"),
                resultSet.getObject("num_dependents") != null ? resultSet.getInt("num_dependents") : 0,
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("updated_at") != null
                        ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected String getInsertQuery() {
        return "(first_name, last_name, phone, email, date_of_birth, department_id, status_id, gender, account_id, position_id, health_insurance_code, social_insurance_code, unemployment_insurance_code, is_meal_support, is_transportation_support, is_accommodation_support, num_dependents, avatar_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, EmployeeDTO obj) throws SQLException {
        statement.setString(1, obj.getFirstName());
        statement.setString(2, obj.getLastName());
        statement.setString(3, obj.getPhone());
        statement.setString(4, obj.getEmail());
        statement.setObject(5, obj.getDateOfBirth());
        statement.setObject(6, obj.getDepartmentId());
        statement.setInt(7, obj.getStatusId());
        statement.setString(8, obj.getGender());
        statement.setObject(9, obj.getAccountId());
        statement.setObject(10, obj.getPositionId());
        statement.setString(11, obj.getHealthInsCode());
        statement.setString(12, obj.getSocialInsCode());
        statement.setString(13, obj.getUnemploymentInsCode());
        statement.setBoolean(14, obj.isMealSupport());
        statement.setBoolean(15, obj.isTransportationSupport());
        statement.setBoolean(16, obj.isAccommodationSupport());
        statement.setInt(17, obj.getNumDependents());
        statement.setString(18, obj.getAvatarUrl());
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Cannot update Employee records.");
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

            return statement.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Error updating personal info (self): " + e.getMessage());
            return false;
        }
    }

    public boolean updatePersonalInfoByAdmin(EmployeeDTO obj) {
        String query = "UPDATE employee SET first_name = ?, last_name = ?, phone = ?, email = ?, date_of_birth = ?, gender = ?, avatar_url = ?, status_id = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, obj.getFirstName());
            statement.setString(2, obj.getLastName());
            statement.setString(3, obj.getPhone());
            statement.setString(4, obj.getEmail());
            statement.setDate(5, obj.getDateOfBirth() != null ? java.sql.Date.valueOf(obj.getDateOfBirth()) : null);
            statement.setString(6, obj.getGender());
            statement.setString(7, obj.getAvatarUrl());
            statement.setInt(8, obj.getStatusId());
            statement.setInt(9, obj.getId());

            return statement.executeUpdate() >= 0;
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

    public boolean updateJobInfo(Connection conn, EmployeeDTO obj) {
        String query = "UPDATE employee SET department_id = ?, position_id = ? WHERE id = ?";

        // Không dùng try-with-resources cho Connection ở đây vì BUS quản lý vòng đời
        // của nó
        try (PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setObject(1, obj.getDepartmentId());
            statement.setInt(2, obj.getPositionId());
            statement.setInt(3, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating job info in DAL: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePayrollInfo(Connection conn, EmployeeDTO obj) throws SQLException {
        String sql = "UPDATE employee SET health_insurance_code = ?, social_insurance_code = ?, " +
                "unemployment_insurance_code = ?, is_meal_support = ?, " +
                "is_transportation_support = ?, is_accommodation_support = ?, num_dependents = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, obj.getHealthInsCode());
            ps.setString(2, obj.getSocialInsCode());
            ps.setString(3, obj.getUnemploymentInsCode());
            ps.setBoolean(4, obj.isMealSupport());
            ps.setBoolean(5, obj.isTransportationSupport());
            ps.setBoolean(6, obj.isAccommodationSupport());
            ps.setInt(7, obj.getNumDependents());
            ps.setInt(8, obj.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Get employee by account ID - [STATELESS] Direct DB query, no cache
     * 
     * @param accountId Account ID to search for
     * @return EmployeeDTO or null if not found
     */
    public EmployeeDTO getByAccountId(int accountId) {
        String query = "SELECT e.*, a.role_id " +
                "FROM employee e " +
                "LEFT JOIN account a ON e.account_id = a.id " +
                "WHERE e.account_id = ? LIMIT 1";
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
                "e.first_name, e.last_name, r.id as role_id, r.name as role_name, " +
                "pos.id as position_id, pos.name as position_name " +
                "FROM account a " +
                "JOIN employee e ON a.id = e.account_id " +
                "JOIN role r ON a.role_id = r.id " +
                "JOIN position pos ON e.position_id = pos.id " +
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
                    session.setPositionId(rs.getInt("position_id"));
                    session.setPositionName(rs.getString("position_name"));

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

        String query = "SELECT COUNT(*) FROM employee e " +
                "INNER JOIN account a ON e.account_id = a.id " +
                "WHERE a.role_id = ?";
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

    /**
     * Query EmployeeDetailDTO - JOIN tất cả bảng cần thiết 1 lần
     * Thay thế cho EmployeeViewProvider (không cần gọi nhiều getById)
     * 
     * @param employeeId ID của employee
     * @return EmployeeDetailDTO hoặc null nếu không tìm thấy
     */
    public EmployeeDetailDTO getDetailById(int employeeId) {
        String sql = "SELECT " +
                "e.*, " +
                "r.id AS role_id_mapped, r.name AS role_name, " +
                "pos.wage AS wage, " +
                "pos.id AS position_id, pos.name AS position_name, " +
                "st.id AS emp_status_id, st.description AS emp_status_desc, " +
                "a.id AS account_id_mapped, a.username, a.status_id AS account_status_id, " +
                "st_acc.description AS account_status_desc, " +
                "d.name AS department_name " +
                "FROM employee e " +
                "LEFT JOIN account a ON e.account_id = a.id " +
                "LEFT JOIN role r ON a.role_id = r.id " +
                "LEFT JOIN position pos ON e.position_id = pos.id " +
                "LEFT JOIN status st ON e.status_id = st.id " +
                "LEFT JOIN status st_acc ON a.status_id = st_acc.id " +
                "LEFT JOIN department d ON e.department_id = d.id " +
                "WHERE e.id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);
            System.out.println("DEBUG: Executing query for employee ID: " + employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.println("DEBUG: Found employee record for ID: " + employeeId);
                    EmployeeDetailDTO result = mapResultSetToDetailDTO(resultSet);
                    System.out.println("DEBUG: Mapped EmployeeDetailDTO: " + result);
                    return result;
                } else {
                    System.out.println("DEBUG: No employee record found for ID: " + employeeId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employee detail: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Map ResultSet từ getDetailById() → EmployeeDetailDTO
     */
    private EmployeeDetailDTO mapResultSetToDetailDTO(ResultSet rs) throws SQLException {
        try {
            int empId = rs.getInt("id");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            System.out.println("DEBUG: Mapping employee - ID: " + empId + ", Name: " + firstName + " " + lastName);

            EmployeeDetailDTO result = EmployeeDetailDTO.builder()
                    // Base info
                    .id(empId)
                    .employeeId(empId)
                    .firstName(firstName)
                    .lastName(lastName)
                    .gender(rs.getString("gender"))
                    .dateOfBirth(rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toLocalDate() : null)
                    .phone(rs.getString("phone"))
                    .email(rs.getString("email"))
                    .healthInsCode(rs.getString("health_insurance_code"))

                    // Department & Role
                    .departmentId(rs.getObject("department_id") != null ? rs.getInt("department_id") : null)
                    .departmentName(rs.getString("department_name"))
                    .roleId(rs.getInt("role_id"))
                    .roleName(rs.getString("role_name"))

                    // Account info
                    .accountId(rs.getObject("account_id_mapped") != null ? rs.getInt("account_id_mapped") : 0)
                    .username(rs.getString("username"))
                    .accountStatusId(rs.getObject("account_status_id") != null ? rs.getInt("account_status_id") : 0)
                    .accountStatus(rs.getString("account_status_desc"))

                    // Employee status
                    .statusId(rs.getInt("status_id"))
                    .statusDescription(rs.getString("emp_status_desc"))

                    // Salary
                    .positionId(rs.getInt("position_id"))
                    .positionName(rs.getString("position_name"))
                    .wage(rs.getObject("wage") != null ? rs.getBigDecimal("wage") : null)

                    .numDependents(rs.getObject("num_dependents") != null ? rs.getInt("num_dependents") : null)

                    // Insurance & Support flags
                    .socialInsCode(rs.getString("social_insurance_code"))
                    .unemploymentInsCode(rs.getString("unemployment_insurance_code"))
                    .isMealSupport(rs.getBoolean("is_meal_support"))
                    .isTransportationSupport(rs.getBoolean("is_transportation_support"))
                    .isAccommodationSupport(rs.getBoolean("is_accommodation_support"))

                    // Timestamps
                    .createdAt(
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime()
                                    : null)
                    .updatedAt(
                            rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime()
                                    : null)

                    .build();

            System.out.println("DEBUG: Built EmployeeDetailDTO: " + result.toString());
            return result;
        } catch (SQLException e) {
            System.err.println("DEBUG: Error mapping ResultSet to EmployeeDetailDTO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public PagedResponse<EmployeeDisplayDTO> filterEmployeesPagedForManageDisplay(
            String keyword, int filterPosId, int statusId, int pageIndex, int pageSize,
            int excludeId, int currentAccountRoleId) {

        List<EmployeeDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        String sql = "SELECT e.id, e.first_name, e.last_name, e.gender, e.status_id, e.account_id, " +
                "pos.name AS positionName, " +
                "pos.id AS position_id, " +
                "s.description AS statusDescription, " +
                "pos.wage AS wage, " +
                "acc.username, " +
                "acc.role_id, " + // Cần lấy role_id để check ẩn hiện trên UI
                "COUNT(*) OVER() AS total_count " +
                "FROM employee e " +
                "LEFT JOIN account acc ON e.account_id = acc.id " +
                "LEFT JOIN status s ON e.status_id = s.id " +
                "LEFT JOIN position pos ON e.position_id = pos.id " +
                "WHERE e.id != ? " + // Loại chính mình
                "AND acc.role_id >= ? " + // PHÂN QUYỀN: Ẩn cấp trên dựa vào Role
                "AND (? = '' OR (" +
                "    CAST(e.id AS CHAR) LIKE ? " +
                "    OR LOWER(e.first_name) LIKE ? " +
                "    OR LOWER(e.last_name) LIKE ? " +
                "    OR LOWER(CONCAT(e.first_name, ' ', e.last_name)) LIKE ?" +
                ")) " +
                "AND (? = -1 OR e.position_id = ?) " + // FILTER: Lọc theo Position (ID từ ComboBox)
                "AND (? = -1 OR e.status_id = ?) " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String cleanKeyword = (keyword == null) ? "" : keyword.trim();
            String searchKey = "%" + cleanKeyword.toLowerCase() + "%";

            int idx = 1;
            ps.setInt(idx++, excludeId);
            ps.setInt(idx++, currentAccountRoleId);

            ps.setString(idx++, cleanKeyword);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);

            // Filter theo Position ID
            ps.setInt(idx++, filterPosId);
            ps.setInt(idx++, filterPosId);

            // Filter theo Status ID
            ps.setInt(idx++, statusId);
            ps.setInt(idx++, statusId);

            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0)
                        totalItems = rs.getInt("total_count");

                    // Lưu ý: Nhớ thêm RoleId vào DTO nếu bạn muốn xử lý ẩn nút trên JavaFX
                    items.add(mapResultSetToDisplayObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lọc nhân viên: " + e.getMessage());
        }

        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    /**
     * Map ResultSet to EmployeeDisplayDTO for manage display
     * 
     * @param rs ResultSet from filterEmployeesPagedForManageDisplay
     * @return EmployeeDisplayDTO
     */
    private EmployeeDisplayDTO mapResultSetToDisplayObject(ResultSet rs) throws SQLException {
        return new EmployeeDisplayDTO(
                rs.getInt("id"),
                rs.getString("first_name") + " " + rs.getString("last_name"),
                rs.getString("gender"),
                rs.getInt("position_id"),
                rs.getString("positionName"),
                rs.getObject("wage") != null ? rs.getBigDecimal("wage") : null,
                rs.getString("username"),
                rs.getInt("status_id"),
                rs.getString("statusDescription"));
    }
    // ==================== 4 NEW GETTER METHODS (per tab) ====================

    /**
     * Lấy thông tin cá nhân của nhân viên (TAB 1: PERSONAL INFO)
     * 
     * @param employeeId ID của employee
     * @return EmployeePersonalInfoDTO hoặc null nếu không tìm thấy
     */
    public EmployeePersonalInfoDTO getPersonalInfo(int employeeId) {
        String sql = "SELECT e.id AS employee_id, e.first_name, e.last_name, e.date_of_birth, e.gender, " +
                "e.phone, e.email, e.status_id, st.description AS status_name, " +
                "e.avatar_url, e.created_at, e.updated_at " +
                "FROM employee e " +
                "LEFT JOIN status st ON e.status_id = st.id " +
                "WHERE e.id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return EmployeePersonalInfoDTO.builder()
                            .employeeId(resultSet.getInt("employee_id"))
                            .firstName(resultSet.getString("first_name"))
                            .lastName(resultSet.getString("last_name"))
                            .dateOfBirth(resultSet.getDate("date_of_birth") != null
                                    ? resultSet.getDate("date_of_birth").toLocalDate()
                                    : null)
                            .gender(resultSet.getString("gender"))
                            .phone(resultSet.getString("phone"))
                            .email(resultSet.getString("email"))
                            .statusId(resultSet.getInt("status_id"))
                            .statusName(resultSet.getString("status_name"))
                            .avatarUrl(resultSet.getString("avatar_url"))
                            .createdAt(resultSet.getTimestamp("created_at") != null
                                    ? resultSet.getTimestamp("created_at").toLocalDateTime()
                                    : null)
                            .updatedAt(resultSet.getTimestamp("updated_at") != null
                                    ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                                    : null)
                            .build();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employee personal info: " + e.getMessage());
        }

        return null;
    }

    /**
     * Lấy thông tin tài khoản của nhân viên (TAB 2: ACCOUNT INFO)
     * 
     * @param employeeId ID của employee
     * @return EmployeeAccountInfoDTO hoặc null nếu không tìm thấy
     */
    public EmployeeAccountInfoDTO getAccountInfo(int employeeId) {
        String sql = "SELECT a.id AS account_id, a.username, a.role_id, r.name AS role_name, " +
                "a.status_id AS account_status_id, st_acc.description AS account_status, " +
                "a.last_login, e.created_at, e.updated_at " +
                "FROM employee e " +
                "LEFT JOIN account a ON e.account_id = a.id " +
                "LEFT JOIN role r ON a.role_id = r.id " +
                "LEFT JOIN status st_acc ON a.status_id = st_acc.id " +
                "WHERE e.id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return EmployeeAccountInfoDTO.builder()
                            .accountId(
                                    resultSet.getObject("account_id") != null ? resultSet.getInt("account_id") : null)
                            .username(resultSet.getString("username"))
                            .roleId(resultSet.getObject("role_id") != null ? resultSet.getInt("role_id") : null)
                            .roleName(resultSet.getString("role_name"))
                            .accountStatusId(resultSet.getObject("account_status_id") != null
                                    ? resultSet.getInt("account_status_id")
                                    : null)
                            .accountStatus(resultSet.getString("account_status"))
                            .lastLogin(resultSet.getTimestamp("last_login") != null
                                    ? resultSet.getTimestamp("last_login").toLocalDateTime()
                                    : null)
                            .createdAt(resultSet.getTimestamp("created_at") != null
                                    ? resultSet.getTimestamp("created_at").toLocalDateTime()
                                    : null)
                            .updatedAt(resultSet.getTimestamp("updated_at") != null
                                    ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                                    : null)
                            .build();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employee account info: " + e.getMessage());
        }

        return null;
    }

    /**
     * Lấy thông tin công việc của nhân viên (TAB 3: JOB INFO)
     * Note: employmentHistoryDetail sẽ được load riêng thông qua BUS
     * 
     * @param employeeId ID của employee
     * @return EmployeeJobInfoDTO hoặc null nếu không tìm thấy
     */
    public EmployeeJobInfoDTO getJobInfo(int employeeId) {
        // Câu SQL JOIN các bảng để lấy Position từ Employee
        String sql = "SELECT " +
                "e.department_id, d.name AS department_name, " +
                "pos.id AS position_id, pos.name AS position_name, pos.wage, " +
                "e.status_id, st.description AS status_description, " +
                "e.created_at, e.updated_at " +
                "FROM employee e " +
                "LEFT JOIN department d ON e.department_id = d.id " +
                "LEFT JOIN status st ON e.status_id = st.id " +
                "LEFT JOIN position pos ON e.position_id = pos.id " + // Lấy Position từ Employee
                "WHERE e.id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return EmployeeJobInfoDTO.builder()
                            .departmentId(
                                    resultSet.getObject("department_id") != null ? resultSet.getInt("department_id")
                                            : null)
                            .departmentName(resultSet.getString("department_name"))

                            // Lấy thông tin Position (Vị trí công việc & Lương)
                            .positionId(
                                    resultSet.getObject("position_id") != null ? resultSet.getInt("position_id") : null)
                            .positionName(resultSet.getString("position_name"))
                            .wage(resultSet.getBigDecimal("wage"))

                            .statusId(resultSet.getObject("status_id") != null ? resultSet.getInt("status_id") : null)
                            .statusDescription(resultSet.getString("status_description"))

                            .createdAt(resultSet.getTimestamp("created_at") != null
                                    ? resultSet.getTimestamp("created_at").toLocalDateTime()
                                    : null)
                            .updatedAt(resultSet.getTimestamp("updated_at") != null
                                    ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                                    : null)
                            .build();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employee job info: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy thông tin lương & bảo hiểm của nhân viên (TAB 4: PAYROLL & BENEFITS)
     * 
     * @param employeeId ID của employee
     * @return EmployeePayrollInfoDTO hoặc null nếu không tìm thấy
     */
    public EmployeePayrollInfoDTO getPayrollInfo(int employeeId) {
        String sql = "SELECT e.id, e.num_dependents, e.health_insurance_code, e.social_insurance_code, " +
                "e.unemployment_insurance_code, " +
                "e.is_meal_support, " +
                "e.is_transportation_support, e.is_accommodation_support, e.created_at, e.updated_at " +
                "FROM employee e " +
                "WHERE e.id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return EmployeePayrollInfoDTO.builder()
                            .id(resultSet.getInt("id"))
                            .healthInsCode(resultSet.getString("health_insurance_code"))
                            .socialInsCode(resultSet.getString("social_insurance_code"))
                            .unemploymentInsCode(resultSet.getString("unemployment_insurance_code"))
                            .taxId(null)
                            .numDependents(resultSet.getObject("num_dependents") != null
                                    ? resultSet.getInt("num_dependents")
                                    : null)
                            .isMealSupport(resultSet.getBoolean("is_meal_support"))
                            .isTransportationSupport(resultSet.getBoolean("is_transportation_support"))
                            .isAccommodationSupport(resultSet.getBoolean("is_accommodation_support"))
                            .createdAt(resultSet.getTimestamp("created_at") != null
                                    ? resultSet.getTimestamp("created_at").toLocalDateTime()
                                    : null)
                            .updatedAt(resultSet.getTimestamp("updated_at") != null
                                    ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                                    : null)
                            .build();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employee payroll info: " + e.getMessage());
        }

        return null;
    }

    /**
     * Insert Employee sử dụng Connection truyền vào (phục vụ Transaction)
     * 
     * @param conn Connection từ Master BUS
     * @param obj  EmployeeDTO cần lưu
     * @return true nếu insert thành công
     * @throws SQLException
     */
    public boolean insertWithConn(Connection conn, EmployeeDTO obj) throws SQLException {
        String sql = "INSERT INTO employee " + getInsertQuery();

        // Sử dụng RETURN_GENERATED_KEYS để lấy ID tự tăng sau khi chèn thành công
        try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Gán các tham số (Dùng lại hàm setInsertParameters bạn đã viết ở trên)
            setInsertParameters(statement, obj);

            // 2. Thực thi lệnh
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                // 3. Lấy ID vừa sinh ra gán ngược lại cho đối tượng DTO
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        obj.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean updateStatus(int id, int newStatusId) {
        String query = "UPDATE employee SET status_id = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, newStatusId);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating employee status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all employees with a specific status ID
     */
    public ArrayList<EmployeeDTO> getByStatusId(int statusId) {
        ArrayList<EmployeeDTO> list = new ArrayList<>();
        String query = "SELECT * FROM employee WHERE status_id = ? ORDER BY id";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, statusId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSetToObject(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting employees by status: " + e.getMessage());
        }
        return list;
    }
}
