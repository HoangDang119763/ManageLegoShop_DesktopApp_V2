package DAL;

import DTO.CustomerDTO;
import DTO.ProductDTO;

import java.sql.*;

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

}
