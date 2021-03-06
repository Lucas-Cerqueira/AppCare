package com.lf.appcare;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AlarmNotificationActivity extends AppCompatActivity {

    private DatabaseReference db;

    private String reminderName, remoteId, caregiverUid, databaseIds, reminderHourString, reminderMinuteString, prefix, typeAndLocalId;
    private int reminderHour, reminderMinute, reminderType, localId;
    Reminder currentReminder;
    private Button dismiss;
    Ringtone r;
    Uri notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_alarm_notification);

        databaseIds = getIntent().getStringExtra("reminderName");
        reminderHour = getIntent().getIntExtra("reminderHour", 1);
        reminderMinute = getIntent().getIntExtra("reminderMinute", 1);
        typeAndLocalId = getIntent().getStringExtra("typeAndLocalId");

        prefix = "0";

        //Cancelando e tratando as merdas
        reminderType = Integer.parseInt(typeAndLocalId.split("_")[0]);
        localId = Integer.parseInt(typeAndLocalId.split("_")[1]);

        // Get user type from preferences
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userUid = myPreferences.getString("UID", "");

        if (reminderType == Reminder.ONCE)
        {
            currentReminder = Reminder.findByLocalId(getApplicationContext(), localId, Reminder.remindersFilename + userUid);
            currentReminder.cancel(getApplicationContext());
        }
        else if (reminderType == Reminder.MONTHLY)
        {
            currentReminder = Reminder.findByLocalId(getApplicationContext(), localId, Reminder.remindersFilename + userUid);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault());
            currentReminder.setDate(dateFormat.format(Calendar.getInstance().getTime()));
            currentReminder.set(getApplicationContext());
        }

        //Tratando as strings de hora e minuto para ficar bonito
        if (reminderHour < 10)
            reminderHourString = prefix.concat(Integer.toString(reminderHour));
        else
            reminderHourString = Integer.toString(reminderHour);



        if (reminderMinute < 10)
            reminderMinuteString = prefix.concat(Integer.toString(reminderMinute));
        else
            reminderMinuteString = Integer.toString(reminderMinute);


        String [] dataArray = databaseIds.split("#");
        reminderName = dataArray[0];
        remoteId = dataArray[1];
        caregiverUid = dataArray[2];

//        System.out.println("remote id " + remoteId + " caregiver uid " + caregiverUid);

        db = FirebaseDatabase.getInstance().getReference();


        System.out.println("Nome do alarme ao chegar no alarmnotification: " + reminderName + " hora: " + reminderHour + " minuto: " + reminderMinute);
        System.out.println("Remote id: " + remoteId + " caregiverUid: " + caregiverUid);


        TextView nameReminderLayout = findViewById(R.id.alarmNotificationText);
        TextView hourReminderLayout = findViewById(R.id.textHour);
        TextView minuteReminderLayout = findViewById(R.id.textMinute);

        notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

        dismiss = findViewById(R.id.alarmDismiss);

        dismiss.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
//                Intent intent = new Intent(AlarmNotificationActivity.this, MainActivityPatient.class);
                //If there is no remote id, reminder came from patient -> there is no caregiver name
                r.stop();
                if (remoteId.equals(""))
                {
                    System.out.println("remote id null -> lembrete criado pelo paciente");
                    finish();
                }
                else if (!caregiverUid.equals("."))
                {
                    System.out.println("remote id not null -> lembrete criado pelo caregiver -> acknowledge");
                    //Reminder was created by caregiver -> needs to access the db so as to get his name
                    db.child("remoteReminders").child(caregiverUid).child(remoteId).child("ack").setValue(1);
                    finish();
                }

            }
        });

        nameReminderLayout.setText(reminderName);
        hourReminderLayout.setText(reminderHourString);
        minuteReminderLayout.setText(reminderMinuteString);
        nameReminderLayout.setTextColor(Color.BLACK);
        hourReminderLayout.setTextColor(Color.BLACK);
        minuteReminderLayout.setTextColor(Color.BLACK);
    }
}
