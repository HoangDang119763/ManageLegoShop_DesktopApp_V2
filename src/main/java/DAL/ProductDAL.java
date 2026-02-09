package DAL;

import DTO.DetailInvoiceDTO;
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
        return "(id, name, stock_quantity, selling_price, import_price, status_id, description, image_url, category_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        statement.setObject(10, obj.getCreatedAt());
        statement.setObject(11, obj.getUpdatedAt());
    }

    @Override
    protected String getUpdateQuery() {
        // Loại bỏ import_price để tránh làm sai lệch giá vốn khi chỉ sửa thông tin sản phẩm
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

    public boolean softDelete(ProductDTO obj) {
        // SQL tự tìm ID dựa vào tên trạng thái 'INACTIVE' hoặc 'DELETED'
        String query = "UPDATE product SET status_id = ? WHERE id = ?";
        try (Connection connection = connectionFactory.newConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setInt(1, obj.getStatusId());
                statement.setString(2, obj.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error soft delete product: " + e.getMessage());
            return false;
        }
    }
}
