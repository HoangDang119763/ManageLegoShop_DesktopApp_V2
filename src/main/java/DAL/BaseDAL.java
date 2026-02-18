package DAL;

import INTERFACE.IDAL;
import INTERFACE.ConnectionFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

public abstract class BaseDAL<T, K> implements IDAL<T, K> {
    protected final ConnectionFactory connectionFactory;
    protected final String table;
    protected final String idColumn;

    protected BaseDAL(ConnectionFactory connectionFactory, String table, String idColumn) {
        this.connectionFactory = connectionFactory;
        this.table = table;
        this.idColumn = idColumn;
    }

    @Override
    public ArrayList<T> getAll() {
        final String query = "SELECT * FROM " + table + " ORDER BY " + idColumn;
        ArrayList<T> list = new ArrayList<>();

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next())
                list.add(mapResultSetToObject(resultSet));
        } catch (SQLException e) {
            System.err.println("Error retrieving " + table + ": " + e.getMessage());
        }
        return list;
    }

    @Override
    public T getById(K id) {
        final String query = "SELECT * FROM " + table + " WHERE " + idColumn + " = ?";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            setIdParameter(statement, id, 1);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next())
                    return mapResultSetToObject(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving " + table + " by ID: " + e.getMessage());
        }
        return null;
    }

    protected abstract T mapResultSetToObject(ResultSet resultSet) throws SQLException;

    @Override
    public boolean insert(T obj) {
        final String query = "INSERT INTO " + table + " " + getInsertQuery();
        if (query.isEmpty())
            throw new UnsupportedOperationException("Insert operation not supported for " + table);

        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = shouldUseGeneratedKeys()
                        ? connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
                        : connection.prepareStatement(query)) {

            setInsertParameters(statement, obj);
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0)
                if (shouldUseGeneratedKeys())
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        setGeneratedKey(obj, generatedKeys);
                    }

            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting into " + table + ": " + e.getMessage());
            return false;
        }
    }

    protected boolean shouldUseGeneratedKeys() {
        return false;
    }

    protected void setGeneratedKey(T obj, ResultSet generatedKeys) throws SQLException {
        throw new UnsupportedOperationException("Set Generated Key operation not supported.");
    }

    protected String getInsertQuery() {
        return "";
    }

    protected void setInsertParameters(PreparedStatement statement, T obj) throws SQLException {
        throw new UnsupportedOperationException("Insert parameters not implemented.");
    }

    @Override
    public boolean update(T obj) {
        final String query = "UPDATE " + table + " " + getUpdateQuery();
        if (query.isEmpty())
            throw new UnsupportedOperationException("Update operation not supported for " + table);
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            setUpdateParameters(statement, obj);
            return statement.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Error updating " + table + ": " + e.getMessage());
            return false;
        }
    }

    protected String getUpdateQuery() {
        return "";
    }

    protected void setUpdateParameters(PreparedStatement statement, T obj) throws SQLException {
        throw new UnsupportedOperationException("Update parameters not implemented.");
    }

    @Override
    public boolean delete(K id) {
        final String query = "DELETE FROM " + table + " WHERE " + idColumn + " = ?";
        return executeDeleteQuery(query, id);
    }

    private boolean executeDeleteQuery(String query, K id) {
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            setIdParameter(statement, id, 1);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting from " + table + ": " + e.getMessage());
            return false;
        }
    }

    private void setIdParameter(PreparedStatement statement, K id, int index) throws SQLException {
        if (id instanceof Integer) {
            statement.setInt(index, (Integer) id);
        } else if (id instanceof String) {
            statement.setString(index, (String) id);
        } else if (id instanceof LocalDateTime) {
            statement.setTimestamp(index, Timestamp.valueOf((LocalDateTime) id));
        } else {
            throw new IllegalArgumentException("Unsupported ID type: " + id.getClass().getSimpleName());
        }
    }

    public int getLastIdEver() {
        // 1. Dùng ? thay vì nối chuỗi để an toàn tuyệt đối
        String sql = "SELECT AUTO_INCREMENT " +
                "FROM information_schema.tables " +
                "WHERE table_name = ? " +
                "AND table_schema = (SELECT DATABASE())";

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, table); // Truyền tên bảng vào tham số

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int nextId = rs.getInt("AUTO_INCREMENT");
                    // Nếu nextId = 1 nghĩa là bảng chưa từng có dữ liệu
                    return (nextId > 1) ? nextId - 1 : 0;
                }
            }
        } catch (SQLException e) {
            // Nên log lỗi bằng Logger (như bạn đã dùng @Slf4j ở chỗ khác)
            System.err.println("Error retrieving Auto Increment for " + table + ": " + e.getMessage());
        }
        return 0;
    }
}
