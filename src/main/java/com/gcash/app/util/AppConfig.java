package com.gcash.app.util;

public class AppConfig {
    // Database configuration
    public static final String DB_NAME = "gcashdatabase";
    public static final String DB_URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    public static final String DB_USER = "root";  // Change to your database username
    public static final String DB_PASSWORD = "#Xelle111406";  // Change to your database password

    // Security settings
    public static final int MAX_LOGIN_ATTEMPTS = 1;
    public static final int SESSION_TIMEOUT_MINUTES = 30;

    // PIN requirements
    public static final int MIN_PIN_LENGTH = 4;
    public static final int MAX_PIN_LENGTH = 4;
}