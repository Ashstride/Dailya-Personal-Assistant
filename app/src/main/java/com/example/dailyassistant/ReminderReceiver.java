package com.example.dailyassistant;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;



public class ReminderReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {



        String taskId =
                intent.getStringExtra(
                        "task_id"
                );



        String taskTitle =
                intent.getStringExtra(
                        "task_title"
                );



        if(taskTitle == null ||
                taskTitle.isEmpty()){


            taskTitle =
                    "Your task";


        }



        NotificationHelper.showTaskReminder(

                context,

                taskTitle,

                taskId

        );



    }


}