package DAL;

import DTO.CustomerDTO;
import DTO.CustomerDisplayDTO;
import DTO.CustomerForInvoiceDTO;
import DTO.PagedResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAL extends BaseDAL<CustomerDTO, Integer> {
    public static final CustomerDAL INSTANCE = new CustomerDAL();

    private CustomerDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "customer", "id");
    }

    public static CustomerDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected CustomerDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new CustomerDTO(
                resultSet.getInt("id"),
                resultSet.getString("first_name"),
                resultSet.getString("last_name"),
                resultSet.getString("phone"),
                resultSet.getString("address"),
                resultSet.getDate("date_of_birth") != null
                        ? resultSet.getDate("date_of_birth").toLocalDate()
                        : null,
                resultSet.getInt("status_id"),
                resultSet.getTimestamp("updated_at") != null ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(CustomerDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(first_name, last_name, phone, address, date_of_birth, status_id) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, CustomerDTO obj) throws SQLException {
        statement.setString(1, obj.getFirstName());
        statement.setString(2, obj.getLastName());
        statement.setString(3, obj.getPhone());
        statement.setString(4, obj.getAddress());
        statement.setObject(5, obj.getDateOfBirth());
        statement.setInt(6, obj.getStatusId());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET first_name = ?, last_name = ?, phone = ?, address = ?, date_of_birth = ?, status_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, CustomerDTO obj) throws SQLException {
        statement.setString(1, obj.getFirstName());
        statement.setString(2, obj.getLastName());
        statement.setString(3, obj.getPhone());
        statement.setString(4, obj.getAddress());
        statement.setObject(5, obj.getDateOfBirth());
        statement.setInt(6, obj.getStatusId());
        statement.setInt(7, obj.getId());
    }

    public boolean updateStatus(int id, int newStatusId) {
        String query = "UPDATE customer SET status_id = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, newStatusId);
            statement.setInt(2, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating customer status: " + e.getMessage());
            return false;
        }
    }

    public ArrayList<CustomerDTO> searchByPhone(String phone) {
        ArrayList<CustomerDTO> list = new ArrayList<>();
        // Sử dụng LIKE để tìm kiếm một phần chuỗi
        String query = "SELECT * FROM customer WHERE phone LIKE ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            // Tìm kiếm mờ: %phone%
            statement.setString(1, "%" + phone.trim() + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSetToObject(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching customer by phone: " + e.getMessage());
        }
        return list;
    }

    public boolean existsByCustomerData(String firstName, String lastName, String phone,
            java.time.LocalDate dob, String address, int currentId) {

        // Sử dụng SELECT 1 và LIMIT 1 để tối ưu hiệu năng
        StringBuilder sql = new StringBuilder("SELECT 1 FROM customer WHERE ");
        sql.append("first_name = ? AND last_name = ? AND phone = ? ");

        // Kiểm tra null cho ngày sinh và địa chỉ nếu DB cho phép null
        sql.append("AND (date_of_birth = ? OR (? IS NULL AND date_of_birth IS NULL)) ");
        sql.append("AND (address = ? OR (? IS NULL AND address IS NULL)) ");

        // Loại trừ ID hiện tại (nếu là thêm mới thì currentId thường là 0 hoặc -1)
        sql.append("AND id != ? LIMIT 1");

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, phone);

            // Xử lý Date of Birth
            statement.setObject(4, dob);
            statement.setObject(5, dob);

            // Xử lý Address
            statement.setString(6, address);
            statement.setString(7, address);

            statement.setInt(8, currentId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next(); // Nếu có bản ghi trả về, nghĩa là đã tồn tại khách tương tự
            }
        } catch (SQLException e) {
            System.err.println("Error checking duplicate customer: " + e.getMessage());
        }
        return false;
    }

    public PagedResponse<CustomerDisplayDTO> filterCustomersPagedForManageDisplay(
            String keyword, int statusId, int pageIndex, int pageSize) {

        List<CustomerDisplayDTO> items = new ArrayList<>();
        int totalItems = 0;
        int offset = pageIndex * pageSize;

        // Sử dụng CONCAT để hỗ trợ tìm kiếm Full Name (Họ + Tên)
        String sql = "SELECT " +
                "c.id, c.first_name, c.last_name, c.phone, c.address, c.date_of_birth, " +
                "c.status_id, s.description as status_description, c.updated_at, " +
                "COUNT(*) OVER() as total_count " +
                "FROM customer c " +
                "LEFT JOIN status s ON c.status_id = s.id " +
                "WHERE (? = '' OR (" +
                "    CAST(c.id AS CHAR) LIKE ? " +
                "    OR LOWER(c.first_name) LIKE ? " +
                "    OR LOWER(c.last_name) LIKE ? " +
                "    OR LOWER(CONCAT(c.first_name, ' ', c.last_name)) LIKE ?" +
                ")) " +
                "AND (? = -1 OR c.status_id = ?) " +
                "LIMIT ?, ?";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            // Chuẩn hóa từ khóa
            String cleanKeyword = (keyword == null) ? "" : keyword.trim();
            String searchKey = "%" + cleanKeyword.toLowerCase() + "%";

            int idx = 1;

            // Gán tham số cho phần Tìm kiếm (Dùng cleanKeyword cho vế ? = '')
            ps.setString(idx++, cleanKeyword);
            ps.setString(idx++, searchKey); // Like ID
            ps.setString(idx++, searchKey); // Like First Name
            ps.setString(idx++, searchKey); // Like Last Name
            ps.setString(idx++, searchKey); // Like Full Name (CONCAT)

            // Gán tham số cho Filter Status
            ps.setInt(idx++, statusId);
            ps.setInt(idx++, statusId);

            // Tham số cho phân trang
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
            System.err.println("Error filtering customers: " + e.getMessage());
        }

        return new PagedResponse<>(items, totalItems, pageIndex, pageSize);
    }

    private CustomerDisplayDTO mapResultSetToDisplayObject(ResultSet rs) throws SQLException {
        return new CustomerDisplayDTO(
                rs.getInt("id"),
                rs.getString("first_name") + " " + rs.getString("last_name"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toLocalDate() : null,
                rs.getInt("status_id"),
                rs.getString("status_description"),
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
    }

    public ArrayList<CustomerForInvoiceDTO> filterCustomersByKeywordForInvoice(String keyword, int statusId) {
        ArrayList<CustomerForInvoiceDTO> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT id, first_name, last_name, phone, address FROM customer WHERE ");

        // Thêm cả LOWER cho first_name đơn lẻ để đảm bảo độ phủ 100%
        sql.append("(? = -1 OR status_id = ?) AND (? = '' OR (" +
                "LOWER(CONCAT(first_name, ' ', last_name)) LIKE ? " + // (1) Tìm "Họ Tên"
                "OR LOWER(first_name) LIKE ? " + // (2) Tìm "Họ"
                "OR LOWER(last_name) LIKE ? " + // (3) Tìm "Tên"
                "OR phone LIKE ?))"); // (4) Tìm "SĐT"

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            int idx = 1;
            String searchKey = keyword == null ? "" : keyword.toLowerCase().trim();
            String pattern = "%" + searchKey + "%";

            // Gán status_id
            ps.setInt(idx++, statusId);
            ps.setInt(idx++, statusId);

            // Gán keyword
            ps.setString(idx++, searchKey); // Kiểm tra rỗng
            ps.setString(idx++, pattern); // Cho CONCAT
            ps.setString(idx++, pattern); // Cho first_name
            ps.setString(idx++, pattern); // Cho last_name
            ps.setString(idx++, pattern); // Cho phone

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CustomerForInvoiceDTO(
                            rs.getInt("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("phone"),
                            rs.getString("address")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering customers for invoice: " + e.getMessage());
        }
        return list;
    }

}
