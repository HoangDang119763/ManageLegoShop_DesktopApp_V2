package DAL;

import DTO.LeaveRequestDTO;
import java.sql.*;
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

    @Override
    protected LeaveRequestDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        // Check if leave_type_name column exists (for JOIN queries)
        String leaveTypeName = "";
        try {
            leaveTypeName = resultSet.getString("leave_type_name");
            if (leaveTypeName == null) leaveTypeName = "";
        } catch (SQLException e) {
            // Column doesn't exist - normal for simple SELECT *
            leaveTypeName = "";
        }
        
        return new LeaveRequestDTO(
                resultSet.getInt("id"),
                resultSet.getInt("leave_type_id"),
                leaveTypeName,
                resultSet.getString("content"),
                resultSet.getDate("start_date") != null ? resultSet.getDate("start_date").toLocalDate() : null,
                resultSet.getDate("end_date") != null ? resultSet.getDate("end_date").toLocalDate() : null,
                resultSet.getInt("status_id"),
                resultSet.getInt("employee_id"));
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
        String query = "SELECT lr.id, lr.leave_type_id, COALESCE(lt.name, '') as leave_type_name, " +
                "lr.content, lr.start_date, lr.end_date, lr.status_id, lr.employee_id " +
                "FROM leave_request lr " +
                "LEFT JOIN leave_type lt ON lr.leave_type_id = lt.id " +
                "ORDER BY lr.id DESC";
        
        ArrayList<LeaveRequestDTO> list = new ArrayList<>();
        try (Connection connection = connectionFactory.newConnection();
             Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
        String query = "SELECT lr.id, lr.leave_type_id, COALESCE(lt.name, '') as leave_type_name, " +
                "lr.content, lr.start_date, lr.end_date, lr.status_id, lr.employee_id " +
                "FROM leave_request lr " +
                "LEFT JOIN leave_type lt ON lr.leave_type_id = lt.id " +
                "WHERE lr.employee_id = ? " +
                "ORDER BY lr.start_date DESC";
        
        ArrayList<LeaveRequestDTO> list = new ArrayList<>();
        try (Connection connection = connectionFactory.newConnection();
             PreparedStatement statement = connection.prepareStatement(query,
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSetToObject(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving leave requests for employee " + employeeId + ": " + e.getMessage());
        }
        return list;
    }

}
