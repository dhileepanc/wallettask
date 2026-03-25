package com.app.wallettask.data;

public class Card {
    private String id;
    private String userId;
    private String cardNumber;
    private String holderName;
    private String expiryDate;
    private String cvv;

    public Card() {}

    public Card(String id, String userId, String cardNumber, String holderName, String expiryDate, String cvv) {
        this.id = id;
        this.userId = userId;
        this.cardNumber = cardNumber;
        this.holderName = holderName;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }
}
