package DAL;

import DTO.ProductDTO;
import java.sql.*;
import java.util.ArrayList;

public class ProductDAL extends BaseDAL<ProductDTO, String> {
    public static final ProductDAL INSTANCE = new ProductDAL();

    private ProductDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "product", "id");
    }

    public static ProductDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected ProductDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new ProductDTO(
                resultSet.getString("id"),
                resultSet.getString("name"),
                resultSet.getInt("stock_quantity"),
                resultSet.getBigDecimal("selling_price"),
                resultSet.getBigDecimal("import_price"),
                resultSet.getInt("status_id"),
                resultSet.getString("description"),
                resultSet.getString("image_url"),
                resultSet.getInt("category_id"),
                resultSet.getTimestamp("created_at") != null ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("updated_at") != null ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected String getInsertQuery() {
        // Chỉ giữ lại: id, name, stock_quantity, selling_price, status_id, description,
        // image_url, category_id
        // Bỏ qua: import_price (set mặc định 0 ở BUS/DB), created_at, updated_at (DB tự
        // sinh)
        return "(id, name, stock_quantity, selling_price, import_price, status_id, description, image_url, category_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, ProductDTO obj) throws SQLException {
        statement.setString(1, obj.getId());
        statement.setString(2, obj.getName());
        statement.setInt(3, obj.getStockQuantity());
        statement.setBigDecimal(4, obj.getSellingPrice());
        statement.setBigDecimal(5, obj.getImportPrice());
        statement.setInt(6, obj.getStatusId());
        statement.setString(7, obj.getDescription());
        statement.setString(8, obj.getImageUrl());
        statement.setInt(9, obj.getCategoryId());
    }

    @Override
    protected String getUpdateQuery() {
        // Loại bỏ import_price để tránh làm sai lệch giá vốn khi chỉ sửa thông tin sản
        // phẩm
        return "SET name = ?, selling_price = ?, status_id = ?, description = ?, image_url = ?, category_id = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, ProductDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setBigDecimal(2, obj.getSellingPrice());
        statement.setInt(3, obj.getStatusId());
        statement.setString(4, obj.getDescription());
        statement.setString(5, obj.getImageUrl());
        statement.setInt(6, obj.getCategoryId());
        statement.setString(7, obj.getId()); // Tham số cho điều kiện WHERE
    }

    public boolean updateProductQuantityAndSellingPrice(ArrayList<ProductDTO> list) {
        String query = "UPDATE product SET stock_quantity = ?, selling_price = ?, import_price = ?, updated_at = ? WHERE id = ?";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            for (ProductDTO obj : list) {
                statement.setInt(1, obj.getStockQuantity());
                statement.setBigDecimal(2, obj.getSellingPrice());
                statement.setBigDecimal(3, obj.getImportPrice());
                statement.setObject(4, obj.getUpdatedAt());
                statement.setString(5, obj.getId());
                statement.addBatch();
            }

            int[] results = statement.executeBatch();

            for (int result : results) {
                if (result < 0) {
                    return false;
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error updating stock quantity, selling price product: " + e.getMessage());
            return false;
        }
    }

    public boolean insertListProductExcel(ArrayList<ProductDTO> list) {
        final String query = "INSERT INTO product (id, name, stock_quantity, selling_price, import_price, status_id, description, image_url, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            for (ProductDTO obj : list) {
                statement.setString(1, obj.getId());
                statement.setString(2, obj.getName());
                statement.setInt(3, obj.getStockQuantity());
                statement.setBigDecimal(4, obj.getSellingPrice());
                statement.setBigDecimal(5, obj.getImportPrice());
                statement.setInt(6, obj.getStatusId());
                statement.setString(7, obj.getDescription());
                statement.setString(8, obj.getImageUrl());
                statement.setInt(9, obj.getCategoryId());
                statement.setObject(10, obj.getCreatedAt());
                statement.setObject(11, obj.getUpdatedAt());
                statement.addBatch();
            }

            int[] results = statement.executeBatch();

            for (int result : results) {
                if (result < 0) {
                    return false;
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error inserting detail invoice: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(String id, int newStatusId) {
        String query = "UPDATE product SET status_id = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, newStatusId);
            statement.setString(2, id);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating product status: " + e.getMessage());
            return false;
        }
    }

    /**
     * [STATELESS] Generate next product ID by querying database
     * No local cache needed
     * 
     * @return next product ID (e.g., "SP00001")
     */
    public String getNextProductId() {
        String query = "SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) as max_id FROM product WHERE id LIKE 'SP%'";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    int maxId = rs.getInt("max_id");
                    if (maxId == 0) {
                        return "SP00001";
                    }
                    return String.format("SP%05d", maxId + 1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting next product ID: " + e.getMessage());
        }
        return "SP00001";
    }

    /**
     * [STATELESS] Check if product name already exists (case-insensitive)
     * No local cache needed
     * 
     * @param exceptId product ID to exclude (for update validation)
     * @param name     product name to check
     * @return true if name exists in DB, false otherwise
     */
    public boolean isProductNameExists(String exceptId, String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        String query = "SELECT COUNT(*) as count FROM product WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))";
        if (exceptId != null && !exceptId.isEmpty()) {
            query += " AND id != ?";
        }

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name);
            if (exceptId != null && !exceptId.isEmpty()) {
                statement.setString(2, exceptId);
            }

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking product name exists: " + e.getMessage());
        }
        return false;
    }

    /**
     * [STATELESS] Check if category is used in any product
     * No local cache needed
     * 
     * @param categoryId category ID to check
     * @return true if category is used, false otherwise
     */
    public boolean isCategoryInUse(int categoryId) {
        String query = "SELECT 1 FROM product WHERE category_id = ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, categoryId);

            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking category in use: " + e.getMessage());
        }
        return false;
    }
}
