package com.gcash.app.Service;

import com.gcash.app.Model.CheckBalance;
import com.gcash.app.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BalanceServiceTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private MockedStatic<DatabaseConnection> mockedDbConnection;

    @BeforeEach
    void setUp() {
        mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(connection);
    }

    @AfterEach
    void tearDown() {
        mockedDbConnection.close();
    }

    @Test
    @DisplayName("Check if balance is the same as the database contains")
    void testCheckBalance() throws SQLException {
        // Specific stubs for this test
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getDouble("amount")).thenReturn(1000.0);
        when(resultSet.getInt("user_id")).thenReturn(1);

        CheckBalance balance = BalanceService.checkBalance(1);

        assertNotNull(balance);
        assertEquals(1000.0, balance.getAmount());
        assertEquals(1, balance.getUser_id());
    }

    @Test
    @DisplayName("Check balance for user that does not exist")
    void testCheckBalanceForNonExistentUser() throws SQLException {
        // Specific stubs for this test
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        CheckBalance balance = BalanceService.checkBalance(99);
        assertNull(balance);
    }
}