package DAL;

import DTO.DetailDiscountDTO;
import DTO.DiscountDTO;
import DTO.PagedResponse;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DiscountDAL extends BaseDAL<DiscountDTO, String> {
    private static final DiscountDAL INSTANCE = new DiscountDAL();

    private DiscountDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "discount", "code");
    }

    public static DiscountDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected DiscountDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new DiscountDTO(resultSet.getString("code"), resultSet.getString("name"), resultSet.getInt("type"),
                resultSet.getDate("startDate") != null ? resultSet.getDate("startDate").toLocalDate().atStartOfDay()
                        : null,
                resultSet.getDate("endDate") != null ? resultSet.getDate("endDate").toLocalDate().atStartOfDay()
                        : null);
    }

    @Override
    protected String getInsertQuery() {
        return "(code, name, type, startDate, endDate) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, DiscountDTO obj) throws SQLException {
        statement.setString(1, obj.getCode());
        statement.setString(2, obj.getName());
        statement.setInt(3, obj.getType());
        statement.setDate(4,
                obj.getStartDate() != null ? java.sql.Date.valueOf(obj.getStartDate().toLocalDate()) : null);
        statement.setDate(5, obj.getEndDate() != null ? java.sql.Date.valueOf(obj.getEndDate().toLocalDate()) : null);
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, type = ?, startDate = ?, endDate = ? WHERE code = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, DiscountDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setInt(2, obj.getType());
        statement.setDate(3,
                obj.getStartDate() != null ? java.sql.Date.valueOf(obj.getStartDate().toLocalDate()) : null);
        statement.setDate(4, obj.getEndDate() != null ? java.sql.Date.valueOf(obj.getEndDate().toLocalDate()) : null);
        statement.setString(5, obj.getCode());
    }

    /**
     * Get all discounts with pagination
     */
    public PagedResponse<DiscountDTO> getAllDiscountsPaged(int pageIndex, int pageSize) {
        List<DiscountDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        String sql = "SELECT *, COUNT(*) OVER() as total_count FROM discount " +
                "ORDER BY code DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0)
                        totalItems = rs.getInt("total_count");
                    items.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách khuyến mãi phân trang: " + e.getMessage());
        }
        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    /**
     * Filter discounts by code with pagination
     */
    public PagedResponse<DiscountDTO> filterDiscountsPagedForManage(String searchCode, int pageIndex, int pageSize) {
        List<DiscountDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        String sql = "SELECT *, COUNT(*) OVER() as total_count FROM discount " +
                "WHERE (? = '' OR code LIKE ? OR name LIKE ?) " +
                "ORDER BY code DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchKey = "%" + (searchCode == null ? "" : searchCode.trim()) + "%";
            ps.setString(1, searchCode == null ? "" : searchCode.trim());
            ps.setString(2, searchKey);
            ps.setString(3, searchKey);
            ps.setInt(4, pageSize);
            ps.setInt(5, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0)
                        totalItems = rs.getInt("total_count");
                    items.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lọc khuyến mãi phân trang: " + e.getMessage());
        }
        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    public boolean existsByCode(String code, String excludeCode) {
        String query = "SELECT 1 FROM discount WHERE UPPER(code) = UPPER(?) AND code != COALESCE(?, '') LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, code != null ? code.trim() : "");
            statement.setString(2, excludeCode != null ? excludeCode.trim() : "");

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking discount code existence: " + e.getMessage());
        }
        return false;
    }

    public boolean insert(Connection conn, DiscountDTO obj) throws SQLException {
        String query = "INSERT INTO discount " + getInsertQuery();
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            setInsertParameters(statement, obj);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Connection conn, DiscountDTO obj) throws SQLException {
        String query = "UPDATE discount " + getUpdateQuery();
        // Lưu ý: Không dùng try-with-resources cho Connection ở đây
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            setUpdateParameters(statement, obj);
            return statement.executeUpdate() >= 0;
        }
    }

    public boolean delete(Connection conn, String code) throws SQLException {
        String query = "DELETE FROM discount WHERE code = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, code);
            return statement.executeUpdate() > 0;
        }
    }

    public ArrayList<DTO.DiscountForInvoiceDTO> filterDiscountsByKeywordForInvoice(String keyword) {
        // Dùng LinkedHashMap để gom nhóm theo code và giữ đúng thứ tự từ SQL
        LinkedHashMap<String, DTO.DiscountForInvoiceDTO> map = new LinkedHashMap<>();

        // Sử dụng đúng tên cột: total_price_invoice và discount_amount
        String sql = "SELECT d.code, d.name, d.type, dd.total_price_invoice, dd.discount_amount " +
                "FROM discount d " +
                "LEFT JOIN detail_discount dd ON d.code = dd.discount_code " +
                "WHERE (? = '' OR d.code LIKE ? OR d.name LIKE ?) " +
                "AND (d.startDate <= CURDATE() AND d.endDate >= CURDATE()) " +
                "ORDER BY d.code ASC, dd.total_price_invoice ASC";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchKey = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            ps.setString(1, keyword == null ? "" : keyword.trim());
            ps.setString(2, searchKey);
            ps.setString(3, searchKey);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String code = rs.getString("code");

                    // Kiểm tra xem mã này đã được add vào map chưa
                    DTO.DiscountForInvoiceDTO dto = map.get(code);
                    if (dto == null) {
                        dto = new DTO.DiscountForInvoiceDTO();
                        dto.setCode(code);
                        dto.setName(rs.getString("name"));
                        dto.setType(rs.getInt("type"));
                        dto.setDetailDiscountList(new ArrayList<>());
                        map.put(code, dto);
                    }

                    // Kiểm tra nếu có chi tiết khuyến mãi thì add vào list
                    BigDecimal conditionPrice = rs.getBigDecimal("total_price_invoice");
                    if (conditionPrice != null) {
                        DetailDiscountDTO detail = new DetailDiscountDTO();
                        detail.setDiscountCode(code);
                        detail.setTotalPriceInvoice(conditionPrice); // Đúng tên cột bạn đặt
                        detail.setDiscountAmount(rs.getBigDecimal("discount_amount")); // Đúng tên cột bạn đặt
                        dto.getDetailDiscountList().add(detail);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi filterDiscountsByKeywordForInvoice: " + e.getMessage());
            return null;
        }
        return new ArrayList<>(map.values());
    }
}
