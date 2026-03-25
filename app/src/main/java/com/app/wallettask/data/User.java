package com.app.wallettask.data;

public class User {
    private String uid;
    private String email;
    private double balance;
    private String qrCodeData;

    public User() {}

    public User(String uid, String email, double balance, String qrCodeData) {
        this.uid = uid;
        this.email = email;
        this.balance = balance;
        this.qrCodeData = qrCodeData;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }
}
