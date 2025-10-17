package com.gcash.app;

import java.util.List;
import java.util.Scanner;

import com.gcash.app.Model.Users;
import com.gcash.app.Model.CheckBalance;
import com.gcash.app.Model.Transactions;
import com.gcash.app.Security.UserAuthentication;
import com.gcash.app.Security.SessionManager;
import com.gcash.app.Service.TransactionService;
import com.gcash.app.Service.BalanceService;

/**
 * GcashApp - A simple banking application
 * Integrates all features: user authentication, balance checking, cash-in,
 * fund transfers, and transaction history
 */
public class GcashApp {
    private static final UserAuthentication auth = new UserAuthentication();
    private static final Scanner scanner = new Scanner(System.in);
    private static int currentUserId = -1;

    public static void main(String[] args) {
        System.out.println("Welcome to GCash App!");
        System.out.println("---------------------");
        System.out.println("© 2025 GCash Banking Services");
        System.out.println("Version 1.0");

        // Main application loop
        while (true) {
            if (currentUserId == -1) {
                // Not logged in - show main menu
                showMainMenu();
            } else {
                // Logged in - show user menu
                showLoggedInMenu();
            }
        }
    }

    /**
     * Displays the main menu for non-logged in users
     */
    private static void showMainMenu() {
        System.out.println("\nMain Menu:");
        System.out.println("---------");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choice: ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear the newline

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    loginUser();
                    break;
                case 3:
                    System.out.println("\nThank you for using GCash App. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine(); // Clear the invalid input
        }
    }

    /**
     * Displays the menu for logged-in users with all available operations
     */
    private static void showLoggedInMenu() {
        // Verify session is still valid
        if (!SessionManager.isSessionValid(currentUserId)) {
            System.out.println("\nYour session has expired. Please login again.");
            currentUserId = -1;
            return;
        }

        System.out.println("\nUser Menu (ID: " + currentUserId + ")");
        System.out.println("------------------");
        System.out.println("1. Check Balance");
        System.out.println("2. Cash In");
        System.out.println("3. Transfer Funds");
        System.out.println("4. View Transactions");
        System.out.println("5. Change PIN");
        System.out.println("6. Logout");
        System.out.println("7. Exit");
        System.out.print("Choice: ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear the newline

            switch (choice) {
                case 1:
                    checkUserBalance();
                    break;
                case 2:
                    cashInFunds();
                    break;
                case 3:
                    transferFunds();
                    break;
                case 4:
                    viewTransactionsMenu();
                    break;
                case 5:
                    changeUserPin();
                    break;
                case 6:
                    logoutUser();
                    break;
                case 7:
                    System.out.println("\nThank you for using GCash App. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }

            // Ask if user wants another transaction
            if (currentUserId != -1) { // Only if still logged in
                System.out.print("\nWould you like another transaction? (Y/N): ");
                String anotherTransaction = scanner.nextLine();
                if (!anotherTransaction.equalsIgnoreCase("Y")) {
                    logoutUser();
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine(); // Clear the invalid input
        }
    }

    /**
     * Register a new user
     */
    private static void registerUser() {
        System.out.println("\n=== User Registration ===");

        System.out.print("Name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Phone Number: ");
        String number = scanner.nextLine();

        System.out.print("PIN (4 digits): ");
        String pin = scanner.nextLine();

        Users newUser = new Users(name, email, number, pin);
        int userId = auth.register(newUser);

        if (userId != -1) {
            System.out.println("Registration successful! Your user ID is: " + userId);
        } else {
            System.out.println("Registration failed. Please check your information and try again.");
        }
    }

    /**
     * Login a user
     */
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

    /**
     * Change user's PIN
     */
    private static void changeUserPin() {
        System.out.println("\n=== Change PIN ===");

        System.out.print("Current PIN: ");
        String oldPin = scanner.nextLine();

        System.out.print("New PIN (4 digits): ");
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

    /**
     * Check user's account balance
     */
    private static void checkUserBalance() {
        System.out.println("\n=== Check Balance ===");

        CheckBalance balance = BalanceService.checkBalance(currentUserId);

        if (balance != null) {
            System.out.println("Your current balance: ₱" + String.format("%.2f", balance.getAmount()));
        } else {
            System.out.println("Unable to retrieve your balance. Please try again later.");
        }
    }

    /**
     * Add funds to user's account
     */
    private static void cashInFunds() {
        System.out.println("\n=== Cash In ===");

        System.out.print("Enter amount to add: ");
        try {
            double amount = scanner.nextDouble();
            scanner.nextLine(); // Clear the newline

            if (amount <= 0) {
                System.out.println("Invalid amount. Please enter a positive number.");
                return;
            }

            boolean success = TransactionService.cashin(amount, currentUserId);

            if (success) {
                System.out.println("Cash-in successful! ₱" + String.format("%.2f", amount) + " has been added to your account.");
            } else {
                System.out.println("Cash-in failed. Please try again later.");
            }
        } catch (Exception e) {
            System.out.println("Invalid amount. Please enter a valid number.");
            scanner.nextLine(); // Clear invalid input
        }
    }

    /**
     * Transfer funds to another user
     */
    private static void transferFunds() {
        System.out.println("\n=== Cash Transfer ===");

        // Check balance first
        CheckBalance balance = BalanceService.checkBalance(currentUserId);
        if (balance == null) {
            System.out.println("Unable to retrieve your balance. Transfer cancelled.");
            return;
        }

        System.out.println("Your current balance: ₱" + String.format("%.2f", balance.getAmount()));

        // Get recipient user ID
        System.out.print("Enter recipient user ID: ");
        try {
            int recipientId = scanner.nextInt();
            scanner.nextLine(); // Clear the newline

            if (recipientId == currentUserId) {
                System.out.println("You cannot transfer money to yourself.");
                return;
            }

            // Get amount to transfer
            System.out.print("Enter amount to transfer: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // Clear the newline

            // Validate amount
            if (amount <= 0) {
                System.out.println("Invalid amount. Please enter a positive number.");
                return;
            }

            if (amount > balance.getAmount()) {
                System.out.println("Insufficient funds. Transfer cancelled.");
                return;
            }

            // Confirm transfer
            System.out.println("\nTransfer Details:");
            System.out.println("Recipient: User #" + recipientId);
            System.out.println("Amount: ₱" + String.format("%.2f", amount));
            System.out.print("Confirm transfer (Y/N): ");
            String confirm = scanner.nextLine();

            if (!confirm.equalsIgnoreCase("Y")) {
                System.out.println("Transfer cancelled.");
                return;
            }

            // Process transfer
            int result = TransactionService.cashTransfer(amount, currentUserId, recipientId);

            switch (result) {
                case 0:
                    System.out.println("Transfer successful! ₱" + String.format("%.2f", amount) +
                            " has been sent to User #" + recipientId);
                    break;
                case 1:
                    System.out.println("Insufficient funds. Transfer failed.");
                    break;
                case 2:
                    System.out.println("Invalid recipient. User not found.");
                    break;
                case 3:
                    System.out.println("Cannot transfer to yourself.");
                    break;
                default:
                    System.out.println("Transfer failed due to a system error. Please try again later.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter valid numbers.");
            scanner.nextLine(); // Clear invalid input
        }
    }

    /**
     * Display transaction view options
     */
    private static void viewTransactionsMenu() {
        System.out.println("\n=== View Transactions ===");

        System.out.println("1. View My Transactions");
        System.out.println("2. View Specific Transaction");
        System.out.println("3. Back");
        System.out.print("Choice: ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine(); // Clear the newline

            switch (choice) {
                case 1:
                    viewUserTransactions();
                    break;
                case 2:
                    viewSpecificTransaction();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine(); // Clear invalid input
        }
    }

    /**
     * View all transactions for the current user
     */
    private static void viewUserTransactions() {
        System.out.println("\n=== My Transactions ===");

        List<Transactions> transactions = TransactionService.viewUserAll(currentUserId);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println("Your transaction history:");
        System.out.println("------------------------");
        for (Transactions t : transactions) {
            System.out.println(t.toString());
        }
    }

    /**
     * View a specific transaction by ID
     */
    private static void viewSpecificTransaction() {
        System.out.println("\n=== View Specific Transaction ===");

        System.out.print("Enter transaction ID: ");
        try {
            int transactionId = scanner.nextInt();
            scanner.nextLine(); // Clear the newline

            Transactions transaction = TransactionService.viewTransaction(transactionId);

            if (transaction == null) {
                System.out.println("Transaction not found.");
                return;
            }

            // Check if the transaction belongs to the current user
            if (transaction.getAccount_id() != currentUserId) {
                System.out.println("You can only view your own transactions.");
                return;
            }

            System.out.println("Transaction details:");
            System.out.println("-------------------");
            System.out.println(transaction.toString());
        } catch (Exception e) {
            System.out.println("Invalid input. Please enter a valid transaction ID.");
            scanner.nextLine(); // Clear invalid input
        }
    }

    /**
     * Logout the current user
     */
    private static void logoutUser() {
        // Use SessionManager to invalidate the session
        if (SessionManager.invalidateSession(currentUserId)) {
            System.out.println("Logout successful. Thank you for using GCash App!");
            currentUserId = -1;
        } else {
            System.out.println("Logout failed.");
        }
    }
}