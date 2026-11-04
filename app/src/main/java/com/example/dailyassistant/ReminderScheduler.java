package com.example.dailyassistant;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReminderScheduler {

    private static final String TAG = "DailyLog";

    public static void scheduleReminder(Context context, Task task) {
        Log.d(TAG, "scheduleReminder called for task: " + (task != null ? task.getTitle() : "null"));

        if (task == null || !task.isReminderActive() || task.getTime() == null || task.getTime().isEmpty()) {
            Log.d(TAG, "Scheduling skipped: Task is null, reminder is inactive, or time is empty.");
            return;
        }

        try {
            Calendar calendar = Calendar.getInstance();

            // ۱. پارس کردن تاریخ تسک
            if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.US);
                Calendar taskDate = Calendar.getInstance();
                taskDate.setTime(dateFormat.parse(task.getDueDate()));

                calendar.set(Calendar.YEAR, taskDate.get(Calendar.YEAR));
                calendar.set(Calendar.MONTH, taskDate.get(Calendar.MONTH));
                calendar.set(Calendar.DAY_OF_MONTH, taskDate.get(Calendar.DAY_OF_MONTH));
                Log.d(TAG, "Task Date set to: " + task.getDueDate());
            }

            // ۲. پارس کردن ساعت Reminder (استفاده از getTime)
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
            Calendar time = Calendar.getInstance();
            time.setTime(timeFormat.parse(task.getTime()));

            calendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Log.d(TAG, "Alarm target time calculated: " + calendar.getTime().toString());

            // ۳. اگر زمان محاسبه شده گذشته است، برای فردا تنظیم شود
            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                Log.d(TAG, "Target time was in the past. Date adjusted to tomorrow.");
            }

            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("task_id", task.getId());
            intent.putExtra("task_title", task.getTitle());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    task.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (alarmManager != null) {
                // بررسی دسترسی ثبت آلارم دقیق در اندروید ۱۲ به بالا
                boolean canScheduleExact = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    canScheduleExact = alarmManager.canScheduleExactAlarms();
                }

                if (canScheduleExact) {
                    // ثبت آلارم به صورت دقیق و بیدار کردن گوشی حتی در وضعیت Doze
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.d(TAG, "SUCCESS: Exact Alarm scheduled successfully for task: " + task.getTitle());
                } else {
                    // ثبت آلارم به عنوان راه حل جایگزین (Fallback) غیر دقیق جهت جلوگیری از کرش
                    alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            pendingIntent
                    );
                    Log.w(TAG, "WARNING: exact alarm permission missing. Fallback to inexact alarm for task: " + task.getTitle());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "ERROR in scheduleReminder: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void cancelReminder(Context context, Task task) {
        if (task == null) return;

        Log.d(TAG, "cancelReminder called for task: " + task.getTitle());

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                task.getId().hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "SUCCESS: Alarm cancelled for task: " + task.getTitle());
        }
    }
}
