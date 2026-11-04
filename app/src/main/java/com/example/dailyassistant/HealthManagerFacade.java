package com.example.dailyassistant;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class HealthManagerFacade {

    private static HealthManagerFacade instance;
    private final HealthRepository repository;

    private HealthManagerFacade(Context context) {
        repository = HealthRepository.getInstance(context);
    }

    public static synchronized HealthManagerFacade getInstance(Context context) {
        if (instance == null) {
            instance = new HealthManagerFacade(context);
        }
        return instance;
    }

    // متدهای کمکی تاریخ
    private String formatDate(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    private String formatDayName(Calendar calendar) {
        return new SimpleDateFormat("EEE", Locale.US).format(calendar.getTime());
    }

    public String getTodayDate() {
        return formatDate(Calendar.getInstance());
    }

    // ===============================
    // 💧 Water Operations
    // ===============================
    public int getWaterForDate(String date) {
        return repository.getWaterForDate(date);
    }

    public void updateWater(String date, int amount) {
        repository.updateWater(date, amount);
    }

    public void updateTodayWater(int amount) {
        repository.updateTodayWater(amount);
    }

    public int getTodayWater() {
        return repository.getTodayWater();
    }

    // ===============================
    // 🌙 Sleep Operations
    // ===============================
    public void updateSleep(String date, float hours, String start, String wake, String quality) {
        repository.updateSleep(date, hours, start, wake, quality);
    }

    // ثبت خواب برای امروز (مخصوص BottomSheet)
    public void updateSleep(float hours, String start, String wake, String quality) {
        repository.updateTodaySleep(hours, start, wake, quality);
    }

    public float getSleepHours(String date) {
        return repository.getSleepHours(date);
    }

    public String getSleepStart(String date) {
        return repository.getSleepStart(date);
    }

    public String getWakeTime(String date) {
        return repository.getWakeTime(date);
    }
    public String getSleepQuality(String date) {
        return repository.getSleepQuality(date);
    }


    // ===============================
    // 📊 Weekly Report Generators
    // ===============================
    public List<WeeklyHealthItem> getWeeklyWater(Calendar weekStartDate) {
        List<WeeklyHealthItem> list = new ArrayList<>();
        Calendar cal = (Calendar) weekStartDate.clone();

        for (int i = 0; i < 7; i++) {
            String date = formatDate(cal);
            int water = repository.getWaterForDate(date);

            list.add(new WeeklyHealthItem(
                    formatDayName(cal),
                    date,
                    water + "/8",
                    WeeklyHealthItem.TYPE_WATER
            ));

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return list;
    }


    public List<WeeklyHealthItem> getWeeklySleep(Calendar weekStartDate) {
        List<WeeklyHealthItem> list = new ArrayList<>();
        Calendar cal = (Calendar) weekStartDate.clone();

        for (int i = 0; i < 7; i++) {
            String date = formatDate(cal);
            float sleep = repository.getSleepHours(date);

            String value;

            if (sleep <= 0f) {
                value = "--";
            } else {
                int hours = (int) sleep;
                int minutes = Math.round((sleep - hours) * 60);

                if (minutes == 60) {
                    hours++;
                    minutes = 0;
                }

                if (minutes == 0) {
                    value = hours + "h";
                } else {
                    value = hours + "h " + minutes + "m";
                }
            }

            list.add(new WeeklyHealthItem(
                    formatDayName(cal),
                    date,
                    value,
                    WeeklyHealthItem.TYPE_SLEEP
            ));

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        return list;
    }

    public float getWeeklyAverageSleep(Calendar weekStartDate) {
        Calendar cal = (Calendar) weekStartDate.clone();

        float totalSleep = 0f;
        int validDays = 0;

        for (int i = 0; i < 7; i++) {
            String date = formatDate(cal);
            float sleep = repository.getSleepHours(date);

            /*
             * فقط روزهایی که واقعاً خواب ثبت شده دارند
             * وارد میانگین می‌شوند.
             */
            if (sleep > 0f) {
                totalSleep += sleep;
                validDays++;
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (validDays == 0) {
            return 0f;
        }

        return totalSleep / validDays;
    }
    public int getWeeklySleepProgressPercent(Calendar weekStartDate) {
        float avgSleep = getWeeklyAverageSleep(weekStartDate);

        if (avgSleep <= 0f) {
            return 0;
        }

        return Math.min(
                100,
                Math.round((avgSleep / 8f) * 100)
        );
    }
    public int getWeeklyWaterProgressPercent(Calendar weekStartDate) {
        Calendar cal = (Calendar) weekStartDate.clone();

        int totalWater = 0;
        int validDays = 0;

        String today = getTodayDate();

        for (int i = 0; i < 7; i++) {
            String date = formatDate(cal);

            if (date.compareTo(today) <= 0) {
                int water = repository.getWaterForDate(date);

                if (water > 0) {
                    totalWater += water;
                    validDays++;
                }
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (validDays == 0) {
            return 0;
        }

        int weeklyGoal = validDays * 8;

        return Math.min(
                100,
                Math.round((totalWater * 100f) / weeklyGoal)
        );
    }

    // ==========================================
    // 📊 متدهای اصلاح‌شده و جدید برای میانگین هفتگی
    // ==========================================

    public String getWeeklyAverageWaterText(Calendar weekStartDate) {
        Calendar cal = (Calendar) weekStartDate.clone();
        int totalWaterGlasses = 0;
        int validDays = 0;
        String today = getTodayDate();

        for (int i = 0; i < 7; i++) {
            String date = formatDate(cal);

            if (date.compareTo(today) <= 0) {
                int water = repository.getWaterForDate(date);

                if (water > 0) {
                    totalWaterGlasses += water;
                    validDays++;
                }
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        if (validDays == 0) {
            return "0.00 L";
        }

        float avgGlasses = (float) totalWaterGlasses / validDays;
        float avgLiters = avgGlasses * 0.25f;

        return String.format(Locale.US, "%.2f L", avgLiters);
    }



    public String getWeeklyAverageSleepNumber(Calendar weekStartDate) {
        float avgSleep = getWeeklyAverageSleep(weekStartDate);

        if (avgSleep <= 0f) {
            return "--";
        }

        int hours = (int) avgSleep;
        int minutes = Math.round((avgSleep - hours) * 60);

        if (minutes == 60) {
            hours++;
            minutes = 0;
        }

        if (minutes == 0) {
            return hours + "h";
        }

        return hours + "h " + minutes + "m";
    }






}
