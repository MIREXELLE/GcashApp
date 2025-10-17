package com.gcash.app.Service;

import com.gcash.app.Model.Transactions;
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

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement checkUserPs, checkBalancePs, updateBalancePs, insertTransactionPs, checkUsersPs, checkRecipientPs, createBalancePs, senderTransactionPs, recipientTransactionPs;
    @Mock
    private ResultSet userRs, balanceRs, generatedKeysRs, usersRs, recipientRs;
    @Mock
    private Statement statement;

    private MockedStatic<DatabaseConnection> mockedDbConnection;

    @BeforeEach
    void setUp() throws SQLException {
        mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getConnection).thenReturn(connection);
        // Removed statement stub from here
    }

    @AfterEach
    void tearDown() {
        mockedDbConnection.close();
    }

    @Test
    @DisplayName("Test if balance is updating on Cash In")
    void testCashIn() throws SQLException {
        // Given
        when(connection.prepareStatement("SELECT id FROM users WHERE id = ?")).thenReturn(checkUserPs);
        when(checkUserPs.executeQuery()).thenReturn(userRs);
        when(userRs.next()).thenReturn(true);

        when(connection.prepareStatement("SELECT id, amount, user_id FROM balance WHERE user_id = ?")).thenReturn(checkBalancePs);
        when(checkBalancePs.executeQuery()).thenReturn(balanceRs);
        when(balanceRs.next()).thenReturn(true); // User has an existing balance record

        when(connection.prepareStatement("UPDATE balance SET amount = amount + ? WHERE user_id = ?")).thenReturn(updateBalancePs);
        when(updateBalancePs.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement("INSERT INTO transaction (amount, name, account_id, date) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)).thenReturn(insertTransactionPs);
        when(insertTransactionPs.executeUpdate()).thenReturn(1);

        // When
        boolean result = TransactionService.cashin(100.0, 1);

        // Then
        assertTrue(result);
        verify(connection).commit();
    }

    @Test
    @DisplayName("Test if balance is updating for two users on Cash Transfer")
    void testCashTransfer() throws SQLException {
        // Given
        when(connection.prepareStatement("SELECT id FROM users WHERE id IN (?, ?)")).thenReturn(checkUsersPs);
        when(checkUsersPs.executeQuery()).thenReturn(usersRs);
        when(usersRs.next()).thenReturn(true, true, false); // Both users exist

        when(connection.prepareStatement("SELECT amount FROM balance WHERE user_id = ?")).thenReturn(checkBalancePs);
        when(checkBalancePs.executeQuery()).thenReturn(balanceRs);
        when(balanceRs.next()).thenReturn(true); // Sender has balance
        when(balanceRs.getDouble("amount")).thenReturn(1000.0);

        when(connection.prepareStatement("UPDATE balance SET amount = amount - ? WHERE user_id = ?")).thenReturn(updateBalancePs);
        when(updateBalancePs.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement("SELECT id FROM balance WHERE user_id = ?")).thenReturn(checkRecipientPs);
        when(checkRecipientPs.executeQuery()).thenReturn(recipientRs);
        when(recipientRs.next()).thenReturn(false); // Recipient has no balance record

        when(connection.prepareStatement("INSERT INTO balance (amount, user_id) VALUES (?, ?)")).thenReturn(createBalancePs);
        when(createBalancePs.executeUpdate()).thenReturn(1);

        when(connection.prepareStatement("INSERT INTO transaction (amount, name, account_id, date, transferToID, transferFromID) " +
                "VALUES (?, ?, ?, ?, ?, ?)")).thenReturn(senderTransactionPs).thenReturn(recipientTransactionPs);
        when(senderTransactionPs.executeUpdate()).thenReturn(1);
        when(recipientTransactionPs.executeUpdate()).thenReturn(1);

        // When
        int result = TransactionService.cashTransfer(500.0, 1, 2);

        // Then
        assertEquals(0, result);
        verify(connection).commit();
    }

    @Test
    @DisplayName("Test if transactions are displayed properly")
    void testViewAllTransactions() throws SQLException {
        // Given
        // Moved statement stub here
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(userRs);
        when(userRs.next()).thenReturn(true, true, false);
        when(userRs.getInt("id")).thenReturn(101, 102);
        when(userRs.getDouble("amount")).thenReturn(500.0, -200.0);
        when(userRs.getString("name")).thenReturn("Cash In", "Transfer to User #2");
        when(userRs.getInt("account_id")).thenReturn(1);
        when(userRs.getTimestamp("date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(userRs.getObject("transferToID", Integer.class)).thenReturn(null, 2);
        when(userRs.getObject("transferFromID", Integer.class)).thenReturn(null, 1);

        // When
        List<Transactions> transactions = TransactionService.viewAll();

        // Then
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        assertEquals(101, transactions.get(0).getId());
        assertEquals("Cash In", transactions.get(0).getName());
        assertEquals(102, transactions.get(1).getId());
        assertEquals("Transfer to User #2", transactions.get(1).getName());
    }
}