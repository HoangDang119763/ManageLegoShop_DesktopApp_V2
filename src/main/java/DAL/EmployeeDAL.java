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

    public boolean updateJobInfo(Connection conn, EmployeeDTO obj) {
        String query = "UPDATE employee SET department_id = ?, role_id = ?, status_id = ? WHERE id = ?";

        // Không dùng try-with-resources cho Connection ở đây vì BUS quản lý vòng đời
        // của nó
        try (PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setObject(1, obj.getDepartmentId());
            statement.setObject(2, obj.getRoleId());
            statement.setInt(3, obj.getStatusId());
            statement.setInt(4, obj.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating job info in DAL: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePayrollInfo(Connection conn, EmployeeDTO obj) throws SQLException {
        String sql = "UPDATE employee SET health_ins_code = ?, is_social_insurance = ?, " +
                "is_unemployment_insurance = ?, is_personal_income_tax = ?, " +
                "is_transportation_support = ?, is_accommodation_support = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, obj.getHealthInsCode());
            ps.setBoolean(2, obj.isSocialInsurance());
            ps.setBoolean(3, obj.isUnemploymentInsurance());
            ps.setBoolean(4, obj.isPersonalIncomeTax());
            ps.setBoolean(5, obj.isTransportationSupport());
            ps.setBoolean(6, obj.isAccommodationSupport());
            ps.setInt(7, obj.getId());
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
                "s.base AS salary_base, s.coefficient AS salary_coefficient, " +
                "st.id AS emp_status_id, st.description AS emp_status_desc, " +
                "a.id AS account_id_mapped, a.username, a.status_id AS account_status_id, " +
                "st_acc.description AS account_status_desc, " +
                "d.name AS department_name, " +
                "tax.id AS tax_id, tax.num_dependents " +
                "FROM employee e " +
                "LEFT JOIN role r ON e.role_id = r.id " +
                "LEFT JOIN salary s ON r.salary_id = s.id " +
                "LEFT JOIN status st ON e.status_id = st.id " +
                "LEFT JOIN account a ON e.account_id = a.id " +
                "LEFT JOIN status st_acc ON a.status_id = st_acc.id " +
                "LEFT JOIN department d ON e.department_id = d.id " +
                "LEFT JOIN tax ON tax.employee_id = e.id " +
                "WHERE e.id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToDetailDTO(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving employee detail: " + e.getMessage());
        }

        return null;
    }

    /**
     * Map ResultSet từ getDetailById() → EmployeeDetailDTO
     */
    private EmployeeDetailDTO mapResultSetToDetailDTO(ResultSet rs) throws SQLException {
        return EmployeeDetailDTO.builder()
                // Base info
                .id(rs.getInt("id"))
                .employeeId(rs.getInt("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .gender(rs.getString("gender"))
                .dateOfBirth(rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toLocalDate() : null)
                .phone(rs.getString("phone"))
                .email(rs.getString("email"))
                .healthInsCode(rs.getString("health_ins_code"))

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
                .salaryId(rs.getObject("salary_base") != null ? 1 : 0) // Dummy ID, actual from role.salary_id
                .baseSalary(rs.getObject("salary_base") != null ? rs.getBigDecimal("salary_base") : null)
                .salaryCoefficient(
                        rs.getObject("salary_coefficient") != null ? rs.getBigDecimal("salary_coefficient") : null)

                // Tax
                .taxId(rs.getObject("tax_id") != null ? rs.getInt("tax_id") : 0)
                .numDependents(rs.getObject("num_dependents") != null ? rs.getInt("num_dependents") : null)

                // Insurance & Support flags
                .isSocialInsurance(rs.getBoolean("is_social_insurance"))
                .isUnemploymentInsurance(rs.getBoolean("is_unemployment_insurance"))
                .isPersonalIncomeTax(rs.getBoolean("is_personal_income_tax"))
                .isTransportationSupport(rs.getBoolean("is_transportation_support"))
                .isAccommodationSupport(rs.getBoolean("is_accommodation_support"))

                // Timestamps
                .createdAt(
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)

                .build();
    }

    public PagedResponse<EmployeeDisplayDTO> filterEmployeesPagedForManageDisplay(
            String keyword, int roleId, int statusId, int pageIndex, int pageSize) {

        List<EmployeeDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // SQL hỗ trợ tìm kiếm theo ID, Họ, Tên, và cả Họ Tên đầy đủ (CONCAT)
        String sql = "SELECT e.id, e.first_name, e.last_name, e.gender, e.role_id, e.status_id, e.account_id, " +
                "r.name AS roleName, " +
                "s.description AS statusDescription, " +
                "sal.base AS salary, " +
                "sal.coefficient AS efficientSalary, " +
                "acc.username, " +
                "COUNT(*) OVER() AS total_count " +
                "FROM employee e " +
                "LEFT JOIN role r ON e.role_id = r.id " +
                "LEFT JOIN status s ON e.status_id = s.id " +
                "LEFT JOIN salary sal ON r.salary_id = sal.id " +
                "LEFT JOIN account acc ON e.account_id = acc.id " +
                "WHERE e.id != 1 " + // Loại trừ admin hệ thống (ID = 1)
                "AND (? = '' OR (" +
                "    CAST(e.id AS CHAR) LIKE ? " +
                "    OR LOWER(e.first_name) LIKE ? " +
                "    OR LOWER(e.last_name) LIKE ? " +
                "    OR LOWER(CONCAT(e.first_name, ' ', e.last_name)) LIKE ?" +
                ")) " +
                "AND (? = -1 OR e.role_id = ?) " +
                "AND (? = -1 OR e.status_id = ?) " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Chuẩn hóa từ khóa tìm kiếm
            String cleanKeyword = (keyword == null) ? "" : keyword.trim();
            String searchKey = "%" + cleanKeyword.toLowerCase() + "%";

            int idx = 1;

            // Gán tham số cho phần Tìm kiếm (Dựa trên cleanKeyword)
            ps.setString(idx++, cleanKeyword);
            ps.setString(idx++, searchKey); // Like ID
            ps.setString(idx++, searchKey); // Like First Name
            ps.setString(idx++, searchKey); // Like Last Name
            ps.setString(idx++, searchKey); // Like Full Name (CONCAT)

            // Gán tham số cho Filter Role
            ps.setInt(idx++, roleId);
            ps.setInt(idx++, roleId);

            // Gán tham số cho Filter Status
            ps.setInt(idx++, statusId);
            ps.setInt(idx++, statusId);

            // Gán tham số cho Phân trang
            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0) {
                        totalItems = rs.getInt("total_count");
                    }
                    items.add(mapResultSetToDisplayObject(rs)); // Sử dụng hàm map đã có của bạn
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lọc nhân viên: " + e.getMessage());
        }

        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    public PagedResponse<EmployeeDisplayDTO> filterEmployeesPagedForManageDisplay(
            String keyword, int roleId, int statusId, int pageIndex, int pageSize, int excludeId) {

        List<EmployeeDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // Thêm điều kiện e.id != ? để loại bỏ chính mình
        String sql = "SELECT e.id, e.first_name, e.last_name, e.gender, e.role_id, e.status_id, e.account_id, " +
                "r.name AS roleName, " +
                "s.description AS statusDescription, " +
                "sal.base AS salary, " +
                "sal.coefficient AS efficientSalary, " +
                "acc.username, " +
                "COUNT(*) OVER() AS total_count " +
                "FROM employee e " +
                "LEFT JOIN role r ON e.role_id = r.id " +
                "LEFT JOIN status s ON e.status_id = s.id " +
                "LEFT JOIN salary sal ON r.salary_id = sal.id " +
                "LEFT JOIN account acc ON e.account_id = acc.id " +
                "WHERE e.id != 1 " + // Loại trừ admin hệ thống
                "AND e.id != ? " + // LOẠI TRỪ CHÍNH MÌNH (Tham số mới)
                "AND (? = '' OR (" +
                "    CAST(e.id AS CHAR) LIKE ? " +
                "    OR LOWER(e.first_name) LIKE ? " +
                "    OR LOWER(e.last_name) LIKE ? " +
                "    OR LOWER(CONCAT(e.first_name, ' ', e.last_name)) LIKE ?" +
                ")) " +
                "AND (? = -1 OR e.role_id = ?) " +
                "AND (? = -1 OR e.status_id = ?) " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String cleanKeyword = (keyword == null) ? "" : keyword.trim();
            String searchKey = "%" + cleanKeyword.toLowerCase() + "%";

            int idx = 1;

            // 1. Gán ID cần loại trừ
            ps.setInt(idx++, excludeId);

            // 2. Gán tham số tìm kiếm
            ps.setString(idx++, cleanKeyword);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);

            // 3. Gán Filter Role
            ps.setInt(idx++, roleId);
            ps.setInt(idx++, roleId);

            // 4. Gán Filter Status
            ps.setInt(idx++, statusId);
            ps.setInt(idx++, statusId);

            // 5. Phân trang
            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0) {
                        totalItems = rs.getInt("total_count");
                    }
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
                rs.getInt("role_id"),
                rs.getString("roleName"),
                rs.getObject("salary") != null ? rs.getBigDecimal("salary") : null,
                rs.getObject("efficientSalary") != null ? rs.getBigDecimal("efficientSalary") : null,
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
                "e.phone, e.email, e.created_at, e.updated_at " +
                "FROM employee e WHERE e.id = ? LIMIT 1";

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
        String sql = "SELECT a.id AS account_id, a.username, a.status_id AS account_status_id, " +
                "st_acc.description AS account_status, a.last_login, e.created_at, e.updated_at " +
                "FROM employee e " +
                "LEFT JOIN account a ON e.account_id = a.id " +
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
        String sql = "SELECT e.department_id, d.name AS department_name, r.id AS role_id, r.name AS role_name, " +
                "e.status_id, st.description AS status_description, s.id AS salary_id, s.base AS base_salary, " +
                "s.coefficient AS salary_coefficient, e.created_at, e.updated_at " +
                "FROM employee e " +
                "LEFT JOIN department d ON e.department_id = d.id " +
                "LEFT JOIN role r ON e.role_id = r.id " +
                "LEFT JOIN status st ON e.status_id = st.id " +
                "LEFT JOIN salary s ON r.salary_id = s.id " +
                "WHERE e.id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return EmployeeJobInfoDTO.builder()
                            .departmentId(resultSet.getObject("department_id") != null
                                    ? resultSet.getInt("department_id")
                                    : null)
                            .departmentName(resultSet.getString("department_name"))
                            .roleId(resultSet.getObject("role_id") != null
                                    ? resultSet.getInt("role_id")
                                    : null)
                            .roleName(resultSet.getString("role_name"))
                            .statusId(resultSet.getObject("status_id") != null
                                    ? resultSet.getInt("status_id")
                                    : null)
                            .statusDescription(resultSet.getString("status_description"))
                            .salaryId(resultSet.getObject("salary_id") != null ? resultSet.getInt("salary_id") : null)
                            .baseSalary(resultSet.getObject("base_salary") != null
                                    ? resultSet.getBigDecimal("base_salary")
                                    : null)
                            .salaryCoefficient(resultSet.getObject("salary_coefficient") != null
                                    ? resultSet.getBigDecimal("salary_coefficient")
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
        String sql = "SELECT e.id, e.health_ins_code, tax.id AS tax_id, tax.num_dependents, " +
                "e.is_social_insurance, e.is_unemployment_insurance, e.is_personal_income_tax, " +
                "e.is_transportation_support, e.is_accommodation_support, e.created_at, e.updated_at " +
                "FROM employee e " +
                "LEFT JOIN tax ON tax.employee_id = e.id " +
                "WHERE e.id = ? LIMIT 1";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return EmployeePayrollInfoDTO.builder()
                            .id(resultSet.getInt("id"))
                            .healthInsCode(resultSet.getString("health_ins_code"))
                            .taxId(resultSet.getObject("tax_id") != null ? resultSet.getInt("tax_id") : null)
                            .numDependents(resultSet.getObject("num_dependents") != null
                                    ? resultSet.getInt("num_dependents")
                                    : null)
                            .isSocialInsurance(resultSet.getBoolean("is_social_insurance"))
                            .isUnemploymentInsurance(resultSet.getBoolean("is_unemployment_insurance"))
                            .isPersonalIncomeTax(resultSet.getBoolean("is_personal_income_tax"))
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
}
