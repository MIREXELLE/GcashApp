package com.gcash.app.Model;


public class Users {
    private int id;
    private String name;
    private String email;
    private String number;
    private String pin;

    public Users() {}

    public Users(String pin, String number, String email, String name) {
        this.pin = pin;
        this.number = number;
        this.email = email;
        this.name = name;
    }

    public Users(int id, String name, String email, String number, String pin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.number = number;
        this.pin = pin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
