package com.heallinkapp.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.app.NotificationManager
import android.app.NotificationChannel
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.heallinkapp.MainActivity
import com.heallinkapp.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        val title = "Reminder"
        val notifId = ID_REPEATING
        if (message != null) {
            showAlarmNotification(context, title, message, notifId)
        }
    }

    fun showAlarmNotification(
        context: Context,
        title: String,
        message: String,
        notifId: Int
    ) {
        val channelId = "Channel_1"
        val channelName = "AlarmManager channel"

        val notificationManagerCompat =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(ContextCompat.getColor(context, android.R.color.transparent))
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
            .setSound(alarmSound)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
            notificationManagerCompat.createNotificationChannel(channel)
        }

        val notification = builder.build()
        notificationManagerCompat.notify(notifId, notification)
    }


    fun setRepeatingAlarm(context: Context,type: String, time: String, message: String) {

        if (isDateInvalid(time, TIME_FORMAT)) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(EXTRA_MESSAGE, message)

        val timeArray = time.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]))
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]))
        calendar.set(Calendar.SECOND, 0)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ID_REPEATING,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Toast.makeText(context, "Repeating alarm set up", Toast.LENGTH_SHORT).show()
    }

    // Metode ini digunakan untuk validasi date dan time
    private fun isDateInvalid(date: String, format: String): Boolean {
        return try {
            val df = SimpleDateFormat(format, Locale.getDefault())
            df.isLenient = false
            df.parse(date)
            false
        } catch (e: ParseException) {
            true
        }
    }

    // Membatalkan alarm berulang
    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val requestCode = ID_REPEATING
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        if (pendingIntent != null) {
            pendingIntent.cancel()
            alarmManager.cancel(pendingIntent)
            Toast.makeText(context, "Repeating alarm dibatalkan", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_MESSAGE = "message"
        private const val ID_REPEATING = 101
        private const val TIME_FORMAT = "HH:mm"
    }
}
