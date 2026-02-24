package DAL;

import DTO.TaxDTO;
import java.sql.*;

public class TaxDAL extends BaseDAL<TaxDTO, Integer> {
    public static final TaxDAL INSTANCE = new TaxDAL();

    private TaxDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "tax", "id");
    }

    public static TaxDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected TaxDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new TaxDTO(
                resultSet.getInt("id"),
                resultSet.getInt("employee_id"),
                resultSet.getInt("num_dependents"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(TaxDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(employee_id, num_dependents) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, TaxDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setInt(2, obj.getNumDependents());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET employee_id = ?, num_dependents = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, TaxDTO obj) throws SQLException {
        statement.setInt(1, obj.getEmployeeId());
        statement.setInt(2, obj.getNumDependents());
        statement.setInt(3, obj.getId());
    }

    public boolean insertWithConn(Connection conn, TaxDTO obj) throws SQLException {
        String sql = "INSERT INTO tax (employee_id, num_dependents) VALUES (?, ?)";

        // Sử dụng Statement.RETURN_GENERATED_KEYS để lấy ID sau khi chèn
        try (PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // 1. Gán các tham số (Dùng lại hàm setInsertParameters đã có ở trên)
            statement.setInt(1, obj.getEmployeeId());
            statement.setInt(2, obj.getNumDependents());
            // 2. Thực thi
            int affectedRows = statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                setGeneratedKey(obj, generatedKeys);
            }
            return affectedRows > 0;
        }
    }

    public boolean updateNumDependent(Connection conn, int employeeId, int numDependent) throws SQLException {
        String sql = "UPDATE tax SET num_dependents = ? WHERE employee_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numDependent);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() >= 0;
        }
    }
}
