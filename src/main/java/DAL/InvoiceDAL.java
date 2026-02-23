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
            int searchId, int statusId, int pageIndex, int pageSize) {
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

    public boolean existsByCustomerId(int customerId) {
        // Sử dụng SELECT 1 và EXISTS để tối ưu tốc độ tối đa
        String sql = "SELECT 1 FROM invoice WHERE customer_id = ? LIMIT 1";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu có ít nhất 1 dòng
            }
        } catch (SQLException e) {
            System.err.println("Error checking invoice existence: " + e.getMessage());
            return false;
        }
    }

    public boolean existsByEmployeeId(int employeeId) {
        // Sử dụng SELECT 1 và EXISTS để tối ưu tốc độ tối đa
        String sql = "SELECT 1 FROM invoice WHERE employee_id = ? LIMIT 1";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, employeeId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu có ít nhất 1 dòng
            }
        } catch (SQLException e) {
            System.err.println("Error checking invoice existence: " + e.getMessage());
            return false;
        }
    }

    public boolean existsByDiscountCode(String discountCode) {
        if (discountCode == null || discountCode.trim().isEmpty())
            return false;

        // Kiểm tra xem mã KM này đã từng xuất hiện trong hóa đơn nào chưa
        String sql = "SELECT 1 FROM invoice WHERE UPPER(discount_code) = ? LIMIT 1";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, discountCode.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Trả về true nếu đã có hóa đơn sử dụng mã này
            }
        } catch (SQLException e) {
            System.err.println("Error checking invoice existence by discount code: " + e.getMessage());
            return false;
        }
    }

    public boolean insert(Connection connection, InvoiceDTO obj) {
        // SQL: Chèn các thông tin cơ bản của hóa đơn bán hàng
        // Giả định bảng invoice có các cột: customer_id, employee_id, total_price,
        // discount_code, discount_amount
        String sql = "INSERT INTO invoice (customer_id, employee_id, total_price, discount_code, discount_amount, status_id) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 1. Set các tham số (Mapping từ DTO sang DB)
            statement.setInt(1, obj.getCustomerId());

            statement.setInt(2, obj.getEmployeeId());
            statement.setBigDecimal(3, obj.getTotalPrice());

            // Xử lý mã giảm giá (Nếu không có thì để null)
            if (obj.getDiscountCode() != null && !obj.getDiscountCode().isEmpty()) {
                statement.setString(4, obj.getDiscountCode());
            } else {
                statement.setNull(4, java.sql.Types.VARCHAR);
            }

            statement.setBigDecimal(5, obj.getDiscountAmount());
            statement.setInt(6, obj.getStatusId());

            // 2. Thực thi lệnh
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            // 3. Lấy ID tự tăng vừa được sinh ra (Dùng để chèn vào invoice_detail)
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    obj.setId(generatedKeys.getInt(1)); // Gán ngược ID vào object DTO
                } else {
                    throw new SQLException("Inserting invoice failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting into invoice table: " + e.getMessage());
            return false;
        }
    }
}
