package me.brandonfowler.thoughtresume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderStore = ReminderStore(context)
        val reminderNotification = ReminderNotification(context)
        reminderNotification.setReminders(reminderStore.activeReminders, true)

        context.applicationContext.sendBroadcast(Intent(ListActivity.UPDATE_LIST_UI));
    }
}