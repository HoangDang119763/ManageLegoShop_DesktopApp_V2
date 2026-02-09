package DAL;

import DTO.FileDTO;
import java.sql.*;

public class FileDAL extends BaseDAL<FileDTO, Integer> {
    public static final FileDAL INSTANCE = new FileDAL();

    private FileDAL() {
        super(ConnectApplication.getInstance().getConnectionFactory(), "file", "id");
    }

    public static FileDAL getInstance() {
        return INSTANCE;
    }

    @Override
    protected FileDTO mapResultSetToObject(ResultSet resultSet) throws SQLException {
        return new FileDTO(
                resultSet.getInt("id"),
                resultSet.getString("file_path"),
                resultSet.getString("file_name"),
                resultSet.getTimestamp("created_at") != null
                        ? resultSet.getTimestamp("created_at").toLocalDateTime()
                        : null);
    }

    @Override
    protected boolean shouldUseGeneratedKeys() {
        return true;
    }

    @Override
    protected void setGeneratedKey(FileDTO obj, ResultSet generatedKeys) throws SQLException {
        if (generatedKeys.next()) {
            obj.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    protected String getInsertQuery() {
        return "(file_path, file_name, created_at) VALUES (?, ?, ?)";
    }

    @Override
    protected void setInsertParameters(PreparedStatement statement, FileDTO obj) throws SQLException {
        statement.setString(1, obj.getFilePath());
        statement.setString(2, obj.getFileName());
        statement.setObject(3, obj.getCreatedAt());
    }

    @Override
    protected String getUpdateQuery() {
        return "SET file_path = ?, file_name = ?, created_at = ? WHERE id = ?";
    }

    @Override
    protected void setUpdateParameters(PreparedStatement statement, FileDTO obj) throws SQLException {
        statement.setString(1, obj.getFilePath());
        statement.setString(2, obj.getFileName());
        statement.setObject(3, obj.getCreatedAt());
        statement.setInt(4, obj.getId());
    }

}
