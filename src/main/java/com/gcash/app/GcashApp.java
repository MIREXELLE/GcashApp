package com.gcash.app;

import com.gcash.app.Security.UserAuthentication;

import java.util.Scanner;
import java.sql.*;

public class GcashApp {
    private static UserAuthentication authentication;
    private static Scanner scan = new Scanner(System.in);
    private static int currentUserId = 0;

    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/login_schema",
                "root",
            "#Xelle111406"
        );

        }
    }

