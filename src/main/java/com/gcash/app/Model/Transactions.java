package com.gcash.app.Model;

import java.time.LocalDateTime;

public class Transactions {
    private int id;
    private double amount;
    private String name;
    private int account_id;
    private LocalDateTime date;
    private Integer transferToID;
    private Integer transferFromID;

    // Constructors
    public Transactions() {}

    public Transactions(int id, double amount, String name, int account_id, LocalDateTime date,
                        Integer transferToID, Integer transferFromID) {
        this.id = id;
        this.amount = amount;
        this.name = name;
        this.account_id = account_id;
        this.date = date;
        this.transferToID = transferToID;
        this.transferFromID = transferFromID;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAccount_id() {
        return account_id;
    }

    public void setAccount_id(int account_id) {
        this.account_id = account_id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Integer getTransferToID() {
        return transferToID;
    }

    public void setTransferToID(Integer transferToID) {
        this.transferToID = transferToID;
    }

    public Integer getTransferFromID() {
        return transferFromID;
    }

    public void setTransferFromID(Integer transferFromID) {
        this.transferFromID = transferFromID;
    }

    @Override
    public String toString() {
        String transactionType;
        if (name.startsWith("Cash In")) {
            transactionType = "CASH IN";
        } else if (name.startsWith("Transfer to")) {
            transactionType = "TRANSFER OUT";
        } else if (name.startsWith("Transfer from")) {
            transactionType = "TRANSFER IN";
        } else {
            transactionType = "TRANSACTION";
        }

        String amountStr = String.format("%.2f", Math.abs(amount));
        if (amount >= 0) {
            amountStr = "+" + amountStr;
        } else {
            amountStr = "-" + amountStr;
        }

        return String.format("[%s] ID: %d | %s | ₱%s | %s",
                date.toString(), id, transactionType, amountStr, name);
    }
}