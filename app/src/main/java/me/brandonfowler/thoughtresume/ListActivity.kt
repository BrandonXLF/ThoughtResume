package me.brandonfowler.thoughtresume

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import me.brandonfowler.thoughtresume.databinding.ActivityListBinding


class ListActivity : Activity() {
    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 50
        const val UPDATE_LIST_UI = "me.brandonfowler.thoughtresume.UPDATE_LIST_UI"
    }

    lateinit var reminderStore: ReminderStore
    lateinit var reminderNotification: ReminderNotification

    private lateinit var binding: ActivityListBinding
    private lateinit var remindersAdapter: ReminderListAdapter
    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var alarmManager: AlarmManager
    private lateinit var alarmIntent: Intent
    private var previousAlarmPendingIntent: PendingIntent? = null
    private var notificationPermissionDialog: AlertDialog? = null

    private val updateUIReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            remindersAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reminderStore = ReminderStore(this)
        reminderNotification = ReminderNotification(this)

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(this, AlarmReceiver::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateUIReceiver, IntentFilter(UPDATE_LIST_UI), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(updateUIReceiver, IntentFilter(UPDATE_LIST_UI))
        }

        binding = ActivityListBinding.inflate(layoutInflater)
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setContentView(binding.root)

        remindersAdapter = ReminderListAdapter(this, R.layout.reminder_listview, reminderStore.reminders)
        remindersAdapter.setUpdatedListener { update(notifyAdapter = false) }

        binding.reminderList.adapter = remindersAdapter

        binding.gap.setOnClickListener {
            binding.reminderText.requestFocus()
            inputMethodManager.showSoftInput(binding.reminderText, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.reminderText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) { }

            override fun beforeTextChanged(s: CharSequence, start: Int, end: Int, count: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, end: Int, count: Int) {
                binding.addButton.visibility = if (s.isNotEmpty()) View.VISIBLE else View.GONE
            }
        })

        binding.reminderText.setOnEditorActionListener { _, action, _ ->
            if (action != EditorInfo.IME_ACTION_DONE)
                return@setOnEditorActionListener false

            val reminderText = binding.reminderText.text.toString()
            binding.reminderText.text.clear()

            if (reminderText.isEmpty())
                return@setOnEditorActionListener true

            reminderStore.reminders.add(ReminderStore.Reminder(reminderText, null))
            update()

            true
        }

        binding.addButton.setOnClickListener {
            binding.reminderText.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }

        binding.clearButton.setOnClickListener {
            reminderStore.reminders.clear()
            update()
        }

        update(notifyAdapter = false, save = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateUIReceiver)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestNotificationPermission() {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                if (notificationPermissionDialog == null) {
                    notificationPermissionDialog = AlertDialog.Builder(this)
                        .setMessage(R.string.permission_reason)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            requestPermissions(
                                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                                NOTIFICATION_PERMISSION_REQUEST_CODE
                            )
                        }
                        .create()
                }

                notificationPermissionDialog!!.show()
            } else {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            notificationPermissionDialog?.dismiss()
            update(notifyAdapter = false, save = false)
        }
    }

    fun update(notifyAdapter: Boolean = true, save: Boolean = true) {
        if (save) {
            reminderStore.save()
        }

        val activeReminders = reminderStore.activeReminders
        reminderNotification.setReminders(activeReminders)
        binding.clearButton.isEnabled = activeReminders.isNotEmpty()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.requestNotificationPermission()
        }

        if (previousAlarmPendingIntent != null) {
            alarmManager.cancel(previousAlarmPendingIntent)
        }

        val seenTimes = HashSet<Long>()
        reminderStore.inactiveReminders.forEach {
            if (seenTimes.contains(it.begins!!)) return@forEach

            val triggerAt = it.begins!! * 1000L
            val pendingIntent = PendingIntent.getBroadcast(this, (it.begins!! % Int.MAX_VALUE).toInt(), alarmIntent, PendingIntent.FLAG_IMMUTABLE)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            }

            seenTimes.add(it.begins!!)
            previousAlarmPendingIntent = pendingIntent
        }

        if (notifyAdapter) {
            remindersAdapter.notifyDataSetChanged()
        }
    }
}