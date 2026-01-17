package me.brandonfowler.thoughtresume

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import me.brandonfowler.thoughtresume.databinding.ActivityListBinding

class ListActivity : Activity() {
    lateinit var reminderStore: ReminderStore
    lateinit var reminderNotification: ReminderNotification

    private lateinit var binding: ActivityListBinding
    private lateinit var remindersAdapter: ReminderListAdapter
    private lateinit var inputMethodManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reminderStore = ReminderStore(this)
        reminderNotification = ReminderNotification(this)

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

    override fun onResume() {
        super.onResume()
        update(notifyAdapter = false, save = false)
    }

    fun update(notifyAdapter: Boolean = true, save: Boolean = true) {
        if (save) {
            reminderStore.save()
        }

        val activeReminders = reminderStore.activeReminders
        reminderNotification.text = activeReminders.joinToString("\n") { it.text }
        binding.clearButton.isEnabled = activeReminders.isNotEmpty()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder(this)
                    .setMessage(R.string.permission_reason)
                    .setNegativeButton(R.string.permission_skip) { _, _ -> }
                    .setPositiveButton(R.string.permission_allow) { _, _ ->
                        requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
                    }
                    .show()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }

        if (notifyAdapter) {
            remindersAdapter.notifyDataSetChanged()
        }
    }
}