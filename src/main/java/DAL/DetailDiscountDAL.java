package DAL;

import DTO.DetailDiscountDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
+ Remain getAll
+ Remain getById
+ Override insert - Not Need GENERATED_KEYS
+ Remain update - Update total_price_invoice and discount_amount
+ Remain delete (hard delete)
+ Need Insert ArrayList
*/

public class DetailDiscountDAL extends BaseDAL<DetailDiscountDTO, String> {
    public static final DetailDiscountDAL INSTANCE = new DetailDiscountDAL();

    private DetailDiscountDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "detail_discount", "discount_code");
    }

    public static DetailDiscountDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected DetailDiscountDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new DetailDiscountDTO(
                resultSet.getString("discount_code"),
                resultSet.getBigDecimal("total_price_invoice"),
                resultSet.getBigDecimal("discount_amount"));
    }

    @Override
    protected String getInsertQuery() {
        return "(discount_code, total_price_invoice, discount_amount) VALUES (?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, DetailDiscountDTO obj) throws SQLException {
        statement.setString(1, obj.getDiscountCode());
        statement.setBigDecimal(2, obj.getTotalPriceInvoice());
        statement.setBigDecimal(3, obj.getDiscountAmount());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET total_price_invoice = ?, discount_amount = ? WHERE discount_code = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, DetailDiscountDTO obj) throws SQLException {
        statement.setBigDecimal(1, obj.getTotalPriceInvoice());
        statement.setBigDecimal(2, obj.getDiscountAmount());
        statement.setString(3, obj.getDiscountCode());
    }

    public boolean insertAllDetailDiscount(Connection conn, List<DetailDiscountDTO> list) {
        final String query = "INSERT INTO detail_discount (discount_code, total_price_invoice, discount_amount) VALUES (?, ?, ?)";

        // Kiểm tra đầu vào sớm
        if (list == null || list.isEmpty()) return true;

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            for (DetailDiscountDTO obj : list) {
                statement.setString(1, obj.getDiscountCode()); // Lấy code trực tiếp từ DTO
                statement.setBigDecimal(2, obj.getTotalPriceInvoice());
                statement.setBigDecimal(3, obj.getDiscountAmount());
                statement.addBatch();
            }
            int[] results = statement.executeBatch();
            if (results == null) return false;
            for (int res : results) {
                if (res == Statement.EXECUTE_FAILED) return false;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting detail discount batch: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteByDiscountCode(Connection conn, String discountCode) throws SQLException {
        String query = "DELETE FROM detail_discount WHERE discount_code = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, discountCode);
            return statement.executeUpdate() >= 0; // Trả về true kể cả khi không có dòng nào để xóa
        }
    }

    public ArrayList<DetailDiscountDTO> getAllDetailDiscountByDiscountCode(String discountCode) {
        final String query = "SELECT * FROM detail_discount WHERE discount_code = ?";
        ArrayList<DetailDiscountDTO> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, discountCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSetToObject(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving " + table + ": " + e.getMessage());
        }
        return list;
    }
}
