package com.gcash.app.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gcash.app.Model.Cashin;
import com.gcash.app.Model.CashTransfer;
import com.gcash.app.Model.CheckBalance;
import com.gcash.app.Model.Transactions;
import com.gcash.app.util.DatabaseConnection;

public class TransactionService {

    /**
     * Add cash to user's account and record transaction
     * @param amount Amount to add
     * @param userId User ID
     * @return true if successful, false otherwise
     */
    public static boolean cashin(double amount, int userId) {
        if (amount <= 0 || userId <= 0) {
            System.err.println("Invalid amount or user ID");
            return false;
        }

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Check if user exists and has a balance record
            CheckBalance currentBalance = null;

            try (PreparedStatement checkUser = conn.prepareStatement(
                    "SELECT id FROM users WHERE id = ?")) {
                checkUser.setInt(1, userId);
                ResultSet rs = checkUser.executeQuery();

                if (!rs.next()) {
                    System.err.println("User ID not found: " + userId);
                    return false;
                }
            }

            // 2. Check if balance record exists, if not create one
            try (PreparedStatement checkBalance = conn.prepareStatement(
                    "SELECT id, amount, user_id FROM balance WHERE user_id = ?")) {
                checkBalance.setInt(1, userId);
                ResultSet rs = checkBalance.executeQuery();

                if (rs.next()) {
                    currentBalance = new CheckBalance(
                            rs.getInt("id"),
                            rs.getDouble("amount"),
                            rs.getInt("user_id")
                    );
                } else {
                    // Create new balance record
                    try (PreparedStatement createBalance = conn.prepareStatement(
                            "INSERT INTO balance (amount, user_id) VALUES (0, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        createBalance.setInt(1, userId);
                        createBalance.executeUpdate();

                        ResultSet generatedKeys = createBalance.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            currentBalance = new CheckBalance(
                                    generatedKeys.getInt(1),
                                    0,
                                    userId
                            );
                        } else {
                            throw new SQLException("Creating balance failed, no ID obtained.");
                        }
                    }
                }
            }

            // 3. Update balance
            try (PreparedStatement updateBalance = conn.prepareStatement(
                    "UPDATE balance SET amount = amount + ? WHERE user_id = ?")) {
                updateBalance.setDouble(1, amount);
                updateBalance.setInt(2, userId);
                updateBalance.executeUpdate();
            }

            // 4. Record transaction
            try (PreparedStatement insertTransaction = conn.prepareStatement(
                    "INSERT INTO transaction (amount, name, account_id, date) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                insertTransaction.setDouble(1, amount);
                insertTransaction.setString(2, "Cash In");
                insertTransaction.setInt(3, userId);
                insertTransaction.setObject(4, LocalDateTime.now());
                insertTransaction.executeUpdate();
            }

            // Commit transaction
            conn.commit();
            System.out.println("Successfully added " + amount + " to user ID " + userId);
            return true;

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Cash-in error: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Transfer money from one user to another
     * @param amount Amount to transfer
     * @param fromUserId User ID sending the money
     * @param toUserId User ID receiving the money
     * @return Error code: 0=success, 1=insufficient funds, 2=invalid user, 3=same user, 4=database error
     */
    public static int cashTransfer(double amount, int fromUserId, int toUserId) {
        // Validate input
        if (amount <= 0) {
            System.err.println("Invalid transfer amount");
            return 1; // Error: Invalid amount
        }

        if (fromUserId <= 0 || toUserId <= 0) {
            System.err.println("Invalid user ID");
            return 2; // Error: Invalid user
        }

        if (fromUserId == toUserId) {
            System.err.println("Cannot transfer to yourself");
            return 3; // Error: Same user
        }

        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Check if both users exist
            try (PreparedStatement checkUsers = conn.prepareStatement(
                    "SELECT id FROM users WHERE id IN (?, ?)")) {
                checkUsers.setInt(1, fromUserId);
                checkUsers.setInt(2, toUserId);
                ResultSet rs = checkUsers.executeQuery();

                int userCount = 0;
                while (rs.next()) {
                    userCount++;
                }

                if (userCount < 2) {
                    System.err.println("One or both users do not exist");
                    return 2; // Error: Invalid user
                }
            }

            // 2. Check if sender has sufficient balance
            double senderBalance = 0;

            try (PreparedStatement checkBalance = conn.prepareStatement(
                    "SELECT amount FROM balance WHERE user_id = ?")) {
                checkBalance.setInt(1, fromUserId);
                ResultSet rs = checkBalance.executeQuery();

                if (rs.next()) {
                    senderBalance = rs.getDouble("amount");
                } else {
                    // User has no balance record yet
                    senderBalance = 0;
                }
            }

            if (senderBalance < amount) {
                System.err.println("Insufficient balance for transfer");
                return 1; // Error: Insufficient funds
            }

            // 3. Deduct from sender's balance
            try (PreparedStatement updateSender = conn.prepareStatement(
                    "UPDATE balance SET amount = amount - ? WHERE user_id = ?")) {
                updateSender.setDouble(1, amount);
                updateSender.setInt(2, fromUserId);
                updateSender.executeUpdate();
            }

            // 4. Add to recipient's balance (create record if doesn't exist)
            try (PreparedStatement checkRecipient = conn.prepareStatement(
                    "SELECT id FROM balance WHERE user_id = ?")) {
                checkRecipient.setInt(1, toUserId);
                ResultSet rs = checkRecipient.executeQuery();

                if (rs.next()) {
                    // Update existing balance
                    try (PreparedStatement updateRecipient = conn.prepareStatement(
                            "UPDATE balance SET amount = amount + ? WHERE user_id = ?")) {
                        updateRecipient.setDouble(1, amount);
                        updateRecipient.setInt(2, toUserId);
                        updateRecipient.executeUpdate();
                    }
                } else {
                    // Create new balance record
                    try (PreparedStatement createBalance = conn.prepareStatement(
                            "INSERT INTO balance (amount, user_id) VALUES (?, ?)")) {
                        createBalance.setDouble(1, amount);
                        createBalance.setInt(2, toUserId);
                        createBalance.executeUpdate();
                    }
                }
            }

            LocalDateTime now = LocalDateTime.now();

            // 5. Record transaction for sender (debit)
            try (PreparedStatement senderTransaction = conn.prepareStatement(
                    "INSERT INTO transaction (amount, name, account_id, date, transferToID, transferFromID) " +
                            "VALUES (?, ?, ?, ?, ?, ?)")) {
                senderTransaction.setDouble(1, -amount); // Negative amount for sender
                senderTransaction.setString(2, "Transfer to User #" + toUserId);
                senderTransaction.setInt(3, fromUserId);
                senderTransaction.setObject(4, now);
                senderTransaction.setInt(5, toUserId);
                senderTransaction.setInt(6, fromUserId);
                senderTransaction.executeUpdate();
            }

            // 6. Record transaction for recipient (credit)
            try (PreparedStatement recipientTransaction = conn.prepareStatement(
                    "INSERT INTO transaction (amount, name, account_id, date, transferToID, transferFromID) " +
                            "VALUES (?, ?, ?, ?, ?, ?)")) {
                recipientTransaction.setDouble(1, amount); // Positive amount for recipient
                recipientTransaction.setString(2, "Transfer from User #" + fromUserId);
                recipientTransaction.setInt(3, toUserId);
                recipientTransaction.setObject(4, now);
                recipientTransaction.setInt(5, toUserId);
                recipientTransaction.setInt(6, fromUserId);
                recipientTransaction.executeUpdate();
            }

            // Commit transaction
            conn.commit();
            System.out.println("Successfully transferred " + amount + " from user " + fromUserId + " to user " + toUserId);
            return 0; // Success

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Transfer error: " + e.getMessage());
            return 4; // Error: Database error
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * View all transactions in the system
     * @return List of all transactions
     */
    public static List<Transactions> viewAll() {
        List<Transactions> transactionList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transaction ORDER BY date DESC")) {

            while (rs.next()) {
                Transactions transaction = mapResultSetToTransaction(rs);
                transactionList.add(transaction);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving transactions: " + e.getMessage());
        }

        return transactionList;
    }

    /**
     * View all transactions for a specific user
     * @param userId User ID to get transactions for
     * @return List of transactions for the specified user
     */
    public static List<Transactions> viewUserAll(int userId) {
        if (userId <= 0) {
            System.err.println("Invalid user ID");
            return new ArrayList<>();
        }

        List<Transactions> transactionList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM transaction WHERE account_id = ? ORDER BY date DESC")) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transactions transaction = mapResultSetToTransaction(rs);
                    transactionList.add(transaction);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving user transactions: " + e.getMessage());
        }

        return transactionList;
    }

    /**
     * View a specific transaction by ID
     * @param transactionId Transaction ID to retrieve
     * @return The transaction or null if not found
     */
    public static Transactions viewTransaction(int transactionId) {
        if (transactionId <= 0) {
            System.err.println("Invalid transaction ID");
            return null;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM transaction WHERE id = ?")) {

            pstmt.setInt(1, transactionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving transaction: " + e.getMessage());
        }

        return null;
    }

    /**
     * Helper method to map ResultSet to Transaction object
     * @param rs ResultSet containing transaction data
     * @return Mapped Transaction object
     * @throws SQLException If a database error occurs
     */
    private static Transactions mapResultSetToTransaction(ResultSet rs) throws SQLException {
        LocalDateTime date = rs.getTimestamp("date").toLocalDateTime();

        Integer transferToID = rs.getObject("transferToID", Integer.class);
        Integer transferFromID = rs.getObject("transferFromID", Integer.class);

        return new Transactions(
                rs.getInt("id"),
                rs.getDouble("amount"),
                rs.getString("name"),
                rs.getInt("account_id"),
                date,
                transferToID,
                transferFromID
        );
    }
}