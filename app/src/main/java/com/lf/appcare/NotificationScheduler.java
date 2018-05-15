package com.lf.appcare;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Jaison on 20/06/17.
 */

public class NotificationScheduler
{
    public static final int DAILY_REMINDER_REQUEST_CODE=100;
    public static final String TAG="NotificationScheduler";

    public static long getMonthInterval(Calendar alarmCalendar){

        int currentMonth = alarmCalendar.get(Calendar.MONTH);
        long currentMilliseconds = alarmCalendar.getTimeInMillis();

        // move month ahead
        currentMonth++;
        // check if has not exceeded threshold of december

        if(currentMonth > Calendar.DECEMBER){
            // alright, reset month to jan and forward year by 1 e.g fro 2013 to 2014
            currentMonth = Calendar.JANUARY;
            // Move year ahead as well
            alarmCalendar.set(Calendar.YEAR, alarmCalendar.get(Calendar.YEAR)+1);
        }
        // reset calendar to next month
        alarmCalendar.set(Calendar.MONTH, currentMonth);
        // get the maximum possible days in this month
        int maximumDay = alarmCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // set the calendar to maximum day (e.g in case of fEB 28th, or leap 29th)
        alarmCalendar.set(Calendar.DAY_OF_MONTH, maximumDay);
        long thenTime = alarmCalendar.getTimeInMillis(); // this is time one month ahead

        return (thenTime - currentMilliseconds); // this is what you set as trigger point time i.e one month after
    }

    public static void setReminder(Context context,Class<?> cls, Calendar alarmCalendar, Reminder reminder)
    {
        long intervalTime;
        switch (reminder.getReminderType())
        {
            case 1:
                intervalTime = 0;
                break;
            case 2:
                intervalTime = AlarmManager.INTERVAL_DAY*24;
                break;
            case 3:
                intervalTime = AlarmManager.INTERVAL_DAY*7;
                break;
            case 4:
                intervalTime = getMonthInterval(alarmCalendar);
                break;
            default:
                intervalTime = 0;
                break;
        }

        // cancel already scheduled reminders
        cancelReminder(context, cls, reminder.getlocalId());

        // Enable a receiver

        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        Intent intent1 = new Intent(context, cls);
        intent1.putExtra("reminderName", reminder.getName() + "_" + reminder.getRemoteId() + "_" + reminder.getCaregiverUid());
        intent1.putExtra("reminderType", reminder.getReminderType());
        intent1.putExtra("reminderHour", alarmCalendar.get(Calendar.HOUR_OF_DAY));
        intent1.putExtra("reminderMinute", alarmCalendar.get(Calendar.MINUTE));
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminder.getlocalId(), intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), intervalTime, pendingIntent);

    }

    public static void cancelReminder(Context context,Class<?> cls, int reminderId)
    {
        // Disable a receiver

        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        Intent intent1 = new Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reminderId, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        am.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    public static void showNotification(Context context,Class<?> cls,String title,String content)
    {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Intent notificationIntent = new Intent(context, cls);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(cls);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(DAILY_REMINDER_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "myId");

        Notification notification = builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(DAILY_REMINDER_REQUEST_CODE, notification);
    }
}
