package com.lf.appcare;


import com.google.common.collect.Lists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        final String reminderName, reminderDate, patientUid, caregiverUid, remoteId, userUid,
                patientName, caregiverName;
        int reminderType;
        Reminder reminder;
        DatabaseReference db;

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
                // Caregiver created a reminder
                // Notify the patient
                case "createdReminder":
                    if (userType.equals(AppCareUser.PATIENT))
                    {
                        System.out.println("Caregiver CREATED reminder");
                        reminderName = data.get ("reminderName");
                        System.out.println("Got name: " + reminderName);
                        System.out.println("ReminderType string: " + data.get ("reminderType"));
                        reminderType = Integer.parseInt (data.get ("reminderType"));
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
                        NotificationScheduler.showNotification(getApplicationContext(), MainActivityPatient.class,
                                "New reminder", "Caregiver created reminder \"" + reminderName + "\"");
                    }
                    else
                    {
                        System.out.println("Not a patient, ops...");
                    }
                    break;

                // Caregiver removed a reminder
                // Notify the patient
                case "removedReminder":
                    if (userType.equals(AppCareUser.PATIENT))
                    {
                        System.out.println("Caregiver REMOVED reminder");
                        remoteId = data.get("reminderId");
                        if (remoteId != null)
                        {
                            reminder = Reminder.findByRemoteId(getApplicationContext(), remoteId);
                            if (reminder != null)
                            {
                                reminder.cancel(getApplicationContext());
                                System.out.println("Remote reminder cancelled");
                                NotificationScheduler.showNotification(getApplicationContext(), MainActivityPatient.class,
                                        "Reminder removed", "Caregiver removed reminder \"" + reminderName + "\"");
                            }
                            else
                                {
                                System.out.println("Reminder with remote id " + remoteId + " not found locally.");
                            }

                        }
                    }
                    else
                        System.out.println("Not a patient, ops...");

                    break;

                // Patient viewed the reminder
                // Notify the caregiver
                case "ackReminder":
                    if (userType.equals(AppCareUser.CAREGIVER))
                    {
                        reminderName = data.get("reminderName");
                        patientUid = data.get("patientUid");
                        db = FirebaseDatabase.getInstance().getReference();
                        db.child("users").child(patientUid).child("firstName").addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                String patientName = dataSnapshot.getValue(String.class);
                                NotificationScheduler.showNotification(getApplicationContext(), MainActivityCaregiver.class,
                                        getString(R.string.ack_title),
                                        getString(R.string.ack_body, patientName, reminderName));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }
                        });
                    }
                    break;

                // New patient-caregiver connection
                // Notify the patient
                case "newConnection":
                    if (userType.equals(AppCareUser.PATIENT))
                    {
                        caregiverName = data.get("caregiverName");
                        NotificationScheduler.showNotification(getApplicationContext(), MainActivityPatient.class,
                                getString(R.string.new_connection_title),
                                getString(R.string.new_connection_body, caregiverName));
                    }
                    break;

                // Removed patient-caregiver connection
                // Notify the patient
                case "removedConnection":
                    if (userType.equals(AppCareUser.PATIENT))
                    {
                        NotificationScheduler.showNotification(getApplicationContext(), MainActivityPatient.class,
                                getString(R.string.removed_connection_title),
                                getString(R.string.removed_connection_body));
                    }
                    break;

                // Patient triggered an emergency request
                // Notify the caregiver
                case "emergencyRequest":
                    if (userType.equals(AppCareUser.CAREGIVER))
                    {
                        patientName = data.get("patientName");
                        NotificationScheduler.showNotification(getApplicationContext(), MainActivityPatient.class,
                                getString(R.string.emergency_title),
                                getString(R.string.emergency_body, patientName));
                    }
                    break;


                //

                default:
                    System.out.println("Received unknown message type");
            }
            //else if ()
            //{

            //}
            //showNotification(data.get("title"), data.get("author"));
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null)
        {
            RemoteMessage.Notification notification = remoteMessage.getNotification();
            String title = notification.getTitle();
            String body = notification.getBody();
            System.out.println("Title: " + title + "\nBody: " + body);
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
