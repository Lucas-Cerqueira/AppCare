package com.lf.appcare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class CreateReminderNameActivity extends AppCompatActivity {
    private EditText reminderName;
    private int reminderType;
    private Reminder reminder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reminder_name);

        reminderName = findViewById(R.id.reminderNameText);

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
                }
                reminder = new Reminder(reminderName.getText().toString().trim(), reminderType);
                System.out.println(reminder.getName() + " " + reminder.getReminderType());
            }
        });
    }
}
