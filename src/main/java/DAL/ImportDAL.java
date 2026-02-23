package DAL;

import DTO.ImportDTO;
import DTO.ImportDisplayDTO;
import DTO.PagedResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ImportDAL extends BaseDAL<ImportDTO, Integer> {
    public static final ImportDAL INSTANCE = new ImportDAL();

    private ImportDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "import", "id");
    }

    public static ImportDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected ImportDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new ImportDTO(
                resultSet.getInt("id"),
                resultSet.getTimestamp("create_date") != null
                        ? resultSet.getTimestamp("create_date").toLocalDateTime()
                        : null,
                resultSet.getInt("employee_id"),
                resultSet.getInt("supplier_id"),
                resultSet.getBigDecimal("total_price"),
                resultSet.getInt("status_id"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(ImportDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(create_date, employee_id, supplier_id, total_price, status_id) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, ImportDTO obj) throws SQLException {
        statement.setTimestamp(1, Timestamp.valueOf(obj.getCreateDate()));
        statement.setInt(2, obj.getEmployeeId());
        statement.setInt(3, obj.getSupplierId());
        statement.setBigDecimal(4, obj.getTotalPrice());
        statement.setInt(5, obj.getStatusId());
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Cannot update permission records.");
    }

    private ImportDisplayDTO mapResultSetToImportDisplay(ResultSet rs) throws SQLException {
        return new ImportDisplayDTO(
                rs.getInt("id"),
                rs.getTimestamp("create_date") != null
                        ? rs.getTimestamp("create_date").toLocalDateTime()
                        : null,
                rs.getInt("employee_id"),
                rs.getInt("supplier_id"),
                rs.getBigDecimal("total_price"),
                rs.getInt("status_id"),
                rs.getString("status_description"));
    }

    /**
     * [OPTIMIZED] Get imports with filter and pagination for manage display
     */
    public PagedResponse<ImportDisplayDTO> filterImportsPagedForManage(
            int searchId, int statusId, int pageIndex, int pageSize) {
        List<ImportDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // JOIN với status table để lấy statusDescription
        String sql = "SELECT " +
                "i.id, i.create_date, i.employee_id, i.supplier_id, i.total_price, i.status_id, " +
                "s.description as status_description, " +
                "COUNT(*) OVER() as total_count " +
                "FROM import i " +
                "LEFT JOIN status s ON i.status_id = s.id " +
                "WHERE (? = -1 OR i.id = ?) " +
                "  AND (? = -1 OR i.status_id = ?) " +
                "ORDER BY i.id DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, searchId);
            ps.setInt(2, searchId);
            ps.setInt(3, statusId);
            ps.setInt(4, statusId);
            ps.setInt(5, pageSize);
            ps.setInt(6, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0)
                        totalItems = rs.getInt("total_count");
                    items.add(mapResultSetToImportDisplay(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lọc phiếu nhập phân trang: " + e.getMessage());
        }
        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    public boolean existsBySupplierId(int supplierId) {
        // Sử dụng SELECT 1 và EXISTS để tối ưu tốc độ tối đa
        String sql = "SELECT 1 FROM import WHERE supplier_id = ? LIMIT 1";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, supplierId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu có ít nhất 1 dòng
            }
        } catch (SQLException e) {
            System.err.println("Error checking import existence: " + e.getMessage());
            return false;
        }
    }

    public boolean existsByEmployeeId(int employeeId) {
        // Sử dụng SELECT 1 và EXISTS để tối ưu tốc độ tối đa
        String sql = "SELECT 1 FROM import WHERE employee_id = ? LIMIT 1";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu có ít nhất 1 dòng
            }
        } catch (SQLException e) {
            System.err.println("Error checking import existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Insert Import using provided connection (for transaction)
     * Thêm phiếu nhập sử dụng connection được cung cấp (cho transaction)
     */
    public boolean insert(Connection connection, ImportDTO obj) {
        // SQL: id tự tăng nên không cần chèn, create_date mặc định CURRENT_TIMESTAMP
        String sql = "INSERT INTO import (employee_id, supplier_id, total_price, status_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Set các tham số (Mapping trực tiếp từ DTO sang DB)
            statement.setInt(1, obj.getEmployeeId());
            statement.setInt(2, obj.getSupplierId());
            statement.setBigDecimal(3, obj.getTotalPrice());
            statement.setInt(4, obj.getStatusId());

            // 2. Thực thi lệnh
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            // 3. Lấy ID tự tăng vừa được sinh ra (Cực kỳ quan trọng để lưu Detail)
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    obj.setId(generatedKeys.getInt(1)); // Gán ngược ID vào object DTO
                } else {
                    throw new SQLException("Inserting import failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting into import table: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(Connection conn, int importId, int newStatusId) throws SQLException {
        String sql = "UPDATE import SET status_id = ? WHERE id = ?";

        // Không dùng try-with-resources cho Connection ở đây vì conn được truyền từ BUS
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStatusId);
            ps.setInt(2, importId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Log lỗi và ném ra ngoài để BUS thực hiện Rollback
            System.err.println("Error updating import status: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Delete import using provided connection (for transaction)
     * Xóa phiếu nhập sử dụng connection được cung cấp (cho transaction)
     */
    public boolean delete(Connection conn, int importId) {
        String sql = "DELETE FROM import WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, importId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting import: " + e.getMessage());
            return false;
        }
    }

}
