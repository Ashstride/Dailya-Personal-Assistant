package com.example.dailyassistant;


import android.content.Context;
import androidx.core.app.NotificationManagerCompat;



public class NotificationManagerHelper {


    public static void cancelNotification(

            Context context,

            int id

    ){

        NotificationManagerCompat
                .from(context)
                .cancel(id);

    }


}