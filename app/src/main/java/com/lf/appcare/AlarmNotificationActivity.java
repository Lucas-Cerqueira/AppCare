package com.lf.appcare;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

public class AlarmNotificationActivity extends AppCompatActivity {

    String reminderName;
    int reminderHour, reminderMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        setContentView(R.layout.activity_alarm_notification);

        reminderName = getIntent().getStringExtra("reminderName");
        reminderHour = getIntent().getIntExtra("reminderHour", 1);
        reminderMinute = getIntent().getIntExtra("reminderMinute", 1);

        System.out.println("Nome do alarme ao chegar no alarmnotification: " + reminderName + " hora: " + reminderHour + " minuto: " + reminderMinute);

        TextView nameReminderLayout = (TextView) findViewById(R.id.alarmNotificationText);
        TextView hourReminderLayout = (TextView) findViewById(R.id.textHour);
        TextView minuteReminderLayout = (TextView) findViewById(R.id.textMinute);


        nameReminderLayout.setText(reminderName);
        hourReminderLayout.setText(String.valueOf(reminderHour));
        minuteReminderLayout.setText(String.valueOf(reminderMinute));
        nameReminderLayout.setTextColor(Color.BLACK);
        hourReminderLayout.setTextColor(Color.BLACK);
        minuteReminderLayout.setTextColor(Color.BLACK);
    }
}
