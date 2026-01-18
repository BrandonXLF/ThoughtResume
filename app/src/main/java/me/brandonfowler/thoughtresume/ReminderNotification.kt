package me.brandonfowler.thoughtresume

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class ReminderNotification(context: Context) {
    companion object {
        const val RESUME_CHANNEL = "RESUME"
        const val RESUME_NOTIFICATION = 1
    }

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val intent = Intent(context, ListActivity::class.java)
    private val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    private val deleteIntent = Intent(context, ShowNotificationReceiver::class.java)
    private val deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_IMMUTABLE)

    private val builder = Notification.Builder(context, RESUME_CHANNEL)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentIntent(pendingIntent)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setDeleteIntent(deletePendingIntent)

    private var text: String = ""

    val shown: Boolean get() = text.isNotEmpty()

    init {
        manager.createNotificationChannel(
            NotificationChannel(
                RESUME_CHANNEL,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    fun setReminders(reminders: List<ReminderStore.Reminder>, forceAlert: Boolean = false) {
        text = reminders.joinToString("\n") { it.text }
        show(forceAlert)
    }

    private fun show(forceAlert: Boolean) {
        if (!shown) {
            manager.cancel(RESUME_NOTIFICATION)
            return
        }

        builder.setContentText(text)
        builder.setOnlyAlertOnce(!forceAlert)

        manager.notify(RESUME_NOTIFICATION, builder.build())
    }
}