package DAL;

import DTO.HrStatisticDTO;
import DTO.LeaveRequestDTO;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LeaveRequestDAL extends BaseDAL<LeaveRequestDTO, Integer> {

    public static final LeaveRequestDAL INSTANCE = new LeaveRequestDAL();

    private LeaveRequestDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "leave_request", "id");
    }

    public static LeaveRequestDAL getInstance() {
        return INSTANCE;
    }

    // =========================
    // MAP RESULT (SAFE)
    // =========================
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

    // =========================
    // INSERT / UPDATE
    // =========================
    @Override
    protected String getInsertQuery() {
        return "(leave_type_id, content, start_date, end_date, status_id, employee_id) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement ps, LeaveRequestDTO obj) throws SQLException {

        if (obj.getLeaveTypeId() > 0) ps.setInt(1, obj.getLeaveTypeId());
        else ps.setNull(1, Types.INTEGER);

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
    // GET DATA
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
    // STATUS
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

    // =========================
    // STATISTICS
    // =========================
    public HrStatisticDTO.LeaveStat getLeaveStat(int month, int year) {
        String sql = """
            SELECT COUNT(*) AS total_requests,
                   COALESCE(SUM(
                       CASE
                           WHEN lr.start_date IS NOT NULL AND lr.end_date IS NOT NULL
                           THEN DATEDIFF(lr.end_date, lr.start_date) + 1
                           ELSE 0
                       END
                   ), 0) AS total_days
            FROM leave_request lr
            WHERE lr.start_date <= ? AND lr.end_date >= ?
        """;

        HrStatisticDTO.LeaveStat stat = new HrStatisticDTO.LeaveStat();
        LocalDate[] range = getMonthRange(month, year);

        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(range[1]));
            ps.setDate(2, Date.valueOf(range[0]));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stat.setTotalRequests(rs.getInt("total_requests"));
                    stat.setTotalDays(rs.getInt("total_days"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stat;
    }

    public List<HrStatisticDTO.LeaveByTypeItem> getLeaveByType(int month, int year) {
        String sql = """
            SELECT COALESCE(lt.name, 'Không xác định') AS leave_type,
                   COUNT(*) AS total
            FROM leave_request lr
            LEFT JOIN leave_type lt ON lt.id = lr.leave_type_id
            WHERE lr.start_date <= ? AND lr.end_date >= ?
            GROUP BY leave_type
            ORDER BY total DESC
        """;

        List<HrStatisticDTO.LeaveByTypeItem> list = new ArrayList<>();
        LocalDate[] range = getMonthRange(month, year);

        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(range[1]));
            ps.setDate(2, Date.valueOf(range[0]));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HrStatisticDTO.LeaveByTypeItem(
                            rs.getString("leave_type"),
                            rs.getInt("total")));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<HrStatisticDTO.LeaveStatusItem> getLeaveByStatus(int month, int year) {
        String sql = """
            SELECT COALESCE(s.description, s.name, 'Không xác định') AS status_name,
                   COUNT(*) AS total
            FROM leave_request lr
            LEFT JOIN status s ON s.id = lr.status_id
            WHERE lr.start_date <= ? AND lr.end_date >= ?
            GROUP BY status_name
            ORDER BY total DESC
        """;

        List<HrStatisticDTO.LeaveStatusItem> list = new ArrayList<>();
        LocalDate[] range = getMonthRange(month, year);

        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(range[1]));
            ps.setDate(2, Date.valueOf(range[0]));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HrStatisticDTO.LeaveStatusItem(
                            rs.getString("status_name"),
                            rs.getInt("total")));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public java.util.List<HrStatisticDTO.LeaveRow> getLeaveRows(int month, int year) {
        String sql = """
                SELECT CONCAT(e.first_name, ' ', e.last_name) AS full_name,
                       COALESCE(lt.name, '—') AS leave_type,
                       DATE_FORMAT(lr.start_date, '%d/%m/%Y') AS start_date,
                       DATE_FORMAT(lr.end_date, '%d/%m/%Y') AS end_date,
                       CASE
                           WHEN lr.start_date IS NOT NULL AND lr.end_date IS NOT NULL
                           THEN DATEDIFF(lr.end_date, lr.start_date) + 1
                           ELSE 0
                       END AS days,
                       COALESCE(s.description, s.name, '—') AS status_name
                FROM leave_request lr
                LEFT JOIN leave_type lt ON lt.id = lr.leave_type_id
                LEFT JOIN status s ON s.id = lr.status_id
                LEFT JOIN employee e ON e.id = lr.employee_id
                WHERE lr.start_date <= ? AND lr.end_date >= ?
                ORDER BY lr.start_date DESC, lr.id DESC
                """;
        java.util.List<HrStatisticDTO.LeaveRow> list = new java.util.ArrayList<>();
        LocalDate[] range = getMonthRange(month, year);
        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(range[1]));
            ps.setDate(2, Date.valueOf(range[0]));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HrStatisticDTO.LeaveRow(
                            rs.getString("full_name"),
                            rs.getString("leave_type"),
                            rs.getString("start_date"),
                            rs.getString("end_date"),
                            rs.getInt("days"),
                            rs.getString("status_name")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting leave rows: " + e.getMessage());
        }
        return list;
    }

    private LocalDate[] getMonthRange(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        return new LocalDate[]{start, start.withDayOfMonth(start.lengthOfMonth())};
    }
}