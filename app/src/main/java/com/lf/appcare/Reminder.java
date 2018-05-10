package com.lf.appcare;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Reminder implements Serializable
{
    public static final int ONCE = 1;
    public static final int DAILY = 2;
    public static final int WEEKLY = 3;
    public static final int MONTHLY = 4;

    public static int nextId = 0;

    private String userUid;
    private String name;
    private int reminderType;
    private String date;

    public Reminder() {}

    Reminder (String name, int reminderType, String date)
    {
        this.userUid = "";
        this.name = name;
        this.reminderType = reminderType;
        this.date = date;
    }

    Reminder (String name, int reminderType, Calendar calendar)
    {
        this.userUid = "";
        this.name = name;
        this.reminderType = reminderType;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault());
        this.date = dateFormat.format(calendar.getTime());
    }

    Reminder (String userUid, String name, int reminderType, Calendar calendar)
    {
        this.userUid = userUid;
        this.name = name;
        this.reminderType = reminderType;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", java.util.Locale.getDefault());
        this.date = dateFormat.format(calendar.getTime());
    }

    Reminder (String userUid, String name, int reminderType, String date)
    {
        this.userUid = userUid;
        this.name = name;
        this.reminderType = reminderType;
        this.date = date;
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
}
