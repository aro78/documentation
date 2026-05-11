package de.example.todoalarm.util

import java.util.concurrent.TimeUnit

data class SnoozeOption(val labelResName: String, val durationMs: Long)

object SnoozeDurations {

    val ONE_MIN: Long = TimeUnit.MINUTES.toMillis(1)
    val THREE_MIN: Long = TimeUnit.MINUTES.toMillis(3)
    val TEN_MIN: Long = TimeUnit.MINUTES.toMillis(10)
    val THIRTY_MIN: Long = TimeUnit.MINUTES.toMillis(30)
    val ONE_HOUR: Long = TimeUnit.HOURS.toMillis(1)
    val THREE_HOURS: Long = TimeUnit.HOURS.toMillis(3)
    val ONE_DAY: Long = TimeUnit.DAYS.toMillis(1)

    val ALL_MS: List<Long> = listOf(
        ONE_MIN, THREE_MIN, TEN_MIN, THIRTY_MIN, ONE_HOUR, THREE_HOURS, ONE_DAY,
    )
}
