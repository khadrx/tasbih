package com.khadr.tasbih.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.khadr.tasbih.MainActivity
import com.khadr.tasbih.R

const val EXTRA_OPEN_TASBIH = "open_tasbih"   // tells MainActivity to jump straight to tasbih

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val isBefore   = intent.getBooleanExtra(EXTRA_IS_BEFORE, true)

        val (title, body) = if (isBefore) {
            "حان وقت $prayerName قريباً" to "استعد للصلاة — لا تنسَ أذكارك"
        } else {
            "بعد صلاة $prayerName" to "وقت التسبيح — سبّح واذكر الله الآن"
        }

        // After-prayer tap → open app and start tasbih directly
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (!isBefore) putExtra(EXTRA_OPEN_TASBIH, true)
        }

        val pending = PendingIntent.getActivity(
            context, if (isBefore) 0 else 1,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        val notifId = if (isBefore) prayerName.hashCode() else prayerName.hashCode() + 1000
        context.getSystemService(NotificationManager::class.java).notify(notifId, notification)
    }
}