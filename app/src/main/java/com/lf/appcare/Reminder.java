package com.lf.appcare;

import java.util.Calendar;

public class Reminder
{
    public static final int ONCE = 1;
    public static final int DAILY = 2;
    public static final int WEEKLY = 3;
    public static final int MONTHLY = 4;

    private String userUid;
    private String name;
    private int reminderType;
    private Calendar calendar;

    public Reminder() {}

    Reminder (String name, int reminderType, Calendar calendar)
    {
        this.userUid = "";
        this.name = name;
        this.reminderType = reminderType;
        this.calendar = calendar;
    }

    Reminder (String userUid, String name, int reminderType, Calendar calendar)
    {
        this.userUid = userUid;
        this.name = name;
        this.reminderType = reminderType;
        this.calendar = calendar;
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
}
