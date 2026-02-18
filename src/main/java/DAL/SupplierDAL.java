package DAL;

import DTO.SupplierDTO;
import DTO.SupplierDisplayDTO;
import DTO.PagedResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAL extends BaseDAL<SupplierDTO, Integer> {
    public static final SupplierDAL INSTANCE = new SupplierDAL();

    private SupplierDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "supplier", "id");
    }

    public static SupplierDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected SupplierDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new SupplierDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getString("phone"),
                resultSet.getString("address"),
                resultSet.getString("email"),
                resultSet.getInt("status_id"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(SupplierDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, phone, address, email, status_id) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, SupplierDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getPhone());
        statement.setString(3, obj.getAddress());
        statement.setString(4, obj.getEmail());
        statement.setInt(5, obj.getStatusId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, phone = ?, address = ?, email = ?, status_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, SupplierDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setString(2, obj.getPhone());
        statement.setString(3, obj.getAddress());
        statement.setString(4, obj.getEmail());
        statement.setInt(5, obj.getStatusId());
        statement.setInt(6, obj.getId());
    }

    /**
     * Filter suppliers with pagination, search, and status filtering for manage
     * display
     * 
     * @param keyword   Search keyword (by ID or name)
     * @param statusId  Status filter (-1 to skip filtering)
     * @param pageIndex Page index (0-based)
     * @param pageSize  Page size
     * @return PagedResponse with SupplierDisplayDTO items
     */
    public PagedResponse<SupplierDisplayDTO> filterSuppliersPagedForManageDisplay(
            String keyword, int statusId, int pageIndex, int pageSize) {

        List<SupplierDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // Cập nhật SQL: Thêm s.phone và s.email vào khối OR
        String sql = "SELECT s.id, s.name, s.phone, s.address, s.email, s.status_id, " +
                "st.description as status_description, " +
                "COUNT(*) OVER() as total_count " +
                "FROM supplier s " +
                "LEFT JOIN status st ON s.status_id = st.id " +
                "WHERE (? = '' OR (" +
                "CAST(s.id AS CHAR) LIKE ? " + // Tìm theo ID
                "OR s.name LIKE ? " + // Tìm theo Tên
                "OR s.phone LIKE ? " + // Tìm theo SĐT
                "OR s.email LIKE ?" + // Tìm theo Email
                ")) " +
                "AND (? = -1 OR s.status_id = ?) " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchKey = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            String rawKeyword = (keyword == null ? "" : keyword.trim());

            // 1. Kiểm tra keyword trống
            ps.setString(1, rawKeyword);

            // 2. Các tham số cho khối OR (Phải đủ 4 cái)
            ps.setString(2, searchKey); // Like ID
            ps.setString(3, searchKey); // Like Name
            ps.setString(4, searchKey); // Like Phone
            ps.setString(5, searchKey); // Like Email

            // 3. Gán tham số cho Filter Status
            ps.setInt(6, statusId);
            ps.setInt(7, statusId);

            // 4. Phân trang
            ps.setInt(8, offset);
            ps.setInt(9, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0)
                        totalItems = rs.getInt("total_count");
                    items.add(mapResultSetToDisplayObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering suppliers: " + e.getMessage());
        }
        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    /**
     * Map ResultSet to SupplierDisplayDTO
     * 
     * @param rs ResultSet from filterSuppliersPagedForManageDisplay
     * @return SupplierDisplayDTO
     */
    private SupplierDisplayDTO mapResultSetToDisplayObject(ResultSet rs) throws SQLException {
        return new SupplierDisplayDTO(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("email"),
                rs.getInt("status_id"),
                rs.getString("status_description"));
    }

}
