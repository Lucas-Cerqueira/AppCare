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
import java.util.Random;

import static android.content.Context.ALARM_SERVICE;


public class NotificationScheduler
{
    public static final int DAILY_REMINDER_REQUEST_CODE=100;
    public static final String TAG="NotificationScheduler";

    public static long getMonthInterval(Calendar alarmCalendar){

        int currentMonth = alarmCalendar.get(Calendar.MONTH);
        System.out.println("Month: " + Integer.toString(currentMonth));
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

        long thenTime = alarmCalendar.getTimeInMillis(); // this is time one month ahead

        return (thenTime - currentMilliseconds); // this is what you set as trigger point time i.e one month after
    }

    public static void setReminder(Context context,Class<?> cls, Calendar alarmCalendar, Reminder reminder)
    {
        long intervalTime;
        switch (reminder.getReminderType())
        {
            case Reminder.ONCE:
                intervalTime = 0;
                break;
            case Reminder.DAILY:
                intervalTime = AlarmManager.INTERVAL_DAY;
                break;
            case Reminder.WEEKLY:
                intervalTime = AlarmManager.INTERVAL_DAY*7;
                break;
            case Reminder.MONTHLY:
                intervalTime = getMonthInterval(alarmCalendar);
                break;
            default:
                intervalTime = 0;
                break;
        }

        System.out.println("Interval time: " + intervalTime);

        // cancel already scheduled reminders
        cancelReminder(context, cls, reminder.getlocalId());

        // Enable a receiver
        ComponentName receiver = new ComponentName(context, cls);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);


        if (alarmCalendar.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
            alarmCalendar.setTimeInMillis(alarmCalendar.getTimeInMillis() + intervalTime);

        System.out.println(alarmCalendar.getTime().toString());

        Intent intent1 = new Intent(context, cls);
        intent1.putExtra("reminderName", reminder.getName() + "#" + reminder.getRemoteId() + "#" + reminder.getCaregiverUid());
        intent1.putExtra("typeAndLocalId", Integer.toString(reminder.getReminderType())+"_"+Integer.toString(reminder.getlocalId()));
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

        Random random = new Random();
        int notification_id = random.nextInt (1000) + 1000;

        //PendingIntent pendingIntent = stackBuilder.getPendingIntent(notification_id, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notification_id, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "myId");

        Notification notification = builder.setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content))
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setSmallIcon(R.mipmap.ic_launcher_icon_round)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Create random notification id to show multiple notifications

        notificationManager.notify(notification_id, notification);
    }

    public static void showNotification(Context context, Intent intent,String title,String content)
    {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        stackBuilder.addNextIntent(intent);

        Random random = new Random();
        int notification_id = random.nextInt (1000) + 1000;

        //PendingIntent pendingIntent = stackBuilder.getPendingIntent(notification_id, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notification_id, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "myId");

        Notification notification = builder.setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(content))
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setSmallIcon(R.mipmap.ic_launcher_icon_round)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Create random notification id to show multiple notifications

        notificationManager.notify(notification_id, notification);
    }
}
