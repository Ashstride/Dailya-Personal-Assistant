package com.example.dailyassistant;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;



public class NotificationHelper {


    private static final String CHANNEL_ID =
            "task_reminder_channel";





    public static void createNotificationChannel(
            Context context
    ){


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){


            NotificationChannel channel =
                    new NotificationChannel(

                            CHANNEL_ID,

                            "Task Reminders",

                            NotificationManager.IMPORTANCE_HIGH

                    );



            channel.setDescription(
                    "Daily Assistant task reminders"
            );



            NotificationManager manager =
                    context.getSystemService(
                            NotificationManager.class
                    );



            if(manager != null){

                manager.createNotificationChannel(channel);

            }


        }


    }









    public static void showTaskReminder(

            Context context,

            String taskTitle,

            String taskId

    ){



        createNotificationChannel(context);




        // ===============================
        // Open App when clicking notification
        // ===============================


        Intent openIntent =
                new Intent(
                        context,
                        DashboardActivity.class
                );


        openIntent.putExtra(
                "open_task",
                true
        );



        PendingIntent openPendingIntent =
                PendingIntent.getActivity(

                        context,

                        taskId.hashCode(),

                        openIntent,

                        PendingIntent.FLAG_UPDATE_CURRENT
                                |
                                PendingIntent.FLAG_IMMUTABLE

                );








        // ===============================
        // DONE ACTION
        // ===============================


        Intent doneIntent =
                new Intent(

                        context,
                        NotificationActionReceiver.class

                );


        doneIntent.setAction(
                "TASK_DONE"
        );


        doneIntent.putExtra(
                "task_id",
                taskId
        );


        PendingIntent donePendingIntent =
                PendingIntent.getBroadcast(

                        context,

                        taskId.hashCode()+1,

                        doneIntent,

                        PendingIntent.FLAG_UPDATE_CURRENT
                                |
                                PendingIntent.FLAG_IMMUTABLE

                );








        // ===============================
        // LATER ACTION
        // ===============================


        Intent laterIntent =
                new Intent(

                        context,
                        NotificationActionReceiver.class

                );


        laterIntent.setAction(
                "TASK_LATER"
        );


        laterIntent.putExtra(
                "task_id",
                taskId
        );


        laterIntent.putExtra(
                "task_title",
                taskTitle
        );



        PendingIntent laterPendingIntent =
                PendingIntent.getBroadcast(

                        context,

                        taskId.hashCode()+2,

                        laterIntent,

                        PendingIntent.FLAG_UPDATE_CURRENT
                                |
                                PendingIntent.FLAG_IMMUTABLE

                );









        NotificationCompat.Builder builder =

                new NotificationCompat.Builder(

                        context,

                        CHANNEL_ID

                )

                        .setSmallIcon(
                                R.drawable.logo
                        )


                        .setContentTitle(
                                "Daily Assistant 🌿"
                        )


                        .setContentText(
                                "Time for: " + taskTitle
                        )


                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )


                        .setAutoCancel(false)


                        .setContentIntent(
                                openPendingIntent
                        )


                        .addAction(

                                R.drawable.ic_check,

                                "Done",

                                donePendingIntent

                        )


                        .addAction(

                                R.drawable.ic_clock,

                                "Later",

                                laterPendingIntent

                        );








        NotificationManagerCompat manager =

                NotificationManagerCompat.from(
                        context
                );




        // Android 13 Permission Check

        // Android 13 Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // این لاگ را اضافه کن تا اگر کار نکرد، در Logcat ببینی چرا
                android.util.Log.e("NotificationHelper", "Permission POST_NOTIFICATIONS is NOT granted!");
                return;
            }
        }

            // اضافه کردن بررسی برای taskId که نال نباشد (برای جلوگیری از کرش)
        if (taskId == null) {
            android.util.Log.e("NotificationHelper", "taskId is null, cannot notify");
            return;
        }

        manager.notify(taskId.hashCode(), builder.build());





        manager.notify(

                taskId.hashCode(),

                builder.build()

        );


    }








    public static void cancelNotification(

            Context context,

            int id

    ){


        NotificationManagerCompat manager =

                NotificationManagerCompat.from(
                        context
                );


        manager.cancel(id);


    }



}