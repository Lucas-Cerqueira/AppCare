package com.lf.appcare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.shawnlin.numberpicker.NumberPicker;

public class CreateReminderHourActivity extends AppCompatActivity {

    static final int DAILY_REMINDER_REQUEST_CODE = 0;

    NumberPicker hourPicker, minutePicker;
    Button confirmButton;
    int reminderDay, reminderMonth, reminderYear, reminderType;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder_hour);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menuCreateReminder));
//        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        reminderDay = getIntent().getIntExtra("reminderDay",1);
        reminderMonth = getIntent().getIntExtra("reminderMonth", 1);
        reminderYear = getIntent().getIntExtra("reminderYear", 18);
        reminderType = getIntent().getIntExtra("reminderType", 1);


        System.out.println("Importando variaveis: " + reminderDay + " " + reminderMonth + " " + reminderYear + " " + reminderType);

        hourPicker = findViewById(R.id.hour_picker);
        minutePicker = findViewById(R.id.minute_picker);

        confirmButton = findViewById(R.id.submit_date);


        hourPicker.setMaxValue(23);
        hourPicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        minutePicker.setMinValue(0);


        // Make it begin on the current month
        hourPicker.setValue(8);
        minutePicker.setValue(0);

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {

                int alarmHour = hourPicker.getValue();
                int alarmMinute = minutePicker.getValue();

                //Going to 2000's
                reminderYear = reminderYear + 2000;

                Calendar alarmCalendar = new GregorianCalendar(reminderYear, reminderMonth - 1, reminderDay, alarmHour, alarmMinute);

                System.out.println("Dia " + reminderDay + " Mes " + reminderMonth + " Ano " + reminderYear);

                System.out.println("hora em milisegundos " + alarmCalendar.getTimeInMillis());

                NotificationScheduler.setReminder(CreateReminderHourActivity.this, AlarmReceiver.class, alarmCalendar, reminderType);

                Intent intent = new Intent(getApplicationContext(), MainActivityPatient.class);
                startActivity(intent);
                finish();

            }
        });
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), CreateReminderDateActivity.class);
            startActivity(intent);
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}

