package me.brandonfowler.thoughtresume

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri

class ReminderNotification(context: Context) {
    companion object {
        const val RESUME_CHANNEL = "RESUME"
        const val RESUME_NOTIFICATION = 1
    }

    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val intent = Intent(context, ListActivity::class.java)
    private val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    private val deleteIntent = Intent(ShowNotificationReceiver.NOTIFICATION_CANCELLED, Uri.EMPTY, context, ShowNotificationReceiver::class.java)
    private val pendingDeleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)

    private val builder = Notification.Builder(context, RESUME_CHANNEL)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentIntent(pendingIntent)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setDeleteIntent(pendingDeleteIntent)

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

    fun setReminders(reminders: List<ReminderStore.Reminder>) {
        text = reminders.joinToString("\n") { it.text }
        show()
    }

    fun show() {
        if (!shown) {
            manager.cancel(RESUME_NOTIFICATION)
            return
        }

        builder.setContentText(text)
        manager.notify(RESUME_NOTIFICATION, builder.build())
    }
}