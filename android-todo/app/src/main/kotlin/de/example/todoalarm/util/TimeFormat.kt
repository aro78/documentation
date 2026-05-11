package de.example.todoalarm.util

import java.text.DateFormat
import java.util.Date

object TimeFormat {
    private val df: DateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    fun format(epochMs: Long): String = df.format(Date(epochMs))
    fun formatOrNull(epochMs: Long?): String? = epochMs?.let { format(it) }
}
