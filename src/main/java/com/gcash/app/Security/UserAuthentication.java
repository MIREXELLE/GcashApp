package com.gcash.app.Security;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import com.gcash.app.Model.Users;
import com.gcash.app.util.DatabaseConnection;

public class UserAuthentication {

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    // Phone number validation pattern (simple version)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{10,15}$");

    // PIN validation pattern (4)
    private static final Pattern PIN_PATTERN =
            Pattern.compile("^\\d{4}$");

    /**
     * Register a new user
     * @param user User object containing registration information
     * @return User ID if successful, -1 if failed
     */
    public int register(Users user) {
        // Validate input fields
        if (!validateRegistration(user)) {
            return -1;
        }

        // Hash the PIN before storing
        String hashedPin = hashPin(user.getPin());
        if (hashedPin == null) {
            return -1;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO users (name, email, number, pin) VALUES (?, ?, ?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getNumber());
            pstmt.setString(4, hashedPin);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    return -1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Validate registration fields
     * @param user User to validate
     * @return true if valid, false otherwise
     */
    private boolean validateRegistration(Users user) {
        // Check for null values
        if (user.getName() == null || user.getEmail() == null ||
                user.getNumber() == null || user.getPin() == null) {
            return false;
        }

        // Check for empty values
        if (user.getName().trim().isEmpty() || user.getEmail().trim().isEmpty() ||
                user.getNumber().trim().isEmpty() || user.getPin().trim().isEmpty()) {
            return false;
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            return false;
        }

        // Validate phone number format
        if (!PHONE_PATTERN.matcher(user.getNumber()).matches()) {
            return false;
        }

        // Validate PIN format
        if (!PIN_PATTERN.matcher(user.getPin()).matches()) {
            return false;
        }

        return true;
    }

    /**
     * Login user with number/email and PIN
     * @param userIdentifier Email or phone number
     * @param pin PIN code
     * @return User ID if successful, -1 if failed
     */
    public int login(String userIdentifier, String pin) {
        // Check for null values
        if (userIdentifier == null || pin == null) {
            return -1;
        }

        // Check for empty values
        if (userIdentifier.trim().isEmpty() || pin.trim().isEmpty()) {
            return -1;
        }

        // Validate PIN format
        if (!PIN_PATTERN.matcher(pin).matches()) {
            return -1;
        }

        String hashedPin = hashPin(pin);
        if (hashedPin == null) {
            return -1;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT id FROM users WHERE (email = ? OR number = ?) AND pin = ?")) {

            pstmt.setString(1, userIdentifier);
            pstmt.setString(2, userIdentifier);
            pstmt.setString(3, hashedPin);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    // Create a session for the user
                    SessionManager.createSession(userId);
                    return userId;
                } else {
                    return -1; // Login failed
                }
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Change user's PIN
     * @param userId User ID
     * @param oldPin Current PIN
     * @param newPin New PIN
     * @return true if successful, false otherwise
     */
    public boolean changePin(int userId, String oldPin, String newPin) {
        // Validate input
        if (userId <= 0 || oldPin == null || newPin == null) {
            return false;
        }

        if (oldPin.trim().isEmpty() || newPin.trim().isEmpty()) {
            return false;
        }

        // Validate PIN format
        if (!PIN_PATTERN.matcher(oldPin).matches() || !PIN_PATTERN.matcher(newPin).matches()) {
            return false;
        }

        String hashedOldPin = hashPin(oldPin);
        String hashedNewPin = hashPin(newPin);

        if (hashedOldPin == null || hashedNewPin == null) {
            return false;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement verifyStmt = conn.prepareStatement(
                     "SELECT id FROM users WHERE id = ? AND pin = ?");
             PreparedStatement updateStmt = conn.prepareStatement(
                     "UPDATE users SET pin = ? WHERE id = ?")) {

            // First verify the old PIN
            verifyStmt.setInt(1, userId);
            verifyStmt.setString(2, hashedOldPin);

            ResultSet rs = verifyStmt.executeQuery();
            if (!rs.next()) {
                return false; // Old PIN doesn't match
            }

            // Update to new PIN
            updateStmt.setString(1, hashedNewPin);
            updateStmt.setInt(2, userId);

            int affectedRows = updateStmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Change PIN error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Logout the user by invalidating their session
     * @param userId User ID
     * @return true if successful, false otherwise
     */
    public boolean logout(int userId) {
        return SessionManager.invalidateSession(userId);
    }

    /**
     * Hash PIN using SHA-256 algorithm
     * @param pin PIN to hash
     * @return Hashed PIN or null if error
     */
    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Hashing error: " + e.getMessage());
            return null;
        }
    }
}