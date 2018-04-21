package com.lf.appcare;

import java.util.Date;

public class Reminder
{
    public static final int ONCE = 1;
    public static final int DAILY = 2;
    public static final int WEEKLY = 3;
    public static final int MONTHLY = 4;

    private String name;
    private int reminderType;
    private Date date;

    Reminder (String name, int reminderType)
    {
        this.name = name;
        this.reminderType = reminderType;
    }

    public String getName() {
        return name;
    }

    public int getReminderType() {
        return reminderType;
    }
}
