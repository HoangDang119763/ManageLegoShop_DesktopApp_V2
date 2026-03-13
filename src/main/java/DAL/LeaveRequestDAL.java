package DAL;

import DTO.HrStatisticDTO;
import DTO.LeaveRequestDTO;
import java.sql.*;
import java.util.ArrayList;

public class LeaveRequestDAL extends BaseDAL<LeaveRequestDTO, Integer> {
    public static final LeaveRequestDAL INSTANCE = new LeaveRequestDAL();

    private LeaveRequestDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "leave_request", "id");
    }

    public static LeaveRequestDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected LeaveRequestDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        String leaveTypeName = "";
        String statusName = "";
        String employeeName = "";

        // Lấy dữ liệu từ các cột JOIN an toàn
        try { leaveTypeName = resultSet.getString("leave_type_name"); } catch (SQLException ignored) {}
        try { statusName = resultSet.getString("status_name"); } catch (SQLException ignored) {}
        try { employeeName = resultSet.getString("employee_name"); } catch (SQLException ignored) {}

        LeaveRequestDTO dto = new LeaveRequestDTO(
                resultSet.getInt("id"),
                resultSet.getInt("leave_type_id"),
                leaveTypeName != null ? leaveTypeName : "",
                resultSet.getString("content"),
                resultSet.getDate("start_date") != null ? resultSet.getDate("start_date").toLocalDate() : null,
                resultSet.getDate("end_date") != null ? resultSet.getDate("end_date").toLocalDate() : null,
                resultSet.getInt("status_id"),
                statusName != null ? statusName : "",
                resultSet.getInt("employee_id")
        );
        
        // Gán tên nhân viên vào DTO (Đảm bảo DTO của bạn có field này)
        dto.setEmployeeName(employeeName != null ? employeeName : "");
        
        return dto;
    }

    @Override
    protected String getInsertQuery() {
        return "(leave_type_id, content, start_date, end_date, status_id, employee_id) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, LeaveRequestDTO obj) throws SQLException {
        statement.setInt(1, obj.getLeaveTypeId());
        statement.setString(2, obj.getContent());
        statement.setDate(3, obj.getStartDate() != null ? Date.valueOf(obj.getStartDate()) : null);
        statement.setDate(4, obj.getEndDate() != null ? Date.valueOf(obj.getEndDate()) : null);
        statement.setInt(5, obj.getStatusId());
        statement.setInt(6, obj.getEmployeeId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET leave_type_id = ?, content = ?, start_date = ?, end_date = ?, status_id = ?, employee_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, LeaveRequestDTO obj) throws SQLException {
        setInsertParameters(statement, obj);
        statement.setInt(7, obj.getId());
    }

    /**
     * Lấy tất cả đơn nghỉ phép kèm tên loại đơn, trạng thái và tên nhân viên
     */
    @Override
    public ArrayList<LeaveRequestDTO> getAll() {
        String query = """
            SELECT lr.*, 
                   COALESCE(lt.name,'') AS leave_type_name, 
                   COALESCE(s.name,'') AS status_name,
                   CONCAT(e.first_name, ' ', e.last_name) AS employee_name
            FROM leave_request lr
            LEFT JOIN leave_type lt ON lr.leave_type_id = lt.id
            LEFT JOIN status s ON lr.status_id = s.id
            LEFT JOIN employee e ON lr.employee_id = e.id
            ORDER BY lr.id DESC
            """;

        ArrayList<LeaveRequestDTO> list = new ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(mapResultSetToObject(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public ArrayList<LeaveRequestDTO> getByEmployeeId(Integer employeeId) {
        String query = """
            SELECT lr.*, 
                   COALESCE(lt.name,'') AS leave_type_name, 
                   COALESCE(s.name,'') AS status_name,
                   CONCAT(e.first_name, ' ', e.last_name) AS employee_name
            FROM leave_request lr
            LEFT JOIN leave_type lt ON lr.leave_type_id = lt.id
            LEFT JOIN status s ON lr.status_id = s.id
            LEFT JOIN employee e ON lr.employee_id = e.id
            WHERE lr.employee_id = ?
            ORDER BY lr.start_date DESC
            """;

        ArrayList<LeaveRequestDTO> list = new ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // --- LOGIC DUYỆT / TỪ CHỐI / HỦY ĐƠN ---

    /**
     * Hàm dùng chung để cập nhật trạng thái đơn
     */
    private boolean updateStatus(int leaveRequestId, int statusId) {
        String sql = "UPDATE leave_request SET status_id = ? WHERE id = ?";
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, statusId);
            ps.setInt(2, leaveRequestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Approve = 21 (Đã duyệt)
    public boolean approve(int leaveRequestId) {
        return updateStatus(leaveRequestId, 21);
    }

    // Reject = 22 (Từ chối)
    public boolean reject(int leaveRequestId) {
        return updateStatus(leaveRequestId, 22);
    }

    // Cancel = 23 (Hủy)
    public boolean cancel(int leaveRequestId) {
        return updateStatus(leaveRequestId, 23);
    }
}