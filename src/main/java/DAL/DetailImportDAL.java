package DAL;

import DTO.DetailImportDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
+ Remain getAll
+ Remain getById
+ Override insert - Not Need GENERATED_KEYS
+ Override update - Not allowed
+ Override delete - Hard delete (allowed)
+ Need Insert ArrayList
*/

public class DetailImportDAL extends BaseDAL<DetailImportDTO, Integer> {
    public static final DetailImportDAL INSTANCE = new DetailImportDAL();

    private DetailImportDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "detail_import", "import_id");
    }

    public static DetailImportDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected DetailImportDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new DetailImportDTO(
                resultSet.getInt("import_id"),
                resultSet.getString("product_id"),
                resultSet.getInt("quantity"),
                resultSet.getBigDecimal("profit_percent"),
                resultSet.getBigDecimal("price"),
                resultSet.getBigDecimal("total_price"),
                resultSet.getBoolean("is_pushed"));
    }

    @Override
    protected String getInsertQuery() {
        return "(import_id, product_id, quantity, profit_percent, price, total_price, is_pushed) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, DetailImportDTO obj) throws SQLException {
        statement.setInt(1, obj.getImportId());
        statement.setString(2, obj.getProductId());
        statement.setInt(3, obj.getQuantity());
        statement.setBigDecimal(4, obj.getProfitPercent());
        statement.setBigDecimal(5, obj.getPrice());
        statement.setBigDecimal(6, obj.getTotalPrice());
        statement.setBoolean(7, obj.isPushed());
    }

    @Override
    protected String getUpdateQuery() {
        throw new UnsupportedOperationException("Cannot update permission records.");
    }

    public boolean insertAllDetailImportByImportId(int importId, ArrayList<DetailImportDTO> list) {
        final String query = "INSERT INTO detail_import (import_id, product_id, quantity, profit_percent, price, total_price, is_pushed) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            for (DetailImportDTO detail : list) {
                statement.setInt(1, importId);
                statement.setString(2, detail.getProductId());
                statement.setInt(3, detail.getQuantity());
                statement.setBigDecimal(4, detail.getProfitPercent());
                statement.setBigDecimal(5, detail.getPrice());
                statement.setBigDecimal(6, detail.getTotalPrice());
                statement.setBoolean(7, detail.isPushed());
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
            System.err.println("Error inserting detail import: " + e.getMessage());
            return false;
        }
    }

    /**
     * Insert Detail Imports using provided connection (for transaction)
     * Thêm chi tiết phiếu nhập sử dụng connection được cung cấp (cho transaction)
     */
    public boolean insertAllDetailImportByImportId(Connection connection, int importId,
            ArrayList<DetailImportDTO> list) {
        final String query = "INSERT INTO detail_import (import_id, product_id, quantity, profit_percent, price, total_price, is_pushed) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, 0)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (DetailImportDTO detail : list) {
                statement.setInt(1, importId);
                statement.setString(2, detail.getProductId());
                statement.setInt(3, detail.getQuantity());
                statement.setBigDecimal(4, detail.getProfitPercent());
                statement.setBigDecimal(5, detail.getPrice());
                statement.setBigDecimal(6, detail.getTotalPrice());
                statement.addBatch();
            }

            int[] results = statement.executeBatch();
            for (int result : results) {
                if (result == PreparedStatement.EXECUTE_FAILED)
                    return false;
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error inserting detail import Batch: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteAllDetailImportByImportId(int importId) {
        final String query = "DELETE FROM detail_import WHERE import_id = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, importId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting from " + table + ": " + e.getMessage());
            return false;
        }
    }

    public ArrayList<DetailImportDTO> getAllDetailImportByImportId(int importId) {
        final String query = "SELECT * FROM detail_import WHERE import_id = ?";
        ArrayList<DetailImportDTO> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, importId);
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

    /**
     * Batch update is_pushed flag for detail import records (1 query)
     * Cập nhật cờ đã đẩy giá cho nhiều chi tiết nhập cùng lúc
     */
    public boolean batchUpdateIsPushed(Connection conn, int importId, List<DTO.DetailPushedInfoDTO> pushedList) {
        if (pushedList == null || pushedList.isEmpty()) {
            return true;
        }

        String sql = "UPDATE detail_import SET is_pushed = ? WHERE import_id = ? AND product_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DTO.DetailPushedInfoDTO info : pushedList) {
                ps.setBoolean(1, info.getIsPushed());
                ps.setInt(2, importId);
                ps.setString(3, info.getProductId());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            for (int result : results) {
                if (result < 0) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error batch updating is_pushed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Batch update is_pushed for multiple detail imports across different imports
     * Cập nhật is_pushed cho nhiều chi tiết nhập từ các phiếu nhập khác nhau
     */
    public boolean batchUpdateIsPushed(Connection conn, List<DTO.DetailPushedInfoDTO> pushedList) {
        if (pushedList == null || pushedList.isEmpty()) {
            return true;
        }

        String sql = "UPDATE detail_import SET is_pushed = ? WHERE product_id = ? AND is_pushed = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (DTO.DetailPushedInfoDTO info : pushedList) {
                ps.setBoolean(1, info.getIsPushed());
                ps.setString(2, info.getProductId());
                ps.setBoolean(3, false); // Only update records that are currently not pushed
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            for (int result : results) {
                if (result < 0) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error batch updating is_pushed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get oldest unpushed detail import for a product (oldest import first)
     * Lấy chi tiết nhập cũ nhất chưa đẩy giá cho một sản phẩm
     */
    public DetailImportDTO getOldestUnpushedDetailImport(Connection conn, String productId) {
        String sql = "SELECT * FROM detail_import WHERE product_id = ? AND is_pushed = 0 ORDER BY import_id ASC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToObject(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting oldest unpushed detail import: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get import ID by product ID (finds which import contains this product)
     * Lấy ID phiếu nhập chứa sản phẩm này
     */
    public Integer getImportIdByProductId(Connection conn, String productId) {
        String sql = "SELECT import_id FROM detail_import WHERE product_id = ? ORDER BY import_id DESC LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("import_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting import id by product id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Check if all detail imports of an import are pushed
     * Kiểm tra xem tất cả chi tiết của một phiếu nhập có đều được đẩy giá không
     */
    public boolean areAllDetailsPushed(Connection conn, int importId) {
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN is_pushed = 1 THEN 1 ELSE 0 END) as pushed FROM detail_import WHERE import_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, importId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int pushed = rs.getInt("pushed");
                    return total > 0 && total == pushed;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking if all details are pushed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Batch get oldest unpushed detail import for multiple products (1 query)
     * Lấy chi tiết nhập cũ nhất chưa được đẩy giá cho nhiều sản phẩm cùng lúc
     */
    public Map<String, DetailImportDTO> getOldestUnpushedDetailImportsByProductIds(Connection conn,
            List<String> productIds) {
        Map<String, DetailImportDTO> resultMap = new HashMap<>();
        if (productIds == null || productIds.isEmpty())
            return resultMap;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < productIds.size(); i++) {
            if (i > 0)
                placeholders.append(", ");
            placeholders.append("?");
        }

        // Sử dụng Window Function để lấy dòng đầu tiên của mỗi nhóm product_id
        String sql = "SELECT * FROM (" +
                "  SELECT *, ROW_NUMBER() OVER (PARTITION BY product_id ORDER BY import_id ASC) as rn " +
                "  FROM detail_import " +
                "  WHERE product_id IN (" + placeholders + ") AND is_pushed = 0" +
                ") as t WHERE t.rn = 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < productIds.size(); i++) {
                ps.setString(i + 1, productIds.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetailImportDTO dto = mapResultSetToObject(rs);
                    resultMap.put(dto.getProductId(), dto); // DB đã lọc rồi, cứ put là xong
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting oldest unpushed detail imports: " + e.getMessage());
        }
        return resultMap;
    }

    /**
     * Batch get mapping of productIds to their importIds (1 query)
     * Lấy map sản phẩm -> phiếu nhập chứa sản phẩm đó
     */
    public Map<String, Integer> getIncompleteImportIdsByProductIds(Connection conn,
            List<String> productIds,
            int completedStatusId) {
        Map<String, Integer> resultMap = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            return resultMap;
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < productIds.size(); i++) {
            if (i > 0)
                placeholders.append(", ");
            placeholders.append("?");
        }

        // JOIN với bảng import để chỉ lấy những phiếu đang ở trạng thái khác COMPLETED
        String sql = "SELECT DISTINCT di.product_id, di.import_id " +
                "FROM detail_import di " +
                "JOIN import i ON di.import_id = i.id " +
                "WHERE di.product_id IN (" + placeholders + ") " +
                "AND i.status_id != ?"; // Chỉ quan tâm các phiếu chưa hoàn thành

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int i = 0;
            for (; i < productIds.size(); i++) {
                ps.setString(i + 1, productIds.get(i));
            }
            // Set tham số cuối cùng là statusId
            ps.setInt(i + 1, completedStatusId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultMap.put(rs.getString("product_id"), rs.getInt("import_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting incomplete import ids: " + e.getMessage());
        }

        return resultMap;
    }

    /**
     * Get all detail imports by import ID using provided connection (for
     * transaction)
     * Lấy tất cả chi tiết nhập theo ID phiếu nhập sử dụng connection được cung cấp
     */
    public ArrayList<DetailImportDTO> getAllDetailImportByImportId(Connection conn, int importId) {
        final String query = "SELECT * FROM detail_import WHERE import_id = ?";
        ArrayList<DetailImportDTO> list = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, importId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapResultSetToObject(resultSet));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving detail import: " + e.getMessage());
        }
        return list;
    }

    /**
     * Delete detail imports by import ID using provided connection (for
     * transaction)
     * Xóa tất cả chi tiết nhập theo ID phiếu nhập sử dụng connection được cung cấp
     */
    public boolean deleteByImportId(Connection conn, int importId) {
        final String query = "DELETE FROM detail_import WHERE import_id = ?";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, importId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting detail import: " + e.getMessage());
            return false;
        }
    }
}
