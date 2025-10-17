package com.gcash.app.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gcash.app.Model.CheckBalance;
import com.gcash.app.util.DatabaseConnection;
import com.gcash.app.util.ErrorHandler;

public class BalanceService {

    /**
     * Retrieve a user's balance by their user ID
     * @param userId The ID of the user
     * @return The user's balance or null if not found
     */
    public static CheckBalance checkBalance(int userId) {
        if (userId <= 0) {
            System.err.println("Invalid user ID");
            return null;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id, amount, user_id FROM balance WHERE user_id = ?")) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new CheckBalance(
                            rs.getInt("id"),
                            rs.getDouble("amount"),
                            rs.getInt("user_id")
                    );
                } else {
                    System.err.println("No balance found for user ID: " + userId);
                    return null;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking balance: " + e.getMessage());
            return null;
        }
    }
}