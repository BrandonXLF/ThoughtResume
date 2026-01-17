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

    private val intent = Intent(context, ListActivity::class.java)
    private val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val builder = Notification.Builder(context, RESUME_CHANNEL)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentTitle(context.getString(R.string.notification_title))
        .setContentIntent(pendingIntent)
        .setOngoing(true)
        .setOnlyAlertOnce(true)

    private var _text: String = ""

    var text: String
        get() = _text
        set(text) {
            _text = text
            update()
        }

    val shown: Boolean
        get() = text.isNotEmpty()

    init {
        manager.createNotificationChannel(
            NotificationChannel(
                RESUME_CHANNEL,
                context.getString(R.string.channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    fun update() {
        if (!shown) {
            manager.cancel(RESUME_NOTIFICATION)
            return
        }

        builder.setContentText(text)
        manager.notify(RESUME_NOTIFICATION, builder.build())
    }
}