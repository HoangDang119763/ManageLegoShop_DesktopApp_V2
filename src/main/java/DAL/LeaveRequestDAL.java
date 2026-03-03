package DAL;

import DTO.LeaveRequestDTO;
import ENUM.Status.Employee;

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

        try {
            leaveTypeName = resultSet.getString("leave_type_name");
            if (leaveTypeName == null) leaveTypeName = "";
        } catch (SQLException ignored) {}

        try {
            statusName = resultSet.getString("status_name");
            if (statusName == null) statusName = "";
        } catch (SQLException ignored) {}

        return new LeaveRequestDTO(
                resultSet.getInt("id"),
                resultSet.getInt("leave_type_id"),
                leaveTypeName,
                resultSet.getString("content"),
                resultSet.getDate("start_date") != null
                        ? resultSet.getDate("start_date").toLocalDate() : null,
                resultSet.getDate("end_date") != null
                        ? resultSet.getDate("end_date").toLocalDate() : null,
                resultSet.getInt("status_id"),
                statusName, // thêm dòng này
                resultSet.getInt("employee_id")
        );
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(LeaveRequestDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(leave_type_id, content, start_date, end_date, status_id, employee_id) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, LeaveRequestDTO obj) throws SQLException {
        statement.setInt(1, obj.getLeaveTypeId());
        statement.setString(2, obj.getContent());
        statement.setDate(3, obj.getStartDate() != null ? java.sql.Date.valueOf(obj.getStartDate()) : null);
        statement.setDate(4, obj.getEndDate() != null ? java.sql.Date.valueOf(obj.getEndDate()) : null);
        statement.setInt(5, obj.getStatusId());
        statement.setInt(6, obj.getEmployeeId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET leave_type_id = ?, content = ?, start_date = ?, end_date = ?, status_id = ?, employee_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, LeaveRequestDTO obj) throws SQLException {
        statement.setInt(1, obj.getLeaveTypeId());
        statement.setString(2, obj.getContent());
        statement.setDate(3, obj.getStartDate() != null ? java.sql.Date.valueOf(obj.getStartDate()) : null);
        statement.setDate(4, obj.getEndDate() != null ? java.sql.Date.valueOf(obj.getEndDate()) : null);
        statement.setInt(5, obj.getStatusId());
        statement.setInt(6, obj.getEmployeeId());
        statement.setInt(7, obj.getId());
    }

    /**
     * Get all leave requests with leave type names from join
     */
    @Override
    public ArrayList<LeaveRequestDTO> getAll() {

        String query =
            "SELECT lr.id, lr.leave_type_id, COALESCE(lt.name,'') AS leave_type_name, " +
            "lr.content, lr.start_date, lr.end_date, " +
            "lr.status_id, COALESCE(s.name,'') AS status_name, " +
            "lr.employee_id " +
            "FROM leave_request lr " +
            "LEFT JOIN leave_type lt ON lr.leave_type_id = lt.id " +
            "LEFT JOIN status s ON lr.status_id = s.id " +
            "ORDER BY lr.id DESC";

        ArrayList<LeaveRequestDTO> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                list.add(mapResultSetToObject(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving all leave requests: " + e.getMessage());
        }

        return list;
    }

    /**
     * Get leave requests by employee ID with leave type names
     */
    public ArrayList<LeaveRequestDTO> getByEmployeeId(Integer employeeId) {

        String query =
            "SELECT lr.id, lr.leave_type_id, COALESCE(lt.name,'') AS leave_type_name, " +
            "lr.content, lr.start_date, lr.end_date, " +
            "lr.status_id, COALESCE(s.name,'') AS status_name, " +
            "lr.employee_id " +
            "FROM leave_request lr " +
            "LEFT JOIN leave_type lt ON lr.leave_type_id = lt.id " +
            "LEFT JOIN status s ON lr.status_id = s.id " +
            "WHERE lr.employee_id = ? " +
            "ORDER BY lr.start_date DESC";

        ArrayList<LeaveRequestDTO> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, employeeId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    list.add(mapResultSetToObject(resultSet));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving leave requests: " + e.getMessage());
        }

        return list;
    }

    public boolean insert(LeaveRequestDTO dto) {
        String sql = """
            INSERT INTO leave_request
            (leave_type_id, content, start_date, end_date, status_id, employee_id)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = connectionFactory.newConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, dto.getLeaveTypeId());
            ps.setString(2, dto.getContent());

            if (dto.getStartDate() != null)
                ps.setDate(3, Date.valueOf(dto.getStartDate()));
            else
                ps.setNull(3, Types.DATE);

            if (dto.getEndDate() != null)
                ps.setDate(4, Date.valueOf(dto.getEndDate()));
            else
                ps.setNull(4, Types.DATE);

            ps.setInt(5, dto.getStatusId());
            ps.setInt(6, dto.getEmployeeId());

            int affected = ps.executeUpdate();

            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    dto.setId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

   public boolean update(LeaveRequestDTO dto) {
        String sql = """
            UPDATE leave_request
            SET
                leave_type_id = ?,
                content = ?,
                start_date = ?,
                end_date = ?,
                status_id = ?,
                employee_id = ?
            WHERE id = ?
        """;

        try (Connection conn = connectionFactory.newConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dto.getLeaveTypeId());
            ps.setString(2, dto.getContent());

            if (dto.getStartDate() != null)
                ps.setDate(3, Date.valueOf(dto.getStartDate()));
            else
                ps.setNull(3, Types.DATE);

            if (dto.getEndDate() != null)
                ps.setDate(4, Date.valueOf(dto.getEndDate()));
            else
                ps.setNull(4, Types.DATE);

            ps.setInt(5, dto.getStatusId());
            ps.setInt(6, dto.getEmployeeId());

            ps.setInt(7, dto.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    } 

    public boolean delete(int id) {
        String sql = "DELETE FROM leave_request WHERE id = ?";

        try (Connection conn = connectionFactory.newConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }   

        return false;
    }

    public boolean updateStatus(int id, ENUM.Status.LeaveRequest status) {

        String sql = """
            UPDATE leave_request
            SET status_id = ?
            WHERE id = ?
        """;

        try (Connection conn = connectionFactory.newConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, status.ordinal());
            ps.setInt(2, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean updateStatus(int leaveRequestId, int statusId) {
        String sql = """
            UPDATE LeaveRequest
            SET idStatus = ?
            WHERE id = ?
        """;

        try (Connection conn = connectionFactory.newConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, statusId);
            ps.setInt(2, leaveRequestId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Approve = 21
    public boolean approve(int leaveRequestId) {
        return updateStatus(leaveRequestId, 21);
    }

    // Reject = 22
    public boolean reject(int leaveRequestId) {
        return updateStatus(leaveRequestId, 22);
    }

    // Cancel = 23
    public boolean cancel(int leaveRequestId) {
        return updateStatus(leaveRequestId, 23);
    }
}
