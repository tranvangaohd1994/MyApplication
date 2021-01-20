package com.google.myapplication;

public class CardDate {
    private String cardDay;
    private String cardTime;

    public CardDate(String cardDay, String cardTime) {
        this.cardDay = cardDay;
        this.cardTime = cardTime;
    }

    public String getCardDay() {
        return cardDay;
    }

    public void setCardDay(String cardDay) {
        this.cardDay = cardDay;
    }

    public String getCardTime() {
        return cardTime;
    }

    public void setCardTime(String cardTime) {
        this.cardTime = cardTime;
    }
}
