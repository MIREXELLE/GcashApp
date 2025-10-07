package com.gcash.app.util;

public class ErrorHandler {
    public static final int ERROR_INVALID_INPUT = 1001;
    public static final int ERROR_DATABASE_CONNECTION = 1002;
    public static final int ERROR_AUTHENTICATION_FAILED = 1003;
    public static final int ERROR_PIN_CHANGE_FAILED = 1004;

    public static String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case ERROR_INVALID_INPUT:
                return "Invalid input. Please check your information and try again.";
            case ERROR_DATABASE_CONNECTION:
                return "Database connection error. Please try again later.";
            case ERROR_AUTHENTICATION_FAILED:
                return "Authentication failed. Invalid credentials.";
            case ERROR_PIN_CHANGE_FAILED:
                return "Failed to change PIN. Please try again.";
            default:
                return "An unexpected error occurred. Please try again.";
        }
    }
}