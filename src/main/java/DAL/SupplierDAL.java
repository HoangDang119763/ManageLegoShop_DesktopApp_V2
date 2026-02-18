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

    public boolean updateStatus(int id, int newStatusId) {
        String query = "UPDATE supplier SET status_id = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, newStatusId);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating supplier status: " + e.getMessage());
            return false;
        }
    }

    public PagedResponse<SupplierDisplayDTO> filterSuppliersPagedForManageDisplay(
            String keyword, int statusId, int pageIndex, int pageSize) {

        List<SupplierDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // SQL: Sử dụng LOWER() cho cột name và email để tìm kiếm không phân biệt hoa
        // thường
        String sql = "SELECT s.id, s.name, s.phone, s.address, s.email, s.status_id, " +
                "st.description as status_description, " +
                "COUNT(*) OVER() as total_count " +
                "FROM supplier s " +
                "LEFT JOIN status st ON s.status_id = st.id " +
                "WHERE (? = '' OR (" +
                "    CAST(s.id AS CHAR) LIKE ? " + // Tìm theo ID
                "    OR LOWER(s.name) LIKE ? " + // Tìm theo Tên (đã hạ tone)
                "    OR s.phone LIKE ? " + // Tìm theo SĐT
                "    OR LOWER(s.email) LIKE ?" + // Tìm theo Email (đã hạ tone)
                ")) " +
                "AND (? = -1 OR s.status_id = ?) " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Chuẩn hóa từ khóa: hạ tone toàn bộ để khớp với LOWER() trong SQL
            String rawKeyword = (keyword == null ? "" : keyword.trim());
            String searchKey = "%" + rawKeyword.toLowerCase() + "%";

            int idx = 1;

            // 1. Kiểm tra keyword trống
            ps.setString(idx++, rawKeyword);

            // 2. Các tham số cho khối OR
            ps.setString(idx++, searchKey); // Like ID
            ps.setString(idx++, searchKey); // Like Name
            ps.setString(idx++, searchKey); // Like Phone
            ps.setString(idx++, searchKey); // Like Email

            // 3. Gán tham số cho Filter Status
            ps.setInt(idx++, statusId);
            ps.setInt(idx++, statusId);

            // 4. Phân trang
            ps.setInt(idx++, offset);
            ps.setInt(idx++, pageSize);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0) {
                        totalItems = rs.getInt("total_count");
                    }
                    items.add(mapResultSetToDisplayObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering suppliers: " + e.getMessage());
        }

        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

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

    public boolean existsBySupplierData(String name, String phone, String address, String email, int currentId) {
        // Sử dụng SELECT 1 để tối ưu: Chỉ cần biết có tồn tại hay không, không cần lấy
        // dữ liệu
        StringBuilder sql = new StringBuilder("SELECT 1 FROM supplier WHERE ");

        // Các trường NOT NULL dùng so sánh trực tiếp
        sql.append("name = ? AND phone = ? AND address = ? ");

        // Trường email có thể NULL nên cần xử lý logic so sánh NULL an toàn
        sql.append("AND (email = ? OR (? IS NULL AND email IS NULL)) ");

        // Loại trừ bản ghi hiện tại khi Update
        sql.append("AND id != ? LIMIT 1");

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int idx = 1;
            statement.setString(idx++, name);
            statement.setString(idx++, phone);
            statement.setString(idx++, address);

            // Xử lý tham số email (xuất hiện 2 lần trong logic check NULL)
            statement.setString(idx++, email);
            statement.setString(idx++, email);

            statement.setInt(idx++, currentId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next(); // Trả về true nếu tìm thấy bản ghi trùng
            }
        } catch (SQLException e) {
            System.err.println("Error checking duplicate supplier: " + e.getMessage());
        }
        return false;
    }

}
