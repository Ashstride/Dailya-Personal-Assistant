package com.example.dailyassistant;

public class WeeklyHealthItem {

    public static final int TYPE_WATER = 1;
    public static final int TYPE_SLEEP = 2;

    private final String day;
    private final String date; // فرمت yyyy-MM-dd
    private final String value;
    private final int type;

    public WeeklyHealthItem(String day, String date, String value, int type) {
        this.day = day;
        this.date = date;
        this.value = value;
        this.type = type;
    }

    public String getDay() {
        return day;
    }

    public String getDate() {
        return date;
    }

    public String getValue() {
        return value;
    }

    public int getType() {
        return type;
    }
}
