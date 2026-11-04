package com.example.dailyassistant;

public class CalendarDayItem {
    private final String dayName;
    private final String dayNumber;
    private final String fullDate;
    private final boolean inCurrentMonth;
    private final boolean today;
    private final boolean hasDiary;

    public CalendarDayItem(String dayName, String dayNumber, String fullDate,
                           boolean inCurrentMonth, boolean today, boolean hasDiary) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.fullDate = fullDate;
        this.inCurrentMonth = inCurrentMonth;
        this.today = today;
        this.hasDiary = hasDiary;
    }

    public CalendarDayItem(String dayName, String dayNumber, String fullDate) {
        this(dayName, dayNumber, fullDate, true, false, false);
    }

    public String getDate() {
        return fullDate;
    }

    public String getDayName() {
        return dayName;
    }

    public String getDayNumber() {
        return dayNumber;
    }

    public String getFullDate() {
        return fullDate;
    }

    public boolean isInCurrentMonth() {
        return inCurrentMonth;
    }

    public boolean isToday() {
        return today;
    }

    public boolean isHasDiary() {
        return hasDiary;
    }
}
