package com.lf.appcare;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderListCaregiverActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference db;
    private ListView reminderListView;
    private List<DataSnapshot> remindersSnapshot = new ArrayList<>();
    private List<Reminder> reminderList = new ArrayList<>();
    private List<String> reminderKeys = new ArrayList<>();
    private FirebaseUser user;
    private AutoCompleteTextView textView;
    private ArrayAdapter<Reminder> arrayAdapterReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list_caregiver);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.reminder_list_caregiver));
//        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        reminderListView = findViewById(R.id.reminderListView);
        // Set lister when clicking an item from the patient list
        reminderListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id)
            {

                final String reminderKey = reminderKeys.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setMessage(getString(R.string.remove_reminder_message))
                        .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                db.child("remoteReminders").child(auth.getCurrentUser().getUid()).child(reminderKey).removeValue();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                            }
                        });
                // Create the AlertDialog object
                AlertDialog dialog = builder.create();
                dialog.show();

                // Configure the buttons
                Button posButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                posButton.setTextColor(getResources().getColor(R.color.colorPrimary));
                negButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });


        if (auth.getCurrentUser() == null)
        {
            startActivity(new Intent(ReminderListCaregiverActivity.this, StartupActivity.class));
            finish();
        }

        user = auth.getCurrentUser();
        String userUid = user.getUid();
        DatabaseReference ref = db.child("remoteReminders").child(userUid);
        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Read all the caregiver stored reminders
                reminderList = new ArrayList<>();
                reminderKeys = new ArrayList<>();
                remindersSnapshot = Lists.newArrayList (dataSnapshot.getChildren().iterator());
                for (DataSnapshot reminderSnapshot : remindersSnapshot)
                {
                    Reminder reminder = reminderSnapshot.getValue(Reminder.class);
                    reminderList.add (reminder);
                    reminderKeys.add (reminderSnapshot.getKey());
                }
                for (String key: reminderKeys)
                {
                    System.out.println("Key: " + key);
                }
                ListReminders();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        // "Create reminder" button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(ReminderListCaregiverActivity.this, CreateReminderNameActivity.class));
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), MainActivityCaregiver.class);
            startActivity(intent);
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    private void ListReminders ()
    {
        if (reminderList == null)
        {
            System.out.println("NOT REMINDERLIST REFERENCE");
            reminderListView.setVisibility(View.GONE);
        }
        else if (reminderList.isEmpty())
        {
            System.out.println("EMPTY REMINDERS LIST");
            reminderListView.setVisibility(View.GONE);
        }
        else
        {
            System.out.println("THERE ARE " + reminderList.size() + " REMINDERS");
            System.out.println("Reminder name:" + reminderList.get(0).getName() + "\nReminder type:" + reminderList.get(0).getReminderType());
            reminderListView.setVisibility(View.VISIBLE);
            arrayAdapterReminder = new ArrayAdapter<Reminder>(this, R.layout.content_reminder_list_caregiver, R.id.reminderName, reminderList)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);

                    TextView text1 = view.findViewById(R.id.reminderName);
                    TextView text2 = view.findViewById(R.id.reminderDateTime);
                    TextView text3 = view.findViewById(R.id.reminderType);
                    TextView text4 = view.findViewById(R.id.patientName);

                    //Getting date in a good format to read
                    String dateMatcher = "(\\d{4})(?:-)(\\d{2})(?:-)(\\d{2})(?:T)(.*)";
                    Pattern datePattern = Pattern.compile(dateMatcher);
                    Matcher date = datePattern.matcher(reminderList.get(position).getDate());
                    if(date.matches())
                    {
                        String newDate = date.group(3) + "/" + date.group(2) + "/" + date.group(1) + " " + date.group(4);
                        System.out.println(newDate);
                        text2.setText(newDate);
                    }

                    text1.setText(reminderList.get(position).getName());

                    System.out.println("tipo do reminder: " + reminderList.get(position).getReminderType());
                    switch (reminderList.get(position).getReminderType())
                    {
                        case Reminder.ONCE:
                            text3.setText(R.string.onceReminderType);
                            break;
                        case Reminder.DAILY:
                            text3.setText(R.string.dailyReminderType);
                            break;
                        case Reminder.WEEKLY:
                            text3.setText(R.string.weeklyReminderType);
                            break;
                        case Reminder.MONTHLY:
                            text3.setText(R.string.monthlyReminderType);
                            break;
                    }

                   text4.setText("Patient name");

                    return view;
                }
            };
            reminderListView.setAdapter(arrayAdapterReminder);
        }
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(ReminderListCaregiverActivity.this, MainActivityCaregiver.class));
        finish();
    }
}
