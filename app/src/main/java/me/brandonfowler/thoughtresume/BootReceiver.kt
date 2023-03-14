package me.brandonfowler.thoughtresume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) return

        ReminderNotification(context).update()
    }
}