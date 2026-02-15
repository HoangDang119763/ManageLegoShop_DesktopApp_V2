package DAL;

import DTO.CustomerDTO;
import DTO.ProductDTO;

import java.sql.*;
import java.util.ArrayList;

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

    public ArrayList<CustomerDTO> filterCustomers(String keyword, int status, int page, int pageSize) {
        ArrayList<CustomerDTO> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM customer WHERE 1=1");
        ArrayList<Object> params = new ArrayList<>();

        // 1. Lọc theo trạng thái
        if (status != -1) {
            sql.append(" AND status_id = ?");
            params.add(status);
        }

        // 2. Tìm kiếm gom hết (Unified OR Search)
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (CAST(id AS CHAR) LIKE ? ") // Tìm theo mã
                    .append(" OR LOWER(first_name) LIKE ? ") // Tìm theo họ đệm
                    .append(" OR LOWER(last_name) LIKE ? ") // Tìm theo tên
                    .append(" OR phone LIKE ?)"); // Tìm theo SĐT

            String dbKeyword = "%" + keyword + "%";
            params.add(dbKeyword);
            params.add(dbKeyword);
            params.add(dbKeyword);
            params.add(dbKeyword);
        }

        // 3. Sắp xếp mới nhất lên đầu
        sql.append(" ORDER BY id DESC");

        // 4. THỰC HIỆN PHÂN TRANG
        if (page > 0 && pageSize > 0) {
            sql.append(" LIMIT ? OFFSET ?");
            int offset = (page - 1) * pageSize;
            params.add(pageSize);
            params.add(offset);
        }

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi phân trang CustomerDAL: " + e.getMessage());
        }
        return list;
    }
}
