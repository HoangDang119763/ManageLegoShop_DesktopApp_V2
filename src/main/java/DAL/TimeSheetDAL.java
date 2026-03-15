package DAL;

import DTO.HrStatisticDTO;
import DTO.TimeSheetDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

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
        TimeSheetDTO dto = new TimeSheetDTO();
        dto.setId(resultSet.getInt("id"));
        dto.setEmployeeId(resultSet.getInt("employee_id"));
        
        // Đảm bảo gán đầy đủ các trường từ JOIN
        try {
            // Kiểm tra cột tồn tại trước khi lấy để tránh lỗi nếu dùng câu SQL đơn giản
            ResultSetMetaData rsmd = resultSet.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                if ("employee_name".equalsIgnoreCase(rsmd.getColumnName(i))) {
                    dto.setEmployeeName(resultSet.getString("employee_name"));
                }
                if ("department_name".equalsIgnoreCase(rsmd.getColumnName(i))) {
                    dto.setDepartmentName(resultSet.getString("department_name"));
                }
            }
        } catch (SQLException ignored) {}

        dto.setCheckIn(resultSet.getTimestamp("check_in") != null 
            ? resultSet.getTimestamp("check_in").toLocalDateTime() : null);
        dto.setCheckOut(resultSet.getTimestamp("check_out") != null 
            ? resultSet.getTimestamp("check_out").toLocalDateTime() : null);
        
        dto.setWorkHours(resultSet.getBigDecimal("work_hours"));
        dto.setOtHours(resultSet.getBigDecimal("ot_hours") != null 
            ? resultSet.getBigDecimal("ot_hours") : BigDecimal.ZERO);
        
        return dto;
    }

    /**
     * Lấy toàn bộ dữ liệu kèm JOIN để hiển thị Dashboard
     */
    public ArrayList<TimeSheetDTO> getFullTimeSheet(String keyword, Integer deptId, Integer month, Integer year) {
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, CONCAT(e.first_name, ' ', e.last_name) AS employee_name, d.name AS department_name " +
            "FROM time_sheet t " +
            "JOIN employee e ON t.employee_id = e.id " +
            "JOIN department d ON e.department_id = d.id WHERE 1=1 "
        );

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (e.first_name LIKE ? OR e.last_name LIKE ? OR e.id LIKE ?) ");
        }
        if (deptId != null && deptId > 0) {
            sql.append("AND e.department_id = ? ");
        }
        if (month != null && year != null) {
            sql.append("AND MONTH(t.check_in) = ? AND YEAR(t.check_in) = ? ");
        }
        sql.append("ORDER BY t.check_in DESC");

        ArrayList<TimeSheetDTO> list = new ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIdx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                String k = "%" + keyword + "%";
                ps.setString(paramIdx++, k);
                ps.setString(paramIdx++, k);
                ps.setString(paramIdx++, k);
            }
            if (deptId != null && deptId > 0) ps.setInt(paramIdx++, deptId);
            if (month != null && year != null) {
                ps.setInt(paramIdx++, month);
                ps.setInt(paramIdx++, year);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToObject(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Các phương thức insertBatch và existsByEmployeeId giữ nguyên logic tốt của bạn
    public int insertBatch(ArrayList<TimeSheetDTO> list) {
        String sql = "INSERT INTO time_sheet (employee_id, check_in, check_out, work_hours, ot_hours) VALUES (?, ?, ?, ?, ?)";
        int count = 0;
        try (Connection conn = connectionFactory.newConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (TimeSheetDTO dto : list) {
                    ps.setInt(1, dto.getEmployeeId());
                    ps.setObject(2, dto.getCheckIn());
                    ps.setObject(3, dto.getCheckOut());
                    ps.setBigDecimal(4, dto.getWorkHours());
                    ps.setBigDecimal(5, dto.getOtHours());
                    ps.addBatch();
                    if (++count % 500 == 0) ps.executeBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return count;
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
        statement.setObject(5, obj.getOtHours());
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
        statement.setObject(5, obj.getOtHours());
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
        String sql = "SELECT COUNT(*) FROM time_sheet WHERE employee_id = ?";
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}