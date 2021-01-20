package com.google.myapplication;

public class WaterCard {
    private String waterTime;
    private int waterTemperature;

    public WaterCard(String waterTime, int waterTemperature) {
        this.waterTime = waterTime;
        this.waterTemperature = waterTemperature;
    }

    public String getWaterTime() {
        return waterTime;
    }

    public void setWaterTime(String waterTime) {
        this.waterTime = waterTime;
    }

    public int getWaterTemperature() {
        return waterTemperature;
    }

    public void setWaterTemperature(int waterTemperature) {
        this.waterTemperature = waterTemperature;
    }
}
