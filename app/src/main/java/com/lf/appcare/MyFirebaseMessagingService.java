package com.lf.appcare;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
    private String reminderName, reminderDate, caregiverUid, remoteId, userUid;
    private int reminderType;
    private Reminder reminder;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Map<String, String> data = remoteMessage.getData();
        // Check if message contains a data payload.
        if (data.size() > 0)
        {
            if (data.get("messageType") == null)
            {
                Log.d ("ERROR", "Missing message type");
                return;
            }

            SharedPreferences myPreferences
                    = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            userUid = myPreferences.getString("UID", "");
            String userType = myPreferences.getString("USERTYPE", "");
            String messageType = data.get("messageType");
            switch (messageType)
            {
                case "createdReminder":
                    if (userType.equals(AppCareUser.PATIENT))
                    {
                        System.out.println("Caregiver CREATED reminder");
                        reminderName = data.get ("reminderName");
                        System.out.println("Got name: " + reminderName);
                        reminderType = Integer.getInteger (data.get ("reminderType"));
                        System.out.println("Got type: " + reminderType);
                        reminderDate = data.get ("reminderDate");
                        System.out.println("Got date: " + reminderDate);
                        caregiverUid = data.get ("caregiverUid");
                        System.out.println("Got caregiver: " + caregiverUid);
                        remoteId = data.get ("reminderId");
                        System.out.println("Got remoteId: " + remoteId);
                        reminder = new Reminder(userUid, reminderName, reminderType, reminderDate,
                                caregiverUid, remoteId);
                        reminder.set(getApplicationContext());
                        System.out.println("Remote reminder set");
                    }
                    else
                    {
                        System.out.println("Not a patient, ops...");
                    }
                    break;

                case "removedReminder":
                    if (userType.equals(AppCareUser.PATIENT))
                    {
                        System.out.println("Caregiver REMOVED reminder");
                        remoteId = data.get("reminderId");
                        if (remoteId != null)
                        {
                            reminder = Reminder.findByRemoteId(getApplicationContext(), remoteId);
                            if (reminder != null)
                                reminder.cancel(getApplicationContext());
                            else
                                System.out.println("Reminder with remote id " + remoteId + " not found locally.");
                            System.out.println("Remote reminder cancelled");
                        }
                    }
                    else
                        System.out.println("Not a patient, ops...");
                    break;

                case "ackReminder":
                    break;

                default:
                    System.out.println("Received unknown message type");
            }
            // If it is a create reminder message
            if (data.get("messageType").equals("newReminder"))
            {
                Log.d("NEW REMINDER",
                        "Reminder name: " + data.get("reminderName") +
                        "\nReminder type: " + data.get("reminderType") +
                        "\nReminder date: " + data.get("reminderDate"));
            }
            //else if ()
            //{

            //}
            //showNotification(data.get("title"), data.get("author"));
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null)
        {

        }
    }

    private void showNotification(String title, String author)
    {
        Intent intent = new Intent(this, MainActivityCaregiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("New Article: " + title)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("By " + author)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
