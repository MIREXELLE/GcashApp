package com.gcash.app.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import com.gcash.app.Model.Cashin;
import com.gcash.app.Model.CheckBalance;
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
}