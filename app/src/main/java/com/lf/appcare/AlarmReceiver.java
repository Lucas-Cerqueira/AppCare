package com.lf.appcare;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


/**
 * Created by Jaison on 17/06/17.
 */

public class AlarmReceiver extends BroadcastReceiver {

    String TAG = "AlarmReceiver";
    String reminderName;
    int reminderHour, reminderMinute, reminderType;
    String typeAndLocalId;

    @Override
    public void onReceive(Context context, Intent intent) {

//        if (intent.getAction() != null && context != null) {
//            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
//                // Set the alarm here.
//                Log.d(TAG, "onReceive: BOOT_COMPLETED");
//                LocalData localData = new LocalData(context);
//                NotificationScheduler.setReminder(context, AlarmReceiver.class, localData.get_hour(), localData.get_min());
//                return;
//            }
//        }

//        Toast.makeText(context, "Recieved!!", Toast.LENGTH_LONG).show();

        reminderName = intent.getStringExtra("reminderName");
        reminderHour = intent.getIntExtra("reminderHour", 1);
        reminderMinute = intent.getIntExtra("reminderMinute",1);
        typeAndLocalId = intent.getStringExtra("typeAndLocalId");


        System.out.println("nome do alarm chegando no alarmreceiver: " + reminderName);



        Log.d(TAG, "onReceive: ");
        //Trigger the notification
        NotificationScheduler.showNotification(context, CreateReminderDateActivity.class,
                "You have 5 unwatched videos", "Watch them now?");

        intent = new Intent();
        intent.setClass(context, AlarmNotificationActivity.class); //Test is a dummy class name where to redirect
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("reminderName", reminderName);
        intent.putExtra("reminderHour", reminderHour);
        intent.putExtra("reminderMinute", reminderMinute);
        intent.putExtra("typeAndLocalId", typeAndLocalId);

        context.startActivity(intent);

    }
}