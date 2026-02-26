package DAL;

import DTO.EmploymentHistoryDTO;
import DTO.EmploymentHistoryDetailBasicDTO;
import DTO.EmploymentHistoryDetailDTO;
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
                resultSet.getInt("position_id"),
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
        return "(employee_id, department_id, position_id, effective_date, approver_id, status_id, reason) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, EmploymentHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setInt(2, obj.getDepartmentId());
        statement.setInt(3, obj.getPositionId());
        statement.setDate(4, obj.getEffectiveDate() != null ? java.sql.Date.valueOf(obj.getEffectiveDate()) : null);
        statement.setObject(5, obj.getApproverId());
        statement.setInt(6, obj.getStatusId());
        statement.setString(7, obj.getReason());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, department_id = ?, position_id = ?, effective_date = ?, approver_id = ?, status_id = ?, reason = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, EmploymentHistoryDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setInt(2, obj.getDepartmentId());
        statement.setInt(3, obj.getPositionId());
        statement.setDate(4, obj.getEffectiveDate() != null ? java.sql.Date.valueOf(obj.getEffectiveDate()) : null);
        statement.setObject(5, obj.getApproverId());
        statement.setInt(6, obj.getStatusId());
        statement.setString(7, obj.getReason());
        statement.setInt(8, obj.getId());
    }

    /**
     * Lấy chi tiết lịch sử công tác với tên phòng ban và vị trí (JOIN)
     * Kết quả gồm: departmentId, departmentName, positionId, positionName,
     * effectiveDate,
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
                "eh.position_id, " +
                "p.name as position_name, " +
                "eh.effective_date, " +
                "eh.created_at, " +
                "COUNT(*) OVER() as total_count " +
                "FROM employment_history eh " +
                "LEFT JOIN department d ON eh.department_id = d.id " +
                "LEFT JOIN position p ON eh.position_id = p.id " +
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
                            rs.getInt("position_id"),
                            rs.getString("position_name"),
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

    public PagedResponse<EmploymentHistoryDetailDTO> getDetailsFullByEmployeeIdPaged(int employeeId, int pageIndex,
            int pageSize) {
        List<EmploymentHistoryDetailDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // SQL lấy đầy đủ thông tin: Lý do, Người duyệt, Phòng ban, Vị trí, Trạng thái
        String sql = "SELECT " +
                "eh.employee_id, eh.effective_date, eh.reason, eh.created_at, " +
                "eh.department_id, d.name as department_name, " +
                "eh.position_id, p.name as position_name, " +
                "eh.approver_id, CONCAT(app.first_name, ' ', app.last_name) as approver_name, " +
                "eh.status_id, s.description as status_description, " +
                "COUNT(*) OVER() as total_count " +
                "FROM employment_history eh " +
                "LEFT JOIN department d ON eh.department_id = d.id " +
                "LEFT JOIN position p ON eh.position_id = p.id " +
                "LEFT JOIN employee app ON eh.approver_id = app.id " +
                "LEFT JOIN status s ON eh.status_id = s.id " +
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

                    // Sử dụng Constructor của EmploymentHistoryDetailDTO
                    EmploymentHistoryDetailDTO dto = new EmploymentHistoryDetailDTO(
                            rs.getInt("employee_id"),
                            rs.getDate("effective_date") != null ? rs.getDate("effective_date").toLocalDate() : null,
                            rs.getObject("department_id") != null ? rs.getInt("department_id") : null,
                            rs.getString("department_name"),
                            rs.getObject("position_id") != null ? rs.getInt("position_id") : null,
                            rs.getString("position_name"),
                            rs.getString("reason"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime()
                                    : null,
                            rs.getInt("approver_id"),
                            rs.getString("approver_name"),
                            rs.getInt("status_id"),
                            rs.getString("status_description"));
                    items.add(dto);
                }
            }
        } catch (SQLException e) {
            // Log lỗi hoặc ném custom exception
            System.err.println("Lỗi DAL lấy lịch sử công tác full: " + e.getMessage());
        }

        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    /**
     * Lấy danh sách lịch sử điều chuyển để hiển thị trong bảng quản lý
     * Có thể filter theo keyword (mã NV, mã quyết định), employee, department,
     * position, status
     */
    public PagedResponse<DTO.EmploymentHistoryDisplayDTO> filterEmploymentHistoryPagedForManageDisplay(
            String keyword,
            Integer employeeId,
            Integer departmentId,
            Integer positionId,
            Integer statusId,
            int pageIndex,
            int pageSize) {

        List<DTO.EmploymentHistoryDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("eh.id, ");
        sql.append("eh.employee_id, ");
        sql.append("CONCAT(emp.first_name, ' ', emp.last_name) as employee_name, ");
        sql.append("eh.department_id, ");
        sql.append("d.name as department_name, ");
        sql.append("eh.position_id, ");
        sql.append("p.name as position_name, ");
        sql.append("eh.effective_date, ");
        sql.append("eh.approver_id, ");
        sql.append("CONCAT(app.first_name, ' ', app.last_name) as approver_name, ");
        sql.append("eh.status_id, ");
        sql.append("s.description as status_description, ");
        sql.append("eh.reason, ");
        sql.append("eh.created_at, ");
        sql.append("COUNT(*) OVER() as total_count ");
        sql.append("FROM employment_history eh ");
        sql.append("LEFT JOIN employee emp ON eh.employee_id = emp.id ");
        sql.append("LEFT JOIN department d ON eh.department_id = d.id ");
        sql.append("LEFT JOIN position p ON eh.position_id = p.id ");
        sql.append("LEFT JOIN employee app ON eh.approver_id = app.id ");
        sql.append("LEFT JOIN status s ON eh.status_id = s.id ");
        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // Filter by keyword (search in employee_id or decision id)
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(
                    "AND (eh.id LIKE ? OR eh.employee_id LIKE ? OR CONCAT(emp.first_name, ' ', emp.last_name) LIKE ?) ");
            String keywordPattern = "%" + keyword.trim() + "%";
            params.add(keywordPattern);
            params.add(keywordPattern);
            params.add(keywordPattern);
        }

        // Filter by employee
        if (employeeId != null && employeeId > 0) {
            sql.append("AND eh.employee_id = ? ");
            params.add(employeeId);
        }

        // Filter by department
        if (departmentId != null && departmentId > 0) {
            sql.append("AND eh.department_id = ? ");
            params.add(departmentId);
        }

        // Filter by position
        if (positionId != null && positionId > 0) {
            sql.append("AND eh.position_id = ? ");
            params.add(positionId);
        }

        // Filter by status
        if (statusId != null && statusId > 0) {
            sql.append("AND eh.status_id = ? ");
            params.add(statusId);
        }

        sql.append("ORDER BY eh.created_at DESC ");
        sql.append("LIMIT ?, ?");
        params.add(offset);
        params.add(pageSize);

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0) {
                        totalItems = rs.getInt("total_count");
                    }

                    DTO.EmploymentHistoryDisplayDTO dto = new DTO.EmploymentHistoryDisplayDTO(
                            rs.getInt("id"),
                            rs.getInt("employee_id"),
                            rs.getString("employee_name"),
                            rs.getObject("department_id") != null ? rs.getInt("department_id") : null,
                            rs.getString("department_name"),
                            rs.getObject("position_id") != null ? rs.getInt("position_id") : null,
                            rs.getString("position_name"),
                            rs.getDate("effective_date") != null ? rs.getDate("effective_date").toLocalDate() : null,
                            rs.getObject("approver_id") != null ? rs.getInt("approver_id") : null,
                            rs.getString("approver_name"),
                            rs.getInt("status_id"),
                            rs.getString("status_description"),
                            rs.getString("reason"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime()
                                    : null);
                    items.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DAL filter employment history: " + e.getMessage());
            e.printStackTrace();
        }

        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }
}
