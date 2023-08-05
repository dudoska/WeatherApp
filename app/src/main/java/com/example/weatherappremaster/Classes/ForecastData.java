package com.example.weatherappremaster.Classes;

public class ForecastData {
    private String data;
    private String avgtemp;
    private String icon;

    public ForecastData(String data, String avgtemp, String icon) {
        this.data = data;
        this.avgtemp = avgtemp;
        this.icon = icon;
    }

    public String getData() {
        return data;
    }

    public String getAvgtemp() {
        return avgtemp;
    }

    public String getIcon() {
        return icon;
    }
}
