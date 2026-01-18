package me.brandonfowler.thoughtresume

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressImeActionButton
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class) class ListActivityTest {
    @get:Rule val activityRule = ActivityScenarioRule(ListActivity::class.java)
    @get:Rule var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    private lateinit var manager: NotificationManager

    @Before fun setUp() {
        activityRule.scenario.onActivity { activity ->
            manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            activity.reminderStore.reminders.clear()
            activity.update()
        }
    }

    @Test fun itemAdded() {
        onView(withId(R.id.reminderText)).perform(
            replaceText("Test resume"),
            pressImeActionButton()
        )

        assertEquals(1, manager.activeNotifications.size)

        with (manager.activeNotifications.first()) {
            assertEquals("Test resume", notification.extras.getString(Notification.EXTRA_TEXT))
        }
    }

    @Test fun itemAddedButton() {
        onView(withId(R.id.reminderText)).perform(replaceText("Test resume"))
        onView(withId(R.id.addButton)).perform(click())

        assertEquals(1, manager.activeNotifications.size)

        with (manager.activeNotifications.first()) {
            assertEquals("Test resume", notification.extras.getString(Notification.EXTRA_TEXT))
        }
    }

    @Test fun multipleItemsAdded() {
        onView(withId(R.id.reminderText)).perform(
            replaceText("Test resume"),
            pressImeActionButton(),
            replaceText("Foo"),
            pressImeActionButton()
        )

        assertEquals(1, manager.activeNotifications.size)

        with (manager.activeNotifications.first()) {
            assertEquals("Test resume\nFoo", notification.extras.getString(Notification.EXTRA_TEXT))
        }
    }

    @Test fun blankItemAdded() {
        onView(withId(R.id.reminderText)).perform(replaceText(""), pressImeActionButton())

        assertEquals(0, manager.activeNotifications.size)
    }

    @Test fun clearButtonPressed() {
        onView(withId(R.id.reminderText)).perform(
            replaceText("Test resume"),
            pressImeActionButton()
        )

        assertEquals(1, manager.activeNotifications.size)

        onView(withId(R.id.clearButton)).perform(click())

        assertEquals(0, manager.activeNotifications.size)
    }
}