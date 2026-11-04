package com.example.dailyassistant;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HealthRepository {

    private static final String PREF_NAME = "HealthPrefs";
    private static HealthRepository instance;
    private final SharedPreferences prefs;

    private HealthRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized HealthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new HealthRepository(context);
        }
        return instance;
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
    }

    // ===============================
    // 💧 Water Logic
    // ===============================
    public int getWaterForDate(String date) {
        return prefs.getInt(date + "_water", 0);
    }

    public void updateWater(String date, int amount) {
        prefs.edit().putInt(date + "_water", amount).apply();
    }

    public int getTodayWater() {
        return getWaterForDate(getTodayDate());
    }

    public void updateTodayWater(int amount) {
        updateWater(getTodayDate(), amount);
    }

    // ===============================
    // 🌙 Sleep Logic
    // ===============================
    public void updateSleep(String date, float hours, String start, String wake, String quality) {
        prefs.edit()
                .putFloat(date + "_sleep", hours)
                .putString(date + "_sleep_start", start)
                .putString(date + "_wake", wake)
                .putString(date + "_quality", quality)
                .apply();
    }

    public void updateTodaySleep(float hours, String start, String wake, String quality) {
        updateSleep(getTodayDate(), hours, start, wake, quality);
    }

    public float getSleepHours(String date) {
        return prefs.getFloat(date + "_sleep", 0f);
    }

    public String getSleepStart(String date) {
        return prefs.getString(date + "_sleep_start", "--");
    }

    public String getWakeTime(String date) {
        return prefs.getString(date + "_wake", "--");
    }

    public String getSleepQuality(String date) {
        return prefs.getString(date + "_quality", "Normal");
    }
}
