package FACTORY;

import INTERFACE.ConnectionFactory;
import lombok.extern.log4j.Log4j2;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
public class ConnectionFactoryImpl implements ConnectionFactory {
    private static final String URL = "jdbc:mysql://localhost:3306/java_sql?useAffectedRows=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    private static HikariDataSource dataSource;

    // Static initializer - creates pool on first use
    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);

            // Pool configuration
            config.setMaximumPoolSize(15); // Max 15 connections
            config.setMinimumIdle(3); // Keep 3 idle connections ready
            config.setConnectionTimeout(10000); // 10 second timeout
            config.setIdleTimeout(600000); // 10 minutes idle before close
            config.setMaxLifetime(1800000); // 30 minutes max lifetime

            // Test connection on borrow
            config.setConnectionTestQuery("SELECT 1");

            // Connection validation
            config.setLeakDetectionThreshold(60000); // Warn if connection held > 60s

            dataSource = new HikariDataSource(config);
            testConnection();
        } catch (Exception e) {
            System.err.println("Failed to initialize HikariCP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Connection newConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Connection pool not initialized");
        }
        return dataSource.getConnection();
    }

    @Override
    public void closeConnection(Connection connection) throws SQLException {
        // ⚠️ Do NOT close - return to pool for reuse
        // HikariCP handles connection lifecycle
        if (connection != null && !connection.isClosed()) {
            connection.close(); // Returns to pool, not closes for real
        }
    }

    @Override
    public boolean isConnected(Connection connection) {
        return connection != null;
    }

    private static void testConnection() {
        if (dataSource == null)
            return;

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ HikariCP Pool connected successfully!");
            // System.out.println(" - Pool Size: " + dataSource.getMaximumPoolSize());
            // System.out.println(" - URL: " + URL);
        } catch (SQLException e) {
            System.err.println("❌ HikariCP connection test failed: " + e.getMessage());
        }
    }

    /**
     * Shutdown pool when app closes (optional)
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("✅ HikariCP Pool closed");
        }
    }
}
