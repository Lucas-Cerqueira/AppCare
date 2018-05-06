package com.lf.appcare;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{

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
