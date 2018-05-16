package com.lf.appcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shawnlin.numberpicker.NumberPicker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class CreateReminderHourActivity extends AppCompatActivity {

    static final int DAILY_REMINDER_REQUEST_CODE = 0;

    private DatabaseReference db;

    private NumberPicker hourPicker, minutePicker;
    private  Button confirmButton;
    private int reminderDay, reminderMonth, reminderYear, reminderType;
    private String reminderName, patientUid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder_hour);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menuCreateReminder));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = FirebaseDatabase.getInstance().getReference();

        reminderDay = getIntent().getIntExtra("reminderDay",1);
        reminderMonth = getIntent().getIntExtra("reminderMonth", 1);
        reminderYear = getIntent().getIntExtra("reminderYear", 18);
        reminderType = getIntent().getIntExtra("reminderType", 1);
        reminderName = getIntent().getStringExtra("reminderName");
        patientUid = getIntent().getStringExtra("patientUid");


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

                // Get user type from preferences
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String userType = myPreferences.getString("USERTYPE", "");
                String userUid = myPreferences.getString("UID", "");

                if (userType.equals(AppCareUser.PATIENT))
                {
                    // Create and set the reminder
                    Reminder reminder = new Reminder (userUid, reminderName, reminderType, alarmCalendar);
                    reminder.set(getApplicationContext());
                    System.out.println("Next id: " + Reminder.nextId);

                    // Return to the main screen
                    Intent intentStart = new Intent(getApplicationContext(), MainActivityPatient.class);
                    startActivity(intentStart);
                    finish();
                }
                else if (userType.equals(AppCareUser.CAREGIVER))
                {
                    // Create the remote reminder and write it to the database
                    System.out.println("Reminder name:" + reminderName + "\nReminder type:" + reminderType);
                    Reminder reminder = new Reminder(patientUid, reminderName, reminderType, alarmCalendar);
                    db.child("remoteReminders").child(userUid).push().setValue(reminder);

                    Intent intentStart = new Intent(getApplicationContext(), ReminderListCaregiverActivity.class);
                    startActivity(intentStart);
                    finish();
                }
            }
        });
    };

    private void ReturnToPreviousScreen()
    {
        Intent intent = new Intent(getApplicationContext(), CreateReminderDateActivity.class);
        startActivity(intent);
        finish(); // close this activity and return to preview activity (if there is any)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home)
        {
            ReturnToPreviousScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        ReturnToPreviousScreen();
    }
}

