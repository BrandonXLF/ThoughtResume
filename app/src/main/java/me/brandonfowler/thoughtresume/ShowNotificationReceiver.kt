package me.brandonfowler.thoughtresume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ShowNotificationReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_CANCELLED = "me.brandonfowler.thoughtresume.NOTIFICATION_CANCELLED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != NOTIFICATION_CANCELLED) return

        val reminderStore = ReminderStore(context)
        val reminderNotification = ReminderNotification(context)
        reminderNotification.setReminders(reminderStore.activeReminders)
    }
}