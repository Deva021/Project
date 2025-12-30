package com.minintercom.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.io.InputStream;
import java.io.IOException;

/**
 * Manages database connections using HikariCP connection pool.
 * Provides a centralized way to acquire connections and ensures efficient
 * resource usage.
 */
public class DatabaseService {
    private static HikariDataSource dataSource;

    private static synchronized HikariDataSource getDataSource() throws SQLException {
        if (dataSource == null) {
            try (InputStream input = DatabaseService.class.getClassLoader()
                    .getResourceAsStream("application.properties")) {
                Properties prop = new Properties();
                if (input == null) {
                    throw new SQLException("Unable to find application.properties");
                }
                prop.load(input);
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(prop.getProperty("db.url"));
                config.setUsername(prop.getProperty("db.username"));
                config.setPassword(prop.getProperty("db.password"));
                config.setDriverClassName("org.postgresql.Driver");

                // HikariCP settings for performance and reliability
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setIdleTimeout(300000);
                config.setConnectionTimeout(30000);

                dataSource = new HikariDataSource(config);
                System.out.println("DEBUG: Database connection pool initialized successfully for URL: "
                        + prop.getProperty("db.url"));
            } catch (IOException ex) {
                System.err.println("ERROR: Failed to load application.properties");
                throw new SQLException("Error loading database configuration", ex);
            } catch (Exception ex) {
                System.err.println("ERROR: Failed to initialize HikariDataSource");
                ex.printStackTrace();
                throw new SQLException("Error initializing database connection pool", ex);
            }
        }
        return dataSource;
    }

    /**
     * Acquires a connection from the HikariCP pool.
     * 
     * @return A database connection.
     * @throws SQLException If a connection cannot be acquired.
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * Executes a query with an automatic tenant_id filter.
     * This ensures that all data access is scoped to the current tenant.
     */
    public static java.sql.PreparedStatement prepareTenantStatement(Connection conn, String sql) throws SQLException {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new SQLException("Tenant context is missing. Access denied.");
        }

        // Simple check to see if WHERE clause already exists
        String separator = sql.toUpperCase().contains(" WHERE ") ? " AND " : " WHERE ";
        String scopedSql = sql + separator + "tenant_id = ?";

        java.sql.PreparedStatement pstmt = conn.prepareStatement(scopedSql);
        pstmt.setObject(1, tenantId);
        return pstmt;
    }

    /**
     * Shuts down the HikariCP connection pool.
     * Should be called when the application is stopping.
     */
    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
