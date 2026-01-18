package me.brandonfowler.thoughtresume

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class) class NotificationTest {
    @get:Rule val activityRule = ActivityScenarioRule(ListActivity::class.java)
    @get:Rule var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notification = ReminderNotification(context)

    @Test fun reminderSet() {
        notification.setReminders(listOf(ReminderStore.Reminder("Foo", null)))

        assertEquals(notification.shown, true)
        assertEquals(1, manager.activeNotifications.size)

        with (manager.activeNotifications.first()) {
            assertEquals("Foo", notification.extras.getString(Notification.EXTRA_TEXT))
        }
    }

    @Test fun remindersSet() {
        notification.setReminders(listOf(ReminderStore.Reminder("Foo", null), ReminderStore.Reminder("Bar", 0)))

        assertEquals(notification.shown, true)
        assertEquals(1, manager.activeNotifications.size)

        with (manager.activeNotifications.first()) {
            assertEquals("Foo\nBar", notification.extras.getString(Notification.EXTRA_TEXT))
        }
    }

    @Test fun blankRemindersSet() {
        notification.setReminders(emptyList())

        assertEquals(notification.shown, false)
        assertEquals(0, manager.activeNotifications.size)
    }
}