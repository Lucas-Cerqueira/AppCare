package com.lf.appcare;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Reminder implements Serializable
{
    public static final String remindersFilename = "reminders";

    public static final int ONCE = 1;
    public static final int DAILY = 2;
    public static final int WEEKLY = 3;
    public static final int MONTHLY = 4;

    public static int nextId = 0;

    // Local reminder attributes
    private String userUid;
    private String name;
    private int reminderType;
    private String date;
    //private Context context;
    //private Class<?> targetClass;
    private int localId;

    // Remote reminder attributes
    private String caregiverUid;
    private String remoteId;

    public Reminder() {}

    // Local reminder
    Reminder (String userUid, String name, int reminderType, Calendar calendar)
    {
        this.userUid = userUid;
        this.name = name;
        this.reminderType = reminderType;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault());
        this.date = dateFormat.format(calendar.getTime());
        this.localId = -1;
        this.remoteId = "";
        this.caregiverUid = ".";
    }

    // Remote reminder
    Reminder (String userUid, String name, int reminderType, String date,
              String caregiverUid, String remoteId)
    {
        this.userUid = userUid;
        this.name = name;
        this.reminderType = reminderType;
        this.date = date;
        this.localId = -1;
        this.caregiverUid = caregiverUid;
        this.remoteId = remoteId;
    }

    public void set (Context context)
    {
        NavigableMap<Integer,Reminder> reminderMap = new TreeMap<>();
        try
        {
            reminderMap = (NavigableMap<Integer,Reminder>) InternalStorage.readObject(context, Reminder.remindersFilename+this.userUid);
            // If the id reset to 0
            if (Reminder.nextId == 0 && !reminderMap.keySet().isEmpty())
                Reminder.nextId = Integer.valueOf (reminderMap.lastEntry().getKey()) + 1;
            else if (reminderMap.keySet().isEmpty())
                Reminder.nextId = 0;

            for (Map.Entry<Integer, Reminder> entry: reminderMap.entrySet())
            {
                System.out.println(entry.getKey()+" : "+entry.getValue().getName()+" : "+entry.getValue().getReminderType());
            }
        }
        catch (IOException e)
        {
            if (!e.getMessage().contains("No such file"))
            {
                System.out.println("REMINDERS FILE: " + e.getMessage());
                System.out.println("Error reading file\nReminder not set");
                return;
            }
            else
                System.out.println("No file on read. It will be created");
        }
        catch (ClassNotFoundException e)
        {
            Log.e("REMINDERS FILE", e.getMessage());
            System.out.println("Class error\nReminder not set");
            return;
        }

        reminderMap.put(Reminder.nextId, this);
        this.localId = Reminder.nextId;
        try
        {
            InternalStorage.writeObject(context, Reminder.remindersFilename+this.userUid, reminderMap);
            System.out.println("Updated local reminders file");
        }
        catch (IOException e)
        {
            System.out.println("REMINDERS FILE: " + e.getMessage());
            System.out.println("Error writing file\nReminder not set");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault());
        try
        {
            Date auxDate = dateFormat.parse(this.date);
            Calendar alarmCalendar = Calendar.getInstance();
            alarmCalendar.setTime(auxDate);
            NotificationScheduler.setReminder(context, AlarmReceiver.class, alarmCalendar, this);
            Reminder.nextId += 1;
        }
        catch (ParseException e)
        {
            System.out.println("Error parsing date\nReminder not set");
            return;
        }
        System.out.println("Reminder successfully set");
    }

    public void cancel (Context context)
    {
        NavigableMap<Integer, Reminder> reminderMap;
        try
        {
            reminderMap = (NavigableMap<Integer, Reminder>) InternalStorage.readObject(context, Reminder.remindersFilename+this.userUid);
            reminderMap.remove(this.localId);
            InternalStorage.writeObject(context, Reminder.remindersFilename + this.userUid, reminderMap);
            NotificationScheduler.cancelReminder(context, AlarmReceiver.class, this.localId);
            System.out.println("Reminder cancelled");
        }
        catch (IOException e)
        {
            System.out.println("REMINDERS FILE: " + e.getMessage());
        }
        catch (ClassNotFoundException e)
        {
            Log.e("REMINDERS FILE", e.getMessage());
        }
    }

    public static Reminder findByRemoteId (Context context, String id, String filename)
    {
        NavigableMap<Integer, Reminder> reminderMap;
        try
        {
            reminderMap = (NavigableMap<Integer, Reminder>) InternalStorage.readObject(context, filename);
            for (Map.Entry<Integer, Reminder> entry : reminderMap.entrySet())
            {
                if (entry.getValue().getRemoteId().equals(id))
                    return entry.getValue();
            }
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
        return null;
    }

    public String getUserUid() {
        return userUid;
    }

    public String getName() {
        return name;
    }

    public int getReminderType() {
        return reminderType;
    }

    public String getDate() {
        return date;
    }

    public int getlocalId() {
        return localId;
    }

    public void setlocalId (int id)
    {
        this.localId = id;
    }

    public String getCaregiverUid() {
        return caregiverUid;
    }

    public String getRemoteId() {
        return remoteId;
    }
}
