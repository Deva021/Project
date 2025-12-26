package com.minintercom.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class DatabaseServiceTest {

    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TenantContext.clear();
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    public void testPrepareTenantStatementWithWhere() throws SQLException {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        String sql = "SELECT * FROM users WHERE active = true";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        DatabaseService.prepareTenantStatement(mockConnection, sql);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(sqlCaptor.capture());

        String capturedSql = sqlCaptor.getValue();
        assertTrue(capturedSql.contains("AND tenant_id = ?"));
        verify(mockPreparedStatement).setObject(1, tenantId);
    }

    @Test
    public void testPrepareTenantStatementWithoutWhere() throws SQLException {
        UUID tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);

        String sql = "SELECT * FROM users";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        DatabaseService.prepareTenantStatement(mockConnection, sql);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(sqlCaptor.capture());

        String capturedSql = sqlCaptor.getValue();
        assertTrue(capturedSql.contains("WHERE tenant_id = ?"));
        verify(mockPreparedStatement).setObject(1, tenantId);
    }

    @Test
    public void testPrepareTenantStatementMissingContext() {
        String sql = "SELECT * FROM users";
        assertThrows(SQLException.class, () -> {
            DatabaseService.prepareTenantStatement(mockConnection, sql);
        });
    }
}
