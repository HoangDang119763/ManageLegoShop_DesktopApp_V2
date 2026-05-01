package DAL;

import DTO.HolidayDTO;
import DTO.PagedResponse;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class HolidayDAL extends BaseDAL<HolidayDTO, Integer> {
    public static final HolidayDAL INSTANCE = new HolidayDAL();

    private HolidayDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "holiday", "id");
    }

    public static HolidayDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected HolidayDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new HolidayDTO(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getDate("date").toLocalDate());
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(HolidayDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(name, date) VALUES (?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, HolidayDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setDate(2, Date.valueOf(obj.getDate()));
    }

    @Override
    protected String getUpdateQuery() {
        return "SET name = ?, date = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, HolidayDTO obj) throws SQLException {
        statement.setString(1, obj.getName());
        statement.setDate(2, Date.valueOf(obj.getDate()));
        statement.setInt(3, obj.getId());
    }

    public int getMaxId() {
        String query = "SELECT MAX(id) as max_id FROM holiday";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("max_id");
            }
        } catch (SQLException e) {
            System.err.println("Error getting max holiday ID: " + e.getMessage());
        }
        return 0;
    }

    public boolean existsByNameAndDate(String name, LocalDate date, int excludeId) {
        String query = "SELECT 1 FROM holiday WHERE LOWER(name) = LOWER(?) AND date = ? AND id != ? LIMIT 1";
        try (Connection connection = connectionFactory.newConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, name != null ? name.trim() : "");
            statement.setDate(2, Date.valueOf(date));
            statement.setInt(3, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking duplicate holiday: " + e.getMessage());
        }
        return false;
    }

    public boolean isHolidayPassed(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public PagedResponse<HolidayDTO> filterHolidaysPagedDisplay(
            String keyword, int pageIndex, int pageSize) {
        ArrayList<HolidayDTO> list = new ArrayList<>();
        int totalCount = 0;

        StringBuilder sql = new StringBuilder(
                "SELECT id, name, date, COUNT(*) OVER() as total_count " +
                        "FROM holiday " +
                        "WHERE 1=1");

        ArrayList<Object> params = new ArrayList<>();

        // Tìm kiếm (Mã hoặc Tên)
        if (keyword != null && !keyword.isEmpty()) {
            sql.append(" AND (CAST(id AS CHAR) LIKE ? OR LOWER(name) LIKE ?)");
            String dbKeyword = "%" + keyword + "%";
            params.add(dbKeyword);
            params.add(dbKeyword);
        }

        // PHÂN TRANG
        sql.append(" ORDER BY date DESC LIMIT ? OFFSET ?");
        int offset = pageIndex * pageSize;
        params.add(pageSize);
        params.add(offset);

        try (Connection conn = connectionFactory.newConnection();
                PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (totalCount == 0) {
                        totalCount = rs.getInt("total_count");
                    }
                    list.add(mapResultSetToObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error filtering holidays with pagination: " + e.getMessage());
        }

        return new PagedResponse<>(list, pageIndex, pageSize, totalCount);
    }
}
