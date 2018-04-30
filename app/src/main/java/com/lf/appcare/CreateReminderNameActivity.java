package com.lf.appcare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Button;

public class CreateReminderNameActivity extends AppCompatActivity {
    private EditText reminderName;
    private int reminderType;
    private Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder_name);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menuCreateReminder));
//        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        reminderName = findViewById(R.id.reminderNameText);
        Button confirmButton = (Button) findViewById(R.id.submit_name);

        RadioButton radioButton = findViewById(R.id.onceRadioButton);
        radioButton.setChecked(true);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                switch (checkedId)
                {
                    case R.id.onceRadioButton:
                        reminderType = Reminder.ONCE;
                        break;
                    case R.id.dailyRadioButton:
                        reminderType = Reminder.DAILY;
                        break;
                    case R.id.weeklyRadioButton:
                        reminderType = Reminder.WEEKLY;
                        break;
                    case R.id.monthlyRadioButton:
                        reminderType = Reminder.MONTHLY;
                        break;
                    default:
                        reminderType = Reminder.ONCE;
                        break;
                }
                reminder = new Reminder(reminderName.getText().toString().trim(), reminderType);
                System.out.println(reminder.getName() + " " + reminder.getReminderType());
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
                Intent intent = new Intent(getApplicationContext(), CreateReminderDateActivity.class);
                intent.putExtra("reminderName", reminder.getName());
                intent.putExtra("reminderType", reminder.getReminderType());
                startActivity(intent);
                finish();
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), MainActivityPatient.class);
            startActivity(intent);
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }
}
