package DAL;

import DTO.HrStatisticDTO;
import DTO.LeaveRequestDTO;
import java.sql.*;
import java.time.LocalDate;
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
        try {
            leaveTypeName = resultSet.getString("leave_type_name");
        } catch (SQLException ignored) {
        }
        try {
            statusName = resultSet.getString("status_name");
        } catch (SQLException ignored) {
        }
        try {
            employeeName = resultSet.getString("employee_name");
        } catch (SQLException ignored) {
        }

        LeaveRequestDTO dto = new LeaveRequestDTO(
                resultSet.getInt("id"),
                resultSet.getInt("leave_type_id"),
                leaveTypeName != null ? leaveTypeName : "",
                resultSet.getString("content"),
                resultSet.getDate("start_date") != null ? resultSet.getDate("start_date").toLocalDate() : null,
                resultSet.getDate("end_date") != null ? resultSet.getDate("end_date").toLocalDate() : null,
                resultSet.getInt("status_id"),
                statusName != null ? statusName : "",
                resultSet.getInt("employee_id"));

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    // ===== HR STATISTIC HELPERS =====

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
            System.err.println("Error getting leave stat: " + e.getMessage());
        }
        return stat;
    }

    public java.util.List<HrStatisticDTO.LeaveByTypeItem> getLeaveByType(int month, int year) {
        String sql = """
                SELECT COALESCE(lt.name, 'Không xác định') AS leave_type,
                       COUNT(*) AS total
                FROM leave_request lr
                LEFT JOIN leave_type lt ON lt.id = lr.leave_type_id
                WHERE lr.start_date <= ? AND lr.end_date >= ?
                GROUP BY COALESCE(lt.name, 'Không xác định')
                ORDER BY total DESC, leave_type
                """;
        java.util.List<HrStatisticDTO.LeaveByTypeItem> list = new java.util.ArrayList<>();
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
            System.err.println("Error getting leave by type: " + e.getMessage());
        }
        return list;
    }

    public java.util.List<HrStatisticDTO.LeaveStatusItem> getLeaveByStatus(int month, int year) {
        String sql = """
                SELECT COALESCE(s.description, s.name, 'Không xác định') AS status_name,
                       COUNT(*) AS total
                FROM leave_request lr
                LEFT JOIN status s ON s.id = lr.status_id
                WHERE lr.start_date <= ? AND lr.end_date >= ?
                GROUP BY COALESCE(s.description, s.name, 'Không xác định')
                ORDER BY total DESC, status_name
                """;
        java.util.List<HrStatisticDTO.LeaveStatusItem> list = new java.util.ArrayList<>();
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
            System.err.println("Error getting leave by status: " + e.getMessage());
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
        return new LocalDate[] { start, start.withDayOfMonth(start.lengthOfMonth()) };
    }
}