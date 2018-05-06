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

        String[] data = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

//        hourPicker.setMaxValue(23);
//        hourPicker.setMinValue(0);
//        minutePicker.setMaxValue(59);
//        minutePicker.setMinValue(0);
        dayPicker.setMaxValue(31);
        dayPicker.setMinValue(0);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(data.length);

        monthPicker.setDisplayedValues(data);
        // Make it begin on the current month
//        hourPicker.setValue(8);
//        minutePicker.setValue(0);


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
                Intent intent = new Intent(getApplicationContext(), CreateReminderHourActivity.class);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), CreateReminderNameActivity.class);
            startActivity(intent);
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

}

