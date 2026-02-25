package com.khadr.tasbih.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val settings = context.loadNotifSettings()

        // Only reschedule if user had active notifications
        if (!settings.notifsActive) return
        val pt = settings.prayerTimes ?: return

        createNotificationChannel(context)
        scheduleAllPrayerAlarms(
            context      = context,
            prayerTimes  = pt,
            beforeMinutes = if (settings.enableBefore) settings.beforeMinutes else 0,
            afterMinutes  = if (settings.enableAfter)  settings.afterMinutes  else 0
        )
    }
}