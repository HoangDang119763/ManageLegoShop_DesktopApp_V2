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
            int searchId, int pageIndex, int pageSize) {
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
                "ORDER BY i.id DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, searchId);
            ps.setInt(2, searchId);
            ps.setInt(3, pageSize);
            ps.setInt(4, offset);

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

}
