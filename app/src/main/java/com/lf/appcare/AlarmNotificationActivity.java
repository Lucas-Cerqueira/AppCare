package com.lf.appcare;

import android.content.Intent;
import android.graphics.Color;
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

public class AlarmNotificationActivity extends AppCompatActivity {

    private DatabaseReference db;

    private String reminderName, remoteId, caregiverUid, databaseIds;
    private int reminderHour, reminderMinute, reminderType;
    private Button dismiss;

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
        reminderType = getIntent().getIntExtra("reminderType", 1);
        reminderHour = getIntent().getIntExtra("reminderHour", 1);
        reminderMinute = getIntent().getIntExtra("reminderMinute", 1);

//        System.out.println("databaseids " + databaseIds);


        String [] dataArray = databaseIds.split("_");
        reminderName = dataArray[0];
        remoteId = dataArray[1];
        caregiverUid = dataArray[2];

//        System.out.println("remote id " + remoteId + " caregiver uid " + caregiverUid);

        db = FirebaseDatabase.getInstance().getReference();


//        System.out.println("Nome do alarme ao chegar no alarmnotification: " + reminderName + " hora: " + reminderHour + " minuto: " + reminderMinute);

        TextView nameReminderLayout = findViewById(R.id.alarmNotificationText);
        TextView hourReminderLayout = findViewById(R.id.textHour);
        TextView minuteReminderLayout = findViewById(R.id.textMinute);
        dismiss = findViewById(R.id.alarmDismiss);

        dismiss.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
//                Intent intent = new Intent(AlarmNotificationActivity.this, MainActivityPatient.class);
                //If there is no remote id, reminder came from patient -> there is no caregiver name
                if (remoteId == "")
                {
                    System.out.println("remote id null -> lembrete criado pelo paciente");
                    finish();
                }
                else if (caregiverUid != null)
                {
                    System.out.println("remote id not null -> lembrete criado pelo caregiver -> acknowledge");
                    //Reminder was created by caregiver -> needs to access the db so as to get his name
                    db.child("remoteReminders").child(caregiverUid).child(remoteId).child("ack").setValue(1);
                    finish();
                }

            }
        });

        nameReminderLayout.setText(reminderName);
        hourReminderLayout.setText(String.valueOf(reminderHour));
        minuteReminderLayout.setText(String.valueOf(reminderMinute));
        nameReminderLayout.setTextColor(Color.BLACK);
        hourReminderLayout.setTextColor(Color.BLACK);
        minuteReminderLayout.setTextColor(Color.BLACK);
    }
}
