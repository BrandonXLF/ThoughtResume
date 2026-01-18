package me.brandonfowler.thoughtresume

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import me.brandonfowler.thoughtresume.databinding.ReminderListviewBinding
import java.time.Instant
import java.util.Calendar
import java.util.Locale

class ReminderListAdapter(context: Context, resource: Int, val items: MutableList<ReminderStore.Reminder>)
    : ArrayAdapter<ReminderStore.Reminder>(context, resource, items) {
    fun interface OnUpdatedListener {
        fun onUpdated()
    }

    class ViewHolder(private var adapter: ReminderListAdapter, private var position: Int) {
        val binding: ReminderListviewBinding = ReminderListviewBinding.inflate(adapter.inflater)

        private var textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) { }

            override fun beforeTextChanged(s: CharSequence, start: Int, end: Int, count: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, end: Int, count: Int) {
                val text = s.toString()

                with (adapter) {
                    if (items[position].text != text) {
                        items[position].text = text
                        listener?.onUpdated()
                    }
                }
            }
        }

        private val onDeleteClickListener = OnClickListener {
            with (adapter) {
                items.removeAt(position)
                listener?.onUpdated()
                notifyDataSetChanged()
            }
        }

        private val onSnoozeClickListener = OnClickListener {
            val context = adapter.context
            val use24Hours = DateFormat.is24HourFormat(context)

            if (isSnoozed()) {
                val format = if (use24Hours) {
                    "EEEE, MMM d, yyyy 'at' HH:mm zzz"
                } else {
                    "EEEE, MMM d, yyyy 'at' hh:mm aaa zzz"
                }

                val pattern = DateFormat.getBestDateTimePattern(Locale.getDefault(), format)
                val str = DateFormat.format(pattern, adapter.items[position].begins!! * 1000)

                AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.snoozed_until) + " " + str)
                    .setNeutralButton(R.string.ok) { _, _ -> }
                    .setNegativeButton(R.string.unsnooze) { _, _ ->
                        with(adapter) {
                            items[position].begins = null
                            listener?.onUpdated()
                            notifyDataSetChanged()
                        }
                    }
                    .show()

                return@OnClickListener
            }

            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            DatePickerDialog(context, { _, year, month, day ->
                TimePickerDialog(context, { _, hour, min ->
                    c.set(year, month, day, hour, min, 0)

                    with (adapter) {
                        items[position].begins = c.timeInMillis / 1000L
                        listener?.onUpdated()
                        notifyDataSetChanged()
                    }
                }, hour, minute, use24Hours).show()
            }, year, month, day).show()
        }

        init {
            binding.editText.setText(adapter.items[position].text)
            binding.editText.addTextChangedListener(textWatcher)
            binding.deleteButton.setOnClickListener(onDeleteClickListener)
            binding.snoozeButton.setOnClickListener(onSnoozeClickListener)
            setStyle()
        }

        private fun isSnoozed(): Boolean {
            return adapter.items[position].begins !== null && adapter.items[position].begins!! > Instant.now().epochSecond;
        }

        private fun setStyle() {
            val snoozed = isSnoozed()

            binding.editText.alpha = if (snoozed) 0.65F else 1F
            binding.snoozeButton.setImageResource(if (snoozed) R.drawable.unsnooze_icon else R.drawable.snooze_icon)
            binding.snoozeButton.contentDescription =
                adapter.context.getString(if (snoozed) R.string.snooze_reminder else R.string.unsnooze_reminder)
        }

        fun updateValue(position: Int) {
            this.position = position
            binding.editText.setText(adapter.items[position].text)
            setStyle()
        }
    }

    var listener: OnUpdatedListener? = null
    val inflater: LayoutInflater = LayoutInflater.from(context)!!

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder(this, position)
            holder.binding.root.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            holder.updateValue(position)
        }

        return holder.binding.root
    }

    fun setUpdatedListener(listener: OnUpdatedListener) {
        this.listener = listener
    }
}