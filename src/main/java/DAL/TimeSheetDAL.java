package DAL;

import DTO.HrStatisticDTO;
import DTO.TimeSheetDTO;
import java.sql.*;

public class TimeSheetDAL extends BaseDAL<TimeSheetDTO, Integer> {
    public static final TimeSheetDAL INSTANCE = new TimeSheetDAL();

    private TimeSheetDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "time_sheet", "id");
    }

    public static TimeSheetDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected TimeSheetDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new TimeSheetDTO(
                resultSet.getInt("id"),
                resultSet.getInt("employee_id"),
                resultSet.getTimestamp("check_in") != null
                        ? resultSet.getTimestamp("check_in").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("check_out") != null
                        ? resultSet.getTimestamp("check_out").toLocalDateTime()
                        : null,
                resultSet.getBigDecimal("work_hours"),
                resultSet.getBigDecimal("ot_hours") != null ? resultSet.getBigDecimal("ot_hours") : new java.math.BigDecimal("0.00"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(TimeSheetDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(employee_id, check_in, check_out, work_hours, ot_hours) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, TimeSheetDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getCheckIn());
        statement.setObject(3, obj.getCheckOut());
        statement.setObject(4, obj.getWorkHours());
        statement.setObject(5, obj.getOtHours() != null ? obj.getOtHours() : new java.math.BigDecimal("0.00"));
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, check_in = ?, check_out = ?, work_hours = ?, ot_hours = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, TimeSheetDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setObject(2, obj.getCheckIn());
        statement.setObject(3, obj.getCheckOut());
        statement.setObject(4, obj.getWorkHours());
        statement.setObject(5, obj.getOtHours() != null ? obj.getOtHours() : new java.math.BigDecimal("0.00"));
        statement.setInt(6, obj.getId());
    }

    // ===== HR STATISTIC =====

    public HrStatisticDTO.AttendanceStat getAttendanceStat(int month, int year) {
        String sql = """
                SELECT COUNT(*) AS total_sessions,
                       COUNT(DISTINCT employee_id) AS employee_count,
                       COALESCE(SUM(work_hours), 0) AS total_work,
                       COALESCE(SUM(ot_hours), 0) AS total_ot
                FROM time_sheet
                WHERE MONTH(check_in) = ? AND YEAR(check_in) = ?
                """;
        HrStatisticDTO.AttendanceStat stat = new HrStatisticDTO.AttendanceStat();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    stat.setTotalSessions(rs.getInt("total_sessions"));
                    stat.setEmployeeCount(rs.getInt("employee_count"));
                    stat.setTotalWorkHours(rs.getBigDecimal("total_work"));
                    stat.setTotalOtHours(rs.getBigDecimal("total_ot"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting attendance stat: " + e.getMessage());
        }
        return stat;
    }

    public java.util.List<HrStatisticDTO.AttendanceRow> getAttendanceRows(int month, int year) {
        String sql = """
                SELECT CONCAT(e.first_name, ' ', e.last_name) AS full_name,
                       COALESCE(d.name, '—') AS dept_name,
                       COUNT(*) AS session_count,
                       COALESCE(SUM(ts.work_hours), 0) AS total_work,
                       COALESCE(SUM(ts.ot_hours), 0) AS total_ot
                FROM time_sheet ts
                LEFT JOIN employee e ON e.id = ts.employee_id
                LEFT JOIN department d ON d.id = e.department_id
                WHERE MONTH(ts.check_in) = ? AND YEAR(ts.check_in) = ?
                GROUP BY ts.employee_id, full_name, dept_name
                ORDER BY total_work DESC
                """;
        java.util.List<HrStatisticDTO.AttendanceRow> list = new java.util.ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HrStatisticDTO.AttendanceRow(
                            rs.getString("full_name"),
                            rs.getString("dept_name"),
                            rs.getInt("session_count"),
                            rs.getBigDecimal("total_work"),
                            rs.getBigDecimal("total_ot")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting attendance rows: " + e.getMessage());
        }
        return list;
    }

    public java.util.List<HrStatisticDTO.DailyWorkPoint> getDailyWorkPoints(int month, int year) {
        String sql = """
                SELECT DATE_FORMAT(check_in, '%d') AS day_label,
                       COALESCE(SUM(work_hours), 0) AS total_work,
                       COUNT(*) AS session_count
                FROM time_sheet
                WHERE MONTH(check_in) = ? AND YEAR(check_in) = ?
                GROUP BY DATE(check_in), day_label
                ORDER BY DATE(check_in)
                """;
        java.util.List<HrStatisticDTO.DailyWorkPoint> list = new java.util.ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new HrStatisticDTO.DailyWorkPoint(
                            rs.getString("day_label"),
                            rs.getBigDecimal("total_work"),
                            rs.getInt("session_count")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting daily work points: " + e.getMessage());
        }
        return list;
    }

    public boolean existsByEmployeeId(int employeeId) {
        // Sử dụng SELECT 1 và EXISTS để tối ưu tốc độ tối đa
        String sql = "SELECT 1 FROM time_sheet WHERE employee_id = ? LIMIT 1";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu có ít nhất 1 dòng
            }
        } catch (SQLException e) {
            System.err.println("Error checking time sheet existence: " + e.getMessage());
            return false;
        }
    }
}
