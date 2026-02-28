package DAL;

import DTO.PositionDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PositionDAL extends BaseDAL<PositionDTO, Integer> {
    public static final PositionDAL INSTANCE = new PositionDAL();

    private PositionDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "position", "id");
    }

    public static PositionDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected PositionDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new PositionDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getObject("wage") != null ? resultSet.getBigDecimal("wage") : null,
                resultSet.getObject("min_experience") != null ? resultSet.getInt("min_experience") : null,
                resultSet.getObject("max_experience") != null ? resultSet.getInt("max_experience") : null,
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null,
                resultSet.getTimestamp("updated_at") != null
                        ? resultSet.getTimestamp("updated_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(PositionDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, wage, min_experience, max_experience) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, PositionDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setBigDecimal(2, obj.getWage() != null ? obj.getWage() : java.math.BigDecimal.ZERO);
        statement.setObject(3, obj.getMinExperience());
        statement.setObject(4, obj.getMaxExperience());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, wage = ?, min_experience = ?, max_experience = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, PositionDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setBigDecimal(2, obj.getWage() != null ? obj.getWage() : java.math.BigDecimal.ZERO);
        statement.setObject(3, obj.getMinExperience());
        statement.setObject(4, obj.getMaxExperience());
        statement.setInt(5, obj.getId());
    }

    @Override
    public ArrayList<PositionDTO> getAll() {
        String sql = "SELECT id, name, wage, min_experience, max_experience, created_at, updated_at FROM position ORDER BY name ASC";
        ArrayList<PositionDTO> result = new ArrayList<>();

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapResultSetToObject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy tất cả vị trí: " + e.getMessage());
        }

        return result;
    }
}
