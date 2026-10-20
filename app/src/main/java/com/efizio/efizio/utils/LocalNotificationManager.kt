package com.efizio.efizio.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val patientName = intent.getStringExtra("patientName") ?: "Klijent"
        val minutesLeft = intent.getIntExtra("minutesLeft", 15)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, "efizio_reminders")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Uskoro termin!")
            .setContentText("Klijent $patientName ima termin za $minutesLeft minuta.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Unikatan ID za prikaz svake notifikacije
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

object LocalNotificationManager {

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "efizio_reminders",
                "Podsetnici za termine",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }


    fun scheduleReminder(context: Context, patientName: String, appointmentTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val intervals = listOf(30, 15, 10,5)

        for (minutes in intervals) {
            val reminderTime = appointmentTimeMillis - (minutes * 60 * 1000)


            if (reminderTime > System.currentTimeMillis()) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("patientName", patientName)
                    putExtra("minutesLeft", minutes)
                }


                val requestCode = (patientName.hashCode().toString() + minutes).hashCode()

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12 (API 31) i noviji
                    if (alarmManager.canScheduleExactAlarms()) {
                        try {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                        } catch (e: SecurityException) {

                            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                        }
                    } else {

                        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                } else {

                    alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                }
            }
        }
    }
}