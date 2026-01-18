package me.brandonfowler.thoughtresume

import android.app.AlertDialog
import android.content.Context
import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonWriter
import java.io.StringReader
import java.io.StringWriter
import java.time.Instant

class ReminderStore(context: Context) {
    companion object {
        const val NOTIFICATION_PREFERENCES = "notification"
    }

    private val preferences = context.getSharedPreferences(NOTIFICATION_PREFERENCES, Context.MODE_PRIVATE)

    class Reminder(var text: String, var begins: Long?)
    lateinit var reminders: MutableList<Reminder>

    val activeReminders: List<Reminder> get() {
        val now = Instant.now().epochSecond
        return reminders.filter { it.begins == null || it.begins!! <= now }
    }

    val inactiveReminders: List<Reminder> get() {
        val now = Instant.now().epochSecond
        return reminders.filter { it.begins != null && it.begins!! > now }
    }

    init {
        val version = preferences.getInt("version", 1)
        val data = preferences.getString("reminders", "")!!

        when (version) {
            1 -> reminders = data.split("\n").map { Reminder(it, null) }.toMutableList()
            2 -> {
                val reader = JsonReader(StringReader(data))

                try {
                    reminders = readReminderList(reader)
                } catch (e: Exception) {
                    AlertDialog.Builder(context)
                        .setMessage(context.getString(R.string.load_failed) + "\n\n" + e.message)
                        .show()
                    reminders = mutableListOf()
                } finally {
                    reader.close()
                }
            }
        }

        if (reminders.size == 1 && reminders[0].text.isEmpty()) {
            reminders.clear()
        }
    }

    private fun readReminderList(reader: JsonReader): MutableList<Reminder> {
        val reminders: MutableList<Reminder> = mutableListOf()

        reader.beginArray()

        while (reader.hasNext()) {
            reminders.add(readReminder(reader))
        }

        reader.endArray()

        return reminders
    }

    private fun readReminder(reader: JsonReader): Reminder {
        var text = ""
        var begin: Long? = null

        reader.beginObject()

        while (reader.hasNext()) {
            val name = reader.nextName()

            if (name == "text") {
                text = reader.nextString()
            } else if (name == "begin" && reader.peek() != JsonToken.NULL) {
                begin = reader.nextLong()
            }
        }

        reader.endObject()

        return Reminder(text, begin)
    }

    fun save() {
        val stringWriter = StringWriter()
        JsonWriter(stringWriter).use { writeReminderList(it, reminders) }
        val text = stringWriter.toString()

        with (preferences.edit()) {
            putInt("version", 2)
            putString("reminders", text)
            apply()
        }
    }

    private fun writeReminderList(writer: JsonWriter, reminders: List<Reminder>) {
        writer.beginArray()

        for (reminder in reminders) {
            writeReminder(writer, reminder)
        }

        writer.endArray()
    }

    private fun writeReminder(writer: JsonWriter, reminder: Reminder) {
        writer.beginObject()

        writer.name("text").value(reminder.text)

        if (reminder.begins !== null) {
            writer.name("begin").value(reminder.begins)
        }

        writer.endObject()
    }
}