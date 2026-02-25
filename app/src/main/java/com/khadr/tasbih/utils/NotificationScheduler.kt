package com.khadr.tasbih.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

const val CHANNEL_ID       = "tasbih_prayer_channel"
const val EXTRA_PRAYER_NAME = "prayer_name"
const val EXTRA_IS_BEFORE   = "is_before"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "تذكير الصلاة",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعارات قبل وبعد أوقات الصلاة"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}

// Schedule a single alarm for a prayer
// offsetMinutes: positive = after prayer, negative = before prayer
fun schedulePrayerAlarm(
    context       : Context,
    prayerNameAr  : String,
    prayerNameKey : String,
    hour          : Int,
    minute        : Int,
    offsetMinutes : Int,
    isBefore      : Boolean,
    requestCode   : Int
) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.MINUTE, offsetMinutes)
        // If time already passed today, schedule for tomorrow
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra(EXTRA_PRAYER_NAME, prayerNameAr)
        putExtra(EXTRA_IS_BEFORE, isBefore)
    }

    val pending = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Use setAlarmClock — precise, shows in status bar, works on all versions
    alarmManager.setAlarmClock(
        AlarmManager.AlarmClockInfo(cal.timeInMillis, pending),
        pending
    )
}

// Schedule all prayer alarms based on settings
fun scheduleAllPrayerAlarms(
    context     : Context,
    prayerTimes : PrayerTimes,
    beforeMinutes: Int,   // 0 = disabled
    afterMinutes : Int    // 0 = disabled
) {
    cancelAllPrayerAlarms(context)
    createNotificationChannel(context)

    val prayers = prayerTimes.toList()

    prayers.forEachIndexed { idx, prayer ->
        val (hour, minute) = prayer.time.toHourMinute()

        if (beforeMinutes > 0) {
            schedulePrayerAlarm(
                context        = context,
                prayerNameAr   = prayer.nameAr,
                prayerNameKey  = prayer.nameKey,
                hour           = hour,
                minute         = minute,
                offsetMinutes  = -beforeMinutes,
                isBefore       = true,
                requestCode    = idx * 10 + 1
            )
        }

        if (afterMinutes > 0) {
            schedulePrayerAlarm(
                context        = context,
                prayerNameAr   = prayer.nameAr,
                prayerNameKey  = prayer.nameKey,
                hour           = hour,
                minute         = minute,
                offsetMinutes  = afterMinutes,
                isBefore       = false,
                requestCode    = idx * 10 + 2
            )
        }
    }
}

fun cancelAllPrayerAlarms(context: Context) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    // Cancel all 10 possible alarms (5 prayers × 2 types)
    for (i in 0..9) {
        val intent  = Intent(context, NotificationReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context, i, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pending?.let { alarmManager.cancel(it) }
    }
}