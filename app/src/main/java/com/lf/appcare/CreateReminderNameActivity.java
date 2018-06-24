package com.lf.appcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateReminderNameActivity extends AppCompatActivity {
    private AutoCompleteTextView patientEmailText;
    private EditText reminderNameText;
    private int reminderType;
    private DatabaseReference db;
    private ArrayList<String> emailList = new ArrayList<>();
    private ArrayAdapter<String> adapterEmail;
    private String userType;
    private CaregiverUser caregiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Get user type from preferences
        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(this);
        userType = myPreferences.getString("USERTYPE", "");
        System.out.println("User Type: " + userType);

        if (userType.equals(AppCareUser.PATIENT))
            setContentView(R.layout.activity_create_reminder_name);
        else
        {
            setContentView(R.layout.activity_create_reminder_name_caregiver);
            // Autocomplete text field for the patient's email
            patientEmailText = findViewById(R.id.patientEmailText);
            patientEmailText.setThreshold(1);
            adapterEmail = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, emailList);
            patientEmailText.setAdapter(adapterEmail);
        }

        reminderNameText = findViewById(R.id.reminderNameText);
        Button confirmButton = findViewById(R.id.submit_name);

        RadioButton radioButton = findViewById(R.id.onceRadioButton);
        radioButton.setChecked(true);

        reminderType = Reminder.ONCE;
        RadioGroup radioGroup = findViewById(R.id.radioGroup);

        String reminderName = getIntent().getStringExtra("reminderName");
        reminderType = getIntent().getIntExtra("reminderType", 1);
        String patientEmail = getIntent().getStringExtra("patientEmail");
        if (reminderName != null)
        {
            reminderNameText.setText(reminderName);
            int id;
            if (reminderType == Reminder.ONCE)
                id = R.id.onceRadioButton;
            else if (reminderType == Reminder.DAILY)
                id = R.id.dailyRadioButton;
            else if (reminderType == Reminder.WEEKLY)
                id = R.id.weeklyRadioButton;
            else if (reminderType == Reminder.MONTHLY)
                id = R.id.monthlyRadioButton;
            else
                id = R.id.onceRadioButton;
            radioGroup.check(id);
        }
        if (patientEmail != null)
            patientEmailText.setText(patientEmail);

        //String patientEmail = FirebaseDatabase.

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menuCreateReminder));
//        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // If it is a caregiver, get the CaregiverUser object from the database
        // and retrieve its patients' email
        if (userType.equals(AppCareUser.CAREGIVER))
        {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            db = FirebaseDatabase.getInstance().getReference();
            String userUid = auth.getCurrentUser().getUid();
            DatabaseReference ref = db.child("users").child(userUid);
            ref.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    emailList = new ArrayList<>();
                    caregiverUser = dataSnapshot.getValue(CaregiverUser.class);
                    if (caregiverUser == null)
                        return;

                    for (PatientUser patient: caregiverUser.getPatientList())
                    {
                        emailList.add(patient.getEmail());
                    }
                    if (emailList.isEmpty())
                        patientEmailText.setHint(getString(R.string.empty_patient_list_hint));
                    else
                        patientEmailText.setHint(getString(R.string.patient_email));

                    for (String email: emailList)
                    {
                        System.out.println("Email: " + email);
                    }

                    adapterEmail.clear();
                    adapterEmail.addAll(emailList);
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }

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
//                reminder = new Reminder(reminderName.getText().toString().trim(), reminderType);
//                System.out.println(reminder.getName() + " " + reminder.getReminderType());
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
                // If it is a daily reminder, skip to hour selection
                Class<?> targetClass;
                if (reminderType == Reminder.DAILY)
                    targetClass = CreateReminderHourActivity.class;
                else
                    targetClass = CreateReminderDateActivity.class;
                Intent intent = new Intent(getApplicationContext(), targetClass);

                if (reminderNameText.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(), R.string.empty_reminder_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("reminderName", reminderNameText.getText().toString().trim());

                intent.putExtra("reminderType", reminderType);

                if (userType.equals(AppCareUser.CAREGIVER))
                {
                    if (patientEmailText.getText().toString().isEmpty())
                    {
                        Toast.makeText(getApplicationContext(), R.string.empty_patient, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    PatientUser patient = caregiverUser.FindPatientByEmail(patientEmailText.getText().toString());
                    if (patient == null)
                    {
                        Toast.makeText(getApplicationContext(), R.string.patient_not_found, Toast.LENGTH_SHORT).show();
                        System.out.println("Invalid patient");
                        return;
                    }
                    intent.putExtra("patientEmail", patient.getEmail());
                    intent.putExtra("patientUid", patient.getUid());
                }
                startActivity(intent);
                finish();
            }
        });

    }

    private void ReturnToPreviousScreen()
    {
        Intent intent;
        if (userType.equals(AppCareUser.PATIENT))
            intent = new Intent(getApplicationContext(), MainActivityPatient.class);
        else
            intent = new Intent(getApplicationContext(), MainActivityCaregiver.class);

        startActivity(intent);
        finish(); // close this activity and return to preview activity (if there is any)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
