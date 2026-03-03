package DAL;

import DTO.AttendanceDTO;
import java.sql.*;
import java.util.ArrayList;

public class AttendanceDAL extends BaseDAL<AttendanceDTO, Integer> {
    public static final AttendanceDAL INSTANCE = new AttendanceDAL();

    private AttendanceDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "time_sheet", "id");
    }

    public static AttendanceDAL getInstance() { return INSTANCE; }

    @Override
    protected AttendanceDTO mapResultSetToObject(ResultSet rs) throws SQLException {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(rs.getInt("id"));
        dto.setEmployeeId(rs.getInt("employee_id"));
        dto.setWorkHours(rs.getBigDecimal("work_hours"));
        dto.setOtHours(rs.getBigDecimal("ot_hours"));
        
        Timestamp in = rs.getTimestamp("check_in");
        if (in != null) dto.setCheckIn(in.toLocalDateTime());
        
        Timestamp out = rs.getTimestamp("check_out");
        if (out != null) dto.setCheckOut(out.toLocalDateTime());

        // Lấy dữ liệu từ các bảng Join (nếu có)
        try {
            dto.setEmployeeName(rs.getString("full_name"));
            dto.setDepartmentName(rs.getString("dept_name"));
        } catch (SQLException ignored) {}
        
        return dto;
    }

    public ArrayList<AttendanceDTO> getFullCompanyAttendance(String keyword, Integer deptId, Integer month, Integer year, Date specificDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, CONCAT(e.first_name, ' ', e.last_name) as full_name, d.name as dept_name " +
            "FROM time_sheet t " +
            "JOIN employee e ON t.employee_id = e.id " +
            "JOIN department d ON e.department_id = d.id WHERE 1=1 "
        );

        if (keyword != null && !keyword.isEmpty()) sql.append("AND (e.first_name LIKE ? OR e.last_name LIKE ? OR e.id = ?) ");
        if (deptId != null && deptId > 0) sql.append("AND e.department_id = ? ");
        if (specificDate != null) sql.append("AND DATE(t.check_in) = ? ");
        else if (month != null && year != null) {
            sql.append("AND MONTH(t.check_in) = ? AND YEAR(t.check_in) = ? ");
        }
        sql.append("ORDER BY t.check_in DESC");

        ArrayList<AttendanceDTO> list = new ArrayList<>();
        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int idx = 1;
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, "%" + keyword + "%");
                ps.setString(idx++, keyword);
            }
            if (deptId != null && deptId > 0) ps.setInt(idx++, deptId);
            if (specificDate != null) ps.setDate(idx++, specificDate);
            else if (month != null && year != null) {
                ps.setInt(idx++, month);
                ps.setInt(idx++, year);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapResultSetToObject(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override protected String getInsertQuery() { return "(employee_id, check_in, check_out, work_hours, ot_hours) VALUES (?, ?, ?, ?, ?)"; }
    @Override protected void setInsertParameters(PreparedStatement ps, AttendanceDTO obj) throws SQLException {
        ps.setInt(1, obj.getEmployeeId());
        ps.setObject(2, obj.getCheckIn());
        ps.setObject(3, obj.getCheckOut());
        ps.setBigDecimal(4, obj.getWorkHours());
        ps.setBigDecimal(5, obj.getOtHours());
    }
}