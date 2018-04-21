package com.lf.appcare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.shawnlin.numberpicker.NumberPicker;

public class CreateReminderDateActivity extends AppCompatActivity {

    NumberPicker hourPicker, minutePicker, monthPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder_date);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.create_reminder_toolbar);
        setSupportActionBar(myToolbar);

        //hourPicker = findViewById(R.id.hourPicker);
        //minutePicker = findViewById(R.id.minutePicker);

        NumberPicker monthPicker = (NumberPicker) findViewById(R.id.month_picker);
        String[] data = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(data.length);
        monthPicker.setDisplayedValues(data);
        // Make it begin on the current month
        monthPicker.setValue(0);

    }

    private String[] getDatesFromCalendar() {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();

        List<String> dates = new ArrayList<String>();
        DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM");
        dates.add(dateFormat.format(c1.getTime()));

        for (int i = 0; i < 60; i++) {
            c1.add(Calendar.DATE, 1);
            dates.add(dateFormat.format(c1.getTime()));
        }
        c2.add(Calendar.DATE, -60);

        for (int i = 0; i < 60; i++) {
            c2.add(Calendar.DATE, 1);
            dates.add(dateFormat.format(c2.getTime()));
        }
        return dates.toArray(new String[dates.size() - 1]);
    }
}
