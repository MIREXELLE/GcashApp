package com.gcash.app.Security;

import com.gcash.app.Model.Users;
import com.gcash.app.util.DatabaseConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserAuthenticationTest {

    @InjectMocks
    private UserAuthentication userAuthentication;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private MockedStatic<DatabaseConnection> mockedDbConnection;
    private MockedStatic<SessionManager> mockedSessionManager;

    @BeforeEach
    void setUp() {
        // Mock the static database connection for all tests
        mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(connection);

        // Mock the static session manager for all tests
        mockedSessionManager = Mockito.mockStatic(SessionManager.class);
    }

    @AfterEach
    void tearDown() {
        // Close the static mocks after each test
        mockedDbConnection.close();
        mockedSessionManager.close();
    }

    @Test
    @DisplayName("Test Valid Login")
    void testValidLogin() throws SQLException {
        // Specific stubs for this test
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id")).thenReturn(1);
        mockedSessionManager.when(() -> SessionManager.createSession(1)).thenReturn(true);

        int userId = userAuthentication.login("test@example.com", "1234");
        assertEquals(1, userId, "Login should be successful for valid credentials.");
    }

    @Test
    @DisplayName("Test Invalid Login - Wrong Password or Email")
    void testInvalidLogin() throws SQLException {
        // Specific stubs for this test
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int userId = userAuthentication.login("wrong@example.com", "0000");
        assertEquals(-1, userId, "Login should fail for invalid credentials.");
    }

    @Test
    @DisplayName("Test User Registration")
    void testRegistration() throws SQLException {
        // Specific stubs for this test
        when(connection.prepareStatement(anyString(), any(int.class))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(100);

        Users newUser = new Users("Test User", "testuser@example.com", "09171234567", "1234");
        int newUserId = userAuthentication.register(newUser);

        assertEquals(100, newUserId);
    }
}