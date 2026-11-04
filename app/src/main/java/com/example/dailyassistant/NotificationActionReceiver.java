package com.example.dailyassistant;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Calendar;


public class NotificationActionReceiver extends BroadcastReceiver {



    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {



        String action =
                intent.getAction();



        String taskId =
                intent.getStringExtra(
                        "task_id"
                );



        String taskTitle =
                intent.getStringExtra(
                        "task_title"
                );





        if(action == null ||
                taskId == null){

            return;

        }

        
        // ==========================
        // DONE
        // ==========================

        if (action.equals("TASK_DONE")) {

            TaskManagerFacade taskFacade = TaskManagerFacade.getInstance(context);
            taskFacade.updateTaskStatus(taskId, true);

            NotificationManagerHelper.cancelNotification(
                    context,
                    taskId.hashCode()
            );
        }

        // ==========================
        // LATER
        // ==========================


        else if(action.equals("TASK_LATER")){


            scheduleSnooze(

                    context,

                    taskId,

                    taskTitle

            );



            NotificationManagerHelper.cancelNotification(

                    context,

                    taskId.hashCode()

            );


        }


    }








    private void scheduleSnooze(Context context, String taskId, String taskTitle) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("task_id", taskId);
        intent.putExtra("task_title", taskTitle);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.hashCode() + 100,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);

        if (alarmManager != null) {
            try {
                // چک کردن مجوز برای اندروید 12 به بالا
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        // اینجا بهتر است لاگ بگذاری یا کاربر را هدایت کنی
                        android.util.Log.e("ReminderScheduler", "Exact alarm permission not granted for Snooze");
                    }
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } catch (SecurityException e) {
                // این دقیقا همان جایی است که مشکلت حل می‌شود
                android.util.Log.e("ReminderScheduler", "SecurityException: " + e.getMessage());
            }
        }
    }





}