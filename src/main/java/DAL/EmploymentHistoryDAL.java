package DAL;

import DTO.EmploymentHistoryDTO;
import DTO.EmploymentHistoryDetailBasicDTO;
import DTO.EmploymentHistoryDetailDTO;
import DTO.EmploymentHistoryDisplayDTO;
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
        // Department ID: 0 means use current, so save as NULL
        statement.setObject(2, obj.getDepartmentId() > 0 ? obj.getDepartmentId() : null);
        // Position ID: 0 means use current, so save as NULL
        statement.setObject(3, obj.getPositionId() > 0 ? obj.getPositionId() : null);
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
        // Department ID: 0 means use current, so save as NULL
        statement.setObject(2, obj.getDepartmentId() > 0 ? obj.getDepartmentId() : null);
        // Position ID: 0 means use current, so save as NULL
        statement.setObject(3, obj.getPositionId() > 0 ? obj.getPositionId() : null);
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
                "eh.status_id, " +
                "s.description as status_description, " + // Lấy mô tả trạng thái từ bảng status
                "COUNT(*) OVER() as total_count " +
                "FROM employment_history eh " +
                "LEFT JOIN department d ON eh.department_id = d.id " +
                "LEFT JOIN position p ON eh.position_id = p.id " +
                "LEFT JOIN status s ON eh.status_id = s.id " + // Join thêm bảng status
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

                    // KHỚP VỚI 7 TRƯỜNG TRONG DTO CỦA HOÀNG
                    EmploymentHistoryDetailBasicDTO dto = new EmploymentHistoryDetailBasicDTO(
                            rs.getInt("department_id"),
                            rs.getString("department_name"),
                            rs.getInt("position_id"),
                            rs.getString("position_name"),
                            rs.getDate("effective_date") != null ? rs.getDate("effective_date").toLocalDate() : null,
                            rs.getInt("status_id"),
                            rs.getString("status_description"));
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
    public PagedResponse<EmploymentHistoryDisplayDTO> filterEmploymentHistoryPagedForManageDisplay(
            String keyword, Integer employeeId, Integer departmentId, Integer positionId,
            Integer statusId, int pageIndex, int pageSize) {

        List<EmploymentHistoryDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // Sử dụng SQL tĩnh với các kỹ thuật lọc linh hoạt
        String sql = "SELECT eh.id, eh.employee_id, " +
                "IFNULL(CONCAT(emp.first_name, ' ', emp.last_name), 'N/A') as employee_name, " +
                "eh.department_id, d.name as department_name, " +
                "eh.position_id, p.name as position_name, eh.effective_date, " +
                "eh.approver_id, IFNULL(CONCAT(app.first_name, ' ', app.last_name), 'Hệ thống') as approver_name, " +
                "eh.status_id, s.description as status_description, eh.reason, eh.created_at, " +
                "COUNT(*) OVER() as total_count " +
                "FROM employment_history eh " +
                "LEFT JOIN employee emp ON eh.employee_id = emp.id " +
                "LEFT JOIN department d ON eh.department_id = d.id " +
                "LEFT JOIN position p ON eh.position_id = p.id " +
                "LEFT JOIN employee app ON eh.approver_id = app.id " +
                "LEFT JOIN status s ON eh.status_id = s.id " +
                "WHERE (? = '' OR (" + // Keyword filter
                "   CAST(eh.id AS CHAR) LIKE ? " +
                "   OR CAST(eh.employee_id AS CHAR) LIKE ? " +
                "   OR LOWER(emp.first_name) LIKE ? " +
                "   OR LOWER(emp.last_name) LIKE ? " +
                "   OR LOWER(CONCAT(emp.first_name, ' ', emp.last_name)) LIKE ?" +
                ")) " +
                "AND (? = -1 OR eh.employee_id = ?) " + // Employee filter
                "AND (? = -1 OR eh.department_id = ?) " + // Department filter
                "AND (? = -1 OR eh.position_id = ?) " + // Position filter
                "AND (? = -1 OR eh.status_id = ?) " + // Status filter
                "ORDER BY eh.created_at DESC " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String cleanKeyword = (keyword == null) ? "" : keyword.trim();
            String searchKey = "%" + cleanKeyword.toLowerCase() + "%";

            int idx = 1;
            // Bind Keyword params
            ps.setString(idx++, cleanKeyword);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);
            ps.setString(idx++, searchKey);

            // Bind ID filters (Dùng -1 làm giá trị "Tất cả")
            ps.setInt(idx++, employeeId != null ? employeeId : -1);
            ps.setInt(idx++, employeeId != null ? employeeId : -1);

            ps.setInt(idx++, departmentId != null ? departmentId : -1);
            ps.setInt(idx++, departmentId != null ? departmentId : -1);

            ps.setInt(idx++, positionId != null ? positionId : -1);
            ps.setInt(idx++, positionId != null ? positionId : -1);

            ps.setInt(idx++, statusId != null ? statusId : -1);
            ps.setInt(idx++, statusId != null ? statusId : -1);

            // Pagination
            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0)
                        totalItems = rs.getInt("total_count");

                    items.add(new EmploymentHistoryDisplayDTO(
                            rs.getInt("id"),
                            rs.getInt("employee_id"),
                            rs.getString("employee_name"),
                            (Integer) rs.getObject("department_id"),
                            rs.getString("department_name"),
                            (Integer) rs.getObject("position_id"),
                            rs.getString("position_name"),
                            rs.getDate("effective_date") != null ? rs.getDate("effective_date").toLocalDate() : null,
                            (Integer) rs.getObject("approver_id"),
                            rs.getString("approver_name"),
                            rs.getInt("status_id"),
                            rs.getString("status_description"),
                            rs.getString("reason"),
                            rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime()
                                    : null));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi DAL filter employment history display: " + e.getMessage());
        }

        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    /**
     * Update only the status of employment history
     * Used for approve/reject operations to avoid updating other fields
     */
    public boolean updateStatus(int id, int statusId) {
        String sql = "UPDATE employment_history SET status_id = ? WHERE id = ?";
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật trạng thái công tác: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gọi stored procedure sp_SyncEmploymentChanges để đồng bộ hóa
     * thay đổi công tác khi quyết định được phê duyệt khi effective_date đến
     */
    public void callSyncEmploymentChanges() throws SQLException {
        String sql = "{CALL sp_SyncEmploymentChanges()}";
        try (Connection conn = connectionFactory.newConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.execute();
        } catch (SQLException e) {
            System.err.println("Lỗi khi gọi sp_SyncEmploymentChanges: " + e.getMessage());
            throw e;
        }
    }
}
