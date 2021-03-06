package com.lf.appcare;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ReminderListPatientActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ListView reminderListView;
    private NavigableMap<Integer, Reminder> reminderMap;
    private List<Reminder> reminderList = new ArrayList<>();
    private List<Integer> reminderKeys = new ArrayList<>();
    private ArrayAdapter<Reminder> arrayAdapterReminder;
    private String userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list_patient);
        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.reminder_list_caregiver));
//        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences myPreferences
                = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userUid = myPreferences.getString("UID", "");

        reminderListView = findViewById(R.id.reminderListView);
        // Set lister when clicking an item from the patient list
        reminderListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                final int reminderKey = reminderKeys.get(position);
                final Reminder reminder = reminderMap.get(reminderKey);

                if (reminder.getRemoteId().equals(""))
                {
                    final Dialog dialog = new Dialog(view.getContext());
                    dialog.setContentView(R.layout.dialog_remove_reminder);
                    dialog.setTitle(R.string.remove_reminder_dialog);

                    Button removeButton = dialog.findViewById(R.id.removeButton);
                    removeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            // Cancel the reminder
                            reminder.cancel(getApplicationContext());
                            // Remove from the map
                            reminderMap.remove(reminderKey);
                            // Refresh the reminders list
                            ListReminders(reminderMap);
                            dialog.cancel();
                        }
                    });
                    Button cancelButton = dialog.findViewById(R.id.cancelButton);
                    cancelButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            dialog.cancel();
                        }
                    });

                    dialog.show();
                }
            }
        });

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null)
        {
            startActivity(new Intent(ReminderListPatientActivity.this, StartupActivity.class));
            finish();
        }

        reminderMap = ReadRemindersFromFile(Reminder.remindersFilename+userUid);
        ListReminders(reminderMap);

//        // "Create reminder" button
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                startActivity(new Intent(ReminderListPatientActivity.this, CreateReminderNameActivity.class));
//                finish();
//            }
//        });
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

    private NavigableMap<Integer, Reminder> ReadRemindersFromFile(String filename)
    {
        try
        {
            NavigableMap<Integer, Reminder> reminderMap = (NavigableMap<Integer, Reminder>) InternalStorage.readObject(ReminderListPatientActivity.this, filename);
            for (Map.Entry<Integer, Reminder> entry: reminderMap.entrySet())
            {
                System.out.println(entry.getKey()+" : "+entry.getValue().getName()+" : "+entry.getValue().getReminderType());
            }
            return reminderMap;
        }
        catch (IOException e)
        {
            System.out.println("REMINDERS FILE: " + e.getMessage());
            return null;
        }
        catch (ClassNotFoundException e)
        {
            Log.e("REMINDERS FILE", e.getMessage());
            return null;
        }
    }

    private void ListReminders (NavigableMap<Integer, Reminder> reminderMap)
    {
        if (reminderMap == null)
        {
            System.out.println("NOT REMINDERMAP REFERENCE");
            reminderListView.setVisibility(View.GONE);
        }
        else if (reminderMap.keySet().isEmpty())
        {
            System.out.println("EMPTY REMINDERS MAP");
            reminderListView.setVisibility(View.GONE);
        }
        else
        {
            reminderList = new ArrayList<>(reminderMap.values());
            reminderKeys = new ArrayList<>(reminderMap.keySet());

            System.out.println("THERE ARE " + reminderList.size() + " REMINDERS");
            System.out.println("Reminder name: " + reminderList.get(0).getName() + "\nReminder type: " + reminderList.get(0).getReminderType());
            reminderListView.setVisibility(View.VISIBLE);
            arrayAdapterReminder = new ArrayAdapter<Reminder>(this, R.layout.content_reminder_list_patient, R.id.reminderName, reminderList)
            {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);

                    TextView text1 = view.findViewById(R.id.reminderName);
                    TextView text2 = view.findViewById(R.id.reminderDateTime);
                    TextView text3 = view.findViewById(R.id.reminderType);
                    TextView text4 = view.findViewById(R.id.caregiverName);

                    Reminder reminder = reminderList.get(position);

                    //Getting date in a good format to read
                    String dateMatcher = "(\\d{4})(?:-)(\\d{2})(?:-)(\\d{2})(?:T)(.*)";
                    Pattern datePattern = Pattern.compile(dateMatcher);
                    Matcher date = datePattern.matcher(reminderList.get(position).getDate());
                    if(date.matches())
                    {
                        String newDate;
                        if (reminder.getReminderType() == Reminder.DAILY)
                            newDate = date.group(4);
                        else
                            newDate = date.group(3) + "/" + date.group(2) + "/" + date.group(1) + " " + date.group(4);
                        System.out.println(newDate);
                        text2.setText(newDate);
                    }

                    text1.setText(reminder.getName());

                    switch (reminder.getReminderType())
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

                    if (reminderList.get(position).getRemoteId().equals(""))
                        text4.setText("");
                    else
                        text4.setText(getString(R.string.reminderListCaregiverField));

                    return view;
                }
            };
            reminderListView.setAdapter(arrayAdapterReminder);
        }
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(ReminderListPatientActivity.this, MainActivityPatient.class));
        finish();
    }
}
