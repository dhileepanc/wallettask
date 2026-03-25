package com.app.wallettask.data;

public class Transaction {
    private String id;
    private String userId;
    private String type;
    private String method;
    private String description;
    private double amount;
    private long timestamp;

    public Transaction() {}

    public Transaction(String id, String userId, String type, String method, String description, double amount, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.method = method;
        this.description = description;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
