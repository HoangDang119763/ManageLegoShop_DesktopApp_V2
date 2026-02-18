package DAL;

import DTO.InvoiceDTO;
import DTO.InvoiceDisplayDTO;
import DTO.PagedResponse;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAL extends BaseDAL<InvoiceDTO, Integer> {
    public static final InvoiceDAL INSTANCE = new InvoiceDAL();

    private InvoiceDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "invoice", "id");
    }

    public static InvoiceDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected InvoiceDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new InvoiceDTO(
                resultSet.getInt("id"),
                resultSet.getTimestamp("create_date") != null
                        ? resultSet.getTimestamp("create_date").toLocalDateTime()
                        : null,
                resultSet.getInt("employee_id"),
                resultSet.getInt("customer_id"),
                resultSet.getString("discount_code"),
                resultSet.getBigDecimal("discount_amount") != null ? resultSet.getBigDecimal("discount_amount")
                        : BigDecimal.ZERO,
                resultSet.getBigDecimal("total_price"),
                resultSet.getInt("status_id"));
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true; // ID là AUTO_INCREMENT
    }

    @Override
    protected void setGeneratedKey(InvoiceDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(create_date, employee_id, customer_id, discount_code, discount_amount, total_price, status_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, InvoiceDTO obj) throws SQLException {
        statement.setTimestamp(1, Timestamp.valueOf(obj.getCreateDate()));
        statement.setInt(2, obj.getEmployeeId());
        statement.setInt(3, obj.getCustomerId());

        // Xử lý discount_code có thể null
        if (obj.getDiscountCode() != null) {
            statement.setString(4, obj.getDiscountCode());
        } else {
            statement.setNull(4, Types.VARCHAR);
        }

        // Luôn đặt giá trị cho discountAmount
        statement.setBigDecimal(5, obj.getDiscountAmount() != null ? obj.getDiscountAmount() : BigDecimal.ZERO);

        statement.setBigDecimal(6, obj.getTotalPrice());
        statement.setInt(7, obj.getStatusId());
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Cannot update permission records.");
    }

    /**
     * [OPTIMIZED] Get all invoices with status description (JOIN)
     * Tránh gọi BUS lẻ lẻ từng dòng
     */
    public PagedResponse<InvoiceDisplayDTO> getAllInvoicesPagedForManage(int pageIndex, int pageSize) {
        List<InvoiceDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // JOIN với status table để lấy statusDescription
        String sql = "SELECT " +
                "i.id, i.create_date, i.employee_id, i.customer_id, i.discount_code, i.discount_amount, i.total_price, i.status_id, "
                +
                "s.description as status_description, " +
                "COUNT(*) OVER() as total_count " +
                "FROM invoice i " +
                "LEFT JOIN status s ON i.status_id = s.id " +
                "ORDER BY i.id DESC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pageSize);
            ps.setInt(2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalItems == 0)
                        totalItems = rs.getInt("total_count");
                    items.add(mapResultSetToInvoiceDisplay(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách hóa đơn phân trang: " + e.getMessage());
        }
        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    /**
     * [OPTIMIZED] Filter invoices with pagination for manage display
     */
    public PagedResponse<InvoiceDisplayDTO> filterInvoicesPagedForManage(
            int searchId, int pageIndex, int pageSize) {
        List<InvoiceDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // JOIN với status table để lấy statusDescription
        String sql = "SELECT " +
                "i.id, i.create_date, i.employee_id, i.customer_id, i.discount_code, i.discount_amount, i.total_price, i.status_id, "
                +
                "s.description as status_description, " +
                "COUNT(*) OVER() as total_count " +
                "FROM invoice i " +
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
                    items.add(mapResultSetToInvoiceDisplay(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lọc hóa đơn phân trang: " + e.getMessage());
        }
        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    /**
     * Map ResultSet to InvoiceDisplayDTO (với status description)
     */
    private InvoiceDisplayDTO mapResultSetToInvoiceDisplay(ResultSet rs) throws SQLException {
        return new InvoiceDisplayDTO(
                rs.getInt("id"),
                rs.getTimestamp("create_date") != null
                        ? rs.getTimestamp("create_date").toLocalDateTime()
                        : null,
                rs.getInt("employee_id"),
                rs.getInt("customer_id"),
                rs.getString("discount_code"),
                rs.getBigDecimal("discount_amount") != null ? rs.getBigDecimal("discount_amount") : BigDecimal.ZERO,
                rs.getBigDecimal("total_price"),
                rs.getInt("status_id"),
                rs.getString("status_description"));
    }

}
