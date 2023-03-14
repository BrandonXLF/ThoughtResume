package me.brandonfowler.thoughtresume

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import me.brandonfowler.thoughtresume.databinding.ActivityListBinding

class ListActivity : Activity() {
    lateinit var reminderNotification: ReminderNotification
    private lateinit var binding: ActivityListBinding
    private lateinit var remindersAdapter: ReminderListAdapter
    private lateinit var inputMethodManager: InputMethodManager
    val reminders = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reminderNotification = ReminderNotification(this)
        binding = ActivityListBinding.inflate(layoutInflater)
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        setContentView(binding.root)

        val reminderList = reminderNotification.text.split("\n")

        if (reminderList.size != 1 || reminderList[0].isNotEmpty())
            reminders.addAll(reminderList)

        remindersAdapter = ReminderListAdapter(this, R.layout.reminder_listview, reminders)
        remindersAdapter.setUpdatedListener { update(false) }

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

            reminders.add(reminderText)
            update()

            true
        }

        binding.addButton.setOnClickListener {
            binding.reminderText.onEditorAction(EditorInfo.IME_ACTION_DONE)
        }

        binding.clearButton.setOnClickListener {
            reminders.clear()
            update()
        }

        reminderNotification.update()
        binding.clearButton.isEnabled = reminders.isNotEmpty()
    }

    fun update(notifyAdapter: Boolean = true) {
        reminderNotification.text = reminders.joinToString("\n")
        binding.clearButton.isEnabled = reminders.isNotEmpty()

        if (notifyAdapter)
            remindersAdapter.notifyDataSetChanged()
    }
}