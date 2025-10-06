package com.gcash.app.Security;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import com.gcash.app.Model.Users;
import com.gcash.app.util.DatabaseConnection;

public class UserAuthentication {
    private final Pattern EMAIL_PATTERN =
            Pattern.compile("[A-Z0-9._%+-] + @[A-Z]+.[A-Z]$", Pattern.CASE_INSENSITIVE);
    private final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{10,15}$");
    private final Pattern PIN_PATTERN =
            Pattern.compile("\\d{4,6}$");
    //Register new User

    public int register(Users user) {
        if (!validateRegistration(user)) {
            return -1;
        }

        String hashedPin = hashPin(user.getPin());
        if (hashedPin == null) {
            return -1;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(
                     "INSERT INTO users (name, email, number, pin) VALUES (?,?,?,?)",
                     PreparedStatement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getName());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getNumber());
            pst.setString(4, hashedPin);

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                return -1;
            }
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
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
    //Check Registration
    private boolean validateRegistration(Users user) {
        
        if (user.getName() == null || user.getEmail() == null ||
            user.getNumber() == null || user.getPin() == null) {
            return false;
        }

        if (user.getName().trim().isEmpty() || user.getEmail().trim().isEmpty() ||
                user.getNumber().trim().isEmpty() || user.getPin().trim().isEmpty()) {
            return false;
        }
        
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()){
            return false;
        }

        if (!PHONE_PATTERN.matcher(user.getNumber()).matches()){
            return false;
        }

        if (!PIN_PATTERN.matcher(user.getPin()).matches()){
            return false;
        }

        return true;
    }
    //Login
    public int login(String userIdentifier, String pin) {
        if (userIdentifier == null || pin == null){
            return -1;
        }
        if (userIdentifier.trim().isEmpty()|| pin.trim().isEmpty()){
            return -1;
        }
        if (!PIN_PATTERN.matcher(pin).matches()){
            return -1;
        }

        String hashedPin = hashPin(pin);
        if (hashedPin == null){
            return -1;
        }

        try(Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    "SELECT id FROM users WHERE (email = ? OR number = ?) AND pin = ?"
            )){

            pst.setString(1, userIdentifier);
            pst.setString(2, userIdentifier);
            pst.setString(3,hashedPin);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else  {
                    return -1;
                }
            }
        }catch(SQLException e){
            System.err.println("Login error: " + e.getMessage());
            return -1;
        }
    }

    //Change Pin


    //Logouts
    //yes
    private String hashPin(String pin) {
        return null;
    }
}
