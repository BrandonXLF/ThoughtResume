package me.brandonfowler.thoughtresume

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import me.brandonfowler.thoughtresume.databinding.ReminderListviewBinding

class ReminderListAdapter(context: Context, resource: Int, val items: ArrayList<String>)
    : ArrayAdapter<String>(context, resource, items) {
    fun interface OnUpdatedListener {
        fun onUpdated()
    }

    class ViewHolder(private var adapter: ReminderListAdapter, private var position: Int) {
        val binding: ReminderListviewBinding = ReminderListviewBinding.inflate(adapter.inflater)

        private var textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) { }

            override fun beforeTextChanged(s: CharSequence, start: Int, end: Int, count: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, end: Int, count: Int) {
                with (adapter) {
                    items[position] = s.toString()
                    listener?.onUpdated()
                }
            }
        }

        private val onDeleteClickListener: OnClickListener = OnClickListener {
            with (adapter) {
                items.removeAt(position)
                listener?.onUpdated()
                notifyDataSetChanged()
            }
        }

        init {
            binding.editText.setText(adapter.items[position])
            binding.editText.addTextChangedListener(textWatcher)
            binding.deleteButton.setOnClickListener(onDeleteClickListener)
        }

        fun setPosition(position: Int) {
           if (position == this.position) return

            this.position = position
            binding.editText.setText(adapter.items[position])
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
            holder.setPosition(position)
        }

        return holder.binding.root
    }

    fun setUpdatedListener(listener: OnUpdatedListener) {
        this.listener = listener
    }
}