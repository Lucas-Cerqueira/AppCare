package com.lf.appcare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.shawnlin.numberpicker.NumberPicker;

public class CreateReminderDateActivity extends AppCompatActivity {

    static final int DAILY_REMINDER_REQUEST_CODE = 0;

    NumberPicker hourPicker, minutePicker, monthPicker, dayPicker, yearPicker;
    Button confirmButton;
    String reminderName, patientUid;
    int reminderType;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder_date);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menuCreateReminder));
//        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        reminderName = getIntent().getStringExtra("reminderName");
        reminderType = getIntent().getIntExtra("reminderType", 1);
        patientUid = getIntent().getStringExtra("patientUid");
        System.out.println("Importando variaveis: " + reminderName + " " + reminderType);

//        hourPicker = findViewById(R.id.hour_picker);
//        minutePicker = findViewById(R.id.minute_picker);
        dayPicker = findViewById(R.id.day_picker);
        yearPicker = findViewById(R.id.year_picker);
        monthPicker = findViewById(R.id.month_picker);
        confirmButton = findViewById(R.id.submit_date);

        String[] months = getResources().getStringArray(R.array.months_array);

        // Get current date to set the initial picker values
        Calendar now = Calendar.getInstance();

        dayPicker.setMaxValue(31);
        dayPicker.setMinValue(1);
        dayPicker.setValue(now.get(Calendar.DAY_OF_MONTH));

        monthPicker.setMaxValue(months.length);
        monthPicker.setMinValue(1);
        monthPicker.setValue(now.get(Calendar.MONTH)+1); // Jan = 0. Why? It is Java...
        monthPicker.setDisplayedValues(months);

        int currentYear = now.get(Calendar.YEAR)-2000;
        yearPicker.setMaxValue(currentYear+10);
        yearPicker.setMinValue(currentYear);
        yearPicker.setValue(currentYear);

        monthPicker.setOnValueChangedListener( new NumberPicker.OnValueChangeListener()
        {
            @Override
           public void onValueChange (NumberPicker picker, int oldVal, int currentMonth) {
               int currentYear = yearPicker.getValue();
               int defaultDay = 1;
               Calendar currentCal = new GregorianCalendar(currentYear, currentMonth - 1, defaultDay);

               int daysInCurrentMonth = currentCal.getActualMaximum(Calendar.DAY_OF_MONTH);
               dayPicker.setMinValue(1);
               dayPicker.setMaxValue(daysInCurrentMonth);
            }
       });

        dayPicker.setOnValueChangedListener( new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange (NumberPicker picker, int oldVal, int currentMonth) {
               System.out.println("Dia " + currentMonth);
            }
        });

        yearPicker.setOnValueChangedListener( new NumberPicker.OnValueChangeListener()
        {
            @Override
            public void onValueChange (NumberPicker picker, int oldVal, int currentMonth) {
                System.out.println("Ano " + currentMonth);
            }
        });


        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
                Intent intent = new Intent(CreateReminderDateActivity.this, CreateReminderHourActivity.class);
                intent.putExtra("reminderDay", dayPicker.getValue());
                intent.putExtra("reminderMonth", monthPicker.getValue());
                intent.putExtra("reminderYear", yearPicker.getValue());
                intent.putExtra("reminderType", reminderType);
                intent.putExtra("reminderName", reminderName);
                if (patientUid != null)
                    intent.putExtra("patientUid", patientUid);

                startActivity(intent);
                finish();
            }
        });


    };

    private void ReturnToPreviousScreen()
    {
        Intent intent = new Intent(getApplicationContext(), CreateReminderNameActivity.class);
        startActivity(intent);
        finish(); // close this activity and return to preview activity (if there is any)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
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

