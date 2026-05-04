package DAL;

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
    protected LeaveRequestDTO mapResultSetToObject(ResultSet rs) throws SQLException {

        String leaveTypeName = "";
        String statusName = "";
        String employeeName = "";

        try { leaveTypeName = rs.getString("leave_type_name"); } catch (Exception ignored) {}
        try { statusName = rs.getString("status_name"); } catch (Exception ignored) {}
        try { employeeName = rs.getString("employee_name"); } catch (Exception ignored) {}

        LeaveRequestDTO dto = new LeaveRequestDTO(
                rs.getInt("id"),
                rs.getInt("leave_type_id"),
                leaveTypeName != null ? leaveTypeName : "",
                rs.getString("content"),
                rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null,
                rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null,
                rs.getInt("status_id"),
                statusName != null ? statusName : "",
                rs.getInt("employee_id")
        );

        dto.setEmployeeName(employeeName != null ? employeeName : "");

        return dto;
    }

    @Override
    protected String getInsertQuery() {
        return "(leave_type_id, content, start_date, end_date, status_id, employee_id) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, LeaveRequestDTO obj) throws SQLException {

        if (obj.getLeaveTypeId() > 0) {
            ps.setInt(1, obj.getLeaveTypeId());
        } else {
            ps.setNull(1, Types.INTEGER);
        }

        ps.setString(2, obj.getContent());
        ps.setDate(3, obj.getStartDate() != null ? Date.valueOf(obj.getStartDate()) : null);
        ps.setDate(4, obj.getEndDate() != null ? Date.valueOf(obj.getEndDate()) : null);
        ps.setInt(5, obj.getStatusId());
        ps.setInt(6, obj.getEmployeeId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET leave_type_id=?, content=?, start_date=?, end_date=?, status_id=?, employee_id=? WHERE id=?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement ps, LeaveRequestDTO obj) throws SQLException {
        setInsertParameters(ps, obj);
        ps.setInt(7, obj.getId());
    }

    // =========================
    // CUSTOM QUERY (FIX LỖI)
    // =========================

    @Override
    public ArrayList<LeaveRequestDTO> getAll() {
        String sql = """
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
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSetToObject(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public ArrayList<LeaveRequestDTO> getByEmployeeId(int employeeId) {
        String sql = """
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
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToObject(rs));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // =========================
    // STATUS UPDATE
    // =========================

    private boolean updateStatus(int id, int status) {
        String sql = "UPDATE leave_request SET status_id=? WHERE id=?";

        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status);
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean approve(int id) { return updateStatus(id, 21); }
    public boolean reject(int id) { return updateStatus(id, 22); }
    public boolean cancel(int id) { return updateStatus(id, 23); }
}