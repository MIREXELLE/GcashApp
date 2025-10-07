package com.gcash.app;

import com.gcash.app.Model.Users;
import com.gcash.app.Security.UserAuthentication;

import java.util.Scanner;

public class GcashApp {
    private static final UserAuthentication auth = new UserAuthentication();
    private static final Scanner scanner = new Scanner(System.in);
    private static int currentUserId = -1;

    public static void main(String[] args) {
        System.out.println("Welcome to Gcash App!");

        while (true) {
            if (currentUserId == -1) {
                // Not logged in
                showMainMenu();
            } else {
                // Logged in
                showLoggedInMenu();
            }
        }
    }
    private  static void showMainMenu(){
        System.out.println("\nPlease select an option: ");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.println("Choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                registerUser();
                break;
            case 2:
                loginUser();
                break;
            case 3:
                System.out.println("Thank you for using GCash App. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    private static void showLoggedInMenu(){
        System.out.println("\nUser Menu (ID: " + currentUserId + ")");
        System.out.println("1. Change PIN");
        System.out.println("2. Logout");
        System.out.println("3. Exit");
        System.out.print("Choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // Clear the newline

        switch (choice) {
            case 1:
                changeUserPin();
                break;
            case 2:
                logoutUser();
                break;
            case 3:
                System.out.println("Thank you for using GCash App. Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }
    private static void registerUser() {
        System.out.println("\n=== User Registration ===");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Phone Number: ");
        String number = scanner.nextLine();

        System.out.print("PIN (4-6 digits): ");
        String pin = scanner.nextLine();

        Users newUser = new Users(name, email, number, pin);
        int userId = auth.register(newUser);

        if (userId != -1) {
            System.out.println("Registration successful! Your user ID is: " + userId);
        } else {
            System.out.println("Registration failed. Please check your information and try again.");
        }
    }

    private static void loginUser() {
        System.out.println("\n=== User Login ===");

        System.out.print("Email or Phone Number: ");
        String identifier = scanner.nextLine();

        System.out.print("PIN: ");
        String pin = scanner.nextLine();

        int userId = auth.login(identifier, pin);

        if (userId != -1) {
            currentUserId = userId;
            System.out.println("Login successful! Welcome back.");
        } else {
            System.out.println("Login failed. Invalid credentials or user not found.");
        }
    }

    private static void changeUserPin() {
        System.out.println("\n=== Change PIN ===");

        System.out.print("Current PIN: ");
        String oldPin = scanner.nextLine();

        System.out.print("New PIN (4-6 digits): ");
        String newPin = scanner.nextLine();

        System.out.print("Confirm New PIN: ");
        String confirmPin = scanner.nextLine();

        if (!newPin.equals(confirmPin)) {
            System.out.println("PIN mismatch. Please try again.");
            return;
        }

        boolean success = auth.changePin(currentUserId, oldPin, newPin);

        if (success) {
            System.out.println("PIN changed successfully!");
        } else {
            System.out.println("Failed to change PIN. Please verify your current PIN is correct.");
        }
    }

    private static void logoutUser() {
        if (auth.logout(currentUserId)) {
            System.out.println("Logout successful.");
            currentUserId = -1;
        } else {
            System.out.println("Logout failed.");
        }
    }
}
