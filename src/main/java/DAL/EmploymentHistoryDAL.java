package DAL;

import DTO.EmploymentHistoryDTO;
import DTO.EmploymentHistoryDetailBasicDTO;
import DTO.PagedResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmploymentHistoryDAL extends BaseDAL<EmploymentHistoryDTO, Integer> {
    public static final EmploymentHistoryDAL INSTANCE = new EmploymentHistoryDAL();

    private EmploymentHistoryDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "employment_history", "id");
    }

    public static EmploymentHistoryDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected EmploymentHistoryDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new EmploymentHistoryDTO(
                resultSet.getInt("id"),
                resultSet.getInt("employee_id"),
                resultSet.getInt("department_id"),
                resultSet.getInt("role_id"),
                resultSet.getDate("effective_date") != null ? resultSet.getDate("effective_date").toLocalDate() : null,
                resultSet.getObject("approver_id") != null ? resultSet.getInt("approver_id") : null,
                resultSet.getInt("status_id"),
                resultSet.getString("reason"),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(EmploymentHistoryDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(employee_id, department_id, role_id, effective_date, approver_id, status_id, reason) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, EmploymentHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setInt(2, obj.getDepartmentId());
        statement.setInt(3, obj.getRoleId());
        statement.setDate(4, obj.getEffectiveDate() != null ? java.sql.Date.valueOf(obj.getEffectiveDate()) : null);
        statement.setObject(5, obj.getApproverId());
        statement.setInt(6, obj.getStatusId());
        statement.setString(7, obj.getReason());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, department_id = ?, role_id = ?, effective_date = ?, approver_id = ?, status_id = ?, reason = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, EmploymentHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setInt(2, obj.getDepartmentId());
        statement.setInt(3, obj.getRoleId());
        statement.setDate(4, obj.getEffectiveDate() != null ? java.sql.Date.valueOf(obj.getEffectiveDate()) : null);
        statement.setObject(5, obj.getApproverId());
        statement.setInt(6, obj.getStatusId());
        statement.setString(7, obj.getReason());
        statement.setInt(8, obj.getId());
    }

    public PagedResponse<EmploymentHistoryDTO> getPagedByEmployeeId(int employeeId, int pageIndex, int pageSize) {
        List<EmploymentHistoryDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // Sử dụng SQL Window Function để lấy total_count trong cùng 1 query
        String sql = "SELECT *, COUNT(*) OVER() as total_count " +
                "FROM employment_history " +
                "WHERE employee_id = ? " +
                "ORDER BY effective_date DESC " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setInt(2, offset);
            ps.setInt(3, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Lấy total_count từ dòng đầu tiên (mọi dòng đều có giá trị này giống nhau)
                    if (totalItems == 0) {
                        totalItems = rs.getInt("total_count");
                    }
                    // Tận dụng hàm mapResultSetToObject bạn đã viết sẵn
                    items.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) {
            // Log lỗi bằng Slf4j bạn đã dùng ở Controller (nếu DAL chưa có thì thêm @Slf4j)
            System.err.println("Lỗi phân trang lịch sử nhân viên: " + e.getMessage());
        }

        // Trả về "hộp" PagedResponse đã chứa đầy đủ mảnh ghép
        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    /**
     * Lấy chi tiết lịch sử công tác với tên phòng ban và chức vụ (JOIN)
     * Kết quả gồm: departmentId, departmentName, roleId, roleName, effectiveDate,
     * createdAt
     */
    public PagedResponse<EmploymentHistoryDetailBasicDTO> getDetailsByEmployeeIdPaged(int employeeId, int pageIndex,
            int pageSize) {
        List<EmploymentHistoryDetailBasicDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        String sql = "SELECT " +
                "eh.department_id, " +
                "d.name as department_name, " +
                "eh.role_id, " +
                "r.name as role_name, " +
                "eh.effective_date, " +
                "eh.created_at, " +
                "COUNT(*) OVER() as total_count " +
                "FROM employment_history eh " +
                "LEFT JOIN department d ON eh.department_id = d.id " +
                "LEFT JOIN role r ON eh.role_id = r.id " +
                "WHERE eh.employee_id = ? " +
                "ORDER BY eh.effective_date DESC " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ps.setInt(2, offset);
            ps.setInt(3, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0) {
                        totalItems = rs.getInt("total_count");
                    }

                    EmploymentHistoryDetailBasicDTO dto = new EmploymentHistoryDetailBasicDTO(
                            rs.getInt("department_id"),
                            rs.getString("department_name"),
                            rs.getInt("role_id"),
                            rs.getString("role_name"),
                            rs.getDate("effective_date") != null ? rs.getDate("effective_date").toLocalDate() : null,
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime()
                                    : null);
                    items.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy lịch sử công tác chi tiết: " + e.getMessage());
        }

        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }
}
