package me.brandonfowler.thoughtresume

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class) class NotificationTest {
    @get:Rule val activityRule = ActivityScenarioRule(ListActivity::class.java)
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val notification = ReminderNotification(context)

    @Test fun textSet() {
        notification.text = "Foo"

        assertEquals(notification.shown, true)
        assertEquals(1, manager.activeNotifications.size)

        with (manager.activeNotifications.first()) {
            assertEquals("Foo", notification.extras.getString(Notification.EXTRA_TEXT))
        }
    }

    @Test fun blankTextSet() {
        notification.text = ""

        assertEquals(notification.shown, false)
        assertEquals(0, manager.activeNotifications.size)
    }
}