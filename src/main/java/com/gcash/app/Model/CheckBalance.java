package com.gcash.app.Model;

public class CheckBalance {
    private int id;
    private double amount;
    private int user_id;

    // Constructors
    public CheckBalance() {}

    public CheckBalance(double amount, int user_id) {
        this.amount = amount;
        this.user_id = user_id;
    }

    public CheckBalance(int id, double amount, int user_id) {
        this.id = id;
        this.amount = amount;
        this.user_id = user_id;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}