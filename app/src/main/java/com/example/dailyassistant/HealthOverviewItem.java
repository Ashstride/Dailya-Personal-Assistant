package com.example.dailyassistant;

public class HealthOverviewItem {

    public static final int TYPE_WATER = 0;
    public static final int TYPE_SLEEP = 1;


    private final int type;

    private final String title;

    private final String value;

    private final String unit;

    private final int progress;


    public HealthOverviewItem(
            int type,
            String title,
            String value,
            String unit,
            int progress
    ) {

        this.type = type;
        this.title = title;
        this.value = value;
        this.unit = unit;
        this.progress = progress;

    }



    public int getType() {

        return type;

    }



    public String getTitle() {

        return title;

    }



    public String getValue() {

        return value;

    }



    public String getUnit() {

        return unit;

    }



    public int getProgress() {

        return progress;

    }

}